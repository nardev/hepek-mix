#include <signal.h>
#include <stdio.h>
#include <stdint.h>
#include <string>
#include <map>
#include <mosquitto.h>
#include <iostream>
#include "rc-switch/RCSwitch.h"
#include "yaml-cpp/yaml.h"

static int run = 1;


// std::map<std::string,int> codes;
std::map<std::string, std::map<std::string, int> > codes;

void connect_callback(struct mosquitto *mosq, void *obj, int result)
{
	//printf ("Connected. connect callback, rc=%d\n", result);
}

void message_callback(struct mosquitto *mosq, void *obj, const struct mosquitto_message  *message)
{
	// printf ("got message '%.*s' for topic '%s'\n", message->payloadlen, (char*) message->payload, message->topic);	
	std::string topic = message->topic;
	std::string code = topic.substr(topic.find("/")+1);
	// std::cout<<"code "<<code<<"\n";
	// int sendCode = codes[code][message->payloadlen];
	// std::cout<<"send code "<<sendCode<<"\n";

	int sendCode = codes[code][(char*) message->payload];

	int PIN=0;
	RCSwitch mySwitch = RCSwitch();
	mySwitch.enableTransmit(PIN);
	mySwitch.send(sendCode, 24);
}

int main(int argc, char *argv[])
{
	// YAML::Node config = YAML::LoadFile("settings.yaml");
	YAML::Node config = YAML::LoadFile("/etc/mqtt2rc/settings.yaml");

	const std::string mqtt_host = config["mqtt_host"].as<std::string>();
	const int mqtt_port = config["mqtt_port"].as<int>();
	const std::string user = config["username"].as<std::string>();
	const std::string pass = config["password"].as<std::string>();

	YAML::Node listen = config["listen"];
	const std::string topic = listen["topic"].as<std::string>();

	for (YAML::const_iterator it=listen.begin(); it!=listen.end() ; ++it ) {
		std::string key = it->first.as<std::string>();
		if (key=="topic") continue;
		for (YAML::const_iterator itc=it->second.begin();itc!=it->second.end();++itc){
			int codeOn = itc->second["true"].as<int>();
			int codeOff = itc->second["false"].as<int>();
			std::string socket = itc->first.as<std::string>();
			codes[socket+key+"socket"]["on"] = codeOn;
			codes[socket+key+"socket"]["off"] = codeOff;
		}
	}

	// for(auto elem : codes)
	// {
	//    std::cout << elem.first << "\n" << "\t" << elem.second["on"] << "\n" << "\t" << elem.second["off"] << "\n";
	// }

	wiringPiSetup();
	// RCSwitch mySwitch = RCSwitch();
	// mySwitch.enableTransmit(0);
	// mySwitch.send(267348, 24);


	uint8_t reconnect = true;
	
	struct mosquitto *mosq;
	int rc = 0;

	mosquitto_lib_init();

	
	mosq = mosquitto_new(NULL, true, 0);

	if(mosq){
		mosquitto_connect_callback_set(mosq, connect_callback);
		mosquitto_message_callback_set(mosq, message_callback);

		mosquitto_username_pw_set(mosq, user.c_str(),pass.c_str());

		rc = mosquitto_connect(mosq, mqtt_host.c_str(), mqtt_port, 60);

		mosquitto_subscribe(mosq, NULL, (topic+"/#").c_str(), 0);

		while(run){
			rc = mosquitto_loop(mosq, -1, 1);
			if(run && rc){
				printf("connection error!\n");
//				sleep(10);
				mosquitto_reconnect(mosq);
			}
		}
		mosquitto_destroy(mosq);
	}

	mosquitto_lib_cleanup();

	return rc;
}
