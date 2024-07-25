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
import io.patriot_framework.generator.eventSimulator.coordinates.UndirectedGraphCoordinate;
import io.patriot_framework.generator.eventSimulator.coordinates.UndirectedGraphSpace;
import io.patriot_framework.generator.eventSimulator.eventGenerator.conductor.Conductor;
import io.patriot_framework.generator.eventSimulator.eventGenerator.eventBus.EventBusClientBase;
import io.patriot_framework.generator.eventSimulator.simulationPackages.graphFire.ChildWithMatches;
import io.patriot_framework.generator.eventSimulator.simulationPackages.graphFire.Fire;
import io.patriot_framework.generator.eventSimulator.simulationPackages.graphFire.RoomTempDataFeed;
import io.patriot_framework.generator.eventSimulator.simulationPackages.graphFire.TemperatureDiffuser;
import io.patriot_framework.hub.PatriotHub;
import io.patriot_framework.hub.PropertiesNotLoadedException;
import io.patriot_framework.virtualsmarthomeplus.DTOs.DeviceDTO;
import io.patriot_framework.virtualsmarthomeplus.DTOs.ThermometerDTO;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import io.patriot_framework.virtualsmarthomeplus.utils.VirtualSmartHomePlusHttpClient;

import java.io.IOException;

public class FireSimulation {
    String vshp1IP;
    String vshp2IP;

    VirtualSmartHomePlusHttpClient vshpClient;
    DeviceDTO thermometerDTO1;
    DeviceDTO thermometerDTO2;



    @BeforeAll
    public void setup() throws PropertiesNotLoadedException {
        vshp1IP = PatriotHub.getInstance().getApplication("smarthome1").getIPAddress();
        vshp2IP = PatriotHub.getInstance().getApplication("smarthome2").getIPAddress();
        vshpClient = new VirtualSmartHomePlusHttpClient(vshp1IP, 8080);

        thermometerDTO1= new ThermometerDTO();
        thermometerDTO1.setLabel("t1");
        thermometerDTO2 = new ThermometerDTO();
        thermometerDTO2.setLabel("t2");

        vshpClient.putDevice("thermometer", thermometerDTO1);
        vshpClient.putDevice("thermometer", thermometerDTO2);
//        createDevice( "thermometer", new Thermometer("t1", new ConstantDataFeed(-1.0)));
//        createDevice(vshp2IP, 8080, "thermometer", new Thermometer("t2", new ConstantDataFeed(-1.0)));
    }

    @Test
    public void test() throws PropertiesNotLoadedException {
        UndirectedGraphSpace houseSpace = new UndirectedGraphSpace.UndirectedGraphSpaceBuilder()
                .addEdge("garage", "entrance")
                .addEdge("garage", "corridor")
                .addEdge("garage", "livingRoom")
                .addEdge("entrance", "corridor")
                .addEdge("corridor", "livingRoom")
                .addEdge("corridor", "workroom")
                .addEdge("livingRoom", "bedroom")
                .addEdge("workroom", "bedroom")
                .build();
        houseSpace.getAll().forEach(x -> x.setData("temperature", new Data(Integer.class, 20)));


        RoomTempAdapter livingRoomAdapter;
        try {
             livingRoomAdapter = new RoomTempAdapter(
                    houseSpace.getCoordinate("livingRoom"),
                    vshp1IP,
                    5683,
                    "t1",
                    "0"
                    );
        } catch (ConnectorException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        RoomTempAdapter garageAdapter;
        try {
            garageAdapter = new RoomTempAdapter(
                    houseSpace.getCoordinate("garage"),
                    vshp2IP,
                    5683,
                    "t2",
                    "0"
            );
        } catch (ConnectorException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        RoomTempDataFeed livingRoomDF = new RoomTempDataFeed(houseSpace.getCoordinate("livingRoom"));
        RoomTempDataFeed garageDF = new RoomTempDataFeed(houseSpace.getCoordinate("garage"));
        RoomTempDataFeed corridorDF = new RoomTempDataFeed(houseSpace.getCoordinate("corridor"));

        Sensor livingRoomThermometer = new Default("livingRoomThermometer", livingRoomDF);
        Sensor garageThermometer = new Default("garageThermometer", garageDF);
        Sensor corridorThermometer = new Default("corridorThermometer", corridorDF);

        TemperatureDiffuser diffuser = new TemperatureDiffuser(houseSpace);
        Fire fire = new Fire(houseSpace, 300);
        ChildWithMatches toby = new ChildWithMatches(houseSpace.getCoordinate("livingRoom"));
        ChildWithMatches sandra = new ChildWithMatches(houseSpace.getCoordinate("garage"));

        Conductor conductor = new Conductor();
        conductor.addSimulation(diffuser);
        conductor.addSimulation(fire);
        conductor.addSimulation(toby);
        conductor.addSimulation(sandra);

        conductor.addSimulation(livingRoomAdapter);
        conductor.addSimulation(garageAdapter);

        conductor.addSimulation(livingRoomDF);
        conductor.addSimulation(garageDF);
        conductor.addSimulation(corridorDF);
        // todo zobecnit fire na cellular automat s nastavitelnou sirkou okoli?

        Thread conductorThread = new Thread(conductor);
        conductorThread.start();

        for(int i = 0; i < 120; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            System.out.println("Time: " + i);
            System.out.println();



//            houseSpace.getAll().forEach(x -> System.out.println(x));
//
//            List<Data> temp = livingRoomThermometer.requestData();
//            System.out.println(temp.get(0));
//
//            List<Data> temp1 = corridorThermometer.requestData();
//            System.out.println(temp1.get(0));
//
//            List<Data> temp2 = garageThermometer.requestData();
//            System.out.println(temp2.get(0));
        }
    }


    private class RoomTempAdapter extends EventBusClientBase {
        private CoapControlClient ccc;
        private String deviceLabel;
        final private String dataFeedLabel;
        private Time time = new DiscreteTimeSeconds();
        private CoapDataFeedHandler dataFeedHandler;
        private Integer temperature = -2;
        private UndirectedGraphCoordinate myRoom;


    public RoomTempAdapter(UndirectedGraphCoordinate room, String ip, int port, String deviceLabel, String dataFeedLabel) throws ConnectorException, IOException {
            this.myRoom = room;
            this.ccc = new CoapControlClient(ip + ":" + port);
            this.deviceLabel = deviceLabel;
            this.dataFeedLabel = dataFeedLabel;

            dataFeedHandler = ccc.getSensor(deviceLabel).getDataFeedHandler(dataFeedLabel);
        }

        @Override
        public void init () {
            registerRecurringAwake(new DiscreteTimeSeconds(1));
        }

        @Override
        public void awake () {
            temperature = myRoom.getData("temperature").get(Integer.class);
            DataFeed newDataFeed = new ConstantDataFeed(temperature);
            newDataFeed.setLabel(dataFeedLabel);
            try {
                dataFeedHandler.updateDataFeed(newDataFeed);
            } catch (ConnectorException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void receive (Data message, String topic){
        }
    }
}
