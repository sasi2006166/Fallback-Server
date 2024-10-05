package me.candiesjar.fallbackserver.objects.text;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Getter
public class TextFile {
    private final Plugin plugin;
    private final String path;

    private Configuration config;
    private final File file;

    private static final List<TextFile> list = Lists.newArrayList();

    public TextFile(Plugin plugin, String path) {
        this.plugin = plugin;
        this.path = path;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        file = new File(plugin.getDataFolder(), path);

        config = create();

        list.add(this);
    }

    @SneakyThrows
    private Configuration create() {
        if (!file.exists()) {
            Files.copy(plugin.getResourceAsStream(path), file.toPath());
        }

        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    }

    @SneakyThrows
    public void reload() {
        config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    }

    @SneakyThrows
    public void save() {
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
    }

    public static void reloadAll() {
        list.forEach(TextFile::reload);
    }
}
