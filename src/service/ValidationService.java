package service;

import model.HealthRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Sanity-checks user input before it hits the database - keeps garbage
 * values (negative heart rate, BP of 0/0, etc.) out of the analysis and
 * out of the exported reports.
 */
public class ValidationService {

    public List<String> validate(HealthRecord r) {
        List<String> errors = new ArrayList<>();

        if (r.getUserId() == null || r.getUserId().isBlank())
            errors.add("User is required.");
        if (r.getRecordDate() == null)
            errors.add("Date is required.");

        boolean anyVitalProvided = r.getHeartRate() != null || r.getBpSystolic() != null
                || r.getBpDiastolic() != null || r.getBloodSugar() != null
                || r.getSpo2() != null || r.getWeightKg() != null;
        if (!anyVitalProvided)
            errors.add("Enter at least one vital (heart rate, BP, sugar, SpO2 or weight).");

        if (r.getHeartRate() != null && (r.getHeartRate() < 20 || r.getHeartRate() > 250))
            errors.add("Heart rate looks unrealistic (expected 20-250 bpm).");

        if (r.getBpSystolic() != null && (r.getBpSystolic() < 50 || r.getBpSystolic() > 260))
            errors.add("Systolic BP looks unrealistic (expected 50-260 mmHg).");

        if (r.getBpDiastolic() != null && (r.getBpDiastolic() < 30 || r.getBpDiastolic() > 180))
            errors.add("Diastolic BP looks unrealistic (expected 30-180 mmHg).");

        if ((r.getBpSystolic() == null) != (r.getBpDiastolic() == null))
            errors.add("Please provide both systolic and diastolic BP values.");

        if (r.getBpSystolic() != null && r.getBpDiastolic() != null && r.getBpSystolic() <= r.getBpDiastolic())
            errors.add("Systolic BP must be greater than diastolic BP.");

        if (r.getBloodSugar() != null && (r.getBloodSugar() < 20 || r.getBloodSugar() > 700))
            errors.add("Blood sugar looks unrealistic (expected 20-700 mg/dL).");

        if (r.getSpo2() != null && (r.getSpo2() < 40 || r.getSpo2() > 100))
            errors.add("SpO2 must be between 40% and 100%.");

        if (r.getWeightKg() != null && (r.getWeightKg() < 2 || r.getWeightKg() > 400))
            errors.add("Weight looks unrealistic (expected 2-400 kg).");

        return errors;
    }

    public boolean isValid(HealthRecord r) {
        return validate(r).isEmpty();
    }

    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    }

    public boolean isStrongPassword(String password) {
        return password != null && password.length() >= 8;
    }
}