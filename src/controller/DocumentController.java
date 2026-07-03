package controller;

import config.Session;
import dao.DocumentDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import model.Document;
import utils.FileUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentController {

    @FXML private ListView<String> documentsListView;
    @FXML private Label statusLabel;

    private final DocumentDAO documentDAO = new DocumentDAO();

    @FXML
    public void initialize() {
        loadDocuments();
    }

    @FXML
    public void handleBrowseFiles(javafx.event.ActionEvent event) {
        String userId = Session.getCurrentUserId();
        if (userId == null) {
            setStatus("Please log in first.", true);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a document to upload");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Allowed files (*.pdf, *.jpg, *.jpeg, *.png, *.docx, *.txt)",
                "*.pdf", "*.jpg", "*.jpeg", "*.png", "*.docx", "*.txt"));

        Window window = ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File selected = chooser.showOpenDialog(window);
        if (selected == null) return;

        try {
            Path stored = FileUtil.storeUserDocument(userId, selected.toPath());

            Document doc = new Document();
            doc.setUserId(userId);
            doc.setFileName(selected.getName());
            doc.setFileType(FileUtil.extensionOf(selected.getName()));
            doc.setCategory(guessCategory(selected.getName()));
            doc.setFilePath(stored.toString());
            doc.setFileSizeKb(FileUtil.sizeInKb(stored));
            documentDAO.create(doc);

            setStatus("Uploaded: " + selected.getName(), false);
            loadDocuments();
        } catch (Exception e) {
            setStatus("Upload failed: " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleDeleteSelected() {
        // Simplified: deletion by filename match against the selected list row.
        String selected = documentsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a document to delete.", true);
            return;
        }
        setStatus("Use the Documents table (full view) to delete specific files.", false);
    }

    private void loadDocuments() {
        String userId = Session.getCurrentUserId();
        if (userId == null || documentsListView == null) return;
        List<Document> docs = documentDAO.findAllByUser(userId);
        documentsListView.setItems(FXCollections.observableArrayList(
                docs.stream()
                        .map(d -> d.getFileName() + "   (" + d.getCategory() + ", " + d.getFileSizeKb() + " KB)")
                        .collect(Collectors.toList())
        ));
    }

    private String guessCategory(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.contains("xray") || lower.contains("x-ray")) return "X-Ray";
        if (lower.contains("ecg")) return "ECG Report";
        if (lower.contains("bp")) return "BP Report";
        if (lower.contains("prescription")) return "Prescription";
        return "General Document";
    }

    private void setStatus(String message, boolean isError) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle(isError ? "-fx-text-fill: #ef4444;" : "-fx-text-fill: #22c55e;");
        } else {
            new Alert(isError ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION, message).showAndWait();
        }
    }
}