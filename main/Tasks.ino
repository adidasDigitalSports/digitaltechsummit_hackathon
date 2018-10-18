/*
 *  Tasks.ino
 *
 *  copyright (c) Deutsche Telekom AG 2018
 *  Licensed under the BSD 3-Clause License. See the LICENSE file for further details.
 */
#include "plan.h"

extern Plan plan;
extern uint8_t gHue;
bool charging = 0;

bool handleSerialDebug() {

  Serial.println("");
  Serial.print("Duration: ");
  Serial.println(plan.phases[currPhase - 1].duration);
  Serial.print("currPlanName: ");
  Serial.println(currPlanName);
  Serial.print("currPlanState: ");
  Serial.println(currPlanState);
  Serial.print("currProgress: ");
  Serial.println(currProgress);
  Serial.print("currPhase: ");
  Serial.print(currPhase);
  Serial.print(" / ");
  Serial.println(numPhases);
  Serial.print("currCtrlMode: : ");
  Serial.println(currCtrlMode);
  Serial.print("currBattLevel: ");
  Serial.println(currBattLevel);
  Serial.print("Iteration: ");
  Serial.print(currIteration);
  Serial.print(" / ");
  Serial.println(plan.iterations);

  return true;
}

bool handleReadBattLevel() {
  usbValue = analogRead(usbPin);
  batValue = analogRead(batPin);
  batDebounceValue = ((prevBatValue + batValue) / 2);
  batVolts = map(batDebounceValue, 2520, 4042, 3000, 4200);
  // Serial.print(" USBin= ");
  // Serial.println(usbValue);
  // Serial.print(" , BATTin = ");
  // Serial.println(batDebounceValue);
  //
  // Serial.print(" , BATT V= ");
  // Serial.println(batVolts);
  prevBatValue = batValue;

  currBattLevel = map(batVolts, 3200, 4200, 0, 99);
  currBattLevel = constrain(currBattLevel, 0, 99);
  if (usbValue >= 1000) {
    currBattLevel = 100;
    charging = 1;
  } else {
    charging = 0;
  }

  return true;
}

bool oneButtonTask() {
  buttonFlag.tick();
  buttonPower.tick();
  button0.tick();

  return true;
}

void clickFlag() {
  dimmingTimeout = millis() + BrightnessTimeout;
  Serial.println("click Flag");
  if (currPlanState == PLAN_STATE_STARTED) {
    currPlanState = PLAN_STATE_PAUSED;
    Serial.println("Pause Training");
  } else if (currPlanState == PLAN_STATE_PAUSED) {
    currPlanState = PLAN_STATE_STARTED;
    Serial.println("Resume Training");
  } else if (currPlanState == PLAN_STATE_READY) {
    currPlanState = PLAN_STATE_STARTED;
    Serial.println("Start Training");
    currIteration = 1;
    currPhase = 1;
    currProgress = 0;
  }
}

void doubleclickFlag() {
  dimmingTimeout = millis() + BrightnessTimeout;
  Serial.println("double click Flag");
  if (currPlanState == PLAN_STATE_STARTED) {
    skipPhase();
    Serial.println("Skip Phase");
  }
}

void longPressStartFlag() {
  dimmingTimeout = millis() + BrightnessTimeout;
  Serial.println("long Press Flag");
  if (currPlanState == PLAN_STATE_STARTED) {
    endPlan();
    Serial.println("Stop Training");
  } else if (currPlanState == PLAN_STATE_ENDED ||
             currPlanState == PLAN_STATE_READY) {
    currPlanState = PLAN_STATE_STARTED;
    currIteration = 1;
    currPhase = 1;
    currProgress = 0;
    manager.startTask(FastLEDTaskId);
    Serial.println("Restart Training");
  }
}

void longPressStartPower() {
  dimmingTimeout = millis() + BrightnessTimeout;
  if (TouchChipPresent == 1) {
    manager.startTask(addToRingbufferTaskId);
    manager.startTask(DS2413TaskId);
  }
  manager.startTask(OneButtonTaskId);
  manager.startTask(readBattLevelTaskId);
  manager.startTask(SerialDebugTaskId);
  manager.startTask(FastLEDTaskId);
  // currProgress = 0;
  // currPhase = 0;
  Serial.println("Power ON");
}

void longPressStopPower() {
  endPlan();
  dimmingTimeout = millis() + BrightnessTimeout;
  manager.stopTask(addToRingbufferTaskId);
  manager.stopTask(DS2413TaskId);
  manager.stopTask(FastLEDTaskId);
  manager.startTask(readBattLevelTaskId);
  manager.startTask(OneButtonTaskId);
  manager.startTask(SerialDebugTaskId);
  Serial.println("Power OFF");
}

void click0() {
  manager.stopTask(FastLEDTaskId);
  ESP.restart();
}

bool DS2413Task() {
  uint8_t state = read();
  IOint = int(state);
  return true;
}

DynamicJsonBuffer jsonBuffer(JSON_BUFF_SIZE);
JsonObject &jsonReply = jsonBuffer.createObject();

void sendTouch(int touch) {
  jsonReply["si"] = "btn";
  switch (touch) {
  case 112:
    jsonReply["bt"] = "ice";
    break;
  case 89:
    jsonReply["bt"] = "a2b";
    break;
  case 98:
    jsonReply["bt"] = "b2a";
    break;
  case 14:
    jsonReply["bt"] = "a";
    break;
  case 11:
    jsonReply["bt"] = "b";
    break;
  case 15:
    jsonReply["bt"] = "ab";
    break;
  }
  sendServerMsg(jsonReply);
  dimmingTimeout = millis() + BrightnessTimeout;
  Serial.printf("Input Sequence sended: %d\n", touch);
}

bool addToRingbufferTask() {
  touchBuffer = IOint;
  countICE = touchBuffer.getLatest() == TOUCH_BOTH ? countICE + 1 : 0;
  if (countICE >= touchBufferSize) {
    countICE = 0;
    // if (touchBuffer.count() * touchTaskDelay >= ICETouchTime && ) {
    sendTouch(112);
    Serial.println("ICE");
    touchBuffer.clear();
    return true;
  }

  if (touchBuffer.getLatest() == TOUCH_NONE) { // if touch is released

    // Serial.printf("Current touchBuffer: ");
    // for (int i = 0; i < touchBuffer.count(); i++) {
    //   Serial.printf(" %d ,", touchBuffer[i]);
    // }
    // Serial.printf("\n    Without release:");
    touchBuffer.sliceHead(1);
    // for (int i = 0; i < touchBuffer.count(); i++) {
    //   Serial.printf(" %d ,", touchBuffer[i]);
    // }
    touchBuffer.slice(debounceTouch);

    int lastTouchValueBeforeRelease =
        touchBuffer.getLatest(); // last real input (no TOUCH_NONE)
    int touchXvalue = 0;
    int touchCount = touchBuffer.count(); //
    // for (int i = 1; i <= touchCount;
    //      i++) { // for so many times as there are real input values
    //   Serial.printf("\nfor loop #%d = %d", i,
    //                 touchBuffer[i - 1]); // print out the iteration
    //   touchXvalue += touchBuffer[i - 1]; // add up all the values in array
    // }
    if (touchCount * touchTaskDelay >= MinimumTouchTime &&
        touchCount * touchTaskDelay <= MaximumTouchTime) {
      Serial.printf("\n%d was last touch, all values * %d (count) = %d, Touch "
                    "Time = %d ms\n",
                    lastTouchValueBeforeRelease, touchCount, touchXvalue,
                    (touchCount * touchTaskDelay));
      Serial.printf("input time valid\n");
      int firstInput = 0, secondInput = 0, thirdInput = 0; // remove duplicates
      firstInput = touchBuffer[0];
      // Serial.printf("touchCount: %d, touchTaskDelay: %d\n", touchCount,
      // touchTaskDelay);

      for (int i = 0; i < touchBuffer.count(); i++) {
        int j = i + 1;
        if (j < touchBuffer.count()) {
          if (firstInput != touchBuffer[j] && secondInput == 0) {
            secondInput = touchBuffer[j];
          } else if (firstInput != touchBuffer[j] &&
                     secondInput != touchBuffer[j] && thirdInput == 0) {
            thirdInput = touchBuffer[j];
          }
        }
      }
      Serial.printf("Sensed: %d, %d, %d\n", firstInput, secondInput,
                    thirdInput);

      if (firstInput == TOUCH_A) {
        if (secondInput == 0) {
          sendTouch(TOUCH_A);
        } else if (secondInput == TOUCH_B ||
                   (secondInput == TOUCH_BOTH && thirdInput == TOUCH_B)) {
          sendTouch(TOUCH_A2B);
        }
      } else if (firstInput == TOUCH_BOTH) {
        if (secondInput == 0) {
          sendTouch(TOUCH_BOTH);
        } else if (secondInput == TOUCH_A) {
          sendTouch(TOUCH_B2A);

        } else if (secondInput == TOUCH_B) {
          sendTouch(TOUCH_A2B);
        }
      } else if (firstInput == TOUCH_B) {
        if (secondInput == 0) {
          sendTouch(TOUCH_B);
        } else if (secondInput == TOUCH_A ||
                   (secondInput == TOUCH_BOTH && thirdInput == TOUCH_A)) {
          sendTouch(TOUCH_B2A);
        }
      }

      // for (int i = 0; i < touchBuffer.count(); i++) {
      //   Serial.printf(" %d ,", touchBuffer[i]);
      // }

    } // endif within min&MaximumTouchTime

    touchBuffer.clear();
    // Serial.printf("buffer cleared\n");
  } // endif touchreleased

  return true;
}
