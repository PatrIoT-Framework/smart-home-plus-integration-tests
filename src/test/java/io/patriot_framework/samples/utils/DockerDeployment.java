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
    private static String device1NetworkIP = "192.168.85.0";
    private static String device2NetworkIP = "192.168.86.0";

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
                .withNetwork("SmartHomeNet1")
                .withIP(device1NetworkIP)
                .withMask(24)
                .create()
                .withNetwork("SmartHomeNet2")
                .withIP(device2NetworkIP)
                .withMask(24)
                .create()
                .withNetwork("BorderNetwork")
                .withInternet(true)
                .create()
                .withRoutes()
                .withSourceNetwork("SmartHomeNet1")
                .withDestNetwork("SmartHomeNet2")
                .withCost(1)
                .viaRouter("Internal1")
                .addRoute()
                .withSourceNetwork("SmartHomeNet2")
                .withDestNetwork("BorderNetwork")
                .withCost(1)
                .viaRouter("External1")
                .addRoute()
                .withSourceNetwork("SmartHomeNet1")
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
        Application smartHome1 = new Application("smarthome1", "Docker");
        Application smartHome2 = new Application("smarthome2", "Docker");

        getHub().deployApplication(smartHome1, "SmartHomeNet1", "patriotframework/virtual-device:latest");
        getHub().deployApplication(smartHome2, "SmartHomeNet2", "patriotframework/virtual-smart-home:latest");

        String ip = smartHome1.getIPAddress();
        Integer port = smartHome1.getManagementPort();
        System.out.println("Management address: " + ip + ":" + port);

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
