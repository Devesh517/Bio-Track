package utils;

import config.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Locale;

/**
 * Handles the "Document" storage feature: copying uploaded files
 * (pdf/jpg/png/docx/txt...) into BioTrack's own managed folder,
 * organized per user, plus basic validation (type + 5MB size cap
 * to match the dashboard's "Allowed: jpg, png, pdf (Max 5MB)" note).
 */
public class FileUtil {

    private FileUtil() { }

    static {
        ensureDirectoriesExist();
    }

    public static void ensureDirectoriesExist() {
        try {
            Files.createDirectories(Paths.get(Constants.DOCUMENTS_DIR));
            Files.createDirectories(Paths.get(Constants.REPORTS_DIR));
            Files.createDirectories(Paths.get(Constants.EXCEL_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not create BioTrack data directories: " + e.getMessage(), e);
        }
    }

    public static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot == -1 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public static boolean isAllowedType(String fileName) {
        String ext = extensionOf(fileName);
        return Arrays.asList(Constants.ALLOWED_DOCUMENT_TYPES).contains(ext);
    }

    public static boolean isWithinSizeLimit(long sizeBytes) {
        return sizeBytes <= Constants.MAX_UPLOAD_SIZE_BYTES;
    }

    /**
     * Copies a source file into /BioTrackData/documents/{userId}/ , renaming it
     * with a timestamp to avoid collisions, and returns the stored path.
     */
    public static Path storeUserDocument(String userId, Path sourceFile) throws IOException {
        long size = Files.size(sourceFile);
        if (!isWithinSizeLimit(size)) {
            throw new IllegalArgumentException("File exceeds the 5MB upload limit.");
        }
        String originalName = sourceFile.getFileName().toString();
        if (!isAllowedType(originalName)) {
            throw new IllegalArgumentException("File type not allowed: ." + extensionOf(originalName));
        }

        Path userDir = Paths.get(Constants.DOCUMENTS_DIR, userId);
        Files.createDirectories(userDir);

        String storedName = DateUtil.fileTimestamp() + "_" + originalName;
        Path target = userDir.resolve(storedName);
        Files.copy(sourceFile, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    public static Path storeUserDocument(String userId, String originalFileName, InputStream data) throws IOException {
        if (!isAllowedType(originalFileName)) {
            throw new IllegalArgumentException("File type not allowed: ." + extensionOf(originalFileName));
        }
        Path userDir = Paths.get(Constants.DOCUMENTS_DIR, userId);
        Files.createDirectories(userDir);

        String storedName = DateUtil.fileTimestamp() + "_" + originalFileName;
        Path target = userDir.resolve(storedName);
        Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);

        if (!isWithinSizeLimit(Files.size(target))) {
            Files.deleteIfExists(target);
            throw new IllegalArgumentException("File exceeds the 5MB upload limit.");
        }
        return target;
    }

    public static double sizeInKb(Path path) throws IOException {
        return Math.round((Files.size(path) / 1024.0) * 100.0) / 100.0;
    }

    public static boolean deleteFile(String path) {
        try {
            return Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            return false;
        }
    }
}