package io.patriot_framework.samples.demoTests;

import io.patriot_framework.generator.Data;

import io.patriot_framework.generator.controll.client.CoapControlClient;
import io.patriot_framework.generator.controll.client.CoapDataFeedHandler;
import io.patriot_framework.generator.eventSimulator.Time.ContinuousTimeSeconds;
import io.patriot_framework.generator.eventSimulator.Time.DiscreteTimeSeconds;
import io.patriot_framework.generator.eventSimulator.Time.Time;
import io.patriot_framework.generator.eventSimulator.coordinates.cartesian.StandardCartesianCoordinate;
import io.patriot_framework.generator.eventSimulator.eventGenerator.conductor.Conductor;
import io.patriot_framework.generator.eventSimulator.eventGenerator.eventBus.EventBusClientBase;
import io.patriot_framework.generator.eventSimulator.eventGenerator.eventBus.EventBusImpl;
import io.patriot_framework.generator.eventSimulator.eventGenerator.simulationAdapter.ActuatorAdapterBase;
import io.patriot_framework.generator.eventSimulator.eventGenerator.simulationAdapter.ActuatorMessenger;
import io.patriot_framework.generator.eventSimulator.eventGenerator.simulationAdapter.DataFeedMessenger;
import io.patriot_framework.generator.eventSimulator.eventGenerator.simulationAdapter.SensorAdapterBase;
import io.patriot_framework.generator.eventSimulator.eventGenerator.simulationAdapter.coapMessengers.CoapActuatorMessenger;
import io.patriot_framework.generator.eventSimulator.eventGenerator.simulationAdapter.coapMessengers.CoapDataFeedMessenger;
import io.patriot_framework.generator.eventSimulator.simulationPackages.equations.LinearMotion;
import io.patriot_framework.hub.PatriotHub;
import io.patriot_framework.hub.PropertiesNotLoadedException;
//import io.patriot_framework.samples.utils.vshp_client.AutomaticDoorDTO;
//import io.patriot_framework.samples.utils.vshp_client.DeviceDTO;
//import io.patriot_framework.samples.utils.vshp_client.VirtualSmartHomePlusHTTPClient;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GarageTest{
    Conductor conductor;
    String vshp1IP;
//    VirtualSmartHomePlusHTTPClient vshpClient;
//    DeviceDTO doorDTO1;

    @BeforeAll
    public void setup() throws PropertiesNotLoadedException {
        vshp1IP = PatriotHub.getInstance().getApplication("smarthome1").getIPAddress();

        conductor = new Conductor(new EventBusImpl(new ContinuousTimeSeconds()));


        var door1 = new DoorSimulation("piston1", "car1", new StandardCartesianCoordinate(0.0));

        CoapControlClient ccc = new CoapControlClient(vshp1IP, 5683);
        var messenger1 = new CoapDataFeedMessenger(ccc.getSensor("proximitySensor-automaticDoor1").getDataFeedHandler("proximityDataFeed"));
        var doorSensor1 = new DoorSensorAdapter(messenger1, new StandardCartesianCoordinate(0.0), "car1");

        var messenger2 = new CoapDataFeedMessenger(ccc.getSensor("proximitySensor-automaticDoor2").getDataFeedHandler("proximityDataFeed"));
        var doorSensor2 = new DoorSensorAdapter(messenger2, new StandardCartesianCoordinate(100.0), "car1");

        var actuatorMessenger = new CoapActuatorMessenger(ccc.getActuator("doorPiston-automaticDoor1"));
        var doorPiston1 = new DoorPistonAdapter(actuatorMessenger, new ContinuousTimeSeconds(0.5), "piston1");


        conductor.addSimulation(doorSensor1);
        conductor.addSimulation(doorSensor2);
        conductor.addSimulation(doorPiston1);
        conductor.addSimulation(door1);



    }

    @Test
    public void carFast() throws PropertiesNotLoadedException, InterruptedException {

        var car = new LinearMotion(
                new StandardCartesianCoordinate(3.0),
                new StandardCartesianCoordinate(-12.0),
                new ContinuousTimeSeconds(0.5),
                "car1"
        );
        conductor.addSimulation(car);

        conductor.runRealTimeFor(new DiscreteTimeSeconds(10));

        Thread.sleep(10000);
    }

    @Test
    public void carSlow() throws PropertiesNotLoadedException, InterruptedException {

        var car = new LinearMotion(
                new StandardCartesianCoordinate(1.0),
                new StandardCartesianCoordinate(-14.0),
                new ContinuousTimeSeconds(0.5),
                "car1"
        );
        conductor.addSimulation(car);

        conductor.runRealTimeFor(new DiscreteTimeSeconds(20));

        Thread.sleep(20000);
    }





    class DoorSensorAdapter extends SensorAdapterBase<Double> {
        private StandardCartesianCoordinate position;
        private String tracee;

        public DoorSensorAdapter(DataFeedMessenger messenger, StandardCartesianCoordinate position, String tracee) {
            super(messenger);
            this.position = position;
            this.tracee = tracee;
        }
        @Override
        public void init() {
            subscribe("LinearMotionPosition:" + tracee);
        }
        @Override
        public void receive(Data message, String topic) {
            double distance = position.distance(message.get(StandardCartesianCoordinate.class));
            updateData(distance);}
        @Override
        public void awake() {
        }
    }


    class DoorPistonAdapter extends ActuatorAdapterBase {
        private String label;
        public DoorPistonAdapter(ActuatorMessenger messenger, Time pollingInterval, String label) {
            super(messenger, pollingInterval);
            this.label = label;
        }
        @Override
        protected void processUpdates() {
            if (hasChanged()) {
                if(peekLast().equals("Extended")) {
                    publish(new Data(Boolean.class, true), "doorOpen:" + label);
                }
                else {
                    publish(new Data(Boolean.class, false), "doorOpen:" + label);
                }
            }
        }
    }


    class DoorSimulation extends EventBusClientBase {
        private boolean opened = true;
        private String pistonTopic;
        private String carTopic;
        private String doorCrashTopic;
        private StandardCartesianCoordinate doorPosition;
        private boolean crash = false;
        public DoorSimulation(String doorLabel, String traceeLabel, StandardCartesianCoordinate doorPosition) {
            pistonTopic = "doorOpen:" + doorLabel;
            carTopic = "LinearMotionPosition:" + traceeLabel;
            doorCrashTopic = "linearMotionStop:" + traceeLabel;
            this.doorPosition = doorPosition;
        }

        @Override
        public void init() {
            registerRecurringAwake(new ContinuousTimeSeconds(0.50));
            subscribe(pistonTopic);
            subscribe(carTopic);
        }

        @Override
        public void receive(Data message, String topic) {
            if(topic.equals(pistonTopic)) {
                opened = message.get(Boolean.class);
                System.out.println("Door opened: " + opened);
            } else {
                if(doorPosition.distance(message.get(StandardCartesianCoordinate.class)) < 1 && !opened) {
                    crash = true;
                    System.out.println("CRASH!");
                }
            }
        }

        @Override
        public void awake() {
            if (crash) {
                publish(null, doorCrashTopic);
                unregisterRecurringAwake();
            }
        }
    }
}

