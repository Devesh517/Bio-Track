package enums;

/**
 * Generic status flag used across vitals (heart rate, BP, sugar, SpO2, BMI)
 * so the UI can consistently show colored badges like "Normal" / "High".
 */
public enum HealthStatus {
    LOW("Low", "#f59e0b"),
    NORMAL("Normal", "#22c55e"),
    ELEVATED("Elevated", "#f59e0b"),
    HIGH("High", "#ef4444"),
    CRITICAL("Critical", "#dc2626"),
    UNKNOWN("Unknown", "#94a3b8");

    private final String label;
    private final String colorHex;

    HealthStatus(String label, String colorHex) {
        this.label = label;
        this.colorHex = colorHex;
    }

    public String getLabel() {
        return label;
    }

    public String getColorHex() {
        return colorHex;
    }
}