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
    private static String smartHomeNetwork = "192.168.85.0";
    private static String gatewayNetworkIP = "192.168.86.0";

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
                .withNetwork("SmartHome")
                .withIP(smartHomeNetwork)
                .withMask(24)
                .create()
                .withNetwork("GatewayNetwork")
                .withIP(gatewayNetworkIP)
                .withMask(24)
                .create()
                .withNetwork("BorderNetwork")
                .withInternet(true)
                .create()
                .withRoutes()
                .withSourceNetwork("SmartHome")
                .withDestNetwork("GatewayNetwork")
                .withCost(1)
                .viaRouter("Internal1")
                .addRoute()
                .withSourceNetwork("GatewayNetwork")
                .withDestNetwork("BorderNetwork")
                .withCost(1)
                .viaRouter("External1")
                .addRoute()
                .withSourceNetwork("SmartHome")
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
        Application smartHome = new Application("smarthome", "Docker");
        Application gateway = new Application("gateway", "Docker");

        getHub().deployApplication(smartHome, "SmartHome", "patriotframework/virtual-home");

        String ip = smartHome.getAddressForNetwork("SmartHome");
        getHub().deployApplication(gateway,
                "GatewayNetwork",
                "patriotframework/smart-gateway",
                Arrays.asList(
                        "IOT_HOST=" + ip + ":8282",
                        "IOT_WS_HOST=" + ip + ":9292"
                ));

        LOGGER.info("Sleeping for 30 to wait for deployments to finish.");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException ex) {
            LOGGER.warn("Error in while sleeping", ex);
        }
    }

    void stopDocker() {
        getHub().destroyHub();
    }
}


























//
//import io.patriot_framework.hub.PatriotHub;
//import io.patriot_framework.network.simulator.api.builder.TopologyBuilder;
//import io.patriot_framework.network.simulator.api.control.Controller;
//import io.patriot_framework.network.simulator.api.model.Topology;
//import io.patriot_framework.network.simulator.api.model.devices.application.Application;
//import io.patriot_framework.network_simulator.docker.control.DockerController;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.sql.SQLOutput;
//import java.util.Arrays;
//
//public class DockerDeployment {
//    public static Logger LOGGER = LoggerFactory.getLogger(DockerDeployment.class);
//    private static String virtualSmartHomePlusNetwork1 = "192.168.85.0";
//    private static String virtualSmartHomePlusNetwork2 = "192.168.86.0";
//
//    private PatriotHub getHub() {
//        PatriotHub hub = null;
//        try {
//            hub = PatriotHub.getInstance();
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//        return hub;
//    }
//
//    public void createTopology() {
//        System.out.println("SSSSS1");
//        Topology top = new TopologyBuilder(2)
//                .withCreator("Docker")
//                .withRouters()
//                    .withName("Internal1")
//                    .createRouter()
//                    .addRouters()
//                .withNetwork("SmartHomeNetwork1")
//                    .withIP(virtualSmartHomePlusNetwork1)
//                    .withMask(24)
//                    .create()
//                .withNetwork("SmartHomeNetwork2")
//                    .withIP(virtualSmartHomePlusNetwork2)
//                    .withMask(24)
//                    .create()
//                .withRoutes()
//                    .withSourceNetwork("SmartHomeNetwork1")
//                    .withDestNetwork("SmartHomeNetwork2")
//                    .withCost(1)
//                    .viaRouter("Internal1")
//                    .addRoute()
//                    .buildRoutes()
//                .build();
//        System.out.println("SSSSS2");
//
//        DockerController c = new DockerController();
//        getHub().getManager().setControllers(Arrays.<Controller>asList(c));
//
//        getHub().deployTopology(top);
//        System.out.println("SSSSS3");
////        Topology top = new TopologyBuilder(3)
////                .withCreator("Docker")
////                .withRouters()
////                .withName("Internal1")
////                .createRouter()
////                .withName("External1")
////                .createRouter()
////                .addRouters()
////                .withNetwork("SmartHome")
////                .withIP(smartHomeNetwork)
////                .withMask(24)
////                .create()
////                .withNetwork("GatewayNetwork")
////                .withIP(gatewayNetworkIP)
////                .withMask(24)
////                .create()
////                .withNetwork("BorderNetwork")
////                .withInternet(true)
////                .create()
////                .withRoutes()
////                .withSourceNetwork("SmartHome")
////                .withDestNetwork("GatewayNetwork")
////                .withCost(1)
////                .viaRouter("Internal1")
////                .addRoute()
////                .withSourceNetwork("GatewayNetwork")
////                .withDestNetwork("BorderNetwork")
////                .withCost(1)
////                .viaRouter("External1")
////                .addRoute()
////                .withSourceNetwork("SmartHome")
////                .withDestNetwork("BorderNetwork")
////                .withCost(4)
////                .addRoute()
////                .buildRoutes()
////                .build();
////
////
////        DockerController c = new DockerController();
////        getHub().getManager().setControllers(Arrays.asList(c));
////        getHub().deployTopology(top);
//    }
//
//    void deployApplications() {
//        System.out.println("DEPLOY APPLICATION 1");
//        Application vshp1 = new Application("vshp1", "Docker");
//        Application vshp2 = new Application("vshp2", "Docker");
//
//        System.out.println("DEPLOY APPLICATION 2");
//        getHub().deployApplication(vshp1, "SmartHomeNetwork1", "vshp-dp:01");
//        System.out.println("DEPLOY APPLICATION 3");
//        getHub().deployApplication(vshp2, "SmartHomeNetwork2", "vshp-dp:01");
//        System.out.println("DEPLOY APPLICATION 4");
////        String ip = vshp1.getAddressForNetwork("SmartHomes");
////        getHub().deployApplication(gateway,
////                "GatewayNetwork",
////                "patriotframework/smart-gateway",
////                Arrays.asList(
////                        "IOT_HOST=" + ip + ":8282",
////                        "IOT_WS_HOST=" + ip + ":9292"
////                ));
//
//        System.out.println("SSSSS");
//        String ip = vshp1.getIPAddress();
//        Integer port = vshp1.getManagementPort();
//        System.out.println(String.format("vshp address: \s, port \s", ip, port));
//
//        LOGGER.info("Sleeping for 30 to wait for deployments to finish.");
//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException ex) {
//            LOGGER.warn("Error in while sleeping", ex);
//        }
//    }
//
//    void stopDocker() {
//        getHub().destroyHub();
//    }
//}
