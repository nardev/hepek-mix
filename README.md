#Hepek Android

It's just a test app, real one under the development

note: define MQTT_USERNAME and MQTT_PASSWORD environment variables prior to building the app


#Hepek Arduino

1. Convert desired wav audio to c header file with this https://github.com/olleolleolle/wav2c
note: have in mind that you have very little space on ATmega328P microcontroller chip
2. Build and upload the scatch
3. Hook the speaker, button and RGB led or use Hepek Shield

#Hepek Arduino Shield

This was just a test of OSH Park service, works but doesn't have audio amplifier
You can order one here: https://oshpark.com/shared_projects/xxv7qtNv

#rc sockets

You need RC Sockets and RC 433MHz transmitter/receiver for this.

Also, get some libs
mosquitto cpp: http://www.kevinboone.net/mosquitto-test.html
rc-switch: https://github.com/sui77/rc-switch
yaml-cpp: https://github.com/jbeder/yaml-cpp

1. Read RC codes fromremote controller
2. Edit .yaml config  file
3. Build the app
4. Set the app to run as a service in rpi

Any issues or questons, ask me at vedran at nardev dot org
