package me.candiesjar.fallbackserver.objects;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
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

        try {
            config = create();
        } catch (IOException e) {
            e.printStackTrace();
        }

        list.add(this);
    }

    private Configuration create() throws IOException {
        if (!file.exists()) {
            Files.copy(plugin.getResourceAsStream(path), file.toPath());
        }

        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    }

    public void recreate() {
        file.delete();

        try {
            create();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reloadAll() {
        list.forEach(TextFile::reload);
    }
}
