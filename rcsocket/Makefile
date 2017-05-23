
# Defines the RPI variable which is needed by rc-switch/RCSwitch.h
CXXFLAGS=-DRPI -std=c++11

all: send codesend RFSniffer a


mqtt2rc: rc-switch/RCSwitch.o mqtt2rc.o
	$(CXX) $(CXXFLAGS) $(LDFLAGS) $+ -o $@ -lwiringPi -lmosquitto -lyaml-cpp

clean:
	$(RM) ../rc-switch/*.o *.o send codesend servo RFSniffer

