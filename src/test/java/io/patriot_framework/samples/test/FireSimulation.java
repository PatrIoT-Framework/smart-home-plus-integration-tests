package io.patriot_framework.samples.test;

import io.patriot_framework.generator.Data;
import io.patriot_framework.generator.controll.client.CoapControlClient;
import io.patriot_framework.generator.controll.client.CoapDataFeedHandler;
import io.patriot_framework.generator.coordinates.UndirectedGraphCoordinate;
import io.patriot_framework.generator.coordinates.UndirectedGraphSpace;
import io.patriot_framework.generator.dataFeed.ConstantDataFeed;
import io.patriot_framework.generator.dataFeed.DataFeed;
import io.patriot_framework.generator.device.Device;
import io.patriot_framework.generator.device.impl.basicSensors.Default;
import io.patriot_framework.generator.device.impl.basicSensors.Thermometer;
import io.patriot_framework.generator.device.passive.sensors.Sensor;
import io.patriot_framework.generator.eventGenerator.Conductor;
import io.patriot_framework.generator.eventGenerator.DiscreteTime;
import io.patriot_framework.generator.eventGenerator.SimulationBase;
import io.patriot_framework.generator.eventGenerator.Time;
import io.patriot_framework.generator.eventGenerator.graphFire.ChildWithMatches;
import io.patriot_framework.generator.eventGenerator.graphFire.Fire;
import io.patriot_framework.generator.eventGenerator.graphFire.RoomTempDataFeed;
import io.patriot_framework.generator.eventGenerator.graphFire.TemperatureDiffuser;
import io.patriot_framework.generator.eventGenerator.simulationAdapter.SimulationAdapter;
import io.patriot_framework.generator.utils.JSONSerializer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class FireSimulation {
    @Test
    public void test() {
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



        createDevice("127.0.0.1", 8080, "thermometer", new Thermometer("t1", new ConstantDataFeed(-1.0)));
        createDevice("127.0.0.1", 8080, "thermometer", new Thermometer("t2", new ConstantDataFeed(-1.0)));

        RoomTempAdapter livingRoomAdapter;
        try {
             livingRoomAdapter = new RoomTempAdapter(
                    houseSpace.getCoordinate("livingRoom"),
                    "127.0.0.1",
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
                    "127.0.0.1",
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

            houseSpace.getAll().forEach(x -> System.out.println(x));

            List<Data> temp = livingRoomThermometer.requestData();
            System.out.println(temp.get(0));

            List<Data> temp1 = corridorThermometer.requestData();
            System.out.println(temp1.get(0));

            List<Data> temp2 = garageThermometer.requestData();
            System.out.println(temp2.get(0));
        }
    }


    private class RoomTempAdapter extends SimulationBase {
        private CoapControlClient ccc;
        private String deviceLabel;
        final private String dataFeedLabel;
        private Time time = new DiscreteTime();
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
            registerAwake(new DiscreteTime(0));
        }

        @Override
        public void awake () {
            time = eventBus.getTime();
            time.setValue(time.getValue() + 1);
            registerAwake(time);

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

    private void createDevice(String ip, int port, String deviceType, Device device) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String uri = "http://" + ip + ":" + port + "/api/v0.1/house/device/" + deviceType + "/" + device.getLabel();
            // Create the PUT request
            System.out.println("uri: " +  uri);
            HttpPut httpPut = new HttpPut(uri);
            // Convert the device object to JSON string
            String json = JSONSerializer.serialize(device);
            // Set the JSON string as the entity of the PUT request
            httpPut.setEntity(new StringEntity(json, "UTF-8"));
            httpPut.setHeader("Content-Type", "application/json");

            // Execute the request
            try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                // Handle the response if needed
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("Response status: " + statusCode);

                // Optionally handle the response body, headers, etc.
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                System.out.println("Response body: " + responseBody);
            }
        } catch (IOException e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }


}
