//package io.patriot_framework.samples.test;
//
//import io.patriot_framework.generator.Data;
//import io.patriot_framework.generator.controll.client.CoapActuatorHandler;
//import io.patriot_framework.generator.controll.client.CoapControlClient;
//import io.patriot_framework.generator.eventGenerator.Conductor;
//import io.patriot_framework.generator.eventGenerator.SimulationBase;
//import io.patriot_framework.generator.eventGenerator.simulationAdapter.ActuatorAdapter;
//import io.patriot_framework.generator.eventSimulator.eventGenerator.conductor.Conductor;
//import io.patriot_framework.generator.eventSimulator.eventGenerator.eventBus.EventBusClientBase;
//import io.patriot_framework.hub.PatriotHub;
//import io.patriot_framework.hub.PropertiesNotLoadedException;
//import io.patriot_framework.virtualsmarthomeplus.DTOs.DeviceDTO;
//import io.patriot_framework.virtualsmarthomeplus.DTOs.FireplaceDTO;
//import io.patriot_framework.virtualsmarthomeplus.DTOs.ThermometerDTO;
//import io.patriot_framework.virtualsmarthomeplus.utils.VirtualSmartHomePlusHttpClient;
//import org.eclipse.californium.elements.exception.ConnectorException;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//
//public class ActuatorAndSensor {
//    String vshp1IP;
//
//    VirtualSmartHomePlusHttpClient vshpClient;
//    ThermometerDTO thermometerDTO1;
//    FireplaceDTO fireplaceDTO1;
//    CoapControlClient coapClient;
//    CoapActuatorHandler actuatorHandler;
//
//    @BeforeEach
//    public void setup() throws PropertiesNotLoadedException, ConnectorException, IOException {
//        vshp1IP = PatriotHub.getInstance().getApplication("smarthome1").getIPAddress();
//        vshpClient = new VirtualSmartHomePlusHttpClient(vshp1IP, 8080);
//
//        thermometerDTO1 = new ThermometerDTO("t1");
//        fireplaceDTO1 = new FireplaceDTO("f1");
//
//        vshpClient.putDevice("thermometer", thermometerDTO1);
//        vshpClient.putDevice("fireplace", fireplaceDTO1);
//
//        coapClient = new CoapControlClient(vshp1IP, 5683);
//        actuatorHandler = coapClient.getActuator("f1");
//    }
//
//    /**
//     * This test demonstrates, that change of the state of the actuator can be propagated to the simulation.
//     * @throws ConnectorException
//     * @throws IOException
//     */
//    @Test
//    public void test() throws ConnectorException, IOException, InterruptedException {
//        fireplaceDTO1.setOnFire();
//        vshpClient.putDevice("fireplace", fireplaceDTO1);
//        fireplaceDTO1.extinguish();
//        vshpClient.putDevice("fireplace", fireplaceDTO1);
//        System.out.println(actuatorHandler.getStateHistory());
//
//        ActuatorAdapter actuatorAdapter = new ActuatorAdapter(vshp1IP, 5683, fireplaceDTO1.getLabel());
//        ActuatorChangeLogger actuatorChangeLogger = new ActuatorChangeLogger();
//
//        Conductor conductor = new Conductor();
//        conductor.addSimulation(actuatorAdapter);
//        conductor.addSimulation(actuatorChangeLogger);
//
//        Thread conductorThread = new Thread(conductor);
//        conductorThread.start();
//
//        Thread.sleep(3000);
//        fireplaceDTO1.setOnFire();
//        vshpClient.putDevice("fireplace", fireplaceDTO1);
//        fireplaceDTO1.extinguish();
//        vshpClient.putDevice("fireplace", fireplaceDTO1);
//        Thread.sleep(3000);
//
//        conductorThread.interrupt();
//    }
//
//
//
//    private class ActuatorChangeLogger extends EventBusClientBase {
//
//        @Override
//        public void init() {
//            subscribe("actuatorUpdate");
//        }
//
//        @Override
//        public void receive(Data message, String topic) {
//
//            System.out.println("actuatorUpdate on eventBus: " + message.get(String.class));
//        }
//
//        @Override
//        public void awake() {
//
//        }
//    }
//
//
//}
