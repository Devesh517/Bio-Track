package service;

import config.AppConfig;
import model.HealthRecord;
import model.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * BioTrack Assistant - a chatbot backed by the Anthropic API that answers
 * questions grounded in the user's *own* stored vitals (heart rate, BP,
 * sugar, SpO2, weight/BMI), and can suggest what to do next in the app
 * (e.g. "log today's blood pressure", "export last month as PDF").
 *
 * This is a lightweight, dependency-light client for POST /v1/messages -
 * it does not use the Anthropic Java SDK to keep the project's footprint
 * small, just java.net.http + org.json.
 */
public class ChatbotService {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final AnalysisService analysisService = new AnalysisService();

    /** In-memory running transcript for the current session (role -> text). */
    private final List<JSONObject> conversation = new ArrayList<>();

    public boolean isConfigured() {
        return AppConfig.isChatbotConfigured();
    }

    public void resetConversation() {
        conversation.clear();
    }

    /**
     * Sends a user message plus a system prompt built from the user's real
     * health records, and returns the assistant's reply text.
     */
    public String ask(User user, List<HealthRecord> recentRecords, String userMessage) throws IOException, InterruptedException {
        if (!isConfigured()) {
            return "The BioTrack Assistant isn't set up yet. Add your ANTHROPIC_API_KEY " +
                    "(environment variable or config.properties) to enable it.";
        }

        String systemPrompt = buildSystemPrompt(user, recentRecords);

        conversation.add(new JSONObject().put("role", "user").put("content", userMessage));

        JSONObject body = new JSONObject();
        body.put("model", AppConfig.anthropicModel());
        body.put("max_tokens", 1024);
        body.put("system", systemPrompt);
        body.put("messages", new JSONArray(conversation));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.anthropicApiUrl()))
                .header("content-type", "application/json")
                .header("x-api-key", AppConfig.anthropicApiKey())
                .header("anthropic-version", "2023-06-01")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            conversation.remove(conversation.size() - 1); // don't keep a failed turn in history
            return "The assistant is temporarily unavailable (HTTP " + response.statusCode() + "). Please try again.";
        }

        JSONObject json = new JSONObject(response.body());
        JSONArray content = json.optJSONArray("content");
        StringBuilder reply = new StringBuilder();
        if (content != null) {
            for (int i = 0; i < content.length(); i++) {
                JSONObject block = content.getJSONObject(i);
                if ("text".equals(block.optString("type"))) {
                    reply.append(block.optString("text"));
                }
            }
        }

        String replyText = reply.toString().trim();
        conversation.add(new JSONObject().put("role", "assistant").put("content", replyText));
        return replyText;
    }

    /**
     * Grounds the assistant in the user's actual vitals/trends so answers
     * are specific ("your average BP this week is 128/84...") rather than
     * generic, and tells it what the app itself can do so it can guide the
     * user to the right feature (health record entry, reports, documents).
     */
    private String buildSystemPrompt(User user, List<HealthRecord> recentRecords) {
        AnalysisService.Summary summary = analysisService.summarize(recentRecords);

        StringBuilder sb = new StringBuilder();
        sb.append("You are the BioTrack Assistant, a helpful and cautious health-tracking companion ")
                .append("inside the BioTrack desktop app. You are NOT a doctor and must never diagnose or ")
                .append("prescribe medication - encourage seeing a real clinician for medical decisions. ")
                .append("Keep answers concise and practical.\n\n");

        sb.append("App features you can point the user to:\n")
                .append("- Health Records: log heart rate, blood pressure, blood sugar, SpO2, weight/BMI.\n")
                .append("- Analytics: charts and trend analysis of their vitals over time.\n")
                .append("- Reports: generate a health report as PDF, Word, TXT, or Excel.\n")
                .append("- Excel log: every saved reading is also appended to a running, date-wise Excel sheet.\n")
                .append("- Documents: upload/store lab reports, prescriptions, scans (pdf/jpg/png/docx/txt, max 5MB).\n")
                .append("- Reminders: medicine, walk, and appointment reminders.\n\n");

        sb.append("Patient: ").append(user.getFullName()).append(" (ID ").append(user.getUserId()).append(")\n");
        if (recentRecords == null || recentRecords.isEmpty()) {
            sb.append("They have no health records yet.\n");
        } else {
            sb.append("Recent vitals summary (based on ").append(recentRecords.size()).append(" records):\n");
            if (summary.avgHeartRate != null) sb.append("- Avg heart rate: ").append(round(summary.avgHeartRate)).append(" bpm\n");
            if (summary.avgSystolic != null) sb.append("- Avg blood pressure: ").append(round(summary.avgSystolic)).append("/").append(round(summary.avgDiastolic)).append(" mmHg\n");
            if (summary.avgSugar != null) sb.append("- Avg blood sugar: ").append(round(summary.avgSugar)).append(" mg/dL\n");
            if (summary.avgSpo2 != null) sb.append("- Avg SpO2: ").append(round(summary.avgSpo2)).append(" %\n");
            if (summary.latestWeight != null) sb.append("- Latest weight: ").append(summary.latestWeight).append(" kg\n");
            if (summary.latestBmi != null) sb.append("- Latest BMI: ").append(summary.latestBmi).append("\n");
            sb.append("- Overall status: ").append(summary.overallStatus.getLabel()).append("\n");
            sb.append("Existing insights already computed by the app:\n");
            for (String i : summary.insights) sb.append("  * ").append(i).append("\n");
        }

        return sb.toString();
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}