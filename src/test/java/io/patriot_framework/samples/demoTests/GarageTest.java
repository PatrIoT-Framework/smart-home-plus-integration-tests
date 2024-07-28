package io.patriot_framework.samples.demoTests;

import io.patriot_framework.generator.Data;

import io.patriot_framework.generator.eventSimulator.Time.ContinuousTimeSeconds;
import io.patriot_framework.generator.eventSimulator.Time.DiscreteTimeSeconds;
import io.patriot_framework.generator.eventSimulator.coordinates.cartesian.StandardCartesianCoordinate;
import io.patriot_framework.generator.eventSimulator.eventGenerator.conductor.Conductor;
import io.patriot_framework.generator.eventSimulator.eventGenerator.eventBus.EventBusClientBase;
import io.patriot_framework.generator.eventSimulator.eventGenerator.eventBus.EventBusImpl;
import io.patriot_framework.generator.eventSimulator.simulationPackages.equations.LinearMotion;
import io.patriot_framework.hub.PatriotHub;
import io.patriot_framework.hub.PropertiesNotLoadedException;
import io.patriot_framework.virtualsmarthomeplus.DTOs.DeviceDTO;
import io.patriot_framework.virtualsmarthomeplus.DTOs.DoorDTO;
import io.patriot_framework.virtualsmarthomeplus.utils.VirtualSmartHomePlusHttpClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GarageTest{
    String vshp1IP;
    VirtualSmartHomePlusHttpClient vshpClient;
    DeviceDTO doorDTO1;

    @BeforeAll
    public void setup() throws PropertiesNotLoadedException {
        vshp1IP = PatriotHub.getInstance().getApplication("smarthome1").getIPAddress();
        vshpClient = new VirtualSmartHomePlusHttpClient(vshp1IP, 8080);

        doorDTO1 = new DoorDTO();
        doorDTO1.setLabel("garageDoor");

        vshpClient.putDevice("door", doorDTO1);

    }

    @Test
    public void test() throws PropertiesNotLoadedException{

        var conductor= new Conductor(new EventBusImpl(new ContinuousTimeSeconds()));

        var car = new LinearMotion(
                new StandardCartesianCoordinate(15.0),
                new StandardCartesianCoordinate(-50.0),
                new ContinuousTimeSeconds(0.5),
                "car1"
        );


        conductor.addSimulation(car);
        conductor.addSimulation(new DoorAdapter());
        conductor.runFor(new DiscreteTimeSeconds(10));



    }

    class DoorAdapter extends EventBusClientBase {

        @Override
        public void init() {
            subscribe("LinearMotionPosition:" + "car1");
        }

        @Override
        public void receive(Data message, String topic) {
            System.out.println(message.get(StandardCartesianCoordinate.class));

        }

        @Override
        public void awake() {

        }
    }
}

