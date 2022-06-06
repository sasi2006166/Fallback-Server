package me.candiesjar.fallbackserver.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@UtilityClass
public class FileUtils {

    @SneakyThrows
    public void downloadFile(String url, String name, File folder) {

        InputStream inputStream = new URL(url).openStream();

        Files.copy(inputStream, Paths.get(folder + "/" + name), StandardCopyOption.REPLACE_EXISTING);

        inputStream.close();

    }
}
