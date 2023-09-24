package me.candiesjar.fallbackserver.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class FilesUtils {

    public void downloadFile(String fileUrl, String targetDirectory) {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(fileUrl);
                Path targetPath = Paths.get(targetDirectory, Paths.get(url.getPath()).getFileName().toString());
                Files.copy(url.openStream(), targetPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @SneakyThrows
    public void deleteFile(String name, File folder) {

        File oldFile = new File(folder, name);

        if (!oldFile.exists()) {
            return;
        }

        Files.delete(oldFile.toPath());

    }
}
