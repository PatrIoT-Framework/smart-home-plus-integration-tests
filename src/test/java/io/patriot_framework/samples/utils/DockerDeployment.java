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

package io.patriot_framework.samples.utils;


import io.patriot_framework.hub.PatriotHub;
import io.patriot_framework.network.simulator.api.builder.TopologyBuilder;
import io.patriot_framework.network.simulator.api.model.Topology;
import io.patriot_framework.network.simulator.api.model.devices.application.Application;
import io.patriot_framework.network_simulator.docker.control.DockerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class DockerDeployment {
    public static Logger LOGGER = LoggerFactory.getLogger(DockerDeployment.class);
    private static final String device1NetworkIP = "192.168.85.0";
    private static final String device1NetworkName = "Device1Net";

    private static final String virtualSmartHome1NetworkIP = "192.168.86.0";
    private static final String virtualSmartHome1NetworkName = "SmartHomeNet1";

    private static final String virtualSmartHome2NetworkIP = "192.168.87.0";
    private static final String virtualSmartHome2NetworkName = "SmartHomeNet2";

    private PatriotHub getHub() {
        PatriotHub hub = null;
        try {
            hub = PatriotHub.getInstance();
        } catch (Exception e) {
            System.out.println(e);
        }
        return hub;
    }

    public void createTopology() {
        Topology top = new TopologyBuilder(3)
                .withCreator("Docker")
                .withRouters()
                .withName("Internal1")
                .createRouter()
                .withName("External1")
                .createRouter()
                .addRouters()
                    .withNetwork(device1NetworkName)
                    .withIP(device1NetworkIP)
                    .withMask(24)
                    .create()
                    .withNetwork(virtualSmartHome1NetworkName)
                    .withIP(virtualSmartHome1NetworkIP)
                    .withMask(24)
                .create()
                    .withNetwork(virtualSmartHome2NetworkName)
                    .withIP(virtualSmartHome2NetworkIP)
                    .withMask(24)
                    .create()
                    .withNetwork("BorderNetwork")
                    .withInternet(true)
                    .create()
                .withRoutes()
                    .withSourceNetwork(device1NetworkName)
                    .withDestNetwork(virtualSmartHome1NetworkName)
                    .withCost(1)
                    .viaRouter("Internal1")
                .addRoute()
                    .withSourceNetwork(device1NetworkName)
                    .withDestNetwork(virtualSmartHome2NetworkName)
                    .withCost(1)
                    .viaRouter("Internal1")
                .addRoute()
                    .withSourceNetwork(virtualSmartHome1NetworkName)
                    .withDestNetwork(virtualSmartHome2NetworkName)
                    .withCost(1)
                    .viaRouter("Internal1")
                .addRoute()
                    .withSourceNetwork(virtualSmartHome1NetworkName)
                    .withDestNetwork("BorderNetwork")
                    .withCost(1)
                    .viaRouter("External1")
                .addRoute()
                    .withSourceNetwork(virtualSmartHome2NetworkName)
                    .withDestNetwork("BorderNetwork")
                    .withCost(1)
                .addRoute()
                    .withSourceNetwork(device1NetworkName)
                    .withDestNetwork("BorderNetwork")
                    .withCost(4)
                .addRoute()
                .buildRoutes()
                .build();

        DockerController c = new DockerController();
        getHub().getManager().setControllers(Arrays.asList(c));
        getHub().deployTopology(top);
    }

    void deployApplications() {
        Application thermometer1 = new Application("thermometer1", "Docker");
        Application smartHome1 = new Application("smarthome1", "Docker");
        Application smartHome2 = new Application("smarthome2", "Docker");

        smartHome1.getIPAddress();

        getHub().deployApplication(thermometer1, device1NetworkName, "patriotframework/virtual-device:latest");
        getHub().deployApplication(smartHome1, virtualSmartHome1NetworkName, "patriotframework/vshp-dp:latest");
        getHub().deployApplication(smartHome2, virtualSmartHome2NetworkName, "patriotframework/vshp-dp:latest");

        LOGGER.info("Sleeping for 20 to wait for deployments to finish.");
        try {
            Thread.sleep(20000);
        } catch (InterruptedException ex) {
            LOGGER.warn("Error in while sleeping", ex);
        }
    }

    void stopDocker() {
        getHub().destroyHub();
    }
}
