#include <SoftwareSerial.h>     // Serial RX,TX use library
#include <Adafruit_NeoPixel.h>  // NeoPixel strap library
#include <MQUnifiedsensor.h>    // MQ-135 sensor library
#include <DHT11.h>              // DHT11 library

// MQ-135 setup
#define placa "Arduino UNO"
#define Voltage_Resolution 5
#define pin A0
#define type "MQ-135"
#define ADC_Bit_Resolution 10
#define RatioMQ135CleanAir 3.6

// Bluetooth setup
#define BT_TX 3   // HC-06 TX -> Arduino D4
#define BT_RX 4   // HC-06 RX -> Arduino D3

// DHT11 and pulse sensor pin setup
DHT11 dht11(7);
int pulsePin = A2;  // Pulse sensor pin

// WS2812 LED setup
#define LED_PIN 6          // Pin connected to the LED strip
#define NUM_LEDS 15        // Number of LEDs in the strip
#define RELAY_PIN 8
Adafruit_NeoPixel strip = Adafruit_NeoPixel(NUM_LEDS, LED_PIN, NEO_GRB + NEO_KHZ800);

//Air conditioner setup
const int TEMPERATURE_THRESHOLD = 27;

//Serial RX,TX setup
SoftwareSerial bluetooth(BT_RX, BT_TX);

// Sensor objects
MQUnifiedsensor MQ135(placa, Voltage_Resolution, ADC_Bit_Resolution, pin, type);
int pulseRate = 0;  // Pulse rate data

// Variables to track pulse rate repetitions
int lastPulseRate = 0;
int repetitionCount = 0;

// Initialize sensors and LED strip
void initializeSensors() {
    Serial.begin(9600);            // Serial monitor
    bluetooth.begin(9600);         // Bluetooth communication speed
    MQ135.init();                  // Initialize MQ-135
    pinMode(pulsePin, INPUT);      // Set pulse sensor pin
    pinMode(RELAY_PIN, OUTPUT);    // Set relay pin as output
    digitalWrite(RELAY_PIN, LOW);  // Ensure the relay is off (LED is off) initially

    // Initialize the LED strip
    strip.begin();
    strip.show();  // Turn off all LEDs initially

    Serial.println("Waiting for sensor data...");
}

// Calibrate the MQ-135 sensor
void calibrateMQ135() {
    Serial.print("Calibrating, please wait.");
    float calcR0 = 0;
    for (int i = 1; i <= 10; i++) {
        MQ135.update();
        calcR0 += MQ135.calibrate(RatioMQ135CleanAir);
        Serial.print(".");
    }
    MQ135.setR0(calcR0 / 10);
    Serial.println("  done!");

    if (isinf(calcR0) || calcR0 == 0) {
        Serial.println("Sensor calibration failed. Check wiring.");
        while (1);
    }
    Serial.println("** MQ-135 Sensor Data **");
}

// Read concentration for a specific gas
float readConcentration(float a, float b) {
    MQ135.setA(a);
    MQ135.setB(b);
    return MQ135.readSensor();
}

// Read temperature and humidity from DHT11
void readTemperatureAndHumidity(int &temperature, int &humidity) {
    dht11.readTemperatureHumidity(temperature, humidity);
}

// Read pulse rate and check for repetition
int readPulseRate() {
    int pulseValue = digitalRead(pulsePin);
    int currentPulseRate = pulseRate;

    if (pulseValue == HIGH) {
        delay(10);  
        currentPulseRate = random(59, 120);  // virtual pulse rate data
    }

    // // Check if the current pulse rate is the same as the last
    // if (currentPulseRate == lastPulseRate) {
    //     repetitionCount++;
    // } else {
    //     repetitionCount = 0;  // Reset count if it's different
    // }

    // lastPulseRate = currentPulseRate;

    // // If the pulse rate is the same for 3 readings, show RGB values
    // if (repetitionCount >= 3) {
    //     // Set LED color based on heart rate
    //     if (currentPulseRate >= 110) {
    //         setLEDBrightness(228, 245, 255);  //9000K
    //         Serial.println("LED: 9000K");
    //     // Set LED color based on heart rate
    //     } else if (currentPulseRate >= 100) {
    //         setLEDBrightness(242, 252, 255);  //8000K
    //         Serial.println("LED: 8000K");
    //     } else if (currentPulseRate >= 90) {
    //         setLEDBrightness(255, 231, 179);  //7000K
    //         Serial.println("LED: 7000K");
    //     }else if (currentPulseRate >= 80) {
    //         setLEDBrightness(255, 219, 153);  //6000K
    //         Serial.println("LED: 6000K");
    //     }else if (currentPulseRate >= 70) {
    //         setLEDBrightness(255, 204, 128);  //5000K
    //         Serial.println("LED: 5000K");
    //     }else if (currentPulseRate >= 60) {  
    //         setLEDBrightness(255, 178, 102);  //4000K
    //         Serial.println("LED: 4000K");
    //     }
    // }

    return currentPulseRate;
}

// Set LED brightness and color
void setLEDBrightness(int r, int g, int b) {
    for (int i = 0; i < NUM_LEDS; i++) {
        strip.setPixelColor(i, strip.Color(r, g, b));  // Set each LED to the received color
    }
    strip.show();  // Update the LED strip
}

// Receive RGB data from Bluetooth and set LED colors
void receiveAndSetLED() {
    if (bluetooth.available()) {
        String rgbData = bluetooth.readStringUntil('\n');  // Read data until newline

        Serial.print("Received RGB Data: ");
        Serial.println(rgbData);

        int commaIndex1 = rgbData.indexOf(',');
        int commaIndex2 = rgbData.lastIndexOf(',');

        if (commaIndex1 > 0 && commaIndex2 > commaIndex1) {
            int r = rgbData.substring(0, commaIndex1).toInt();
            int g = rgbData.substring(commaIndex1 + 1, commaIndex2).toInt();
            int b = rgbData.substring(commaIndex2 + 1).toInt();

            Serial.print("Parsed R: ");
            Serial.println(r);
            Serial.print("Parsed G: ");
            Serial.println(g);
            Serial.print("Parsed B: ");
            Serial.println(b);

            setLEDBrightness(r, g, b);  // Set the LED color
        } else {
            Serial.println("Error: Invalid RGB data format.");
        }
    }
}

// Print and send data via Bluetooth
void printAndSendData(float CO, float Alcohol, float CO2, float Tolueno,
                      float NH4, float Acetona, int temperature,
                      int humidity, int pulse) {
    // Concatenate data with '|' as separator
    String data = String(pulse) + "|" +
                  String(CO) + "|" +
                  String(Alcohol) + "|" +
                  String(CO2) + "|" +
                  String(Tolueno) + "|" +
                  String(NH4) + "|" +
                  String(Acetona) + "|" +
                  String(temperature) + "|" +
                  String(humidity);

    Serial.println(data);  // Print to serial monitor
    bluetooth.println(data);  // Send via Bluetooth
}

void setup() {
    initializeSensors();
    calibrateMQ135();
}

void loop() {
    int temperature = 0;
    int humidity = 0;

    readTemperatureAndHumidity(temperature, humidity);

  if (temperature > TEMPERATURE_THRESHOLD) {
      digitalWrite(RELAY_PIN, HIGH);  // Turn on the relay (LED is ON)
      Serial.println("Temperature exceeded 27°C. Relay ON (LED ON).");
  } else {
      digitalWrite(RELAY_PIN, LOW);   // Turn off the relay (LED is OFF)
      Serial.println("Temperature below 27°C. Relay OFF (LED OFF).");
  }

    pulseRate = readPulseRate();  // Measure pulse rate
    MQ135.update();               // Update MQ-135 data

    float CO = readConcentration(605.18, -3.937);
    float Alcohol = readConcentration(77.255, -3.18);
    float CO2 = readConcentration(110.47, -2.862);
    float Tolueno = readConcentration(44.947, -3.445);
    float NH4 = readConcentration(102.2, -2.473);
    float Acetona = readConcentration(34.668, -3.369);

    printAndSendData(CO, Alcohol, CO2, Tolueno, NH4, Acetona,
                     temperature, humidity, pulseRate);
    receiveAndSetLED();  // Check for incoming RGB data and update LEDs

    delay(1000);  // Data refresh rate
}
