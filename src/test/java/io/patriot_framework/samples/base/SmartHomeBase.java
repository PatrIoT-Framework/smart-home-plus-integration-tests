/*
 * Copyright 2019 Patriot project
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.patriot_framework.samples.base;

import io.patriot_framework.hub.PatriotHub;
import io.patriot_framework.hub.PropertiesNotLoadedException;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SmartHomeBase {
    private static Logger LOGGER = LoggerFactory.getLogger(SmartHomeBase.class);

    @BeforeAll
//    @ExtendWith({ EnvironmentSetup.class})
    public static void init() {
        LOGGER.debug("Instantiation of test base class");
        String address = null;
        try {
            address = PatriotHub.getInstance().getApplication("smarthome").getAddressForNetwork("SmartHome");
        } catch (PropertiesNotLoadedException e) {
            LOGGER.error("Couldn't load properties file", e);
        }
        System.out.println("address: " + address);
        RestAssured.baseURI = "http://" + address;
        RestAssured.port = 8282;
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void tearDown() {
        RestAssured.reset();
    }
}
