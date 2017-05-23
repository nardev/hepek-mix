#include <Arduino.h>

// triColorLEDs.h
// Troy W. Weber
// Aug 18, 2012
// class to help with interfacing tri-color LEDs more easily

#ifndef TRICOLORLEDS_H
#define TRICOLORLEDS_H

// common accessible colors
#define  NONE       { 0,   0,   0   }
#define  RED        { 255, 0,   0   }
#define  YELLOW     { 255, 255, 0   }
#define  GREEN      { 0,   255, 0   }
#define  TORQUOISE  { 0,   255, 255 }
#define  BLUE       { 0,   0,   255 }
#define  PURPLE     { 255, 0,   255 }
#define  WHITE      { 255, 255, 255 }

// class method for handling LED
class triColorLED
{
  public:
    triColorLED(int pin_red, int pin_green, int pin_blue, const int color[3], double brightness);
    ~triColorLED();
    void setLED(int color[3], double brightness);
    void setBrightness(double brightness);
    void setColor(int color[3]);
    void on(void);
    void off(void);
    int* color(void);
    double brightness(void);
  private:
    int currentColor[3];
    double currentBrightness;
    int pins[3];
};

#endif
