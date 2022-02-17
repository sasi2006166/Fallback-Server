package me.candiesjar.fallbackserver.velocity.utils;

import org.simpleyaml.configuration.file.YamlFile;

import java.nio.file.Path;

public class ConfigurationUtil {

    public static YamlFile configFile;

    public static void saveConfiguration(Path path) {
        configFile = new YamlFile(path.toFile() + "/velocity-config.yml");
        try {
            if (!configFile.exists()) {
                configFile.createNewFile(true);
            }
            configFile.load();
            configFile.addDefault("Messages.not_player", "&cYou are not a player!");
            configFile.save();
            configFile.load();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static YamlFile getConfigFile() {
        return ConfigurationUtil.configFile;
    }

    public static void reloadConfig() {
        try {
            configFile.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
