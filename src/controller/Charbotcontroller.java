package controller;

import config.Session;
import dao.HealthRecordDAO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import model.HealthRecord;
import service.ChatbotService;

import java.util.List;

/**
 * Controller for the BioTrack Assistant chat screen. Every question is
 * answered grounded in the logged-in user's own recent health records
 * (see ChatbotService.buildSystemPrompt), and the app suggests next
 * actions (log a reading, generate a report, etc.).
 */
public class ChatbotController {

    @FXML private ListView<String> chatListView;
    @FXML private TextField messageField;
    @FXML private Label statusLabel;

    private final ChatbotService chatbotService = new ChatbotService();
    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();

    @FXML
    public void initialize() {
        if (!chatbotService.isConfigured() && statusLabel != null) {
            statusLabel.setText("Assistant not configured - set ANTHROPIC_API_KEY to enable it.");
            statusLabel.setStyle("-fx-text-fill: #f59e0b;");
        }
        if (chatListView != null) {
            chatListView.getItems().add("BioTrack Assistant: Hi! Ask me about your recent vitals, "
                    + "trends, or what you can do next in the app.");
        }
    }

    @FXML
    public void handleSend() {
        String userId = Session.getCurrentUserId();
        String message = messageField.getText() == null ? "" : messageField.getText().trim();
        if (message.isEmpty() || userId == null) return;

        chatListView.getItems().add("You: " + message);
        messageField.clear();
        setStatus("Thinking...", false);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                List<HealthRecord> recent = healthRecordDAO.findLastNDays(userId, 30);
                return chatbotService.ask(Session.getCurrentUser(), recent, message);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            chatListView.getItems().add("BioTrack Assistant: " + task.getValue());
            setStatus("", false);
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            chatListView.getItems().add("BioTrack Assistant: Sorry, something went wrong reaching the assistant.");
            setStatus(task.getException() != null ? task.getException().getMessage() : "Unknown error", true);
        }));

        new Thread(task, "chatbot-request").start();
    }

    private void setStatus(String message, boolean isError) {
        if (statusLabel == null) return;
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: #ef4444;" : "-fx-text-fill: #94a3b8;");
    }
}