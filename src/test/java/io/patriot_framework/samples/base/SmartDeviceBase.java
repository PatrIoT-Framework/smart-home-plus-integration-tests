package io.patriot_framework.samples.base;

import io.patriot_framework.hub.PatriotHub;
import io.patriot_framework.hub.PropertiesNotLoadedException;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartDeviceBase {
    private static Logger LOGGER = LoggerFactory.getLogger(SmartHomeBase.class);

    @BeforeAll
    public static void init() {
        LOGGER.debug("Instantiation of device test base class");
        String address = null;
        try {
            address = PatriotHub.getInstance().getApplication("smarthome1").getAddressForNetwork("SmartHomeNet1");
        } catch (PropertiesNotLoadedException e) {
            LOGGER.error("Couldn't load properties file", e);
        }
        System.out.println("address: " + address);
        RestAssured.baseURI = "http://" + address;
        RestAssured.port = 8080;
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void tearDown() {
        RestAssured.reset();
    }
}
