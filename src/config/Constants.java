package config;

/**
 * Application-wide constant values: normal vital-sign ranges (used by
 * ValidationService / AnalysisService), file directories and misc labels.
 */
public class Constants {

    private Constants() { }

    public static final String APP_NAME    = "BioTrack";
    public static final String APP_VERSION = "2.0.0";

    // ---------- Normal vital ranges (adult) ----------
    public static final int HEART_RATE_MIN = 60;
    public static final int HEART_RATE_MAX = 100;

    public static final int BP_SYSTOLIC_MIN  = 90;
    public static final int BP_SYSTOLIC_MAX  = 120;
    public static final int BP_DIASTOLIC_MIN = 60;
    public static final int BP_DIASTOLIC_MAX = 80;

    public static final double SUGAR_FASTING_MIN = 70;
    public static final double SUGAR_FASTING_MAX = 100;   // mg/dL, fasting
    public static final double SUGAR_RANDOM_MAX   = 140;  // mg/dL, random/post-meal

    public static final double SPO2_MIN = 95.0;

    public static final double BMI_UNDERWEIGHT = 18.5;
    public static final double BMI_NORMAL_MAX  = 24.9;
    public static final double BMI_OVERWEIGHT_MAX = 29.9;

    // ---------- File storage ----------
    public static final String DATA_ROOT_DIR   = System.getProperty("user.home") + "/BioTrackData";
    public static final String DOCUMENTS_DIR   = DATA_ROOT_DIR + "/documents";
    public static final String REPORTS_DIR     = DATA_ROOT_DIR + "/reports";
    public static final String EXCEL_DIR       = DATA_ROOT_DIR + "/excel";

    public static final long MAX_UPLOAD_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB, matches UI "Max 5MB"

    // ---------- Misc ----------
    public static final String[] ALLOWED_DOCUMENT_TYPES = {"pdf", "jpg", "jpeg", "png", "docx", "txt"};
    public static final String DATE_DISPLAY_FORMAT = "dd MMM, yyyy";
    public static final String DATETIME_DISPLAY_FORMAT = "dd MMM yyyy, hh:mm a";
}