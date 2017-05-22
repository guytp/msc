
#include <Adafruit_PWMServoDriver.h>
#include <Adafruit_NeoPixel.h>
#include <Wire.h>
#include <SoftwareSerial.h>

enum LightMode {
  LightModeOff = 0,
  LightModeCycle = 1,
  LightModeSolid = 2
} 
typedef LightMode;


struct VibrationSettingValue {
  uint16_t Start;
  uint16_t End;
  uint16_t Duration;
} typedef VibrationSettingValue;

struct VibrationSetting {
  uint8_t NumberValues;
  VibrationSettingValue Values[5];
} typedef VibrationSetting;

// Bluetooth declarations
SoftwareSerial _bluetoothSerial(3, 2);
const byte _bluetoothBufferSize = 300;
char _bluetoothBuffer[_bluetoothBufferSize];
byte _bluetoothBufferLength = 0;

// Lighting declarations
Adafruit_NeoPixel _lightStrip = Adafruit_NeoPixel(30, 12, NEO_GRB + NEO_KHZ800);
unsigned long _lightLoopLastTime = 0;
LightMode _lightMode = LightModeCycle;
uint32_t _lightSolidColour = 0xFF0000;
uint16_t _lightCycleCurrentOffsetTime = 0; // How many ms are we into the current sequence for when we are colour cycling
uint8_t _lightCycleColourCount = 7;
uint32_t _lightCycleColours[10];
uint32_t _lightCycleDuration = 7500; // In ms
uint32_t _lightCycleBlendAmount = _lightCycleDuration * 0.25; // How many different colours are visible of total amount at any time (i.e. 30 means each LED is a different colour within the cycle, 60 means the same but with every second colour skipped and 1 means everything is the same colour)

// Vibration declarations
Adafruit_PWMServoDriver _pwmDriver = Adafruit_PWMServoDriver();
VibrationSetting _vibrationSettings[9];
uint32_t _vibrationLoopLastTime = 0;
uint16_t _vibrationCycleCurrentOffsetTime = 0; // How many ms are we into the current sequence for vibrating?
uint32_t _vibrationCycleDuration = 2500; // In ms

void setup() {
  // Begin serial for debugging purposes
  Serial.begin(9600);
  Serial.println(F("Cushion Firmware - Debug Interface"));

  // Setup neopixel strip to be enabled with correct brightness and all pixels turned off
  _lightStrip.begin();
  _lightStrip.setBrightness(255);
  for(uint16_t i=0; i<_lightStrip.numPixels(); i++)
    _lightStrip.setPixelColor(i, 0);
  _lightStrip.show();
  _lightCycleColours[0] = 0xFF0000;
  _lightCycleColours[1] = 0xFFFFFF;
  _lightCycleColours[2] = 0x00FF00;
  _lightCycleColours[3] = 0xFFFFFF;
  _lightCycleColours[4] = 0x0000FF;
  _lightCycleColours[5] = 0xFFFFFF;
  _lightCycleColours[6] = 0xFF0000;

  // Setup PWM control for motors and turn everything off to start with and give it a default pattern
  _pwmDriver.begin();
  _pwmDriver.setPWMFreq(40);
  for (byte i = 0; i < 16; i++) {
    _pwmDriver.setPin(i, 0);
    if (i >= 9)
      break;
    _vibrationSettings[i].NumberValues = 3;
    _vibrationSettings[i].Values[0].Start = 1500;
    _vibrationSettings[i].Values[0].End = 2500;
    _vibrationSettings[i].Values[0].Duration = 750;
    _vibrationSettings[i].Values[1].Start = 2500;
    _vibrationSettings[i].Values[1].End = 1500;
    _vibrationSettings[i].Values[1].Duration = 750;
    _vibrationSettings[i].Values[2].Start = 1500;
    _vibrationSettings[i].Values[2].End = 1500;
    _vibrationSettings[i].Values[2].Duration = 100;
  }

  // Enable bluetooth interface
  _bluetoothSerial.begin(9600); 
} 

void lightPerformLoop() {
  // Determine how long since we were last called - if we're within threshold then we do nothing
  uint64_t nowTime = millis();
  uint16_t timeSinceLast = (uint16_t)(nowTime - _lightLoopLastTime);
  if (timeSinceLast < 50) // Without this arbitrary delay the lower the delay is the more likely we get serial data corruption, the joys of hard-coded assembly
    return;
  _lightLoopLastTime = nowTime;

  // If lights are solid or off we can set direclty
  uint16_t pixelCount = _lightStrip.numPixels();
  if (_lightMode == LightModeOff || _lightMode == LightModeSolid) {
    uint32_t colour = _lightMode == LightModeOff ? 0 : _lightSolidColour;
    for (uint16_t i=0; i<pixelCount; i++)
      _lightStrip.setPixelColor(i, colour);
    _lightStrip.show();
    return;
  }

  // We need to determine where we are in the sequence by adding the ms offset and clamping it to total duration to cope with overflow
  _lightCycleCurrentOffsetTime += timeSinceLast;
  while (_lightCycleCurrentOffsetTime > _lightCycleDuration)
    _lightCycleCurrentOffsetTime -= _lightCycleDuration;

  // Now for each light we can determine where in the sequence it is
  uint16_t firstValue = _lightCycleCurrentOffsetTime;
  uint16_t lastValue = firstValue + _lightCycleBlendAmount;
  float increment = (float)(lastValue - firstValue) / (float)pixelCount;
  uint16_t lastCalculatedValue = 0;
  uint32_t lastCalculatedColour = 0;
  uint32_t durationPerSegment = _lightCycleDuration / (_lightCycleColourCount - 1);
  for(uint8_t i=0; i< pixelCount; i++)
  {
    // First determine which ms value from our entire range this loop is
    uint16_t thisValue = (uint16_t)(firstValue + (increment * i));
    while (thisValue > _lightCycleDuration)
      thisValue -= _lightCycleDuration;
    if (thisValue != lastCalculatedValue || i == 0) {
      uint8_t segmentNumber = thisValue / durationPerSegment;
      lastCalculatedColour = blendedColour(_lightCycleColours[segmentNumber], _lightCycleColours[segmentNumber + 1], durationPerSegment, thisValue % durationPerSegment);
      lastCalculatedValue = thisValue;
    }
    _lightStrip.setPixelColor(i, lastCalculatedColour);
  }

  // Update the lights
  _lightStrip.show();
}

void vibrationPerformLoop() {  // Determine how long since we were last called - if we're within threshold then we do nothing
  uint64_t nowTime = millis();
  uint16_t timeSinceLast = (uint16_t)(nowTime - _vibrationLoopLastTime);
  if (timeSinceLast < 50) // Without this arbitrary delay the lower the delay is the more likely we get serial data corruption, the joys of hard-coded assembly
    return;
  _vibrationLoopLastTime = nowTime;

  // We need to determine where we are in the sequence by adding the ms offset and clamping it to total duration to cope with overflow
  _vibrationCycleCurrentOffsetTime += timeSinceLast;
  while (_vibrationCycleCurrentOffsetTime > _vibrationCycleDuration)
    _vibrationCycleCurrentOffsetTime -= _vibrationCycleDuration;
  
  // Now we enumerate through each motor's settings to determine where it currently is
  for (uint8_t pwmChannel = 0; pwmChannel < 9; pwmChannel++) {
    // Skip if there are no settings for this channel
    if (_vibrationSettings[pwmChannel].NumberValues < 1)
      continue;
    
    // Now based upon our current timestamp determine which value we should be using
    uint16_t startTime = 0;
    for (uint8_t valueIndex = 0; valueIndex < _vibrationSettings[pwmChannel].NumberValues; valueIndex++) {
      // If we have exceeded the end time for this value, simply update our start time and continue
      if (_vibrationCycleCurrentOffsetTime > _vibrationSettings[pwmChannel].Values[valueIndex].Duration + startTime) { 
        startTime += _vibrationSettings[pwmChannel].Values[valueIndex].Duration;
        continue;
      }
      
      // Determine how far through this segment we are
      float currentProgress = (float)(_vibrationCycleCurrentOffsetTime - startTime) / (float)_vibrationSettings[pwmChannel].Values[valueIndex].Duration;
      float diff = (int32_t)_vibrationSettings[pwmChannel].Values[valueIndex].End - (int32_t)_vibrationSettings[pwmChannel].Values[valueIndex].Start;
      uint16_t newValue = (uint16_t)(diff * currentProgress) + _vibrationSettings[pwmChannel].Values[valueIndex].Start;
      _pwmDriver.setPin(pwmChannel, newValue < 0 ? 0 : newValue > 4095 ? 4095 : newValue);
      
      // We've now set the value so we can break
      break;
    }
  }
}

void bluetoothParseInput() {
  // Return if no data to read
  uint32_t availableBytes = _bluetoothSerial.available();
  if (availableBytes < 1)
    return;

  // Now attempt to read data a single line at a time up to a newline
  for (uint32_t i = 0; i < availableBytes; i++)
  {
    char c = _bluetoothSerial.read();

    // If this character is a newline, finish the string and parse it
    if (c == '\n' || c == '\r')
    {
      // Skip for empty strings
      if (_bluetoothBufferLength == 0)
        continue;

      // Parse and reset
      Serial.print(F("Handling command: "));
      Serial.println(_bluetoothBuffer[0]);
      if (_bluetoothBuffer[0] == 'L')
        bluetoothLightCommand();
      else if (_bluetoothBuffer[0] == 'V')
        bluetoothVibrateCommand();
      else {
        Serial.print(F("Unknown command: "));
        Serial.println(_bluetoothBuffer[0]);
      }
      _bluetoothBufferLength = 0;
      continue;
    }

    // If this buffer is full, discard the character
    else if (_bluetoothBufferLength == _bluetoothBufferSize - 1)
      continue;

    // Add this to buffer and increment location
    _bluetoothBuffer[_bluetoothBufferLength] = c;
    _bluetoothBufferLength++;
  }
}

void bluetoothLightCommand() {
  if (_bluetoothBuffer[1] == 'C') {
    _lightMode = LightModeCycle;
    _lightCycleCurrentOffsetTime = 0;
    _lightCycleDuration = (((uint32_t)(_bluetoothBuffer[2] & 0xFF)) << 24) | (((uint32_t)(_bluetoothBuffer[3] & 0xFF)) << 16) | (((uint32_t)(_bluetoothBuffer[4] & 0xFF)) << 8) | ((uint32_t)(_bluetoothBuffer[5] & 0xFF));
    if (_lightCycleDuration < 1)
      _lightCycleDuration = 1;
    _lightCycleBlendAmount = (((uint32_t)(_bluetoothBuffer[6] & 0xFF)) << 24) | (((uint32_t)(_bluetoothBuffer[7] & 0xFF)) << 16) | (((uint32_t)(_bluetoothBuffer[8] & 0xFF)) << 8) | ((uint32_t)(_bluetoothBuffer[9] & 0xFF));
    if (_lightCycleBlendAmount < 1)
      _lightCycleBlendAmount = 1;
    _lightCycleColourCount = _bluetoothBuffer[10] & 0xFF;
    for (int offset = 11, i = 0; i < 10; i++, offset += 3)
      _lightCycleColours[i] = (((uint32_t)(_bluetoothBuffer[offset] & 0xFF)) << 16) | (((uint32_t)(_bluetoothBuffer[offset + 1]  & 0xFF)) << 8) | ((uint32_t)(_bluetoothBuffer[offset + 2]  & 0xFF));
      Serial.print(F("Switch to cycle with duration "));
      Serial.print(_lightCycleDuration);
      Serial.print(F("ms and "));
      Serial.print(_lightCycleColourCount);
      Serial.print(F("colours blending with "));
      Serial.println(_lightCycleBlendAmount);
  }
  else if (_bluetoothBuffer[1] == 'O')
    _lightMode = LightModeOff;
  else if (_bluetoothBuffer[1] == 'S') {
    _lightMode = LightModeSolid;
    if (_bluetoothBufferLength == 5) {
      uint32_t r = _bluetoothBuffer[2] & 0xFF;
      uint32_t g = _bluetoothBuffer[3] & 0xFF;
      uint32_t b = _bluetoothBuffer[4] & 0xFF;
      r = r << 16;
      g = g << 8;
      _lightSolidColour = r|g|b;
    }
  }
}

void bluetoothVibrateCommand() {
  // Each VibrationSetingValue is 6 bytes: [Start-2b][End-2b][Duration-2b]
  // Each VibrationSetting is 31 bytes: [NumberValues-1b][Values-30b]
  // The command itself is then formed of 281bytes: [Duration-2b][VibrationSettings x9-279b]
  
  // First ensure buffer length is correct
  if (_bluetoothBufferLength != 282) {
    Serial.print(F("Unexpected buffer length - got "));
    Serial.print(_bluetoothBufferLength);
    Serial.println(F(" bytes"));
    return;
  }
  
  // Read out the duration to begin
  uint16_t offset = 1;
  _vibrationCycleDuration = shortFromDataBuffer(offset);
  offset += 2;
  
  // Now read each of the 9 motor settings out
  for (uint8_t motorIndex = 0; motorIndex < 9; motorIndex++) {
    // Determine the number of values to use
    _vibrationSettings[motorIndex].NumberValues = shortFromDataBuffer(offset);
    offset += 2;
    
    // Now go through the five blocks we have for values - even if we don't use them as this command is fixed-width
    for (uint8_t valueIndex = 0; valueIndex < 5; valueIndex++) {
      _vibrationSettings[motorIndex].Values[valueIndex].Start = shortFromDataBuffer(offset);
      offset += 2;
      _vibrationSettings[motorIndex].Values[valueIndex].End = shortFromDataBuffer(offset);
      offset += 2;
      _vibrationSettings[motorIndex].Values[valueIndex].Duration = shortFromDataBuffer(offset);
      offset += 2;
    }
  }

  // Finally reset our progress for the new cycle
  _vibrationLoopLastTime = millis();
  _vibrationCycleCurrentOffsetTime = 0;
}

uint16_t shortFromDataBuffer(int offset) {
  uint16_t returnValue = 0;
  returnValue |= _bluetoothBuffer[offset];
  returnValue << 8;
  returnValue |= _bluetoothBuffer[offset + 1];
  return returnValue;
}

void loop() {
  // Handle any pending commands before we do anything else
  bluetoothParseInput();

  // Perform lighting loop if required
  lightPerformLoop();

  // Perform vibration loop if required
  vibrationPerformLoop();
}

uint32_t blendedColour(uint32_t startColour, uint32_t endColour, float divisions, float index) {
  // Get individual start/end bytes and determine difference between them
  byte startR = (byte)(startColour >> 16);
  byte startG = (byte)(startColour >> 8);
  byte startB = (byte)startColour;
  byte endR = (byte)(endColour >> 16);
  byte endG = (byte)(endColour >> 8);
  byte endB = (byte)endColour;
  short diffR = endR - startR;
  short diffG = endG - startG;
  short diffB = endB - startB;

  // Use linear calculation to determine a new value for R,G and B
  float idx = index < 0 ? 0 : index > divisions ? divisions : index;
  uint32_t newR = (uint32_t)((diffR / divisions) * idx) + startR;
  uint32_t newG = (uint32_t)((diffG / divisions) * idx) + startG;
  uint32_t newB = (uint32_t)((diffB / divisions) * idx) + startB;

  // Clamp new values 0-255
  if (newR < 0)
    newR += 255;
  else if (newR > 255)
    newR -= 255;
  if (newG < 0)
    newG += 255;
  else if (newG > 255)
    newG -= 255;
  if (newB < 0)
    newB += 255;
  else if (newB > 255)
    newB -= 255;

  // Shift new R/G values then combine them
  newR = newR << 16;
  newG = newG << 8;
  uint32_t result =  newR|newG|newB;
  return result;
}





