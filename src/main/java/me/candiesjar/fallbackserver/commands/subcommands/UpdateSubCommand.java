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

        if (FallbackServerBungee.getInstance().isAlpha()) {
            sender.sendMessage(new TextComponent("§7Updater is disabled in §c§lALPHA§7!"));
            return;
        }

        String PLUGIN_VERSION = Utils.getRemoteVersion();
        String REMOTE_NAME = "FallbackServer-" + PLUGIN_VERSION + ".jar";
        String OLD_JAR_NAME = "FallbackServer-" + instance.getVersion() + ".jar";
        String UPDATE_URL = "https://github.com/sasi2006166/Fallback-Server/releases/download/" + PLUGIN_VERSION + "/" + REMOTE_NAME;

        sender.sendMessage(new TextComponent("Update started..."));

        FileUtils.downloadFile(UPDATE_URL, ProxyServer.getInstance().getPluginsFolder().getName());

        FallbackServerBungee.getInstance().setNeedsUpdate(true);

        sender.sendMessage(new TextComponent("update finished.."));


    }
}
