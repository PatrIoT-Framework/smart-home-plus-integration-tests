package io.patriot_framework.samples.demoTests;

import io.patriot_framework.generator.Data;
import io.patriot_framework.generator.controll.client.CoapControlClient;
import io.patriot_framework.generator.controll.client.CoapDataFeedHandler;
import io.patriot_framework.generator.dataFeed.ConstantDataFeed;
import io.patriot_framework.generator.dataFeed.DataFeed;
import io.patriot_framework.generator.device.impl.basicSensors.Default;
import io.patriot_framework.generator.device.passive.sensors.Sensor;
import io.patriot_framework.generator.eventSimulator.Time.DiscreteTimeSeconds;
import io.patriot_framework.generator.eventSimulator.Time.Time;

import io.patriot_framework.generator.eventSimulator.coordinates.graph.UndirectedGraphCoordinate;
import io.patriot_framework.generator.eventSimulator.coordinates.graph.UndirectedGraphSpace;
import io.patriot_framework.generator.eventSimulator.eventGenerator.conductor.Conductor;
import io.patriot_framework.generator.eventSimulator.eventGenerator.eventBus.EventBusClientBase;
import io.patriot_framework.generator.eventSimulator.eventGenerator.eventBus.EventBusImpl;
import io.patriot_framework.generator.eventSimulator.eventGenerator.simulationAdapter.DataFeedMessenger;
import io.patriot_framework.generator.eventSimulator.eventGenerator.simulationAdapter.SensorAdapterBase;
import io.patriot_framework.generator.eventSimulator.eventGenerator.simulationAdapter.coapMessengers.CoapDataFeedMessenger;
import io.patriot_framework.generator.eventSimulator.simulationPackages.graphFire.ChildWithMatches;
import io.patriot_framework.generator.eventSimulator.simulationPackages.graphFire.Fire;
import io.patriot_framework.generator.eventSimulator.simulationPackages.graphFire.TemperatureDiffuser;
import io.patriot_framework.hub.PatriotHub;
import io.patriot_framework.hub.PropertiesNotLoadedException;
import io.patriot_framework.virtualsmarthomeplus.DTOs.DeviceDTO;
import io.patriot_framework.virtualsmarthomeplus.DTOs.ThermometerDTO;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import io.patriot_framework.virtualsmarthomeplus.utils.VirtualSmartHomePlusHttpClient;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FireSimulation {
    String vshp1IP;
    CoapControlClient ccc;
    VirtualSmartHomePlusHttpClient vshpClient;
    Conductor conductor;


    @BeforeAll
    public void setup() throws PropertiesNotLoadedException {
        vshp1IP = PatriotHub.getInstance().getApplication("smarthome1").getIPAddress();
        vshpClient = new VirtualSmartHomePlusHttpClient(vshp1IP, 8080);

        UndirectedGraphSpace houseSpace = new UndirectedGraphSpace.UndirectedGraphSpaceBuilder()
                .addEdge("workroom", "bedroom")
                .addEdge("workroom", "corridor")
                .addEdge("corridor", "toilet")
                .addEdge("corridor", "bathroom")
                .addEdge("corridor", "livingRoom")
                .addEdge("toilet", "bathroom")
                .addEdge("toilet", "kitchen")
                .addEdge("kitchen", "bathroom")
                .addEdge("livingRoom", "kitchen")
                .addEdge("livingRoom", "bathroom")
                .addEdge("livingRoom", "bedroom")
                .build();
        houseSpace.getAll().forEach(x -> x.setData("temperature", new Data(Integer.class, 20)));

        ccc = new CoapControlClient(vshp1IP + ":" + 5683);

        var livingRoomAdapter = new RoomTempAdapter(
                houseSpace.getCoordinate("livingRoom"),
                new CoapDataFeedMessenger(ccc.getSensor("thermometer1").getDataFeedHandler("0"))
        );
        var workroomAdapter = new RoomTempAdapter(
                houseSpace.getCoordinate("workroom"),
                new CoapDataFeedMessenger(ccc.getSensor("thermometer2").getDataFeedHandler("0"))
        );
        var corridorAdapter = new RoomTempAdapter(
                houseSpace.getCoordinate("corridor"),
                new CoapDataFeedMessenger(ccc.getSensor("thermometer3").getDataFeedHandler("0"))
        );


        TemperatureDiffuser diffuser = new TemperatureDiffuser(houseSpace);
        Fire fire = new Fire(houseSpace, 300);
        ChildWithMatches toby = new ChildWithMatches(houseSpace.getCoordinate("livingRoom"));
        ChildWithMatches sandra = new ChildWithMatches(houseSpace.getCoordinate("corridor"));

        conductor = new Conductor(new EventBusImpl(new DiscreteTimeSeconds()));
        conductor.addSimulation(diffuser);
        conductor.addSimulation(fire);
        conductor.addSimulation(toby);
        conductor.addSimulation(sandra);

        conductor.addSimulation(livingRoomAdapter);
        conductor.addSimulation(workroomAdapter);
        conductor.addSimulation(corridorAdapter);
    }

    @Test
    public void test() throws InterruptedException {


        conductor.runRealTimeUntil(new DiscreteTimeSeconds(100));

        for (int i = 0; i < 100; i++) {
            Thread.sleep(1000);
            System.out.println("Temperature in living room:" +
                    ((ThermometerDTO) vshpClient.getDevice("thermometer", "thermometer1")).getTemperature());
            System.out.println("Temperature in workroom:" +
                    ((ThermometerDTO) vshpClient.getDevice("thermometer", "thermometer2")).getTemperature());
            System.out.println("Temperature in corridor:" +
                    ((ThermometerDTO) vshpClient.getDevice("thermometer", "thermometer3")).getTemperature());
        }
    }



    private class RoomTempAdapter extends SensorAdapterBase<Integer> {

        private Time time = new DiscreteTimeSeconds();
        private Integer temperature = -2;
        private UndirectedGraphCoordinate myRoom;
        public RoomTempAdapter(UndirectedGraphCoordinate room, DataFeedMessenger messenger) {
            super(messenger);
            this.myRoom = room;
        }


        @Override
        public void init () {
            registerRecurringAwake(new DiscreteTimeSeconds(1));
        }


        @Override
        public void awake () {
            temperature = myRoom.getData("temperature").get(Integer.class);
            System.out.println(myRoom.getName() + "teplota: " + temperature);
            updateData(temperature);
        }


        @Override
        public void receive (Data message, String topic){
        }
    }
}
