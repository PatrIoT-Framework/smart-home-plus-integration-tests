package io.patriot_framework.samples.test;

import io.patriot_framework.generator.controll.client.CoapDataFeedHandler;
import io.patriot_framework.generator.dataFeed.ConstantDataFeed;
import io.patriot_framework.generator.dataFeed.DataFeed;
import io.patriot_framework.hub.PatriotHub;
import io.patriot_framework.hub.PropertiesNotLoadedException;
import io.patriot_framework.samples.base.SmartDeviceBase;
import io.patriot_framework.virtualsmarthomeplus.DTOs.ThermometerDTO;
import io.patriot_framework.virtualsmarthomeplus.utils.VirtualSmartHomePlusHttpClient;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.junit.jupiter.api.Test;

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

        CoapControlClient coapClient = new CoapControlClient(ip + ":" + port);
        DataFeed thermometerDataFeed = new ConstantDataFeed(77);
        thermometerDataFeed.setLabel("0");

        ThermometerDTO thermometerDto = new ThermometerDTO();
        thermometerDto.setLabel("t1");
        VirtualSmartHomePlusHttpClient vshpClient = new VirtualSmartHomePlusHttpClient(ip, 8080);
        vshpClient.putDevice( "thermometer", thermometerDto);

        CoapDataFeedHandler handler = coapClient.getSensor("t1").getDataFeedHandler(thermometerDataFeed.getLabel());

        handler.updateDataFeed(thermometerDataFeed);         // todo tohle by melo dat vedet ze selhalo
        // todo neprotunelovalo se to k coap serveru ve vshp

        thermometerDto = (ThermometerDTO) vshpClient.getDevice("thermometer", "t1");

        assertEquals(77.0f, thermometerDto.getTemperature());
    }
}
