package projecthealth;
public class analyzer {
    public static String analyzeData(double weight, double height, double bmi, double temperature,
                                     int bloodPressure, int heartRate, int sugarLevel) {
        StringBuilder analysis = new StringBuilder();

        if (heartRate < 60 || heartRate > 100) {
            analysis.append("Abnormal heart rate detected!\n");
        }

        if (temperature < 36.0 || temperature > 37.5) {
            analysis.append("Abnormal body temperature detected!\n");
        }

        if (bloodPressure < 90 || bloodPressure > 132) {
            analysis.append("Abnormal blood pressure detected!\n");
        }

        if (sugarLevel < 70 || sugarLevel > 100) {
            analysis.append("Abnormal sugar level detected!\n");
        }

        if (bmi < 18.5) {
            analysis.append("Underweight detected!\n");
        } else if (bmi >= 18.5 && bmi < 24.9) {
            analysis.append("BMI is in the normal range.\n");
        } else if (bmi >= 25 && bmi < 29.9) {
            analysis.append("Overweight detected!\n");
        } else {
            analysis.append("Obesity detected!\n");
        }

        return analysis.length() == 0 ? "All parameters are normal." : analysis.toString();
    }
}
