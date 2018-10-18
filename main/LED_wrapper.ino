/*
 *  LED_wrapper.ino
 *
 *  copyright (c) Deutsche Telekom AG 2018
 *  Licensed under the BSD 3-Clause License. See the LICENSE file for further details.
 */


extern uint8_t gHue;
extern unsigned long BrightnessTimeout;

bool setupFastLED() {
  Serial.println("startFastLED");
  FastLED.addLeds<LED_TYPE, DATA_PIN, COLOR_ORDER>(leds, ALL_LEDS)
      .setCorrection(TypicalLEDStrip);
  FastLED.setBrightness(BRIGHTNESS);
  /*
  int core = xPortGetCoreID();
  Serial.print("Main code running on core ");
  Serial.println(core);
  xTaskCreatePinnedToCore(FastLEDshowTask, "FastLEDshowTask", 2048, NULL, 2,
                          &FastLEDshowTaskHandle, FASTLED_SHOW_CORE);
  */
  FastLED.clear();
  //  FastLEDshowESP32();
  FastLED.show();
  return true;
}

bool initFastLED() {
  Serial.println("initFastLED");
  FastLED.clear();
  // FastLEDshowESP32();
  FastLED.show();
  manager.startTask(dimmingTaskId);
  return true;
}

bool deinitFastLED() {
  Serial.println("deinitFastLED");
  FastLED.clear();
  // FastLEDshowESP32();
  FastLED.show();
  manager.stopTask(dimmingTaskId);
  return true;
}

bool handleDimming() {
  if (plan.phases[currPhase-1].duration < 10000) {
    return true;
  }
  unsigned long m = millis();
  if (m  < dimmingTimeout ) {
    BRIGHTNESS = 255;
  }
  else if (m < (dimmingTimeout+BrightnessTimeout)){
    BRIGHTNESS = map((dimmingTimeout+BrightnessTimeout-m),BrightnessTimeout,0,255, minimalBrightness);
  }
  return true;
}



bool handleFastLED() {
  // Serial.println("handleFastLED");
  around();
  fillleds();
  //  FastLEDshowESP32();
  FastLED.show();
  return true;
}

void fillleds() {

  // for (int i = 5; i <= 9; i++) {
  // int iVal = map(currProgress, ((i - 5) * 20), (((i - 5) * 20) + 20), 0,
  // 255); iVal = constrain(iVal, 0, 255); leds[i] = CHSV(180, 200, iVal);
  // }

  int t = currProgress;
  int h = map(392, 0, 360, 0, 255);
  int s = 100;
  for (int i = 5; i <= 9; i++) {
    //  leds[i] = t > 20 ? CHSV(180, 200, 255) : t <= 0 ? CHSV(180, 200, 0) :
    //  CHSV(180, 200, map(t, 0, 20, 0, 255));
    
//    leds[i] = t > 20 ? CHSV(180, 200, BRIGHTNESS)
//                     : t <= 0 ? CHSV(180, 200, 0)
//                              : CHSV(180, 200, map(t, 0, 20, 0, BRIGHTNESS));

    leds[i] = t > 20 ? CHSV(h, s, BRIGHTNESS)
                     : t <= 0 ? CHSV(0, 0, 0)
                              : CHSV(h, s, map(t, 0, 20, 0, BRIGHTNESS));
    t -= 20;
  }
}

void around() {
  fadeToBlackBy(leds, NUM_LEDS, DOTFADE);
  if (millis() > (DOTSPEEDmilli + DOTSPEED) && currPlanState == PLAN_STATE_STARTED) {
    //  Serial.print("doing the around(+-), pos = ");
    //  Serial.println(pos);
    DOTSPEEDmilli = millis();
    switch (DIRECTION) {
    case 1:
      if (pos >= (NUM_LEDS - 1)) {
        pos = 0;
      } else {
        pos++;
      }
      break;

    case -1:
      if (pos <= 0) {
        pos = (NUM_LEDS - 1);
      } else {
        pos--;
      }
      break;

    case 0:
      // do some blinking magic - needs to get out of the DOTSPEEDmilli case
      break;
    }
    // for (int i=0; i < 5; i++) {
    //   leds[i] = CHSV(0, 255, 0);
    // }
    // Serial.printf("%d %d %d\n", (pos+(2*DIRECTION)+5) % 5,
    // (pos+(1*DIRECTION)+5) % 5, pos); leds[(pos-(2*DIRECTION)+5) % 5] =
    // CHSV((gHue), 255, BRIGHTNESS/4); leds[(pos-(1*DIRECTION)+5) % 5] =
    // CHSV((gHue), 255, BRIGHTNESS/1.5);
    leds[pos] = CHSV((gHue), 255, BRIGHTNESS);
  }
  else if (millis() > (DOTSPEEDmilli + 600) &&
                  currPlanState == PLAN_STATE_PAUSED)
    {
    fadeToBlackBy(leds, NUM_LEDS, 50);
    DOTSPEEDmilli = millis();
    for (int i = 0; i < 5; i++){
      leds[i] = CHSV((20), 255, BRIGHTNESS); //20 = orange, indicating pause
    }
  }
}
int handleColor(String v) {
  // Serial.printf("String v: .%s.\n", v.c_str());
  if (v.compareTo("r") == 0) {
    return 0;
  } else if (v.compareTo("y") == 0) {
    return 60.0 / 360.0 * 255.0;
  } else if (v.compareTo("g") == 0) {
    return 120.0 / 360.0 * 255.0;
  } else if (v.compareTo("b") == 0) {
    return 240.0 / 360.0 * 255.0;
  } else
    BRIGHTNESS = 0;
  return 0;
}

/*
void FastLEDshowESP32() {
  if (userTaskHandle == 0) {
    // -- Store the handle of the current task, so that the show task can
    //    notify it when it's done
    userTaskHandle = xTaskGetCurrentTaskHandle();

    // -- Trigger the show task
    xTaskNotifyGive(FastLEDshowTaskHandle);

    // -- Wait to be notified that it's done
    const TickType_t xMaxBlockTime = pdMS_TO_TICKS(200);
    ulTaskNotifyTake(pdTRUE, xMaxBlockTime);
    userTaskHandle = 0;
  }
}

void FastLEDshowTask(void *pvParameters) {
  // -- Run forever...
  for (;;) {
    // -- Wait for the trigger
    ulTaskNotifyTake(pdTRUE, portMAX_DELAY);

    // -- Do the show (synchronously)
    FastLED.show();

    // -- Notify the calling task
    xTaskNotifyGive(userTaskHandle);
  }
}
*/
