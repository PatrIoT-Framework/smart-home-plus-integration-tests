package io.patriot_framework.samples.test;

import io.patriot_framework.samples.base.VirtualSmartHomeBase;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class VirtualSmartHomeTest extends VirtualSmartHomeBase {
    @Test
    public void checkHttpConnection() {
        given()
                .basePath("/api/v0.1/house/")
                .when()
                .get()
                .then()
                .statusCode(200);
    }
}
