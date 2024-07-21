package me.candiesjar.fallbackserveraddon.utils.nms;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

@UtilityClass
public class ActionBarCreator {

    private static String nmsVersion;
    private static boolean useOldMethods = false;

    @SneakyThrows
    public static void sendActionBar(Player player, String message, FallbackServerAddon instance) {

        if (!player.isOnline()) {
            return;
        }

        if (instance.isPLIB()) {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SET_ACTION_BAR_TEXT);
            packet.getChatComponents().write(0, WrappedChatComponent.fromText(message));
            protocolManager.sendServerPacket(player, packet);
            return;
        }

        nmsVersion = instance.getServer().getClass().getPackage().getName();
        nmsVersion = nmsVersion.substring(nmsVersion.lastIndexOf(".") + 1);
        System.out.println("NMS Version: " + nmsVersion);

        if (nmsVersion.equalsIgnoreCase("craftbukkit")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
            return;
        }

        if (nmsVersion.equalsIgnoreCase("v1_8_R1") || nmsVersion.startsWith("v1_7_")) {
            useOldMethods = true;
        }

        Object packet = createActionBarPacket(message);
        sendPacket(player, packet);
    }

    private static Object createActionBarPacket(String message) throws Exception {
        Class<?> packetPlayOutChatClass = getNMSClass("PacketPlayOutChat");
        Object packet;

        if (useOldMethods) {
            packet = createOldActionBarPacket(message, packetPlayOutChatClass);
        } else {
            packet = createNewActionBarPacket(message, packetPlayOutChatClass);
        }

        return packet;
    }

    private static Object createOldActionBarPacket(String message, Class<?> packetPlayOutChatClass) throws Exception {
        Class<?> chatSerializerClass = getNMSClass("ChatSerializer");
        Class<?> iChatBaseComponentClass = getNMSClass("IChatBaseComponent");

        Method chatSerializerMethod = chatSerializerClass.getDeclaredMethod("a", String.class);
        Object cbc = iChatBaseComponentClass.cast(chatSerializerMethod.invoke(chatSerializerClass, "{\"text\": \"" + message + "\"}"));

        return packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, byte.class).newInstance(cbc, (byte) 2);
    }

    private static Object createNewActionBarPacket(String message, Class<?> packetPlayOutChatClass) throws Exception {
        Class<?> chatComponentTextClass = getNMSClass("ChatComponentText");
        Class<?> iChatBaseComponentClass = getNMSClass("IChatBaseComponent");

        try {
            Class<?> chatMessageTypeClass = getNMSClass("ChatMessageType");
            Object chatMessageType = getChatMessageType(chatMessageTypeClass);
            Object chatCompontentText = chatComponentTextClass.getConstructor(String.class).newInstance(message);
            return packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, chatMessageTypeClass).newInstance(chatCompontentText, chatMessageType);
        } catch (ClassNotFoundException exception) {
            Object chatCompontentText = chatComponentTextClass.getConstructor(String.class).newInstance(message);
            return packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, byte.class).newInstance(chatCompontentText, (byte) 2);
        }
    }

    private static Object getChatMessageType(Class<?> chatMessageTypeClass) {
        Object[] chatMessageTypes = chatMessageTypeClass.getEnumConstants();
        Object chatMessageType = null;
        for (Object obj : chatMessageTypes) {
            if (obj.toString().equals("GAME_INFO")) {
                chatMessageType = obj;
            }
        }
        return chatMessageType;
    }

    private static Object getCraftPlayerHandle(Object craftPlayer) throws Exception {
        Method craftPlayerHandleMethod = craftPlayer.getClass().getDeclaredMethod("getHandle");
        return craftPlayerHandleMethod.invoke(craftPlayer);
    }

    private static Class<?> getCraftPlayerClass() throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer");
    }

    private static Class<?> getNMSClass(String className) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + nmsVersion + "." + className);
    }

    private static void sendPacket(Player player, Object packet) throws Exception {
        Class<?> craftPlayerClass = getCraftPlayerClass();
        Object craftPlayer = craftPlayerClass.cast(player);
        Object craftPlayerHandle = getCraftPlayerHandle(craftPlayer);
        Object playerConnection = craftPlayerHandle.getClass().getDeclaredField("playerConnection").get(craftPlayerHandle);

        Method sendPacketMethod = playerConnection.getClass().getDeclaredMethod("sendPacket", getNMSClass("Packet"));
        sendPacketMethod.invoke(playerConnection, packet);
    }
}
