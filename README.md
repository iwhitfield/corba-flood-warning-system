# corba-flood-warning-system

This repo contains a University assignment based around the use of CORBA, in order to create an flood warning system (simulation). I thought I'd share it as a **point of reference** for future students :)

The structure of the system was defined in the application, there are 3 major components:

* RMC - a Regional Monitoring Center covers the region and receives flood reports from an LMS
* LMS - a Local Monitoring Station, receiving and aggregating reports from local sensors
* Sensor - the lowest level, a sensor which reads water levels and alerts in error cases

### Relationships

* RMC -> LMS
  - 1 -> N
* LMS -> Sensor
  - 1 -> N

### Setup & Usage

1. Ensure tnameserv is running:

        tnameserv -ORBInitialPort 1050

2. Generate code from IDL definition:

        cd src/main/java && idlj -fall flood-warning.idl && cd -

3. Generate the .jar file

        mvn clean package

4. Creating an RMC instance (only one can run at once, right now):

        java -cp target/flood-warning-1.0-SNAPSHOT.jar com.zackehh.floodz.rmc.RMCDriver -ORBInitialPort 1050

5. Creating an RMC GUI instance:

        java -cp target/flood-warning-1.0-SNAPSHOT.jar com.zackehh.floodz.rmc.RMCInterface -ORBInitialPort 1050

6. Creating an LMS instance (can use -name to launch automatically, prompted otherwise):

        java -cp target/flood-warning-1.0-SNAPSHOT.jar com.zackehh.floodz.lms.LMSClient -ORBInitialPort 1050 -name MyLMS

7. Creating an Sensor instance (can use -zone and -lms to launch automatically, prompted otherwise):

        java -cp target/flood-warning-1.0-SNAPSHOT.jar com.zackehh.floodz.sensor.SensorClient -ORBInitialPort 1050 -zone MyZone -lms MyLMS
