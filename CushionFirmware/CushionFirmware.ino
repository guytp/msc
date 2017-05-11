#include <Adafruit_PWMServoDriver.h>
#include <Adafruit_NeoPixel.h>
#include <Wire.h>
#include <SoftwareSerial.h>

/*
 * ToDo
 *  - Cycle through app [10 colours, count of how many to use, duration slider, blend amount 0-200%]
 *  - Vibrate On/Off/Set pattern
 *  - Tidy up code
 */

enum LightMode {
  LightModeOff = 0,
  LightModeCycle = 1,
  LightModeSolid = 2
} 
typedef LightMode;


// Bluetooth declarations
SoftwareSerial _bluetoothSerial(2, 3);
const byte _bluetoothBufferSize = 42;
char _bluetoothBuffer[_bluetoothBufferSize];
byte _bluetoothBufferLength = 0;

// Lighting declarations
Adafruit_NeoPixel _lightStrip = Adafruit_NeoPixel(30, 6, NEO_GRB + NEO_KHZ800);
unsigned long _lightLoopLastTime = 0;
LightMode _lightMode = LightModeCycle;
uint32_t _lightSolidColour = 0xFF0000;
uint16_t _lightCycleCurrentOffsetTime = 0; // How many ms are we into the current sequence for when we are colour cycling
uint8_t _lightCycleColourCount = 4;
uint32_t _lightCycleColours[10];
uint32_t _lightCycleDuration = 10000; // In ms
uint32_t _lightCycleBlendAmount = _lightCycleDuration * 1; // How many different colours are visible of total amount at any time (i.e. 30 means each LED is a different colour within the cycle, 60 means the same but with every second colour skipped and 1 means everything is the same colour)

// Vibration declarations
Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver();
unsigned long _lastMotorCycle = 0;
unsigned char _motorSequence = 0;


void setup() {
  // Begin serial for debugging purposes
  Serial.begin(9600);
  Serial.println(F("Cushion Firmware - Debug Interface"));

  // Setup neopixel strip to be enabled with correct brightness and all pixels turned off
  _lightStrip.begin();
  _lightStrip.setBrightness(64);
  for(uint16_t i=0; i<_lightStrip.numPixels(); i++)
    _lightStrip.setPixelColor(i, 0);
  _lightStrip.show();
  _lightCycleColours[0] = 0xFF0000;
  _lightCycleColours[1] = 0x00FF00;
  _lightCycleColours[2] = 0x0000FF;
  _lightCycleColours[3] = 0xFF0000;

  // Setup PWM control for motors and turn everything off to start with
  pwm.begin();
  pwm.setPWMFreq(40);
  for (byte i = 0; i < 16; i++)
    pwm.setPin(i, 0);

  // Enable bluetooth interface
  _bluetoothSerial.begin(9600); 
} 

void lightPerformLoop() {
  // Determine how long since we were last called - if we're in same millisecond then we do nothing  
  uint64_t lastLightLoopTime = millis();
  uint16_t timeSinceLast = (uint16_t)(lastLightLoopTime - _lightLoopLastTime);
  if (timeSinceLast < 50) // Without this arbitrary delay the lower the delay is the more likely we get serial data corruption, the joys of hard-coded assembly
    return;
  _lightLoopLastTime = lastLightLoopTime;

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
}


void loop() {
  // Handle any pending commands before we do anything else
  bluetoothParseInput();

  // Perform lighting loop if required
  lightPerformLoop();

  // Determine if motors need an update
  unsigned long motorCycles = (millis() / 10) + 1;
  if (motorCycles > _lastMotorCycle)
  {
    int numberCycles = motorCycles - _lastMotorCycle;
    for (int i = 0; i < numberCycles; i++)
      updateMotors();
    _lastMotorCycle = motorCycles;
  }
}

void updateMotors() {
  int maxSeq = 400;
  int minVal = 1000;
  int maxVal = 4095;
  int range = maxVal - minVal;
  int pauseSeqs = 300;

  int thisVal = 0;
  int incr  = range / maxSeq;
  if (_motorSequence < maxSeq)
    thisVal = (incr * (_motorSequence + 1)) + minVal;
  else
    thisVal = maxVal;
  //  else if (_motorSequence < maxSeq * 2)
  //    thisVal = 4095 - (incr * _motorSequence);
  pwm.setPin(12, thisVal);
  pwm.setPin(13, thisVal);

  _motorSequence++;
  if (_motorSequence >= maxSeq + pauseSeqs)
    _motorSequence = 0;

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





