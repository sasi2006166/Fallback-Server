package me.candiesjar.fallbackserver.commands;

import me.candiesjar.fallbackserver.utils.Fields;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class HubCommand extends Command {

    public HubCommand() {
        super("hub");
    }

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(Fields.NOTPLAYER.getFormattedString());
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;
        if (p.getServer().getInfo().equals(ProxyServer.getInstance().getServerInfo(Fields.LOBBYSERVER.getString()))) {
            sender.sendMessage(new TextComponent(Fields.ALREADYINHUB.getFormattedString()));
            return;
        }
        p.connect(ProxyServer.getInstance().getServerInfo(Fields.LOBBYSERVER.getString()));
        p.sendMessage(new TextComponent(Fields.HUBCONNESSION.getFormattedString()));
    }
}


