/*
 *  BLE.ino
 *
 *  copyright (c) Deutsche Telekom AG 2018
 *  Licensed under the BSD 3-Clause License. See the LICENSE file for further details.
 *
 *
 *  Based on schema v4.
 *   Works with test app App_BLE_07_schema4, and the Beta version of the actual
 *   app. Sets up the jacket service and allows to test using the Serial port.
 *   Commands received via Serial will trigger the same function as those received
 *   via BLE. Likewise, commands/replies will be sent to Serial and BLE.
 *   When a client message is received the LED stays on for 500 msec.
 *   Some test behaviour is implemented. The server replies to some of the
 *   commands and simulates some of the state values - see handleClientCommand()
 *   and loop(). Client commands are received as Json objects: void
 *   handleClientCommand(JsonObject& jsonObj) at the bottom. Server messages are
 *   sent as Json objects: void sendServerMsg(JsonObject& jsonObj) The following
 *   are used to set timestamp and state in the BLE service: void
 *   setTimestamp(unsigned long ulTimestamp) void setPlanName(String &
 *   strPlanName) void setState(uint8_t planState, uint8_t phase, uint8_t
 *   progress, uint8_t ctrlMode, uint8_t battLevel) call in setup:
 *       setupJacketBLE();
 *     call in loop:
 *       BLEwrap(); (time delay 50ms)
 */

#define JSON_BUFF_SIZE 256
#define CLIENT_MSG_BUFF_SIZE 256

#define PIN_LED 5
#define TIMEOUT_LED 500

extern uint8_t gHue;

///////////////////////////////////////////////////////////////////////////////////////////
/// BLE SERVICE DEFINES
/// ///////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

#define DEVICE_NAME "Smarte Jacke"

#define UUID_SERVICE "E708EB00-AD98-4158-B7C2-A748744694AB"
#define UUID_CHARACT_TIMESTAMP "E708EB01-AD98-4158-B7C2-A748744694AB"
#define UUID_CHARACT_PLAN_NAME "E708EB02-AD98-4158-B7C2-A748744694AB"
#define UUID_CHARACT_STATE "E708EB03-AD98-4158-B7C2-A748744694AB"
#define UUID_CHARACT_CLIENT_CMD "E708EB04-AD98-4158-B7C2-A748744694AB"
#define UUID_CHARACT_SERVER_MSG "E708EB05-AD98-4158-B7C2-A748744694AB"

///////////////////////////////////////////////////////////////////////////////////////////
/// PROTOCOL AND JSON DATA DEFINES
/// ////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

#define PROTOCOL_COMMAND "co"
#define PROTOCOL_NEW_PLAN "npl"
#define PROTOCOL_START_PLAN "spl"

#define PROTOCOL_ACKNOWLEDGE "ack"
#define PROTOCOL_STATUS_CODE "rs"

#define DATA_PLAN "pl"
#define DATA_NAME "na"
#define DATA_PHASES "ph"

#define STATUS_OK 200
#define STATUS_NO_CONTENT 204
#define STATUS_BAD_REQUEST 400
#define STATUS_NOT_ALLOWED 405
#define STATUS_INTERNAL_ERR 500
#define STATUS_NOT_IMPLEM 501

///////////////////////////////////////////////////////////////////////////////////////////
/// JACKET BLE GLOBALS
/// ////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

BLEServer *pJacketServer;
BLEService *pJacketService;
BLECharacteristic *pJacketCharactTimestamp;
BLECharacteristic *pJacketCharactPlanName;
BLECharacteristic *pJacketCharactState;
BLECharacteristic *pJacketCharactClientCmd;
BLECharacteristic *pJacketCharactServerMsg;

bool deviceConnected = false;

///////////////////////////////////////////////////////////////////////////////////////////
/// GLOBALS
/// ///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

#define PLAN_STATE_NONE 0
#define PLAN_STATE_READY 1
#define PLAN_STATE_STARTED 2
#define PLAN_STATE_PAUSED 3
#define PLAN_STATE_ENDED 4

#define CTRL_MODE_NONE 0
#define CTRL_MODE_MUSIC 1
#define CTRL_MODE_CALL 2

unsigned long timeLastRx = 0;
unsigned long timeLastProgress = 0; // for simulation purposes
unsigned long currentDuration = 100;
String currentBlinkDirection = "f";
unsigned long currentBlinkInterval = 200;

String currPlanName = "";
uint8_t currPlanState = PLAN_STATE_NONE;
uint8_t currPhase = 0;
uint8_t currProgress = 0;
uint8_t currCtrlMode = CTRL_MODE_NONE;
uint8_t currIteration = 0;

int numPhases = 0;
int currBattLevel = 0;

char buffClientMsg[CLIENT_MSG_BUFF_SIZE];
boolean hasClientMessage = false;

///////////////////////////////////////////////////////////////////////////////////////////
/// FORWARD DECLARATIONS
/// //////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////
extern int FastLEDTaskId;
extern void handleColor();

////////////////////////////////////////
///////// JSON-Pasing Stuff ////////////
////////////////////////////////////////
#include "plan.h"

StaticJsonBuffer<2048> JSONBuffer;
Plan plan;

int parseJSONPlan(String json) {
  JSONBuffer.clear();
  JsonObject &p = JSONBuffer.parseObject(String(json));
  Serial.println(String(json));
  Serial.print("?success:");
  Serial.println(p.success());
  if (p.success()) {
    // endPlan();	// end the current running plan
    JsonArray &phases = p["pl"]["ph"].as<JsonArray>();
    plan.phasesCount = 0;
    plan.currentIteration = 0;
    for (auto phase : phases) {
      plan.phases[plan.phasesCount].name = phase["na"] | "";
      plan.phases[plan.phasesCount].color = phase["co"] | "n";
      plan.phases[plan.phasesCount].blinkDirection = phase["bd"] | "r";
      plan.phases[plan.phasesCount].duration = phase["du"].as<long>();
      plan.phases[plan.phasesCount].blinkInterval =
          phase["bi"] | 380 /*plan.phases[plan.phasesCount].duration*/;
      plan.phases[plan.phasesCount].phaseEnd = 0;
      plan.phases[plan.phasesCount].nextBlinkToggle = 0;
      plan.phasesCount++;
    }
    Serial.print("Duration: ");
    Serial.println(plan.phases[plan.phasesCount].duration);
    plan.iterations = p["pl"]["it"] | 1;
    plan.name = p["pl"]["na"] | "noname";

    // Serial.print("parseJSONPlan -- Color: ");
    // Serial.println(plan.phases[0].color);
    // Serial.println(plan.phases[1].color);
    // Serial.println(plan.phases[2].color);
    return 1;
  }
  Serial.println("no success parsing plan");
  return 0;
}

bool BLETask() {

  unsigned long timeCurr = millis();

  // tries to read a Json object from Serial and triggers
  // the same function as an actual client command from BLE
  updateSerial();

  // the LED stays on for 500 msec when a client message is received
  if (timeCurr - timeLastRx < TIMEOUT_LED)
    digitalWrite(PIN_LED, HIGH);
  else
    digitalWrite(PIN_LED, LOW);

  // if (deviceConnected) {

  if (hasClientMessage) {
    dimmingTimeout = millis()+BrightnessTimeout;
    timeLastRx = millis();
    // Serial.println("Processing client message");
    DynamicJsonBuffer jsonBuffer(JSON_BUFF_SIZE);
    JsonObject &jsonObj = jsonBuffer.parseObject(String(buffClientMsg));
    parseJSONPlan(String(buffClientMsg));
    /*
        Serial.println("Message received -- 8 Zeilen:");
        Serial.println(plan.name);
        Serial.println(plan.phases[plan.phasesCount].name);
        Serial.println(plan.phases[0].name);
        Serial.println(plan.phases[1].name);
        Serial.println(plan.phases[plan.phasesCount].color);
        Serial.println(plan.phases[0].color);
        Serial.println(plan.phases[1].color);
        Serial.println(String(buffClientMsg));
    */
    hasClientMessage = false;

    if (jsonObj.success()) {
      handleClientCommand(jsonObj);
    } else {
      Serial.println("ERROR parsing client command");
    }
  }

  // set the timestamp
  setTimestamp(timeCurr);

  // update the battery level
  // currBattLevel = 100 - ((timeCurr / 1000) % 100);

  // update the phase and progression
  if (currPlanState == PLAN_STATE_STARTED) {
    if (currProgress == 0) {
      currentDuration = plan.phases[currPhase - 1].duration;
      DIRECTION = getBlinkDirection(plan.phases[currPhase - 1].blinkDirection);
      DOTSPEED = plan.phases[currPhase - 1].blinkInterval;
      gHue = handleColor(plan.phases[currPhase - 1].color);
      printf("new phase started -- currentDuration: %ld DIRECTION: %ld "
             "DOTSPEED: %ld \n",
             currentDuration, DIRECTION, DOTSPEED);
    }
    if (timeCurr - timeLastProgress > (currentDuration / 100)) {
      timeLastProgress = timeCurr;
      currProgress++;

      if (currProgress >= 100) {
        skipPhase();


      }
    }
  }

  // update the state on the BLE service
  setState(currPlanState, currPhase, currProgress, currCtrlMode,
           (uint8_t)currBattLevel);

  // if (currPlanState =
  //         PLAN_STATE_ENDED && (currPhase != 0 || currProgress != 0)) {
  //   endPlan();
  //   Serial.println("ended&&(phase!=0||progress!=0)");
  // }

  return true;
}

void skipPhase() {
  BRIGHTNESS = 255;
  dimmingTimeout = millis()+BrightnessTimeout;
  currProgress = 0;
  currPhase++;
  if (currPhase > numPhases) {
    currIteration++;
    Serial.println("curr>numPhases -> currIteration++, Phase = 1");
    currPhase = 1;
    if (currIteration > plan.iterations && plan.iterations != 0) {
      endPlan();
    }
  }
}

void endPlan() {
  Serial.println("endPlan()");
  dimmingTimeout = millis()+BrightnessTimeout;
  currIteration = 0;
  currPhase = 0;
  currProgress = 0;
  currPlanState = PLAN_STATE_ENDED;
  manager.stopTask(FastLEDTaskId);
}

int getBlinkDirection(String v) {
  if (v.compareTo("b") == 0) {
    return 2;
  }
  if (v.compareTo("r") == 0) {
    return -1;
  }
  if (v.compareTo("s") == 0) {
    return 0;
  }
  // if (v.compareTo("f"))
  {
    return 1; // default always "f"
  }
}

///////////////////////////////////////////////////////////////////////////////////////////
/// CALLBACKS
/// /////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

class JacketServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer *pServer) {
    Serial.println("Connected to client");
    deviceConnected = true;
  };

  void onDisconnect(BLEServer *pServer) {
    Serial.println("Disconnected from client");
    deviceConnected = false;
  }
};

class JacketCharactCallbacks : public BLECharacteristicCallbacks {

  void onRead(BLECharacteristic *pCharacteristic) {
    Serial.print("S");
    // Serial.printf("onRead(): [%s]\n",
    // pCharacteristic->getUUID().toString().c_str());
  };

  void onWrite(BLECharacteristic *pCharacteristic) {
    Serial.printf("onWrite(): [%s]\n",
                  pCharacteristic->getUUID().toString().c_str());

    if (pCharacteristic == pJacketCharactClientCmd) {
      std::string stringValue = pCharacteristic->getValue();
      storeClientMessage(stringValue);
    } else {
      Serial.println("JacketCharactCallbacks::onWrite() ERROR: no case defined "
                     "for this characteristic");
    }
  }
};

JacketServerCallbacks jacketServerCallbacks;
JacketCharactCallbacks jacketCharactCallbacks;

///////////////////////////////////////////////////////////////////////////////////////////
/// FUNCTIONS
/// /////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

// Convenience method to handle creation of characteristics
BLECharacteristic *createCharacteristic(BLEService *pService,
                                        const char *strUUID,
                                        uint32_t properties,
                                        BLECharacteristicCallbacks *pCallbacks,
                                        BLEDescriptor *pDescriptor) {

  BLECharacteristic *pCharacteristic =
      new BLECharacteristic(BLEUUID(strUUID), properties);

  if (pDescriptor != NULL)
    pCharacteristic->addDescriptor(pDescriptor);

  if (pCallbacks != NULL)
    pCharacteristic->setCallbacks(pCallbacks);

  pService->addCharacteristic(pCharacteristic);

  return pCharacteristic;
}

void setupJacketBLE() {
  pinMode(PIN_LED, OUTPUT);
  // Create the BLE Device
  BLEDevice::init(DEVICE_NAME);

  // Create the BLE Server
  pJacketServer = BLEDevice::createServer();
  pJacketServer->setCallbacks(&jacketServerCallbacks);

  // Create the BLE Service
  pJacketService = pJacketServer->createService(UUID_SERVICE);

  // Create BLE Characteristics using
  //
  // createCharacteristic(
  //   service,
  //   UUID,
  //   property,
  //   callbacks, (can be NULL)
  //   descriptor (can be NULL)
  // );

  pJacketCharactServerMsg =
      createCharacteristic(pJacketService, UUID_CHARACT_SERVER_MSG,
                           BLECharacteristic::PROPERTY_NOTIFY,
                           &jacketCharactCallbacks, new BLE2902());

  pJacketCharactPlanName = createCharacteristic(
      pJacketService, UUID_CHARACT_PLAN_NAME, BLECharacteristic::PROPERTY_READ,
      &jacketCharactCallbacks, new BLE2902());

  pJacketCharactState = createCharacteristic(
      pJacketService, UUID_CHARACT_STATE, BLECharacteristic::PROPERTY_READ,
      &jacketCharactCallbacks, new BLE2902());

  pJacketCharactClientCmd =
      createCharacteristic(pJacketService, UUID_CHARACT_CLIENT_CMD,
                           BLECharacteristic::PROPERTY_WRITE,
                           &jacketCharactCallbacks, new BLE2902());

  pJacketCharactTimestamp = createCharacteristic(
      pJacketService, UUID_CHARACT_TIMESTAMP, BLECharacteristic::PROPERTY_READ,
      &jacketCharactCallbacks, new BLE2902());

  // Start the service
  pJacketService->start();

  // Start advertising
  pJacketServer->getAdvertising()->start();
  Serial.println("Waiting for a client connection...");
}

///////////////////////////////////////////////////////////////////////////////////////////
/// BLE SERVICE WRAPPERS
/// //////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

// send a message from the server to the client
void sendServerMsg(JsonObject &jsonObj) {
  Serial.println("sendServerMsg()");
  jsonObj.printTo(Serial);
  Serial.println();

  char msgBuffer[JSON_BUFF_SIZE];
  int msgSize = jsonObj.printTo(msgBuffer, JSON_BUFF_SIZE);
  pJacketCharactServerMsg->setValue((uint8_t *)msgBuffer, msgSize);
  pJacketCharactServerMsg->notify();

  // I fail to understand why the following doesn't work :'(
  //   std::string jsonString;
  //   jsonObj.printTo(jsonString);
}
// Defines what happens when a "client command" is received
void handleClientCommand(JsonObject &jsonObj) {
  Serial.print("Received from client: ");
  jsonObj.printTo(Serial);
  Serial.println();

  DynamicJsonBuffer jsonBuffer(JSON_BUFF_SIZE);
  JsonObject &jsonReply = jsonBuffer.createObject();

  String cmd = jsonObj[PROTOCOL_COMMAND].as<String>();
  Serial.printf("Command is <%s>\n", cmd.c_str());

  if (cmd == PROTOCOL_NEW_PLAN) {

    manager.startTask(FastLEDTaskId);

    String planName = jsonObj[DATA_PLAN][DATA_NAME].as<String>();
    currPlanName = planName;
    setPlanName(currPlanName);
    currPlanState = PLAN_STATE_READY;
    JsonArray &planPhases = jsonObj[DATA_PLAN][DATA_PHASES];
    numPhases = planPhases.size();
    currPhase = 1; //todo?
    currProgress = 0;
    Serial.printf("Starting plan <%s> with %d phases\n", planName.c_str(),
                  numPhases);

    jsonReply[PROTOCOL_ACKNOWLEDGE] = true;
    jsonReply[PROTOCOL_STATUS_CODE] = STATUS_OK;
    sendServerMsg(jsonReply);
  }
  /*
  else if (cmd == "sph") {
    String phaseName = jsonObj["ph"]["na"].as<String>();
    //Serial.printf("Starting phase <%s>\n", phaseName.c_str());
    currProgress = 0;
    jsonReply["msg"] = "ack";
    jsonReply["phn"] = phaseName;
    sendServerMsg(jsonReply);
  }
  else if (cmd == "skp") {
    if (currPlanState == PLAN_STATE_STARTED || currPlanState ==
  PLAN_STATE_PAUSED) { Serial.printf("Skipping phase, current phase is <%d>\n",
  currPhase); currProgress = 0; currPhase++; if (currPhase > numPhases) {
        currPhase = 0;
        currPlanState = PLAN_STATE_ENDED;
      }
      jsonReply["msg"] = "ack";
      jsonReply["pln"] = currPlanName;
      jsonReply["phn"] = currPhase;
      sendServerMsg(jsonReply);
    }
    else {
      Serial.println("Can't skip phase, no training ongoing");
    }
  }
  */
  else {
    Serial.println("Unsupported command");
    jsonReply[PROTOCOL_ACKNOWLEDGE] = false;
    jsonReply[PROTOCOL_STATUS_CODE] =
        STATUS_NOT_IMPLEM; // method not implemented
    sendServerMsg(jsonReply);
  }
}

// set the current timestamp, converting the uLong into a 4-byte array
void setTimestamp(unsigned long ulTimestamp) {
  // Serial.printf("setTimestamp(): %lu\n", ulTimestamp);
  uint8_t bytesTimestamp[4];
  uLongToFourBytes(ulTimestamp, bytesTimestamp);
  pJacketCharactTimestamp->setValue(bytesTimestamp, 4);
}

// set the current plan name
void setPlanName(String &strPlanName) {
  pJacketCharactPlanName->setValue((uint8_t *)strPlanName.c_str(),
                                   strPlanName.length());
}

// set the current state chracteristic, packing 5 bytes
void setState(uint8_t planState, uint8_t phase, uint8_t progress,
              uint8_t ctrlMode, uint8_t battLevel) {
  // Serial.printf("setState() (decimal): %d %d %d %d\n", progType, progState,
  // ctrlMode, battLevel);
  uint8_t byteValue[5] = {planState, phase, progress, ctrlMode, battLevel};
  pJacketCharactState->setValue(byteValue, 5);
}
