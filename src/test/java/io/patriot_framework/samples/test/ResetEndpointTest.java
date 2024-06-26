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

import io.patriot_framework.samples.base.SmartHomeBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

public class ResetEndpointTest extends SmartHomeBase {
    private static Logger LOGGER = LoggerFactory.getLogger(ResetEndpointTest.class);

    @Test
    public void testBasicResetEndpoint() {
        given()
                .basePath("/window/open")
                .when()
                .get()
                .then()
                .statusCode(200);

        waitOneSec();

        given()
                .basePath("/window")
                .when()
                .get()
                .then()
                .statusCode(200)
                .and()
                .body("state", equalTo("Open"));

        given()
                .basePath("/fireplace/on")
                .when()
                .get()
                .then()
                .statusCode(200);

        given()
                .basePath("/reset")
                .when()
                .get()
                .then()
                .statusCode(200);

        given()
                .basePath("/all")
                .when()
                .get()
                .then()
                .statusCode(200)
                .and()
                .body("[0].label", equalTo("rear-door"))
                .body("[0].state", startsWith("Closing"))
                .body("[1].label", equalTo("fireplace"))
                .body("[1].enabled", equalTo(false))
                .body("[12].label", equalTo("air-conditioner"))
                .body("[12].enabled", equalTo(false));

        waitOneSec();

        given()
                .basePath("/window")
                .when()
                .get()
                .then()
                .statusCode(200)
                .and()
                .body("state", equalTo("Closed"));
    }

    private void waitOneSec() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.warn("Interupted sleep", ex);
        }
    }
}
