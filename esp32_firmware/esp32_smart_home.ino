/*
 * ===============================================================================
 * SMART HOME AUTOMATION SYSTEM — ESP32 ARDUINO FIRMWARE
 * ===============================================================================
 * Description:
 *   Complete standalone HTTP REST API server for ESP32 DevKit to control:
 *   - 3 LEDs (Living Room, Bedroom, Kitchen)
 *   - 1 Servo Motor (Smart Door Lock: 0° - 180°)
 *   - 1 DC Motor (Cooling Fan / Appliance via L298N Driver)
 *   - 1 Ultrasonic Sensor HC-SR04 (Distance in cm)
 *   - 1 PIR Motion Sensor (Security motion alert)
 *   - 2 IR Proximity Sensors (Obstacle detection)
 *   - Optional SSD1306 OLED Display (I2C 0.96")
 *
 * Board: ESP32 Dev Module / ESP32 DevKit V1
 * Baud Rate: 115200
 * Libraries Required (Install via Arduino Library Manager):
 *   - ESP32Servo by Kevin Harrington
 *   - Adafruit SSD1306 & Adafruit GFX (Optional, for OLED)
 * ===============================================================================
 */

#include <WiFi.h>
#include <WebServer.h>
#include <ESP32Servo.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

// ===============================================================================
// 1. WI-FI CREDENTIALS (Change to your Home Wi-Fi SSID and Password)
// ===============================================================================
const char* WIFI_SSID     = "YOUR_WIFI_SSID";
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";

// Fallback SoftAP mode if router connection fails:
const char* AP_SSID       = "SmartHome-ESP32";
const char* AP_PASSWORD   = "12345678";

// ===============================================================================
// 2. GPIO PIN CONFIGURATION (Safe pins for ESP32 DevKit)
// ===============================================================================
// LEDs (Output via 220 ohm resistors)
const int PIN_LED_1 = 25;
const int PIN_LED_2 = 26;
const int PIN_LED_3 = 27;

// Servo Motor (Door) - Signal wire (Orange)
const int PIN_SERVO_DOOR = 13;

// DC Motor - Connected to L298N IN1 or Relay Module
const int PIN_DC_MOTOR   = 14;

// Ultrasonic Sensor HC-SR04
const int PIN_US_TRIG    = 18;
// IMPORTANT: Echo is 5V. Use 1k/2k ohm voltage divider to step down to 3.3V!
const int PIN_US_ECHO    = 19;

// PIR Motion Sensor (Active HIGH output)
const int PIN_PIR_MOTION = 4;

// IR Sensors (Usually Active LOW on detection; configured below)
const int PIN_IR_1       = 32;
const int PIN_IR_2       = 33;
const bool IR_ACTIVE_LOW = true; // Set to false if your IR module outputs HIGH on detection

// OLED Display (SSD1306 I2C 128x64)
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);
bool oledAvailable = false;

// ===============================================================================
// 3. HARDWARE & SERVER OBJECTS
// ===============================================================================
WebServer server(80);
Servo doorServo;

// Internal State Variables
bool stateLed1 = false;
bool stateLed2 = false;
bool stateLed3 = false;
int  currentServoAngle = 0;
String doorCommandedState = "CLOSED";
bool stateDcMotor = false;

bool ir1Detected = false;
bool ir2Detected = false;
float ultrasonicDistanceCm = 0.0f;
bool ultrasonicValid = false;
bool pirMotionDetected = false;

// Non-blocking timer variables
unsigned long lastSensorReadTime = 0;
const unsigned long SENSOR_INTERVAL_MS = 250; // Read sensors every 250ms

// ===============================================================================
// 4. FUNCTION DECLARATIONS
// ===============================================================================
void setupWiFi();
void setupGPIO();
void setupOLED();
void readSensors();
void updateOLED();
void handleGetStatus();
void handlePostLed();
void handlePostServo();
void handlePostMotor();
void handleNotFound();
void setCORSHeaders();

// ===============================================================================
// 5. ARDUINO SETUP
// ===============================================================================
void setup() {
  Serial.begin(115200);
  delay(1000);
  Serial.println("\n\n=========================================");
  Serial.println("  SMART HOME AUTOMATION SYSTEM — ESP32   ");
  Serial.println("=========================================");

  setupGPIO();
  setupOLED();
  setupWiFi();

  // Attach Servo
  ESP32PWM::allocateTimer(0);
  doorServo.setPeriodHertz(50);
  doorServo.attach(PIN_SERVO_DOOR, 500, 2400);
  doorServo.write(currentServoAngle);

  // Configure REST API Endpoints
  server.on("/api/status", HTTP_GET, handleGetStatus);
  server.on("/api/led", HTTP_POST, handlePostLed);
  server.on("/api/servo", HTTP_POST, handlePostServo);
  server.on("/api/motor", HTTP_POST, handlePostMotor);
  server.onNotFound(handleNotFound);

  // Handle CORS preflight options
  server.on("/api/status", HTTP_OPTIONS, []() { setCORSHeaders(); server.send(204); });
  server.on("/api/led", HTTP_OPTIONS, []() { setCORSHeaders(); server.send(204); });
  server.on("/api/servo", HTTP_OPTIONS, []() { setCORSHeaders(); server.send(204); });
  server.on("/api/motor", HTTP_OPTIONS, []() { setCORSHeaders(); server.send(204); });

  server.begin();
  Serial.println("[HTTP] REST API Server started on port 80!");
  Serial.print("[HTTP] Accessible at: http://");
  Serial.print(WiFi.status() == WL_CONNECTED ? WiFi.localIP() : WiFi.softAPIP());
  Serial.println("/api/status");
  Serial.println("=========================================\n");

  updateOLED();
}

// ===============================================================================
// 6. MAIN LOOP (NON-BLOCKING)
// ===============================================================================
void loop() {
  server.handleClient();

  // Periodic sensor read without blocking delay()
  unsigned long currentMillis = millis();
  if (currentMillis - lastSensorReadTime >= SENSOR_INTERVAL_MS) {
    lastSensorReadTime = currentMillis;
    readSensors();
  }

  // Periodic display refresh
  static unsigned long lastDisplayTime = 0;
  if (currentMillis - lastDisplayTime >= 1000) {
    lastDisplayTime = currentMillis;
    updateOLED();
  }
}

// ===============================================================================
// 7. GPIO INITIALIZATION
// ===============================================================================
void setupGPIO() {
  pinMode(PIN_LED_1, OUTPUT);
  pinMode(PIN_LED_2, OUTPUT);
  pinMode(PIN_LED_3, OUTPUT);
  digitalWrite(PIN_LED_1, LOW);
  digitalWrite(PIN_LED_2, LOW);
  digitalWrite(PIN_LED_3, LOW);

  pinMode(PIN_DC_MOTOR, OUTPUT);
  digitalWrite(PIN_DC_MOTOR, LOW);

  pinMode(PIN_US_TRIG, OUTPUT);
  pinMode(PIN_US_ECHO, INPUT);
  digitalWrite(PIN_US_TRIG, LOW);

  pinMode(PIN_PIR_MOTION, INPUT);
  pinMode(PIN_IR_1, INPUT);
  pinMode(PIN_IR_2, INPUT);

  Serial.println("[GPIO] Pins initialized successfully.");
}

// ===============================================================================
// 8. WI-FI SETUP WITH FALLBACK AP MODE
// ===============================================================================
void setupWiFi() {
  Serial.print("[WiFi] Connecting to: ");
  Serial.println(WIFI_SSID);

  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20) {
    delay(500);
    Serial.print(".");
    attempts++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n[WiFi] Connected successfully!");
    Serial.print("[WiFi] ESP32 IP Address: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\n[WiFi] Failed to connect to router. Starting Fallback Access Point (AP)...");
    WiFi.mode(WIFI_AP);
    WiFi.softAP(AP_SSID, AP_PASSWORD);
    Serial.print("[WiFi AP] SSID: ");
    Serial.println(AP_SSID);
    Serial.print("[WiFi AP] IP Address: ");
    Serial.println(WiFi.softAPIP());
    Serial.println("[WiFi AP] Connect your phone to this network and open 192.168.4.1");
  }
}

// ===============================================================================
// 9. SENSOR READING LOGIC
// ===============================================================================
void readSensors() {
  // Read PIR Motion Sensor
  pirMotionDetected = (digitalRead(PIN_PIR_MOTION) == HIGH);

  // Read Dual IR Sensors
  int rawIr1 = digitalRead(PIN_IR_1);
  int rawIr2 = digitalRead(PIN_IR_2);
  ir1Detected = IR_ACTIVE_LOW ? (rawIr1 == LOW) : (rawIr1 == HIGH);
  ir2Detected = IR_ACTIVE_LOW ? (rawIr2 == LOW) : (rawIr2 == HIGH);

  // Read Ultrasonic HC-SR04
  digitalWrite(PIN_US_TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(PIN_US_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(PIN_US_TRIG, LOW);

  // Timeout after 25ms (covers ~4 meters)
  long durationUs = pulseIn(PIN_US_ECHO, HIGH, 25000);
  if (durationUs > 0) {
    float dist = (durationUs * 0.0343f) / 2.0f;
    if (dist >= 2.0f && dist <= 400.0f) {
      ultrasonicDistanceCm = dist;
      ultrasonicValid = true;
    } else {
      ultrasonicValid = false;
    }
  } else {
    ultrasonicValid = false;
  }
}

// ===============================================================================
// 10. REST API HANDLERS
// ===============================================================================
void setCORSHeaders() {
  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.sendHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  server.sendHeader("Access-Control-Allow-Headers", "Content-Type");
}

void handleGetStatus() {
  setCORSHeaders();

  String ipStr = (WiFi.status() == WL_CONNECTED) ? WiFi.localIP().toString() : WiFi.softAPIP().toString();

  // Construct JSON response matching Android Esp32Status data class
  String json = "{";
  json += "\"esp32_ip\":\"" + ipStr + "\",";
  json += "\"wifi_connected\":" + String(WiFi.status() == WL_CONNECTED ? "true" : "false") + ",";
  json += "\"led1\":" + String(stateLed1 ? "true" : "false") + ",";
  json += "\"led2\":" + String(stateLed2 ? "true" : "false") + ",";
  json += "\"led3\":" + String(stateLed3 ? "true" : "false") + ",";
  json += "\"servo_angle\":" + String(currentServoAngle) + ",";
  json += "\"door_commanded_state\":\"" + doorCommandedState + "\",";
  json += "\"dc_motor\":" + String(stateDcMotor ? "true" : "false") + ",";
  json += "\"ir1_detected\":" + String(ir1Detected ? "true" : "false") + ",";
  json += "\"ir2_detected\":" + String(ir2Detected ? "true" : "false") + ",";
  json += "\"ultrasonic_distance_cm\":" + String(ultrasonicDistanceCm, 1) + ",";
  json += "\"ultrasonic_valid\":" + String(ultrasonicValid ? "true" : "false") + ",";
  json += "\"pir_motion\":" + String(pirMotionDetected ? "true" : "false") + ",";
  json += "\"uptime_ms\":" + String(millis());
  json += "}";

  server.send(200, "application/json", json);
}

void handlePostLed() {
  setCORSHeaders();
  if (!server.hasArg("plain")) {
    server.send(400, "application/json", "{\"success\":false,\"error\":\"Missing request body\"}");
    return;
  }

  String body = server.arg("plain");
  Serial.print("[API POST /api/led] Received body: ");
  Serial.println(body);

  // Simple string parser for JSON: {"led":1,"state":true}
  int ledIndex = -1;
  int ledPos = body.indexOf("\"led\"");
  if (ledPos != -1) {
    int colon = body.indexOf(':', ledPos);
    if (colon != -1) {
      ledIndex = body.substring(colon + 1).toInt();
    }
  }

  bool newState = false;
  int statePos = body.indexOf("\"state\"");
  if (statePos != -1) {
    int colon = body.indexOf(':', statePos);
    if (colon != -1) {
      String stateVal = body.substring(colon + 1);
      stateVal.trim();
      newState = (stateVal.startsWith("true") || stateVal.startsWith("1"));
    }
  }

  if (ledIndex < 1 || ledIndex > 3) {
    server.send(400, "application/json", "{\"success\":false,\"error\":\"Invalid LED index (must be 1, 2, or 3)\"}");
    return;
  }

  if (ledIndex == 1) {
    stateLed1 = newState;
    digitalWrite(PIN_LED_1, stateLed1 ? HIGH : LOW);
  } else if (ledIndex == 2) {
    stateLed2 = newState;
    digitalWrite(PIN_LED_2, stateLed2 ? HIGH : LOW);
  } else if (ledIndex == 3) {
    stateLed3 = newState;
    digitalWrite(PIN_LED_3, stateLed3 ? HIGH : LOW);
  }

  server.send(200, "application/json", "{\"success\":true,\"message\":\"LED " + String(ledIndex) + " updated\"}");
}

void handlePostServo() {
  setCORSHeaders();
  if (!server.hasArg("plain")) {
    server.send(400, "application/json", "{\"success\":false,\"error\":\"Missing request body\"}");
    return;
  }

  String body = server.arg("plain");
  Serial.print("[API POST /api/servo] Received body: ");
  Serial.println(body);

  int angle = -1;
  int anglePos = body.indexOf("\"angle\"");
  if (anglePos != -1) {
    int colon = body.indexOf(':', anglePos);
    if (colon != -1) {
      angle = body.substring(colon + 1).toInt();
    }
  }

  if (angle < 0 || angle > 180) {
    server.send(400, "application/json", "{\"success\":false,\"error\":\"Angle out of range (0-180)\"}");
    return;
  }

  currentServoAngle = angle;
  doorCommandedState = (currentServoAngle >= 45) ? "OPEN" : "CLOSED";
  doorServo.write(currentServoAngle);

  server.send(200, "application/json", "{\"success\":true,\"message\":\"Servo moved to " + String(currentServoAngle) + " degrees\"}");
}

void handlePostMotor() {
  setCORSHeaders();
  if (!server.hasArg("plain")) {
    server.send(400, "application/json", "{\"success\":false,\"error\":\"Missing request body\"}");
    return;
  }

  String body = server.arg("plain");
  Serial.print("[API POST /api/motor] Received body: ");
  Serial.println(body);

  bool newState = false;
  int statePos = body.indexOf("\"state\"");
  if (statePos != -1) {
    int colon = body.indexOf(':', statePos);
    if (colon != -1) {
      String stateVal = body.substring(colon + 1);
      stateVal.trim();
      newState = (stateVal.startsWith("true") || stateVal.startsWith("1"));
    }
  }

  stateDcMotor = newState;
  digitalWrite(PIN_DC_MOTOR, stateDcMotor ? HIGH : LOW);

  server.send(200, "application/json", "{\"success\":true,\"message\":\"Motor set to " + String(stateDcMotor ? "ON" : "OFF") + "\"}");
}

void handleNotFound() {
  setCORSHeaders();
  server.send(404, "application/json", "{\"success\":false,\"error\":\"Endpoint not found\"}");
}

// ===============================================================================
// 11. OPTIONAL OLED DISPLAY SUPPORT
// ===============================================================================
void setupOLED() {
  Wire.begin(21, 22); // SDA=21, SCL=22
  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println("[OLED] SSD1306 not found at 0x3C. Continuing without display.");
    oledAvailable = false;
    return;
  }

  oledAvailable = true;
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.println("Smart Home ESP32");
  display.println("Initializing...");
  display.display();
}

void updateOLED() {
  if (!oledAvailable) return;

  display.clearDisplay();
  display.setCursor(0, 0);
  display.setTextSize(1);
  display.println("SMART HOME ESP32");

  String ipStr = (WiFi.status() == WL_CONNECTED) ? WiFi.localIP().toString() : WiFi.softAPIP().toString();
  display.print("IP: ");
  display.println(ipStr);

  display.print("L1:");
  display.print(stateLed1 ? "1 " : "0 ");
  display.print("L2:");
  display.print(stateLed2 ? "1 " : "0 ");
  display.print("L3:");
  display.println(stateLed3 ? "1" : "0");

  display.print("Door: ");
  display.print(doorCommandedState);
  display.print(" (");
  display.print(currentServoAngle);
  display.println("*)");

  display.print("Dist: ");
  if (ultrasonicValid) {
    display.print(ultrasonicDistanceCm, 1);
    display.println(" cm");
  } else {
    display.println("-- cm");
  }

  display.print("PIR: ");
  display.print(pirMotionDetected ? "MOTION! " : "Clear ");
  display.print("M:");
  display.println(stateDcMotor ? "ON" : "OFF");

  display.display();
}
