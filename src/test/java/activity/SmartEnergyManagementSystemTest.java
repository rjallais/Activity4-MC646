package activity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

class SmartEnergyManagementSystemTest {
    private SmartEnergyManagementSystem energySystem;

    @BeforeEach
    void setUp() {
        energySystem = new SmartEnergyManagementSystem();
    }

    @Test
    void testEnergySavingModeActivates() {
        double currentPrice = 0.25;
        double priceThreshold = 0.20;
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Heating", 1);
        devicePriorities.put("Cooling", 1);
        devicePriorities.put("Lights", 2);
        devicePriorities.put("Appliances", 3);

        LocalDateTime currentTime = LocalDateTime.of(2024, 10, 16, 10, 0);
        double currentTemperature = 18.0;  // Below desired range to trigger heating
        double[] desiredTemperatureRange = {20.0, 24.0};
        double energyUsageLimit = 30.0;
        double totalEnergyUsedToday = 15.0;

        SmartEnergyManagementSystem.EnergyManagementResult result = energySystem.manageEnergy(
                currentPrice, priceThreshold, devicePriorities, currentTime, currentTemperature,
                desiredTemperatureRange, energyUsageLimit, totalEnergyUsedToday, List.of()
        );

        Assertions.assertTrue(result.energySavingMode);
        Assertions.assertFalse(result.deviceStatus.get("Lights"));
        Assertions.assertFalse(result.deviceStatus.get("Appliances"));
        Assertions.assertTrue(result.deviceStatus.get("Heating"));  // Heating should be ON because temperature is low
        Assertions.assertFalse(result.deviceStatus.get("Cooling"));  // Cooling should be OFF because temperature is low
    }


    @Test
    void testNightMode() {
        double currentPrice = 0.15;
        double priceThreshold = 0.20;
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Security", 1);
        devicePriorities.put("Lights", 2);
        devicePriorities.put("Refrigerator", 1);

        LocalDateTime currentTime = LocalDateTime.of(2024, 10, 16, 23, 30);
        double currentTemperature = 22.0;
        double[] desiredTemperatureRange = {20.0, 24.0};
        double energyUsageLimit = 30.0;
        double totalEnergyUsedToday = 15.0;

        SmartEnergyManagementSystem.EnergyManagementResult result = energySystem.manageEnergy(
                currentPrice, priceThreshold, devicePriorities, currentTime, currentTemperature,
                desiredTemperatureRange, energyUsageLimit, totalEnergyUsedToday, List.of()
        );

        Assertions.assertTrue(result.deviceStatus.get("Security"));
        Assertions.assertTrue(result.deviceStatus.get("Refrigerator"));
        Assertions.assertFalse(result.deviceStatus.get("Lights"));
    }

    @Test
    void testTemperatureRegulation() {
        double currentPrice = 0.15;
        double priceThreshold = 0.20;
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Heating", 1);
        devicePriorities.put("Cooling", 1);

        LocalDateTime currentTime = LocalDateTime.of(2024, 10, 16, 10, 0);
        double currentTemperature = 18.0;  // Below desired range
        double[] desiredTemperatureRange = {20.0, 24.0};
        double energyUsageLimit = 30.0;
        double totalEnergyUsedToday = 15.0;

        SmartEnergyManagementSystem.EnergyManagementResult result = energySystem.manageEnergy(
                currentPrice, priceThreshold, devicePriorities, currentTime, currentTemperature,
                desiredTemperatureRange, energyUsageLimit, totalEnergyUsedToday, List.of()
        );

        Assertions.assertTrue(result.deviceStatus.get("Heating"));
        Assertions.assertFalse(result.deviceStatus.get("Cooling"));
        Assertions.assertTrue(result.temperatureRegulationActive);

        // Test for temperature above range
        currentTemperature = 25.0;  // Above desired range

        result = energySystem.manageEnergy(
                currentPrice, priceThreshold, devicePriorities, currentTime, currentTemperature,
                desiredTemperatureRange, energyUsageLimit, totalEnergyUsedToday, List.of()
        );

        Assertions.assertFalse(result.deviceStatus.get("Heating"));
        Assertions.assertTrue(result.deviceStatus.get("Cooling"));
        Assertions.assertTrue(result.temperatureRegulationActive);
    }

    @Test
    void testEnergyUsageLimit() {
        double currentPrice = 0.15;
        double priceThreshold = 0.20;
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Heating", 1);  // High priority
        devicePriorities.put("Lights", 2);   // Low priority
        devicePriorities.put("Appliances", 3);  // Low priority
        devicePriorities.put("Cooling", 4);  // Low priority


        LocalDateTime currentTime = LocalDateTime.of(2024, 10, 16, 10, 0);
        double currentTemperature = 18.0;
        double[] desiredTemperatureRange = {20.0, 24.0};
        double energyUsageLimit = 30.0;
        double totalEnergyUsedToday = 31.0;  // Above the limit

        SmartEnergyManagementSystem.EnergyManagementResult result = energySystem.manageEnergy(
                currentPrice, priceThreshold, devicePriorities, currentTime, currentTemperature,
                desiredTemperatureRange, energyUsageLimit, totalEnergyUsedToday, List.of()
        );

        // Expect Lights and Appliances to be turned off as they are low-priority devices
        Assertions.assertFalse(result.deviceStatus.get("Lights"), "Lights should be turned off as the energy limit is near.");
        Assertions.assertFalse(result.deviceStatus.get("Appliances"), "Appliances should be turned off as the energy limit is near.");
        Assertions.assertTrue(result.deviceStatus.get("Heating"), "Heating should remain on as it is a high-priority device.");
    }

    @Test
    void testScheduledDevices() {
        double currentPrice = 0.15;
        double priceThreshold = 0.20;
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Oven", 3);

        LocalDateTime currentTime = LocalDateTime.of(2024, 10, 16, 18, 0);
        double currentTemperature = 22.0;
        double[] desiredTemperatureRange = {20.0, 24.0};
        double energyUsageLimit = 30.0;
        double totalEnergyUsedToday = 15.0;

        List<SmartEnergyManagementSystem.DeviceSchedule> scheduledDevices = List.of(
                new SmartEnergyManagementSystem.DeviceSchedule("Oven", currentTime)
        );

        SmartEnergyManagementSystem.EnergyManagementResult result = energySystem.manageEnergy(
                currentPrice, priceThreshold, devicePriorities, currentTime, currentTemperature,
                desiredTemperatureRange, energyUsageLimit, totalEnergyUsedToday, scheduledDevices
        );

        Assertions.assertTrue(result.deviceStatus.get("Oven"));
    }

    @Test
    void testNightModeActiveBefore6AM() {
        double currentPrice = 0.15;  // Price below threshold
        double priceThreshold = 0.20;
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Heating", 1);      // High priority
        devicePriorities.put("Lights", 2);       // Low priority
        devicePriorities.put("Appliances", 3);   // Low priority
        devicePriorities.put("Security", 1);     // Essential device, should remain ON
        devicePriorities.put("Refrigerator", 1); // Essential device, should remain ON

        // Set currentTime to 5 AM (active night mode)
        LocalDateTime currentTime = LocalDateTime.of(2024, 10, 16, 5, 0);
        double currentTemperature = 22.0;  // Normal temperature
        double[] desiredTemperatureRange = {20.0, 24.0};
        double energyUsageLimit = 30.0;
        double totalEnergyUsedToday = 15.0;  // Well below the limit

        SmartEnergyManagementSystem.EnergyManagementResult result = energySystem.manageEnergy(
                currentPrice, priceThreshold, devicePriorities, currentTime, currentTemperature,
                desiredTemperatureRange, energyUsageLimit, totalEnergyUsedToday, List.of()
        );

        // Check the status of devices
        Assertions.assertTrue(result.deviceStatus.get("Security"), "Security should remain ON during night mode.");
        Assertions.assertTrue(result.deviceStatus.get("Refrigerator"), "Refrigerator should remain ON during night mode.");
        Assertions.assertFalse(result.deviceStatus.get("Heating"), "Heating should be OFF during night mode.");
        Assertions.assertFalse(result.deviceStatus.get("Lights"), "Lights should be OFF during night mode.");
        Assertions.assertFalse(result.deviceStatus.get("Appliances"), "Appliances should be OFF during night mode.");
    }

    @Test
    void testScheduledDeviceNotActivated() {
        double currentPrice = 0.25;  // Price above threshold
        double priceThreshold = 0.20;
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Oven", 3);  // Low priority, scheduled to turn ON
        devicePriorities.put("Heating", 1); // High priority
        devicePriorities.put("Lights", 2);  // Low priority

        // Set currentTime to 6 PM (18:00), which does not match the scheduled time
        LocalDateTime currentTime = LocalDateTime.of(2024, 10, 16, 18, 0);
        double currentTemperature = 18.0;
        double[] desiredTemperatureRange = {20.0, 24.0};
        double energyUsageLimit = 30.0;
        double totalEnergyUsedToday = 15.0;  // Below limit

        // Schedule the Oven to turn ON at 7 PM (19:00)
        List<SmartEnergyManagementSystem.DeviceSchedule> scheduledDevices = List.of(
                new SmartEnergyManagementSystem.DeviceSchedule("Oven", LocalDateTime.of(2024, 10, 16, 19, 0))
        );

        SmartEnergyManagementSystem.EnergyManagementResult result = energySystem.manageEnergy(
                currentPrice, priceThreshold, devicePriorities, currentTime, currentTemperature,
                desiredTemperatureRange, energyUsageLimit, totalEnergyUsedToday, scheduledDevices
        );

        // Check the status of devices
        Assertions.assertTrue(result.deviceStatus.get("Heating"), "Heating should remain ON.");
        Assertions.assertFalse(result.deviceStatus.get("Lights"), "Lights should be OFF because of low-priority.");
        Assertions.assertFalse(result.deviceStatus.get("Oven"), "Oven should NOT be ON since current time does not match scheduled time.");
    }

    @Test
    void testEnergyUsageLimitNoHeating() {
        double currentPrice = 0.15;
        double priceThreshold = 0.20;
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Heating", 1);  // High priority

        LocalDateTime currentTime = LocalDateTime.of(2024, 10, 16, 10, 0);
        double currentTemperature = 21.5;
        double[] desiredTemperatureRange = {20.0, 24.0};
        double energyUsageLimit = 30.0;
        double totalEnergyUsedToday = 31.0;  // Above the limit

        SmartEnergyManagementSystem.EnergyManagementResult result = energySystem.manageEnergy(
                currentPrice, priceThreshold, devicePriorities, currentTime, currentTemperature,
                desiredTemperatureRange, energyUsageLimit, totalEnergyUsedToday, List.of()
        );

        // Expect Lights and Appliances to be turned off as they are low-priority devices
        Assertions.assertFalse(result.deviceStatus.get("Heating"), "Heating should be turned off.");
    }

    @Test
    void testEnergyUsageLimitDeviceOFF() {
        double currentPrice = 0.15;
        double priceThreshold = 0.20;
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Heating", 1);  // High priority
        devicePriorities.put("Lights", 2);       // Low priority
        devicePriorities.put("Appliances", 3);   // Low priority

        LocalDateTime currentTime = LocalDateTime.of(2024, 10, 16, 10, 0);
        double currentTemperature = 21.5;
        double[] desiredTemperatureRange = {20.0, 24.0};
        double energyUsageLimit = 30.0;
        double totalEnergyUsedToday = 31.0;  // Above the limit

        SmartEnergyManagementSystem.EnergyManagementResult result = energySystem.manageEnergy(
                currentPrice, priceThreshold, devicePriorities, currentTime, currentTemperature,
                desiredTemperatureRange, energyUsageLimit, totalEnergyUsedToday, List.of()
        );

        // Expect Lights and Appliances to be turned off as they are low-priority devices
        Assertions.assertFalse(result.deviceStatus.get("Heating"), "Heating should be turned off.");
    }
}
