package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@UtilityClass
public class FileUtils {

    public void downloadFile(String url, String name, File folder) {

        try {
            InputStream inputStream = new URL(url).openStream();
            Files.copy(inputStream, Paths.get(folder + "/" + name), StandardCopyOption.REPLACE_EXISTING);
            inputStream.close();
        } catch (IOException e) {
            FallbackServerBungee.getInstance().getLogger().severe("Update failed!  " + e);
        }

    }

    public void deleteFile(String name, File folder) {

        File oldFile = new File(folder, name);

        if (oldFile.exists()) {
            oldFile.delete();
        }

    }
}
