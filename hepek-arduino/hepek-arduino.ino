/*

  HEPEK TEST

*/

#include <Arduino.h>
#include <stdint.h>
#include <avr/interrupt.h>
#include <avr/io.h>
#include <avr/pgmspace.h>

#include <triColorLEDs.h>
#include "audio.h"

#include <SPI.h>
#include <Ethernet.h>
#include <PubSubClient.h>

/*
* MQTT Shit
*/


// Update these with values suitable for your network.
byte mac[]    = {  0xDE, 0xED, 0xBA, 0xFE, 0xFE, 0xED };
IPAddress ip(192, 168, 1, 33);
IPAddress server(138, 197, 16, 119);

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i=0;i<length;i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();
}

EthernetClient ethClient;
PubSubClient client(server, 1883, callback, ethClient);


/*
* SRANJE VEZANO ZA AUDIO.. FUNKCIJE I OSTALA GOVNA
*/

//int speakerPin = 11; // Can be either 3 or 11, two PWM outputs connected to Timer 2
int speakerPin = 3; // Can be either 3 or 11, two PWM outputs connected to Timer 2

#define SAMPLE_RATE 4000
volatile uint16_t sample;
byte lastSample;


void zaustaviMuziku()
{
    // Disable playback per-sample interrupt.
    TIMSK1 &= ~_BV(OCIE1A);

    // Disable the per-sample timer completely.
    TCCR1B &= ~_BV(CS10);

    // Disable the PWM timer.
    TCCR2B &= ~_BV(CS10);

    digitalWrite(speakerPin, LOW);
}

// This is called at 4000 Hz to load the next sample.
ISR(TIMER1_COMPA_vect) {
    if (sample >= sounddata_length) {
        if (sample == sounddata_length + lastSample) {
            zaustaviMuziku();
        }
        else {
            if(speakerPin==11){
                // Ramp down to zero to reduce the click at the end of playback.
                OCR2A = sounddata_length + lastSample - sample;
            } else {
                OCR2B = sounddata_length + lastSample - sample;
            }
        }
    }
    else {
        if(speakerPin==11){
            OCR2A = pgm_read_byte(&sounddata_data[sample]);
        } else {
            OCR2B = pgm_read_byte(&sounddata_data[sample]);
        }
    }

    ++sample;
}

void pustiMuziku()
{
    pinMode(speakerPin, OUTPUT);

    // Set up Timer 2 to do pulse width modulation on the speaker
    // pin.

    // Use internal clock (datasheet p.160)
    ASSR &= ~(_BV(EXCLK) | _BV(AS2));

    // Set fast PWM mode  (p.157)
    TCCR2A |= _BV(WGM21) | _BV(WGM20);
    TCCR2B &= ~_BV(WGM22);

    if(speakerPin==11){
        // Do non-inverting PWM on pin OC2A (p.155)
        // On the Arduino this is pin 11.
        TCCR2A = (TCCR2A | _BV(COM2A1)) & ~_BV(COM2A0);
        TCCR2A &= ~(_BV(COM2B1) | _BV(COM2B0));
        // No prescaler (p.158)
        TCCR2B = (TCCR2B & ~(_BV(CS12) | _BV(CS11))) | _BV(CS10);

        // Set initial pulse width to the first sample.
        OCR2A = pgm_read_byte(&sounddata_data[0]);
    } else {
        // Do non-inverting PWM on pin OC2B (p.155)
        // On the Arduino this is pin 3.
        TCCR2A = (TCCR2A | _BV(COM2B1)) & ~_BV(COM2B0);
        TCCR2A &= ~(_BV(COM2A1) | _BV(COM2A0));
        // No prescaler (p.158)
        TCCR2B = (TCCR2B & ~(_BV(CS12) | _BV(CS11))) | _BV(CS10);

        // Set initial pulse width to the first sample.
        OCR2B = pgm_read_byte(&sounddata_data[0]);
    }

    // Set up Timer 1 to send a sample every interrupt.

    cli();

    // Set CTC mode (Clear Timer on Compare Match) (p.133)
    // Have to set OCR1A *after*, otherwise it gets reset to 0!
    TCCR1B = (TCCR1B & ~_BV(WGM13)) | _BV(WGM12);
    TCCR1A = TCCR1A & ~(_BV(WGM11) | _BV(WGM10));

    // No prescaler (p.134)
    TCCR1B = (TCCR1B & ~(_BV(CS12) | _BV(CS11))) | _BV(CS10);

    // Set the compare register (OCR1A).
    // OCR1A is a 16-bit register, so we have to do this with
    // interrupts disabled to be safe.
    OCR1A = F_CPU / SAMPLE_RATE;    // 16e6 / 4000 = 2000

    // Enable interrupt when TCNT1 == OCR1A (p.136)
    TIMSK1 |= _BV(OCIE1A);

    lastSample = pgm_read_byte(&sounddata_data[sounddata_length-1]);
    sample = 0;
    sei();
}


/*
* SRANJE VEZANO ZA LED DIODE.. FUNKCIJE I OSTALA GOVNA
*/


#define RED_1   3  // PWM, check LED specs for max red amperage ( mine<50mA )
#define GREEN_1 6  // PWM, check LED specs for max green amperage ( mine<30mA )
#define BLUE_1  5  // PWM, check LED specs for max blue amperage ( mine<30mA )
#define PAUSE   1000

int colors[][3] = {WHITE,RED,YELLOW,GREEN,TORQUOISE,BLUE,PURPLE};
int color_value = 1;
double bright_value = 1;

triColorLED LED1(RED_1, GREEN_1, BLUE_1, colors[color_value], 1.0);
double inc = 1.0/16;

void cycleInt( int* value, int increment, int minValue, int maxValue )
{
  (*value) += increment;
  if ( *value > maxValue )
    *value = minValue;
  if ( *value < minValue )
    *value = maxValue;
}

void cycleDouble( double* value, double increment, double minValue, double maxValue )
{
  (*value) += increment;
  if ( *value > maxValue )
    *value = minValue;
  if ( *value < minValue )
    *value = maxValue;
}

void pustiSvjetlo()
{
      LED1.on();
      delay(PAUSE/24);
      LED1.off();
      delay(PAUSE/24);

      cycleInt(&color_value,1,0,6);
      LED1.setColor(colors[color_value]);      // cycles through the array of pre-defined colors
      cycleDouble(&bright_value,-0.1,0,1);     // cycles down from full brightness to 1/10th brightness
      LED1.setBrightness(bright_value);

}


/*
* SRANJE VEZANO ZA DUGME.. FUNKCIJE I OSTALA GOVNA
*/


boolean fixDugme = LOW;
boolean dugme = LOW;
int ledLevel = 0;

int dugmePin = 8;  // vea add - ostat ce ovako

boolean debounce(boolean last)
{
  boolean current = digitalRead(dugmePin);
  if (last != current)
  {
    delay(5);
    current = digitalRead(dugmePin);
  }
  return current;
}



void setup()
{
  pinMode(dugmePin, INPUT);
  digitalWrite(dugmePin, HIGH);
  
  Ethernet.begin(mac, ip);
  // Note - the default maximum packet size is 128 bytes. If the
  // combined length of clientId, username and password exceed this,
  // you will need to increase the value of MQTT_MAX_PACKET_SIZE in
  // PubSubClient.h
  
  if (client.connect("arduinoClient", "mosquitto", "Sarajevo!1984%")) {
    client.publish("wccontrol","pustiVodu");
    client.subscribe("wccontrol");
  }

  Serial.begin(9600);
  Serial.print("test");

}

void loop()
{
  client.loop();

//  pustiSvjetlo();

//  pustiMuziku();
//  pustiSvjetlo();
//
  dugme = debounce(fixDugme);

  if (dugme)
  {
      //Serial.println(1);
  } else {
      //Serial.println(2);
      pustiMuziku();
      
      
      
      unsigned long start = millis();
      while (millis() - start <= 3400) {
       pustiSvjetlo();
      }

 }
 fixDugme = dugme;

 //Serial.println(dugme);

}
