/*
 *  main.ino
 *
 *  copyright (c) Deutsche Telekom AG 2018
 *  Licensed under the BSD 3-Clause License. See the LICENSE file for further details.
 */

/*
user Parameters:
int DOTFADE = 64; //  0-255 - fading time until running LEDs will turn off. 255 = no fade, 64 = med fade, 1 = very slow fade, 0 = all LEDS stay on
#define FRAMES_PER_SECOND 43 - LED refresh FPS
unsigned long BrightnessTimeout = 4000; - Delay after the LEDs will fade out
int minimalBrightness = 50; - Brightness Percentage to set to after fading out
int MinimumTouchTime = 200; - Minimum Touch Time to be sensed
int MaximumTouchTime = 1000; - Maximum Touch Time to be sensed
int ICETouchTime = 3000; - Delay after which the emergency call will be sent after holding both touch planes
int debounceTouch = 2; - values to be removed from start and end of ringbuffer

in BLE.ino:
#define DEVICE_NAME "Smarte Jacke"

in plan.h:
#define MAXPHASES 100



*/

//  "" = suche lokal, <> = suche System
#include <ArduinoJson.h>
#include <BLE2902.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUUID.h>
#include <BLEUtils.h>
#include <FastLED.h> //IMPORTANT: As of May 2018,
// the FastLED v.3.1.6 doesn't fully work with the ESP32, that's why
// the code needs the version from "github.com/samguyer/FastLED" to run.
#include <OneButton.h>
#include <OneWire.h>
#include "DS2413helper.h"
#include "Ringbuffer.h"
#include "TaskManager.h"
#define DS2413_ONEWIRE_PIN (33)
#define DS2413_FAMILY_ID 0x3A
#define DS2413_ACCESS_READ 0xF5
#define DS2413_ACCESS_WRITE 0x5A
#define DS2413_ACK_SUCCESS 0xAA
#define DS2413_ACK_ERROR 0xFF
#define usbPin 35
#define batPin 34
int prevBatValue = 0;
int usbValue = 0;         // VMax = 5V
int batDebounceValue = 0; // VMax = 4.3V
int batValue = 0;         // VMax = 4.3V
int usbVolts = 0;         // Voltage Divider: Ru = 1Mohm, Rd = 300kohm
int batVolts = 0;         // Voltage Divider: Ru = 1Mohm, Rd = 680kohm
#define ONEWIRE_PIN 33
#define FLAG_PIN 19  // pushbutton pin
#define POWER_PIN 27 // power switch (arm) pin
#define DATA_PIN 22  // neopixel pin
OneButton buttonFlag(FLAG_PIN, true);
OneButton buttonPower(POWER_PIN, true);
OneButton button0(0, true);
#define LED_TYPE WS2811
#define COLOR_ORDER GRB
#define ALL_LEDS 10
#define NUM_LEDS 5
CRGB leds[ALL_LEDS];
int BRIGHTNESS = 255;  // akr. 2018-06-27 12-51. was: 100
#define FRAMES_PER_SECOND 43
#define FASTLED_SHOW_CORE 0 // -- The core to run FastLED.show()
static TaskHandle_t FastLEDshowTaskHandle = 0; // Task handles for notifications
static TaskHandle_t userTaskHandle = 0;
int pos = 0;
int DOTSPEEDmilli = 0;
int DOTSPEED = 200; // 40 = fast, 200 = med, 800 = very slow
int DOTFADE = 64; // 0-255 - 255 no fade, 64 med fade, 1 very slow fade, 0 = all
                  // LEDs stay on
int DIRECTION = 1;
uint8_t gHue = 0; // rotating "base color" 360°-> x/256th
uint8_t gValue = 0;
unsigned int progress = 0; // range = 0-100
unsigned int analogValue = 0;
unsigned long dimmingTimeout = 0;
unsigned long BrightnessTimeout = 4000;
//int minimalBrightness = 50;
int minimalBrightness = 255; // akr. 2018-06-27 12-46. basically disanled dimming. was: 50
TaskManager manager;
int FastLEDTaskId;
int SerialDebugTaskId;
int readBattLevelTaskId;
int DS2413TaskId;
// int readTouchTaskId;
int addToRingbufferTaskId;
int OneButtonTaskId;
int BLETaskId;
int dimmingTaskId;
#define COLOR_NONE 0
#define COLOR_RED 1
#define COLOR_YELLOW 2
#define COLOR_GREEN 3
#define COLOR_BLUE 4
boolean TouchChipPresent = 0;

int touchBufferSize = 70;
RingBuffer<int> touchBuffer(touchBufferSize);
int touchTaskDelay = 50;
int MinimumTouchTime = 200;
int MaximumTouchTime = 1000;
int ICETouchTime = 3000;
int countICE = 0;

OneWire oneWire(DS2413_ONEWIRE_PIN);
uint8_t address[8] = {0, 0, 0, 0, 0, 0, 0, 0};
int IOint = 0;
int IOA = 0;
int IOB = 2;
int debounceTouch = 2; // values to be removed from start and end of ringbuffer
                       // value = touchTaskdelay ms


void printBytes(uint8_t *addr, uint8_t count, bool newline = 0) {
  for (uint8_t i = 0; i < count; i++) {
    Serial.print(addr[i] >> 4, HEX);
    Serial.print(addr[i] & 0x0f, HEX);
    Serial.print(" ");
  }
  if (newline) {
    Serial.println();
  }
}

byte read(void) {
  bool ok = false;
  uint8_t results;
  oneWire.reset();
  oneWire.select(address);
  oneWire.write(DS2413_ACCESS_READ);

  results = oneWire.read();                 /* Get the register results   */
  ok = (!results & 0x0F) == (results >> 4); /* Compare nibbles            */
  results &= 0x0F;                          /* Clear inverted values      */

  oneWire.reset();

  // return ok ? results : -1;
  return results;
}

///////////////////////////////////////////////////////////////////////////////////////////
/// FORWARD DECLARATIONS
/// //////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////
void setupJacketBLE();
void BLEwrap();
void clickFlag();
void doubleclickFlag();
void longPressStopFlag();
void longPressStartPower();
void longPressStopPower();
void click0();

///////////////////////////////////////////////////////////////////////////////////////////

void setupRingbuffer() {
  Serial.println(F("Ringbuffer started"));
  addToRingbufferTaskId =
      manager.addTask(addToRingbufferTask, touchTaskDelay, true);
}

void setupDS2413() {
  DS2413TaskId = manager.addTask(DS2413Task, touchTaskDelay, true);

  Serial.println(F("Looking for a DS2413 on the bus"));

  /* Try to find a device on the bus */
  oneWire.reset_search();
  delay(250);
  if (!oneWire.search(address)) {
    printBytes(address, 8);
    Serial.println(F("No device found on the bus!"));
    oneWire.reset_search();
    return;
  }

  /* Check the CRC in the device address */
  if (OneWire::crc8(address, 7) != address[7]) {
    Serial.println(F("Invalid CRC!"));
    return;
  }

  /* Make sure we have a DS2413 */
  if (address[0] != DS2413_FAMILY_ID) {
    printBytes(address, 8);
    Serial.println(F(" is not a DS2413!"));
    return;
  }

  Serial.print(F("Found a DS2413: "));
  printBytes(address, 8);
  Serial.println(F(""));
  TouchChipPresent = 1;
  manager.startTask(addToRingbufferTaskId);
  manager.startTask(DS2413TaskId);
}

/// ARDUINO FUNCTIONS
/// /////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

void setup() {
  buttonFlag.attachClick(clickFlag);
  buttonFlag.attachDoubleClick(doubleclickFlag);
  buttonFlag.attachLongPressStart(longPressStartFlag);
  // buttonFlag.attachLongPressStop(longPressStopFlag);
  // buttonFlag.attachDuringLongPress(longPressFlag);
  // buttonPower.attachClick(clickPower);
  // buttonPower.attachDoubleClick(doubleclickPower);
  buttonPower.attachLongPressStart(longPressStartPower);
  buttonPower.attachLongPressStop(longPressStopPower);
  // buttonPower.attachDuringLongPress(longPressPower);
  button0.attachClick(click0);
  // button0.attachDoubleClick(doubleclick0);
  // button0.attachLongPressStart(longPressStart0);
  // button0.attachLongPressStop(longPressStop0);
  // button0.attachDuringLongPress(longPress0);
  pinMode(usbPin, INPUT);
  pinMode(batPin, INPUT);
  // analogSetAttenuation(ADC_6db);
  // analogReadResolution(11);

  // btStop();
  Serial.begin(115200);
  Serial.println("setup");
  pinMode(5, OUTPUT);
  setupDS2413();
  setupRingbuffer();
  // syntax:
  // int TaskId = manager.addTask(doSth, Interval, Start@boot?) or
  // int TaskId = manager.addTask(doSth,initSth,deinitSth,
  //                   Interval, Start@boot?)
  // manager.startTask(taskId, startafterbootdelay,
  //                   stopafterbootdelay, executeForXTimes,
  //                   bool0=countfromstart/1=countafterexecution)
  setupFastLED(); // removed the .show to stop it from rebooting on startup
  Serial.println("after setupFastLED");
  // readTouchTaskId = manager.addTask(readTouchTask, 75, false);
  OneButtonTaskId = manager.addTask(oneButtonTask, 25, true);
  BLETaskId = manager.addTask(BLETask, 10, true);
  FastLEDTaskId =
      manager.addTask(handleFastLED, initFastLED, deinitFastLED, 50, false);
  readBattLevelTaskId = manager.addTask(handleReadBattLevel, 1000, true);
  SerialDebugTaskId = manager.addTask(handleSerialDebug, 5000, true);
  dimmingTaskId = manager.addTask(handleDimming, 200, false);
  Serial.println("before setupJacketBLE");
  setupJacketBLE();
  Serial.println("before setupDS2413");
  setupDS2413();
  Serial.println("finished setup");
}

void loop() { manager.runTasks(); }
