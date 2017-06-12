package com.example.groupfourtwo.bluetoothsensorapp.bluetooth;

import java.util.HashSet;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by patrick on 23.05.17.
 */

class SensortagUUIDs {

    static HashSet<UUID> gattServices = new HashSet<>();
    static HashMap<UUID, UUID> data = new HashMap<>();
    static HashMap<UUID, UUID> config = new HashMap<>();
    static HashMap<UUID, String> attributes = new HashMap<>();

    final static UUID UUID_IR_TEMP_SERV = UUID.fromString("F000AA00-0451-4000-B000-000000000000");
    final static UUID UUID_IR_TEMP_DATA = UUID.fromString("F000AA01-0451-4000-B000-000000000000");
    final static UUID UUID_IR_TEMP_CONF = UUID.fromString("F000AA02-0451-4000-B000-000000000000");
    final static UUID UUID_IR_TEMP_PERI = UUID.fromString("F000AA03-0451-4000-B000-000000000000");

    final static UUID UUID_HUM_SERV = UUID.fromString("F000AA20-0451-4000-B000-000000000000");
    final static UUID UUID_HUM_DATA = UUID.fromString("F000AA21-0451-4000-B000-000000000000");
    final static UUID UUID_HUM_CONF = UUID.fromString("F000AA22-0451-4000-B000-000000000000");
    final static UUID UUID_HUM_PERI = UUID.fromString("F000AA23-0451-4000-B000-000000000000");

    final static UUID UUID_BAROMETER_SERV = UUID.fromString("F000AA40-0451-4000-B000-000000000000");
    final static UUID UUID_BAROMETER_DATA = UUID.fromString("F000AA41-0451-4000-B000-000000000000");
    final static UUID UUID_BAROMETER_CONF = UUID.fromString("F000AA42-0451-4000-B000-000000000000");
    final static UUID UUID_BAROMETER_CALI = UUID.fromString("F000AA43-0451-4000-B000-000000000000");
    final static UUID UUID_BAROMETER_PERI = UUID.fromString("F000AA44-0451-4000-B000-000000000000");


    final static UUID UUID_LUXMETER_SERV = UUID.fromString("F000AA70-0451-4000-B000-000000000000");
    final static UUID UUID_LUXMETER_DATA = UUID.fromString("F000AA71-0451-4000-B000-000000000000");
    final static UUID UUID_LUXMETER_CONF = UUID.fromString("F000AA72-0451-4000-B000-000000000000");
    final static UUID UUID_LUXMETER_PERI = UUID.fromString("F000AA73-0451-4000-B000-000000000000");

    final static UUID UUID_MAGNET_SERV = UUID.fromString("F000AA30-0451-4000-B000-000000000000");
    final static UUID UUID_MAGNET_DATA = UUID.fromString("F000AA31-0451-4000-B000-000000000000");
    final static UUID UUID_MAGNET_CONF = UUID.fromString("F000AA32-0451-4000-B000-000000000000");
    final static UUID UUID_MAGNET_PERI = UUID.fromString("F000AA33-0451-4000-B000-000000000000");

    final static UUID UUID_ACCEL_SERV = UUID.fromString("F000AA10-0451-4000-B000-000000000000");
    final static UUID UUID_ACCEL_DATA = UUID.fromString("F000AA11-0451-4000-B000-000000000000");
    final static UUID UUID_ACCEL_CONF = UUID.fromString("F000AA12-0451-4000-B000-000000000000");// 0: disable, 1: enable
    final static UUID UUID_ACCEL_PERI = UUID.fromString("F000AA13-0451-4000-B000-000000000000");

    final static UUID UUID_GYRO_SERV = UUID.fromString("F000AA50-0451-4000-B000-000000000000");
    final static UUID UUID_GYRO_DATA = UUID.fromString("F000AA51-0451-4000-B000-000000000000");
    final static UUID UUID_GYRO_CONF = UUID.fromString("F000AA52-0451-4000-B000-000000000000");// 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z
    final static UUID UUID_GYRO_PERI = UUID.fromString("F000AA53-0451-4000-B000-000000000000");

    final static UUID UUID_MOVE_SERV = UUID.fromString("F000AA80-0451-4000-B000-000000000000");
    final static UUID UUID_MOVE_DATA = UUID.fromString("F000AA81-0451-4000-B000-000000000000");
    final static UUID UUID_MOVE_CONF = UUID.fromString("F000AA82-0451-4000-B000-000000000000");// 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z
    final static UUID UUID_MOVE_PERI = UUID.fromString("F000AA83-0451-4000-B000-000000000000");

    final static UUID UUID_TST_SERV = UUID.fromString("F000AA64-0451-4000-B000-000000000000");
    final static UUID UUID_TST_DATA = UUID.fromString("F000AA65-0451-4000-B000-000000000000"); // Test result

    final static UUID UUID_KEY_SERV = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");
    final static UUID UUID_KEY_DATA = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");

    final static UUID UUID_CCC = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    final static UUID UUID_DEVINFO_SERV = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB");
    final static UUID UUID_DEVINFO_FWREV = UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB");


    static {
        gattServices.add(UUID_IR_TEMP_SERV);
        gattServices.add(UUID_HUM_SERV);
        gattServices.add(UUID_BAROMETER_SERV);
        gattServices.add(UUID_LUXMETER_SERV);

        data.put(UUID_IR_TEMP_SERV, UUID_IR_TEMP_DATA);
        data.put(UUID_HUM_SERV, UUID_HUM_DATA);
        data.put(UUID_BAROMETER_SERV, UUID_BAROMETER_DATA);
        data.put(UUID_LUXMETER_SERV, UUID_LUXMETER_DATA);

        config.put(UUID_IR_TEMP_SERV, UUID_IR_TEMP_CONF);
        config.put(UUID_HUM_SERV, UUID_HUM_CONF);
        config.put(UUID_BAROMETER_SERV, UUID_BAROMETER_CONF);
        config.put(UUID_LUXMETER_SERV, UUID_LUXMETER_CONF);

        attributes.put(UUID_DEVINFO_SERV, "Device Info Service");
        attributes.put(UUID_IR_TEMP_SERV, "Temperature Sensor Service");
        attributes.put(UUID_ACCEL_SERV, "Accelerometer Service");
        attributes.put(UUID_HUM_SERV, "Humidity Sensor Service");
        attributes.put(UUID_MAGNET_SERV, "Magnetometer Service");
        attributes.put(UUID_LUXMETER_SERV, "Luxmeter Service");
        attributes.put(UUID_BAROMETER_SERV, "Barometer Service");
        attributes.put(UUID_GYRO_SERV, "Gyrometer Service");
        attributes.put(UUID_MOVE_SERV, "Movement Sensor Service");
        attributes.put(UUID_TST_SERV, "Test Service");
        attributes.put(UUID_KEY_SERV, "Key ? Service");

        // Characteristics
        // Data
        attributes.put(UUID_DEVINFO_FWREV, "Firmware Revision");
        attributes.put(UUID_IR_TEMP_DATA, "Temperature Sensor Data");
        attributes.put(UUID_ACCEL_DATA, "Accelerometer Data");
        attributes.put(UUID_HUM_DATA, "Humidity Sensor Data");
        attributes.put(UUID_MAGNET_DATA, "Magnetometer Data");
        attributes.put(UUID_LUXMETER_DATA, "Luxmeter Data");
        attributes.put(UUID_BAROMETER_DATA, "Barometer Data");
        attributes.put(UUID_GYRO_DATA, "Gyrometer Data");
        attributes.put(UUID_MOVE_DATA, "Movement Sensor Data");
        attributes.put(UUID_TST_DATA, "Test Data");
        attributes.put(UUID_KEY_DATA, " Key Data?");

        // Update intervals
        attributes.put(UUID_IR_TEMP_PERI, "Temperature Sensor Update Interval");
        attributes.put(UUID_ACCEL_PERI, "Accelerometer Update Interval");
        attributes.put(UUID_HUM_PERI, "Humidity Sensor Update Interval");
        attributes.put(UUID_MAGNET_PERI, "Magnetometer Update Interval");
        attributes.put(UUID_LUXMETER_PERI, "Luxmeter Update Interval");
        attributes.put(UUID_BAROMETER_PERI, "Barometer Update Interval");
        attributes.put(UUID_GYRO_PERI, "Gyrometer Update Interval");
        attributes.put(UUID_MOVE_PERI, "Movement Sensor Update Interval");

        // Conf
        attributes.put(UUID_CCC, "Client Characteristics Config");
        attributes.put(UUID_IR_TEMP_CONF, "Temperature Sensor Config");
        attributes.put(UUID_ACCEL_CONF, "Accelerometer Config");
        attributes.put(UUID_HUM_CONF, "Humidity Config");
        attributes.put(UUID_MAGNET_CONF, "Magnetometer Config");
        attributes.put(UUID_LUXMETER_CONF, "Luxmeter Config");
        attributes.put(UUID_BAROMETER_CONF, "Barometer Config");
        attributes.put(UUID_BAROMETER_CALI, "Barometer Calibration");
        attributes.put(UUID_GYRO_CONF, "Gyrometer Config");
        attributes.put(UUID_MOVE_CONF, "Movement Sensor Config");

    }

    public static String lookup(UUID uuid) {
        String name = attributes.get(uuid);
        return name == null ? "NOT FOUND" : name;
    }


}



