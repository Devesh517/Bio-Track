package projecthealth;

import java.util.Random;

public class healthdata {
    private static final Random random = new Random();

    public static int getHeartRate() {
        return 60 + random.nextInt(41); // 60-100 bpm
    }

    public static double getBodyTemperature() {
        return 36.0 + random.nextDouble() * 1.5; // 36.0-37.5°C
    }

    public static int getBloodPressure() {
        return 90 + random.nextInt(31); // 90-120 mmHg
    }

    public static double getWeight() {
        return 50 + random.nextDouble() * 50; // 50-100 kg
    }

    public static double getHeight() {
        return 1.5 + random.nextDouble() * 0.5; // 1.5-2.0 meters
    }

    public static double getcalculateBMI(double weight, double height) {
        return weight / (height * height); // BMI formula
    }

    public static int getSugarLevel() {
        return 70 + random.nextInt(131); // 70-200 mg/dL
    }
}

