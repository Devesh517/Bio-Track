package utils;

import enums.HealthStatus;

/** BMI = weight(kg) / height(m)^2 */
public class BMIUtil {

    private BMIUtil() { }

    public static double calculate(double weightKg, double heightCm) {
        if (heightCm <= 0) return 0;
        double heightM = heightCm / 100.0;
        double bmi = weightKg / (heightM * heightM);
        return Math.round(bmi * 100.0) / 100.0;
    }

    public static String category(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi <= 24.9) return "Normal";
        if (bmi <= 29.9) return "Overweight";
        return "Obese";
    }

    public static HealthStatus status(double bmi) {
        if (bmi <= 0) return HealthStatus.UNKNOWN;
        if (bmi < 18.5) return HealthStatus.LOW;
        if (bmi <= 24.9) return HealthStatus.NORMAL;
        if (bmi <= 29.9) return HealthStatus.ELEVATED;
        return HealthStatus.HIGH;
    }
}