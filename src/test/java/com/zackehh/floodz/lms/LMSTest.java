package com.zackehh.floodz.lms;

import corba.Alert;
import corba.Reading;
import corba.SensorPair;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class LMSTest {

    private final LMS lms = new LMS();

    private enum Zones {
        BRADFORD,
        HUDDERSFIELD
    }

    @BeforeMethod
    public void emptyLMSServerMapping() throws Exception {
        Iterator it = lms.getZoneMapping().entrySet().iterator();
        while(it.hasNext()){
            it.next();
            it.remove();
        }
        Assert.assertEquals(lms.getZoneMapping().size(), 0);
    }

    @Test
    public void testRegisterSensor() throws Exception {
        lms.registerSensor(Zones.HUDDERSFIELD.name());

        Assert.assertEquals(lms.getZoneMapping().size(), 1);

        ConcurrentHashMap<String, Reading> zone = lms.getZone(Zones.HUDDERSFIELD.name());

        Assert.assertNotNull(zone);
        Assert.assertEquals(zone.size(), 1);
        Assert.assertNotNull(zone.get("1"));
        Assert.assertEquals(zone.get("1").measurement, 0);
    }

    @Test
    public void testRegisterSensorWithIncrementingIds() throws Exception {
        lms.registerSensor(Zones.BRADFORD.name());
        lms.registerSensor(Zones.HUDDERSFIELD.name());

        Assert.assertEquals(lms.getZoneMapping().size(), 2);

        // TODO
        ConcurrentHashMap<String, Reading> zone = lms.getZone(Zones.HUDDERSFIELD.name());

        Assert.assertNotNull(zone);
        Assert.assertEquals(zone.size(), 1);
        Assert.assertNotNull(zone.get("1"));
        Assert.assertEquals(zone.get("1").measurement, 0);
    }

    @Test
    public void testRemoveSensorWithCorrectZoneAndSensor() throws Exception {
        lms.registerSensor(Zones.HUDDERSFIELD.name());

        Assert.assertEquals(lms.getZoneMapping().size(), 1);

        ConcurrentHashMap<String, Reading> zone = lms.getZone(Zones.HUDDERSFIELD.name());

        Assert.assertNotNull(zone);
        Assert.assertEquals(zone.size(), 1);
        Assert.assertNotNull(zone.get("1"));
        Assert.assertEquals(zone.get("1").measurement, 0);

        lms.removeSensor(new SensorPair(Zones.HUDDERSFIELD.name(), "1"));

        Assert.assertNotNull(zone);
        Assert.assertEquals(zone.size(), 0);
    }

    @Test
    public void testRemoveSensorWithInvalidSensor() throws Exception {
        lms.registerSensor(Zones.HUDDERSFIELD.name());

        Assert.assertEquals(lms.getZoneMapping().size(), 1);

        ConcurrentHashMap<String, Reading> zone = lms.getZone(Zones.HUDDERSFIELD.name());

        Assert.assertNotNull(zone);
        Assert.assertEquals(zone.size(), 1);
        Assert.assertNotNull(zone.get("1"));
        Assert.assertEquals(zone.get("1").measurement, 0);

        lms.removeSensor(new SensorPair(Zones.HUDDERSFIELD.name(), "2"));

        Assert.assertNotNull(zone);
        Assert.assertEquals(zone.size(), 1);
    }

    @Test
    public void testRemoveSensorWithInvalidZone() throws Exception {
        lms.removeSensor(new SensorPair(Zones.HUDDERSFIELD.name(), "1"));
    }

    @Test
    public void testReceiveAlertWithValidValues() throws Exception {
        lms.registerSensor(Zones.HUDDERSFIELD.name());

        ConcurrentHashMap<String, Reading> zone1 = lms.getZone(Zones.HUDDERSFIELD.name());

        Assert.assertNotNull(zone1);
        Assert.assertEquals(zone1.size(), 1);
        Assert.assertNotNull(zone1.get("1"));
        Assert.assertEquals(zone1.get("1").measurement, 0);

        lms.receiveAlert(
                new Alert(
                        new Reading(System.currentTimeMillis(), 45),
                        new SensorPair(Zones.HUDDERSFIELD.name(), "1")
                )
        );

        ConcurrentHashMap<String, Reading> zone2 = lms.getZone(Zones.HUDDERSFIELD.name());

        Assert.assertNotNull(zone2);
        Assert.assertEquals(zone2.size(), 1);
        Assert.assertNotNull(zone2.get("1"));
        Assert.assertEquals(zone2.get("1").measurement, 45);

        lms.receiveAlert(
                new Alert(
                        new Reading(System.currentTimeMillis(), 65),
                        new SensorPair(Zones.HUDDERSFIELD.name(), "1")
                )
        );

        ConcurrentHashMap<String, Reading> zone3 = lms.getZone(Zones.HUDDERSFIELD.name());

        Assert.assertNotNull(zone3);
        Assert.assertEquals(zone3.size(), 1);
        Assert.assertNotNull(zone3.get("1"));
        Assert.assertEquals(zone3.get("1").measurement, 65);
    }

    @Test
    public void testReceiveAlertWithInvalidZone() throws Exception {
        lms.receiveAlert(
                new Alert(
                        new Reading(System.currentTimeMillis(), 45),
                        new SensorPair(Zones.HUDDERSFIELD.name(), "1")
                )
        );

        ConcurrentHashMap<String, ConcurrentHashMap<String, Reading>> zoneMapping = lms.getZoneMapping();

        Assert.assertNotNull(zoneMapping);
        Assert.assertEquals(zoneMapping.size(), 0);
    }

    @Test
    public void testAlertLog() throws Exception {
        lms.registerSensor(Zones.HUDDERSFIELD.name());

        ConcurrentHashMap<String, Reading> zone1 = lms.getZone(Zones.HUDDERSFIELD.name());

        Assert.assertNotNull(zone1);
        Assert.assertEquals(zone1.size(), 1);
        Assert.assertNotNull(zone1.get("1"));
        Assert.assertEquals(zone1.get("1").measurement, 0);

        lms.receiveAlert(
                new Alert(
                        new Reading(System.currentTimeMillis(), 45),
                        new SensorPair(Zones.HUDDERSFIELD.name(), "1")
                )
        );

        Alert[] alerts = lms.alertLog();

        Assert.assertNotNull(alerts);
        Assert.assertEquals(alerts.length, 1);
    }

}
