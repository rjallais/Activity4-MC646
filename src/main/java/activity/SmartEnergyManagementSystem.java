package activity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartEnergyManagementSystem {

    public static class DeviceSchedule {
        String deviceName;
        LocalDateTime scheduledTime;

        public DeviceSchedule(String deviceName, LocalDateTime scheduledTime) {
            this.deviceName = deviceName;
            this.scheduledTime = scheduledTime;
        }
    }

    public static class EnergyManagementResult {
        Map<String, Boolean> deviceStatus;
        boolean energySavingMode;
        boolean temperatureRegulationActive;
        double totalEnergyUsed;

        public EnergyManagementResult(Map<String, Boolean> deviceStatus, boolean energySavingMode, boolean temperatureRegulationActive, double totalEnergyUsed) {
            this.deviceStatus = deviceStatus;
            this.energySavingMode = energySavingMode;
            this.temperatureRegulationActive = temperatureRegulationActive;
            this.totalEnergyUsed = totalEnergyUsed;
        }
    }

    public EnergyManagementResult manageEnergy(double currentPrice, double priceThreshold, Map<String, Integer> devicePriorities,
                                               LocalDateTime currentTime, double currentTemperature, double[] desiredTemperatureRange,
                                               double energyUsageLimit, double totalEnergyUsedToday, List<DeviceSchedule> scheduledDevices) {

        Map<String, Boolean> deviceStatus = new HashMap<>();
        boolean energySavingMode = false;
        boolean temperatureRegulationActive;

        // Activate energy-saving mode if price exceeds threshold
        if (currentPrice > priceThreshold) {
            energySavingMode = true;
            // Turn off low-priority devices
            for (Map.Entry<String, Integer> entry : devicePriorities.entrySet()) {
                if (entry.getValue() > 1) {  // Priorities > 1 are low priority
                    deviceStatus.put(entry.getKey(), false);
                } else {
                    deviceStatus.put(entry.getKey(), true);  // High-priority devices stay on
                }
            }
        } else {
            // No energy-saving mode; keep devices on
            for (String device : devicePriorities.keySet()) {
                deviceStatus.put(device, true);
            }
        }

        // Night mode between 11 PM and 6 AM
        if (currentTime.getHour() == 23 || currentTime.getHour() < 6) {
            for (String device : devicePriorities.keySet()) {
                if (!device.equals("Security") && !device.equals("Refrigerator")) {
                    deviceStatus.put(device, false);
                }
            }
        }

        // Temperature regulation
        if (currentTemperature < desiredTemperatureRange[0]) {
            deviceStatus.put("Heating", true);
            deviceStatus.put("Cooling", false);
            temperatureRegulationActive = true;
        } else if (currentTemperature > desiredTemperatureRange[1]) {
            deviceStatus.put("Heating", false);
            deviceStatus.put("Cooling", true);
            temperatureRegulationActive = true;
        } else {
            deviceStatus.put("Heating", false);
            deviceStatus.put("Cooling", false);
            temperatureRegulationActive = false;
        }


        // Shut down devices as energy limit is approached
        boolean reductionOccurred = true;
        while (totalEnergyUsedToday >= energyUsageLimit && reductionOccurred) {
            reductionOccurred = false;
            for (Map.Entry<String, Integer> entry : devicePriorities.entrySet()) {
                String deviceName = entry.getKey();
                if (deviceStatus.getOrDefault(deviceName, false) && entry.getValue() > 1) {
                    deviceStatus.put(deviceName, false);
                    totalEnergyUsedToday -= 1;  // Simulate energy reduction
                    reductionOccurred = true;
                    break;  // Break to re-evaluate the while condition with updated energy usage
                }
            }
        }

        // Handle scheduled devices
        for (DeviceSchedule schedule : scheduledDevices) {
            if (schedule.scheduledTime.equals(currentTime)) {
                deviceStatus.put(schedule.deviceName, true);
            }
        }

        return new EnergyManagementResult(deviceStatus, energySavingMode, temperatureRegulationActive, totalEnergyUsedToday);
    }

}