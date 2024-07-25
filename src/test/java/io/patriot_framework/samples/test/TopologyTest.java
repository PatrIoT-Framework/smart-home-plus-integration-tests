package io.patriot_framework.samples.test;

import io.patriot_framework.hub.PatriotHub;
import io.patriot_framework.hub.PropertiesNotLoadedException;
import io.patriot_framework.network.simulator.api.builder.TopologyBuilder;
import io.patriot_framework.network.simulator.api.model.Topology;
import io.patriot_framework.network.simulator.api.model.devices.application.Application;
import io.patriot_framework.network_simulator.docker.control.DockerController;
import io.patriot_framework.samples.utils.DockerDeployment;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class TopologyTest {

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


    @Test
    public void test(){
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

        Application thermometer1 = new Application("thermometer1", "Docker");
        Application thermometer2 = new Application("thermometer2", "Docker");
        Application smartHome1 = new Application("smarthome1", "Docker");
        Application smartHome2 = new Application("smarthome2", "Docker");

        smartHome1.getIPAddress();


        getHub().deployApplication(thermometer1, device1NetworkName, "patriotframework/virtual-device:latest");
        getHub().deployApplication(thermometer1, device1NetworkName, "patriotframework/virtual-device:latest");
        getHub().deployApplication(smartHome1, virtualSmartHome1NetworkName, "patriotframework/vshp-dp:latest");
        getHub().deployApplication(smartHome2, virtualSmartHome2NetworkName, "patriotframework/vshp-dp:latest");

        LOGGER.info("Sleeping for 20 to wait for deployments to finish.");
        try {
            Thread.sleep(20000);
        } catch (InterruptedException ex) {
            LOGGER.warn("Error in while sleeping", ex);
        }

        getHub().destroyHub();

    }
































//    public static Logger LOGGER = LoggerFactory.getLogger(DockerDeployment.class);
//    private static final String device1NetworkIP = "192.168.85.0";
//    private static final String device1NetworkName = "Device1Net";
//
//
//    @Test
//    public void test() throws PropertiesNotLoadedException {
//        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//
//        PatriotHub hub = PatriotHub.getInstance();
//
//        Topology top = new TopologyBuilder(2)
//                .withCreator("Docker")
//                .withRouters()
//                .withName("Internal1")
//                .withCorner(true)
//                .createRouter()
//                .addRouters()
//
//
//                .withNetwork(device1NetworkName)
//                .withIP(device1NetworkIP)
//                .withMask(24)
//                .create()
//
//                .withNetwork("BorderNetwork")
//                .withInternet(true)
//                .create()
//
//
//                .withRoutes()
//                .withSourceNetwork(device1NetworkName)
//                .withDestNetwork("BorderNetwork")
//                .withCost(1)
//                .viaRouter("Internal1")
//                .addRoute()
//                .buildRoutes()
//                .build();
//
//        DockerController c = new DockerController();
//        hub.getManager().setControllers(Arrays.asList(c));
//        hub.deployTopology(top);
//
//
//
//
//        Application thermometer1 = new Application("thermometer1", "Docker");
//        Application smartHome1 = new Application("smarthome1", "Docker");
//        Application smartHome2 = new Application("smarthome2", "Docker");
//
//
//        hub.deployApplication(thermometer1, device1NetworkName, "patriotframework/virtual-device:latest");
////        getHub().deployApplication(smartHome1, virtualSmartHome1NetworkName, "patriotframework/vshp-dp:latest");
////        getHub().deployApplication(smartHome2, virtualSmartHome2NetworkName, "patriotframework/vshp-dp:latest");
//
//        LOGGER.info("Sleeping for 20 to wait for deployments to finish.");
//        try {
//            Thread.sleep(20000);
//        } catch (InterruptedException ex) {
//            LOGGER.warn("Error in while sleeping", ex);
//        }
//
//    }

}
