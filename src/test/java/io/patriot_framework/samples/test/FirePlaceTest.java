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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class FirePlaceTest extends SmartHomeBase {

    @Test
    public void testTheSwitchingFireplace() {
        given()
                .basePath("/fireplace/on")
                .when()
                .get()
                .then()
                .statusCode(200)
                .and()
                .body("enabled", equalTo(true))
                .body("label", equalTo("fireplace"));
    }
}
