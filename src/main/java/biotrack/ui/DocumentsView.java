package biotrack.ui;
import biotrack.dao.DocumentDAO;
import biotrack.dao.HealthRecordDAO;
import biotrack.model.HealthRecord;
import biotrack.model.MedicalDocument;
import biotrack.model.User;
import biotrack.service.ExcelExportService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

public class DocumentsView {

    public static Parent getView(User user) {
        VBox root = new VBox(14);
        root.getStyleClass().add("view-card");
        root.setPadding(new Insets(10));

        Label title = new Label("Medical Documents - " + user.getName());
        title.getStyleClass().add("section-title");

        Label status = new Label();
        status.setWrapText(true);

        // ---- Filter bar -----------------------------------------------------
        ComboBox<String> categoryFilter = new ComboBox<>(FXCollections.observableArrayList("All"));
        categoryFilter.getItems().addAll(MedicalDocument.CATEGORIES);
        categoryFilter.setValue("All");
        DatePicker fromPicker = new DatePicker();
        DatePicker toPicker = new DatePicker();
        ComboBox<Integer> linkedRecordFilter = new ComboBox<>();
        linkedRecordFilter.setPromptText("Linked record");
        Button filterBtn = new Button("Filter");
        filterBtn.getStyleClass().add("secondary-button");
        Button clearFilterBtn = new Button("Clear");
        clearFilterBtn.getStyleClass().add("secondary-button");
        Button exportBtn = new Button("Export to Excel");
        exportBtn.getStyleClass().add("secondary-button");

        // FlowPane instead of HBox: an HBox never wraps, so on a narrower
        // window (or a maximized window on a smaller display) this row of
        // 6+ controls would run past the edge and everything after the cutoff
        // point becomes invisible. FlowPane wraps extra controls onto a new
        // line instead, so every label/field/button always stays visible.
        FlowPane filterBar = new FlowPane(10, 8, new Label("Category:"), categoryFilter,
                new Label("From:"), fromPicker, new Label("To:"), toPicker,
                new Label("Linked record:"), linkedRecordFilter, filterBtn, clearFilterBtn, exportBtn);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass().add("filter-bar");

        try {
            List<HealthRecord> records = new HealthRecordDAO().getRecordsByUser(user.getUserId());
            linkedRecordFilter.getItems().add(null);
            for (HealthRecord r : records) linkedRecordFilter.getItems().add(r.getRecordId());
        } catch (Exception ignored) { /* record list is a filter convenience only */ }

        ListView<MedicalDocument> listView = new ListView<>();
        listView.getStyleClass().add("app-list");
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MedicalDocument doc, boolean empty) {
                super.updateItem(doc, empty);
                if (empty || doc == null) {
                    setText(null);
                } else {
                    setText(doc.getFileName() + "  [" + doc.getCategory() + "]  (" + doc.getFileType().toUpperCase() +
                            ", " + (doc.getFileSize() / 1024) + " KB, uploaded " + doc.getUploadedAt() +
                            (doc.getLinkedRecordId() != null ? ", linked to record #" + doc.getLinkedRecordId() : "") + ")");
                }
            }
        });

        Runnable refresh = () -> {
            try {
                List<MedicalDocument> docs = new DocumentDAO().getDocumentsByUser(user.getUserId());
                listView.setItems(FXCollections.observableArrayList(docs));
                status.setText(docs.size() + " document(s).");
            } catch (Exception ex) {
                status.setText("Error loading documents: " + ex.getMessage());
            }
        };
        refresh.run();

        filterBtn.setOnAction(e -> {
            try {
                LocalDate from = fromPicker.getValue();
                LocalDate to = toPicker.getValue();
                List<MedicalDocument> docs = new DocumentDAO().search(user.getUserId(),
                        categoryFilter.getValue(), from, to, linkedRecordFilter.getValue());
                listView.setItems(FXCollections.observableArrayList(docs));
                status.setText(docs.size() + " document(s) match your filters.");
            } catch (Exception ex) {
                status.setText("Filter failed: " + ex.getMessage());
            }
        });

        clearFilterBtn.setOnAction(e -> {
            categoryFilter.setValue("All");
            fromPicker.setValue(null);
            toPicker.setValue(null);
            linkedRecordFilter.setValue(null);
            refresh.run();
        });

        exportBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("BioTrack_Documents_" + user.getUserId() + ".xlsx");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"));
            File dest = chooser.showSaveDialog(MainApp.primaryStage);
            if (dest != null) {
                try {
                    ExcelExportService.exportDocuments(listView.getItems(), dest);
                    status.setText("Exported to " + dest.getAbsolutePath());
                } catch (Exception ex) {
                    status.setText("Export failed: " + ex.getMessage());
                }
            }
        });

        // ---- Upload -----------------------------------------------------------
        ComboBox<String> uploadCategoryBox = new ComboBox<>(FXCollections.observableArrayList(MedicalDocument.CATEGORIES));
        uploadCategoryBox.setValue(MedicalDocument.CATEGORIES[0]);
        uploadCategoryBox.setPromptText("Category (required)");

        Button uploadBtn = new Button("Upload Document");
        uploadBtn.getStyleClass().add("primary-button");
        uploadBtn.setOnAction(e -> {
            if (uploadCategoryBox.getValue() == null) {
                status.setText("Please select a category before uploading.");
                return;
            }
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select a document");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    "Documents", "*.pdf", "*.jpg", "*.jpeg", "*.png", "*.doc", "*.docx"));
            File file = chooser.showOpenDialog(MainApp.primaryStage);
            if (file != null) {
                try {
                    byte[] data = Files.readAllBytes(file.toPath());
                    String name = file.getName();
                    String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
                    new DocumentDAO().insertDocument(user.getUserId(), name, ext, uploadCategoryBox.getValue(),
                            linkedRecordFilter.getValue(), data);
                    status.setText("Uploaded: " + name);
                    refresh.run();
                } catch (Exception ex) {
                    status.setText("Upload failed: " + ex.getMessage());
                }
            }
        });

        Button downloadBtn = new Button("Download Selected");
        downloadBtn.getStyleClass().add("secondary-button");
        downloadBtn.setOnAction(e -> {
            MedicalDocument selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                status.setText("Select a document first.");
                return;
            }
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName(selected.getFileName());
            File dest = chooser.showSaveDialog(MainApp.primaryStage);
            if (dest != null) {
                try {
                    byte[] data = new DocumentDAO().getDocumentData(selected.getDocId());
                    try (FileOutputStream fos = new FileOutputStream(dest)) {
                        fos.write(data);
                    }
                    status.setText("Saved to " + dest.getAbsolutePath());
                } catch (Exception ex) {
                    status.setText("Download failed: " + ex.getMessage());
                }
            }
        });

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> {
            MedicalDocument selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                status.setText("Select a document first.");
                return;
            }
            try {
                new DocumentDAO().deleteDocument(selected.getDocId());
                status.setText("Deleted " + selected.getFileName());
                refresh.run();
            } catch (Exception ex) {
                status.setText("Delete failed: " + ex.getMessage());
            }
        });

        FlowPane uploadRow = new FlowPane(10, 8, new Label("Upload category:"), uploadCategoryBox, uploadBtn, downloadBtn, deleteBtn);
        uploadRow.setAlignment(Pos.CENTER_LEFT);

        listView.setPrefHeight(320);
        VBox.setVgrow(listView, Priority.ALWAYS);

        root.getChildren().addAll(title, filterBar, uploadRow, listView, status);

        // Wrap in a ScrollPane so the status line and other content never get
        // clipped off the bottom when the window is shorter than the content
        // (e.g. lots of documents, or a smaller/maximized screen).
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-scroll");
        return scrollPane;
    }
}