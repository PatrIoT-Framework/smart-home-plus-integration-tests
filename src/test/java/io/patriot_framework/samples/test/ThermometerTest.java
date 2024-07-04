package io.patriot_framework.samples.test;

import io.patriot_framework.generator.controll.client.CoapDataFeedHandler;
import io.patriot_framework.generator.dataFeed.ConstantDataFeed;
import io.patriot_framework.generator.dataFeed.DataFeed;
import io.patriot_framework.generator.device.Device;
import io.patriot_framework.generator.device.impl.basicSensors.Thermometer;
import io.patriot_framework.generator.utils.JSONSerializer;
import io.patriot_framework.hub.PatriotHub;
import io.patriot_framework.hub.PropertiesNotLoadedException;
import io.patriot_framework.samples.base.SmartDeviceBase;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.patriot_framework.generator.controll.client.CoapControlClient;

import java.io.IOException;

public class ThermometerTest extends SmartDeviceBase {
    @Test
    public void success() {
        assertTrue(true);
    }

    @Test
    public void checkHttpConnection() {
        given()
                .basePath("/")
                .when()
                .get()
                .then()
                .statusCode(200);
    }

    @Test
    public void checkBody() {
        given()
                .basePath("/")
                .when()
                .get()
                .then()
                .statusCode(200)
                .and()
                .body(containsString("ok"));
    }

    @Test
    public void checkThermometer() {
        given()
                .basePath("/t1")
                .when()
                .get()
                .then()
                .statusCode(200)
                .and()
                .body("data", equalTo("25.0"));
    }

    @Test
    public void checkCoAPserver() throws PropertiesNotLoadedException, ConnectorException, IOException {
        String ip = PatriotHub.getInstance().getApplication("smarthome1").getAddressForNetwork("SmartHomeNet1");
        String port = "5683";
        String pathToResource = "/";
        // Define the CoAP server URI
        String coapUrl = "coap://" + ip +":" + port + pathToResource;

        // Create CoAP client
        CoapClient client = new CoapClient(coapUrl);

        // Send GET request
        CoapResponse response = client.get();

        // Process the response
        if (response != null) {
            System.out.println("Response Code: " + response.getCode());
            System.out.println("Response Text: " + response.getResponseText());
        } else {
            System.out.println("No response received.");
        }

        // Shutdown the client
        client.shutdown();

        assertEquals(true, true);

//        CoapControlClient ccli = new CoapControlClient();
    }



    @Test
    public void coapClientTest() throws PropertiesNotLoadedException, ConnectorException, IOException {
        String ip = PatriotHub.getInstance()
                .getApplication("smarthome1")
                .getAddressForNetwork("SmartHomeNet1");
        String port = "5683";

        CoapControlClient coapClient = new CoapControlClient("127.0.0.1" + ":5683");
        DataFeed thermometerDataFeed = new ConstantDataFeed(77);
        thermometerDataFeed.setLabel("0");
        Device thermometer = new Thermometer("thermometer2", thermometerDataFeed);
        createDevice("127.0.0.1", 8080, "thermometer", thermometer);

        CoapDataFeedHandler handler = coapClient.getSensor(thermometer.getLabel()).getDataFeedHandler(thermometerDataFeed.getLabel());



        handler.updateDataFeed(thermometerDataFeed); // todo tohle by melo dat vedet ze selhalo
        // todo neprotunelovalo se to k coap serveru ve vshp
        assertTrue(false);
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
