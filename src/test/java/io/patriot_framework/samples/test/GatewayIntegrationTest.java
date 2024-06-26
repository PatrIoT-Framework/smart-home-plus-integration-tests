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

package io.patriot_framework.samples.test;

import io.patriot_framework.hub.PatriotHub;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

public class GatewayIntegrationTest {

    private static RequestSpecification getRequest(String appName, String networkName, int port) {
        try {
            return given()
                    .baseUri("http://" + PatriotHub.getInstance().getApplication(appName).getAddressForNetwork(networkName))
                    .port(port);
        } catch (Exception ex) {
            return null;
        }
    }

    @Test
    public void doorOpenTest() {
        RestAssured.defaultParser = Parser.JSON;
        RequestSpecification home = getRequest("smarthome", "SmartHome", 8282);
        RequestSpecification gateway = getRequest("gateway", "GatewayNetwork", 8283);

        gateway.basePath("/mobile").param("button", "7").when().get().then().statusCode(200);
        home.basePath("/door")
                .when()
                .get()
                .then()
                .statusCode(200)
                .and()
                .body("label", equalTo("front-door"))
                .body("state", startsWith("Opening"));
    }
}
