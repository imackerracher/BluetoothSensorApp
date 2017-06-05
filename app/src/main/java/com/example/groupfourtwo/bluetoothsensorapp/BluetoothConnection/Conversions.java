package com.example.groupfourtwo.bluetoothsensorapp.BluetoothConnection;

import static java.lang.Math.pow;

/**
 * Created by patrick on 05.06.17.
 * Taken from TI Sensortag app.
 */

public class Conversions {

    public static float[] conversionHum(final byte[] value) {
        int a = shortUnsignedAtOffset(value, 2);
        /* bits [1..0] are status bits and need to be cleared according
        * to the user guide, but the iOS code doesn't bother. It should
        * have minimal impact.
        */
        a = a - (a % 4);

        return new float[]{(-6f) + 125f * (a / 65535f), 0, 0};

    }

    public static float[] conversionBaro(final byte[] value) {
        if (value.length > 4) {
            Integer val = twentyFourBitUnsignedAtOffset(value, 2);
            return new float[]{(float) (val / 100.0), 0, 0};
        } else {
            int m;
            int e;
            Integer sfloat = shortUnsignedAtOffset(value, 2);

            m = sfloat & 0x0FFF;
            e = (sfloat >> 12) & 0xFF;

            double output;
            double magnitude = pow(2.0f, e);
            output = (m * magnitude);
            return new float[]{(float) (output / 100.0f), 0, 0};
        }

    }

    public static float[] conversionLux(final byte[] value) {
        int m;
        int e;
        Integer sfloat = shortUnsignedAtOffset(value, 0);

        m = sfloat & 0x0FFF;
        e = (sfloat >> 12) & 0xFF;

        double output;
        double magnitude = pow(2.0f, e);
        output = (m * magnitude);

        return new float[]{(float) (output / 100.0f), 0, 0};

    }

    public static float[] conversionIRTemp(final byte[] value) {
        			/*
             * The IR Temperature sensor produces two measurements; Object ( AKA target or IR) Temperature, and Ambient ( AKA die ) temperature.
			 * Both need some conversion, and Object temperature is dependent on Ambient temperature.
			 * They are stored as [ObjLSB, ObjMSB, AmbLSB, AmbMSB] (4 bytes) Which means we need to shift the bytes around to get the correct values.
			 */

        float ambient = extractAmbientTemperature(value);
        float target = extractTargetTemperature(value, ambient);
        float targetNewSensor = extractTargetTemperatureTMP007(value);
        return new float[]{ambient, target, targetNewSensor};

    }

    private static float extractAmbientTemperature(byte[] v) {
        int offset = 2;
        return (float) (shortUnsignedAtOffset(v, offset) / 128.0);
    }


    private static float extractTargetTemperature(byte[] v, double ambient) {
        Integer twoByteValue = shortSignedAtOffset(v, 0);

        double Vobj2 = twoByteValue.doubleValue();
        Vobj2 *= 0.00000015625;

        double Tdie = ambient + 273.15;

        double S0 = 5.593E-14; // Calibration factor
        double a1 = 1.75E-3;
        double a2 = -1.678E-5;
        double b0 = -2.94E-5;
        double b1 = -5.7E-7;
        double b2 = 4.63E-9;
        double c2 = 13.4;
        double Tref = 298.15;
        double S = S0 * (1 + a1 * (Tdie - Tref) + a2 * pow((Tdie - Tref), 2));
        double Vos = b0 + b1 * (Tdie - Tref) + b2 * pow((Tdie - Tref), 2);
        double fObj = (Vobj2 - Vos) + c2 * pow((Vobj2 - Vos), 2);
        double tObj = pow(pow(Tdie, 4) + (fObj / S), .25);

        return (float) (tObj - 273.15);
    }


    private static float extractTargetTemperatureTMP007(byte[] v) {
        int offset = 0;
        return (float) (shortUnsignedAtOffset(v, offset) / 128.0);
    }

    /**
     * Gyroscope, Magnetometer, Barometer, IR temperature all store 16 bit two's complement values as LSB MSB, which cannot be directly parsed
     * as getIntValue(FORMAT_SINT16, offset) because the bytes are stored as little-endian.
     *
     * This function extracts these 16 bit two's complement values.
     */
    private static Integer shortSignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset + 1]; // // Interpret MSB as signed
        return (upperByte << 8) + lowerByte;
    }


    private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset + 1] & 0xFF;
        return (upperByte << 8) + lowerByte;
    }

    private static Integer twentyFourBitUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer mediumByte = (int) c[offset + 1] & 0xFF;
        Integer upperByte = (int) c[offset + 2] & 0xFF;
        return (upperByte << 16) + (mediumByte << 8) + lowerByte;
    }
}
