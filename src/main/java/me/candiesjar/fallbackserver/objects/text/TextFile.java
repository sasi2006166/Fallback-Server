package me.candiesjar.fallbackserver.objects.text;

import lombok.SneakyThrows;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class TextFile {
    private final YamlFile yamlFile;

    @SneakyThrows
    public TextFile(Path path, String fileName) {
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }

        Path configPath = path.resolve(fileName);

        if (!Files.exists(configPath)) {
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
                Files.copy(Objects.requireNonNull(in), configPath);
            }
        }

        yamlFile = new YamlFile(configPath.toFile());
        yamlFile.load();

    }

    public YamlFile getConfig() {
        return yamlFile;
    }

    @SneakyThrows
    public void reload() {
        yamlFile.load();
    }
}
