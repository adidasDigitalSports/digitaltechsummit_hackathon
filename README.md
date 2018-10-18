# ServiceFactory Smart Jacket Code
#### Arduino program using BLE to communicate with Smartphone App
## Libraries to include:
#### Libraries that need to be copied to "/USER/Documents/Arduino/libraries/"

- **<ArduinoJson.h>** - [https://github.com/bblanchon/ArduinoJson](https://github.com/bblanchon/ArduinoJson) v.5.13.1 (don't use newer version!!!)
- **<FastLED.h>** - IMPORTANT: As of May 2018, the FastLED v.3.1.6 doesn't work with the ESP32, therefore use [https://github.com/samguyer/FastLED](https://github.com/samguyer/FastLED) to compile. v.3.1.6
- **<OneButton.h>** - [https://github.com/mathertel/OneButton](https://github.com/mathertel/OneButton)
 v.1.2.0
- **<OneWire.h>** - [https://github.com/PaulStoffregen/OneWire](https://github.com/PaulStoffregen/OneWire)
 v.2.3.4

#### Libraries that are included within ESP32 Framework - have to exist in "USER/Documents/Arduino/hardware/espressif/esp32/libraries/BLE/" v.0.4.12

- **<BLE2902.h>**
- **<BLEDevice.h>**
- **<BLEServer.h>**
- **<BLEUUID.h>**
- **<BLEUtils.h>**

#### Local header files (contained within the code)

- **"DS2413helper.h"** - local helper file for Touch Inputs from OneWire Chip DS2413  
- **"Ringbuffer.h"** - local helper file for comparing touch inputs ([https://github.com/ankraft/arduino-tools](https://github.com/ankraft/arduino-tools))  
- **"TaskManager.h"** - local helper file for running independently timed tasks ([https://github.com/ankraft/arduino-tools](https://github.com/ankraft/arduino-tools))

# Hardware:
#### Main Controller = Sparkfun ESP32 Thing (Arduino-compatible µC with WiFi/BLE and Battery charging circuit)

- **Pin 19** - Button Input towards GND (Flag button)
- **Pin 22** - WS2811-compatible Neopixel-LEDs (#0-#4 = Intensity, #5-#9 = Progress)
- **Pin 27** - Toggle Power Switch towards GND (Connected to GND = ON, open = OFF)
- **Pin 33** - OneWire Pin connected to DS2413, pulled up 4k7Ω
- **Pin 34** - Battery sensing circuit - Rpup = 1MΩ, Rpdn = 300kΩ Voltage Divider, Rpin = 20kΩ as Input Protection (Pin35 - USB Voltage measure - to be ignored)

#### Touch circuit:

- Communication via One-Wire Protocol - Data Channel is pulled up with a 4k7Ω Resistor
- Protocol Controller Chip: MAXIM DS2413, 2 Port OneWire GPIO Extender
- Touch Chips: Atmel AT42QT1011, QTouch Technology Capacitive Sensing
- Eagle Files for Schematic and PCB are included in [doc/eagle](doc/eagle)

# Inputs:

- Flag Switch:
  - Short Press: Pause/Resume Training, Start Training after Loading from App
  - Long Press: Abort running Training, Restart Training if loaded by App
  - Double Click: Skip running Phase
- Power Switch: (if there is no state change, Power will be ON by default)
  - Closed = Power ON
  - Open   = Power OFF

#### Touch Inputs:
Touch Input can be done on two touch panels, sequentially leading to the following input gestures:

- Short Press Panel A - Vol Down
- Short Press Panel B - Vol Up
- Slide A->B - Volume Up
- Slide B->A - Volume Down        
- Short Press A & B - Play/Pause
- Holding both Panels for >3 seconds - Emergency Call Trigger
- Input Sequences have to be completed between 200 ms and 1,2 sec


#### Config Parameters:

The following settings can be configured in [main.ino](main/main.ino):

- ```int DOTFADE = 64;```  
   0-255 - fading time until running LEDs will turn off. (255 = no fade, 64 = med fade, 1 = very slow fade, 0 = all LEDS stay on)
- ```#define FRAMES_PER_SECOND 43```  
   LED refresh FPS
- ```unsigned long BrightnessTimeout = 4000;```  
  Delay after the LEDs will fade out
- ```int minimalBrightness = 50;```  
  Brightness Percentage to set to after fading out
- ```int MinimumTouchTime = 200;``` 
  Minimum Touch Time to be sensed
- ```int MaximumTouchTime = 1000;```  
  Maximum Touch Time to be sensed
- ```int ICETouchTime = 3000;```
  Delay after which the emergency call will be sent after holding both touch planes
- ```int debounceTouch = 2;```
  values to be removed from start and end of ringbuffer

And in [BLE.ino](main/BLE.ino):

- ```#define DEVICE_NAME "Smarte Jacke"```  
  Bluetooth Broadcasting Name of the Device


#### Compiling / Building:
- install the Arduino IDE (e.g. for Windows [https://www.arduino.cc/download_handler.php?f=/arduino-1.8.5-windows.exe](https://www.arduino.cc/download_handler.php?f=/arduino-1.8.5-windows.exe))
- download all the libraries
- Start Arduino and open Preferences window.
- Enter *https://dl.espressif.com/dl/package_esp32_index.json* into *Additional Board Manager URLs* field. You can add multiple URLs, separating them with commas.
- Open Boards Manager from *Tools > Board* menu and install esp32 platform
  (and don't forget to select your ESP32 board from *Tools > Board* menu after installation).

Alternative, if the above method didn't work for you:

- install the ESP32 Core ([https://learn.sparkfun.com/tutorials/esp32-thing-hookup-guide/installing-the-esp32-arduino-core](https://learn.sparkfun.com/tutorials/esp32-thing-hookup-guide/installing-the-esp32-arduino-core))
- compile and upload using the Arduino IDE
- if necessary, install the FTDI Drivers: [https://learn.sparkfun.com/tutorials/how-to-install-ftdi-drivers/all](https://learn.sparkfun.com/tutorials/how-to-install-ftdi-drivers/all)
