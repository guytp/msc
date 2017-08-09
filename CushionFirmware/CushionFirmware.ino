
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
} 
typedef VibrationSettingValue;

struct VibrationSetting {
  uint8_t NumberValues;
  VibrationSettingValue Values[5];
} 
typedef VibrationSetting;

// Bluetooth declarations
SoftwareSerial _bluetoothSerial(3, 2);
const uint16_t _bluetoothBufferSize = 300;
char _bluetoothBuffer[_bluetoothBufferSize];
uint16_t _bluetoothBufferLength = 0;

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
bool _isVibrationEnabled = true;

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

  // Setup PWM control for motors and turn everything off to start with and give it a default pattern
  _pwmDriver.begin();
  _pwmDriver.setPWMFreq(40);
  for (byte i = 0; i < 16; i++)
    _pwmDriver.setPin(i, 0);

  // Setup default state
  //setQuizical();
  setCalm();

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

void vibrationPerformLoop() {  
  // Determine how long since we were last called - if we're within threshold then we do nothing
  uint64_t nowTime = millis();
  uint16_t timeSinceLast = (uint16_t)(nowTime - _vibrationLoopLastTime);
  if (timeSinceLast < 50) // Without this arbitrary delay the lower the delay is the more likely we get serial data corruption, the joys of hard-coded assembly
    return;
  _vibrationLoopLastTime = nowTime;

  
  // If we're not vibrating then just set everything off
  if (!_isVibrationEnabled) {
    for (byte i = 0; i < 16; i++)
      _pwmDriver.setPin(i, 0);
    return;
  }

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
  while (true) {
    uint32_t availableBytes = _bluetoothSerial.available();
    if (availableBytes < 1)
      return;
  
    // Now attempt to read data a single line at a time up to a newline
    for (uint32_t i = 0; i < availableBytes; i++)
    {
      char c = _bluetoothSerial.read();
  
      // If this ends command process it
      if ((c == '\n' || c == '\r') && (_bluetoothBufferLength == 0 || (_bluetoothBuffer[0] == 'V' && _bluetoothBufferLength >= 282) || _bluetoothBuffer[0] != 'V'))
      {
        // Ignore newlines during a V command
        // Skip for empty strings
        if (_bluetoothBufferLength == 0)
          continue;
        
        /*
        Serial.print(F("Newline for cmd "));
        Serial.print(_bluetoothBuffer[0]);
        Serial.println((uint8_t)_bluetoothBuffer[0], HEX);
        for (int i =0; i < _bluetoothBufferLength; i++) {
          Serial.print(_bluetoothBuffer[i], HEX);
          Serial.print(F("-"));
          if (i % 16 == 0)
            Serial.println(F(""));
        }
        Serial.println(F(""));
        */
  
        // Parse and reset
        Serial.print(F("Handling command: "));
        Serial.print(_bluetoothBuffer[0]);
        Serial.print(F(" with buffer len "));
        Serial.println(_bluetoothBufferLength);
        bool success = false;
        if (_bluetoothBuffer[0] == 'L')
          success = bluetoothLightCommand();
        else if (_bluetoothBuffer[0] == 'V')
          success = bluetoothVibrateCommand();
        else if (_bluetoothBuffer[0] == 'S')
          success = bluetoothStateCommand();
        else {
          Serial.print(F("Unknown command: "));
          Serial.println(_bluetoothBuffer[0]);
        }
        _bluetoothSerial.write(success ? 1 : 0);
        _bluetoothBufferLength = 0;
        continue;
      }
  
      // If this buffer is full, discard the character
      else if (_bluetoothBufferLength == _bluetoothBufferSize - 1)
      {
        Serial.print(F("Discarding extra character ("));
        Serial.print(c);
        Serial.print(F(") - current length is "));
        Serial.print(_bluetoothBufferLength);
        Serial.print(F(" of max "));
        Serial.println(_bluetoothBufferSize - 1);
        continue;
      }
  
      // Add this to buffer and increment location
      _bluetoothBuffer[_bluetoothBufferLength] = c;
      _bluetoothBufferLength++;
    }
  }
}

bool bluetoothStateCommand() {
  byte state = 0;
  if (_bluetoothBufferLength > 1)
    state = _bluetoothBuffer[1];
  if (state > 4)
    state = 0;
  _isVibrationEnabled = state != 0;
  _lightMode = state == 0 ? LightModeOff : LightModeCycle;
  if (state == 0)
    Serial.println(F("State to off"));
  else if (state == 1) {
    // Happy
    Serial.println(F("State to happy"));
    setHappy();
  } else if (state == 2) {
    // Sad
    Serial.println(F("State to sad"));
    setSad();
  } else if (state == 3) {
    // Calm
    Serial.println(F("State to calm"));
    setCalm();    
  } else if (state == 4) {
    // Angry
    Serial.println(F("State to angry"));
    setAngry();
  }

  // Set loops to immediately process
  _vibrationLoopLastTime = millis();
  _vibrationCycleCurrentOffsetTime = 0;
  _lightCycleCurrentOffsetTime = 0;
  Serial.println(F("State toggling complete"));
  return true;
}

void setQuizical() {  
    _lightCycleColours[0] = 0xFFc700ff;
    _lightCycleColours[1] = 0xFFc700ff;
    _lightCycleColours[2] = 0xFFc700ff;
    _lightCycleColours[3] = 0xFFc700ff;
    _lightCycleColours[4] = 0xFFc700ff;
    _lightCycleColours[5] = 0xFFc700ff;
    _lightCycleColours[6] = 0xFFFF0000;
    _lightCycleColours[7] = 0xFF00FF00;
    _lightCycleColours[8] = 0xFF0000FF;
    _lightCycleColourCount = 9;
    _lightCycleDuration = 2000;
    _lightCycleBlendAmount = 20;
    _vibrationCycleDuration = 2000;
    for (int i = 0; i < 9; i++) {
        boolean isCycle1 = i == 0 || i == 3 || i == 6;
        boolean isCycle2 = i == 1 || i == 4 || i == 7;
        boolean isCycle3 = !isCycle1 && !isCycle2;
        _vibrationSettings[i].NumberValues = 5;
        _vibrationSettings[i].Values[0].Start = 4095;
        _vibrationSettings[i].Values[0].End = 4095;
        _vibrationSettings[i].Values[0].Duration = 550;
        _vibrationSettings[i].Values[1].Start = 4095;
        _vibrationSettings[i].Values[1].End = 2500;
        _vibrationSettings[i].Values[1].Duration = 550;
        _vibrationSettings[i].Values[2].Start = (short)(isCycle1 ? 4095 : 0);
        _vibrationSettings[i].Values[2].End = (short)(isCycle1 ? 4095 : 0);
        _vibrationSettings[i].Values[2].Duration = 300;
        _vibrationSettings[i].Values[3].Start = (short)(isCycle2 ? 4095 : 0);
        _vibrationSettings[i].Values[3].End = (short)(isCycle2 ? 4095 : 0);
        _vibrationSettings[i].Values[3].Duration = 300;
        _vibrationSettings[i].Values[4].Start = (short)(isCycle3 ? 4095 : 0);
        _vibrationSettings[i].Values[4].End = (short)(isCycle3 ? 4095 : 0);
        _vibrationSettings[i].Values[4].Duration = 300;
    }    
}

void setHappy() {
  
    _lightCycleColours[0] = 0xff2500;
    _lightCycleColours[1] = 0xff2500;
    _lightCycleColours[2] = 0xff2500;
    _lightCycleColours[3] = 0xff3400;
    _lightCycleColours[4] = 0xff4300;
    _lightCycleColours[5] = 0xff5200;
    _lightCycleColours[6] = 0xff6100;
    _lightCycleColours[7] = 0x00FF00;
    _lightCycleColours[8] = 0xff6100;
    _lightCycleColours[9] = 0xff2500;
    _lightCycleColourCount = 10;
    _lightCycleDuration = 4000;
    _lightCycleBlendAmount = 20;
    _vibrationCycleDuration = 4000;
    for (int i = 0; i < 9; i++) {
        _vibrationSettings[i].NumberValues = 3;
        _vibrationSettings[i].Values[0].Start = 2500;
        _vibrationSettings[i].Values[0].End = 2500;
        _vibrationSettings[i].Values[0].Duration = 2667;
        _vibrationSettings[i].Values[1].Start = 2500;
        _vibrationSettings[i].Values[1].End = 4095;
        _vibrationSettings[i].Values[1].Duration = 1000;
        _vibrationSettings[i].Values[2].Start = 4095;
        _vibrationSettings[i].Values[2].End = 4095;
        _vibrationSettings[i].Values[2].Duration = 333;
    }    
  /* Previouys fairly static attempt
    _lightCycleColours[0] = 0XFFFF008C;
    _lightCycleColours[1] = 0xff219A;
    _lightCycleColours[2] = 0XFFFF008C;
    _lightCycleColourCount = 3;
    _lightCycleDuration = 2400;
    _lightCycleBlendAmount = 1;
    _vibrationCycleDuration = 2400;
    for (int i = 0; i < 9; i++) {
        _vibrationSettings[i].NumberValues = 4;
        _vibrationSettings[i].Values[i > 4 ? 0 : 3].Start = 3500;
        _vibrationSettings[i].Values[i > 4 ? 0 : 3].End = 4095;
        _vibrationSettings[i].Values[i > 4 ? 0 : 3].Duration = 800;
        _vibrationSettings[i].Values[i > 4 ? 1 : 2].Start = 4095;
        _vibrationSettings[i].Values[i > 4 ? 1 : 2].End = 3500;
        _vibrationSettings[i].Values[i > 4 ? 1 : 2].Duration = 600;
        _vibrationSettings[i].Values[i > 4 ? 2 : 1].Start = 2500;
        _vibrationSettings[i].Values[i > 4 ? 2 : 1].End = 2500;
        _vibrationSettings[i].Values[i > 4 ? 2 : 1].Duration = 400;
        _vibrationSettings[i].Values[i > 4 ? 3 : 0].Start = 0;
        _vibrationSettings[i].Values[i > 4 ? 3 : 0].End = 0;
        _vibrationSettings[i].Values[i > 4 ? 3 : 0].Duration = 600;
    }
    */
}

void setSad() {  
    _lightCycleColours[0] = 0xFFc700ff;
    _lightCycleColours[1] = 0xff19002b;
    _lightCycleColours[2] = 0xFFc700ff;
    _lightCycleColourCount = 3;
    _lightCycleDuration = 12000;
    _lightCycleBlendAmount = 1;
    _vibrationCycleDuration = 6000;
    for (int i = 0; i < 9; i++) {
        _vibrationSettings[i].NumberValues = 2;
        _vibrationSettings[i].Values[0].Start = 2500;
        _vibrationSettings[i].Values[0].End = 0;
        _vibrationSettings[i].Values[0].Duration = 5000;
        _vibrationSettings[i].Values[1].Start = 0;
        _vibrationSettings[i].Values[1].End = 0;
        _vibrationSettings[i].Values[1].Duration = 1000;
    }
}

void setCalm() {
    _lightCycleColours[0] = 0xFF00a5ff;
    _lightCycleColours[1] = 0xFF54ff00;
    _lightCycleColours[2] = 0xFF00a5ff;
    _lightCycleColourCount = 3;
    _lightCycleDuration = 6000;
    _lightCycleBlendAmount = 20;
    _vibrationCycleDuration = 1500;
    for (int i = 0; i < 9; i++) {
        _vibrationSettings[i].NumberValues = 3;
        _vibrationSettings[i].Values[0].Start = 1500;
        _vibrationSettings[i].Values[0].End = 2500;
        _vibrationSettings[i].Values[0].Duration = 800;
        _vibrationSettings[i].Values[1].Start = 2500;
        _vibrationSettings[i].Values[1].End = 1500;
        _vibrationSettings[i].Values[1].Duration = 600;
        _vibrationSettings[i].Values[2].Start = 0;
        _vibrationSettings[i].Values[2].End = 0;
        _vibrationSettings[i].Values[2].Duration = 100;
    }
}

void setAngry() {
    _lightCycleColours[0] = 0xFFFF0000;
    _lightCycleColours[1] = 0xFFff2600;
    _lightCycleColours[2] = 0xFFFF0000;
    _lightCycleColourCount = 3;
    _lightCycleDuration = 1000;
    _lightCycleBlendAmount = 20;
    _vibrationCycleDuration = 1500;
    for (int i = 0; i < 9; i++) {
        _vibrationSettings[i].NumberValues = 5;
        _vibrationSettings[i].Values[0].Start = 4095;
        _vibrationSettings[i].Values[0].End = 4095;
        _vibrationSettings[i].Values[0].Duration = 200;
        _vibrationSettings[i].Values[1].Start = 0;
        _vibrationSettings[i].Values[1].End = 0;
        _vibrationSettings[i].Values[1].Duration = 200;
        _vibrationSettings[i].Values[2].Start = 4095;
        _vibrationSettings[i].Values[2].End = 4095;
        _vibrationSettings[i].Values[2].Duration = 400;
        _vibrationSettings[i].Values[3].Start = 0;
        _vibrationSettings[i].Values[3].End = 0;
        _vibrationSettings[i].Values[3].Duration = 200;
        _vibrationSettings[i].Values[4].Start = 4095;
        _vibrationSettings[i].Values[4].End = 0;
        _vibrationSettings[i].Values[4].Duration = 500;
    }
}

bool bluetoothLightCommand() {
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
  return true;
}

bool bluetoothVibrateCommand() {
  // Each VibrationSetingValue is 6 bytes: [Start-2b][End-2b][Duration-2b]
  // Each VibrationSetting is 31 bytes: [NumberValues-1b][Values-30b]
  // The command itself is then formed of 281bytes: [Duration-2b][VibrationSettings x9-279b]

  // First ensure buffer length is correct
  if (_bluetoothBufferLength != 282) {
    Serial.print(F("Unexpected buffer length - got "));
    Serial.print(_bluetoothBufferLength);
    Serial.println(F(" bytes"));
    return false;
  }

  // Read out the duration to begin
  uint16_t offset = 1;
  _vibrationCycleDuration = shortFromDataBuffer(offset);
  offset += 2;

  // Now read each of the 9 motor settings out
  for (uint8_t motorIndex = 0; motorIndex < 9; motorIndex++) {
    // Determine the number of values to use
    _vibrationSettings[motorIndex].NumberValues = _bluetoothBuffer[offset];
    offset++;

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
  Serial.println(F("Loaded new vibration settings"));
  _vibrationLoopLastTime = millis();
  _vibrationCycleCurrentOffsetTime = 0;
  _isVibrationEnabled = true;
  return true;
}

uint16_t shortFromDataBuffer(int offset) {
  uint16_t returnValue = 0;
  uint8_t b1 = _bluetoothBuffer[offset];
  uint8_t b2 = _bluetoothBuffer[offset + 1];
  returnValue |= b1;
  returnValue = returnValue << 8;
  returnValue |= b2;
  return returnValue;
}

void loop() {
  // Handle any pending commands before we do anything else
  bluetoothParseInput();
  if (_bluetoothBufferLength > 0)
    return;

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






