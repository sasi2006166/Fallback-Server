package me.candiesjar.fallbackserveraddon.listeners.standalone;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MessageListener implements PluginMessageListener {

    private final FallbackServerAddon plugin;

    public MessageListener(FallbackServerAddon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String s, @NotNull Player player, byte[] bytes) {
        if (s.equals("fs:reconnect")) {
            ByteArrayDataInput dataInput = ByteStreams.newDataInput(bytes);
            String receivedMessage = dataInput.readUTF();
            Player receivedPlayer = plugin.getServer().getPlayer(UUID.fromString(receivedMessage));
            if (receivedPlayer != null) receivedPlayer.kickPlayer("Lost Connection");
        }
    }
}
