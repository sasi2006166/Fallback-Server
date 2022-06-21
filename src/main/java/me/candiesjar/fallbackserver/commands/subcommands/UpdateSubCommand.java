package me.candiesjar.fallbackserver.commands.subcommands;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.utils.FileUtils;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

public class UpdateSubCommand implements SubCommand {

    private final FallbackServerBungee instance = FallbackServerBungee.getInstance();

    @Override
    public String getPermission() {
        return BungeeConfig.UPDATE_COMMAND_PERMISSION.getString();
    }

    @Override
    public boolean isEnabled() {
        return BungeeConfig.UPDATE_COMMAND.getBoolean();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {

        final String PLUGIN_VERSION = Utils.getRemoteVersion();
        final String PLUGIN_NAME = "FallbackServer-" + PLUGIN_VERSION + ".jar";
        final String OLD_JAR_NAME = "FallbackServer-" + instance.getVersion() + ".jar";
        final String UPDATE_URL = "https://github.com/sasi2006166/Fallback-Server/releases/download/" + PLUGIN_VERSION + "/" + PLUGIN_NAME;

        sender.sendMessage(new TextComponent("Update started..."));

        FileUtils.downloadFile(UPDATE_URL, PLUGIN_NAME, ProxyServer.getInstance().getPluginsFolder());

        FileUtils.deleteFile(OLD_JAR_NAME, ProxyServer.getInstance().getPluginsFolder());
        sender.sendMessage(new TextComponent("Deleted " + OLD_JAR_NAME));

        sender.sendMessage(new TextComponent("update finished.."));


    }
}
