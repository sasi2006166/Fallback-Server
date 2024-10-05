package me.candiesjar.fallbackserver.utils;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboFactory;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualWorld;
import net.elytrium.limboapi.api.file.BuiltInWorldFileType;
import net.elytrium.limboapi.api.file.WorldFile;
import net.elytrium.limboapi.api.player.GameMode;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class WorldUtil {

    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();

    @Getter
    private Limbo fallbackLimbo;

    @Getter
    private final LimboFactory factory = (LimboFactory) fallbackServerVelocity.getServer()
            .getPluginManager()
            .getPlugin("limboapi")
            .flatMap(PluginContainer::getInstance)
            .orElseThrow();

    @SneakyThrows
    public void createLimbo() {
        int x = VelocityConfig.RECONNECT_LIMBO_X.get(Integer.class);
        int y = VelocityConfig.RECONNECT_LIMBO_Y.get(Integer.class);
        int z = VelocityConfig.RECONNECT_LIMBO_Z.get(Integer.class);
        float yaw = (float) VelocityConfig.RECONNECT_LIMBO_YAW.get(Integer.class);
        float pitch = (float) VelocityConfig.RECONNECT_LIMBO_PITCH.get(Integer.class);
        String worldDimension = VelocityConfig.RECONNECT_LIMBO_DIMENSION.get(String.class);

        switch (worldDimension) {
            case "OVERWORLD":
            case "NETHER":
            case "THE_END":
                break;
            default:
                Utils.printDebug("Invalid dimension, defaulting to OVERWORLD", true);
                worldDimension = "OVERWORLD";
                break;
        }

        VirtualWorld world = factory.createVirtualWorld(
                Dimension.valueOf(worldDimension), x, y, z, yaw, pitch
        );

        boolean useSchematic = VelocityConfig.RECONNECT_USE_SCHEMATIC.get(Boolean.class);

        if (useSchematic) {
            loadSchematic(world);
        }

        String name = VelocityConfig.RECONNECT_LIMBO_NAME.get(String.class);
        GameMode gameMode = getGameMode(VelocityConfig.RECONNECT_LIMBO_GAMEMODE.get(String.class));
        int worldTime = VelocityConfig.RECONNECT_LIMBO_WORLD_TIME.get(Integer.class);
        boolean shouldJoin = VelocityConfig.RECONNECT_JOIN_LIMBO.get(Boolean.class);

        fallbackLimbo = factory.createLimbo(world)
                .setName(name)
                .setWorldTime(worldTime)
                .setShouldRejoin(shouldJoin)
                .setGameMode(gameMode)
                .setShouldRespawn(true);

        ServerInfo serverInfo = new ServerInfo("FallbackLimbo", InetSocketAddress.createUnresolved("0.0.0.0", 12345));
        fallbackServerVelocity.getServer().registerServer(serverInfo);
    }

    @SneakyThrows
    private void loadSchematic(VirtualWorld world) {
        File schematic = new File(fallbackServerVelocity.getPath() + "/schematics");

        if (!Files.exists(schematic.toPath())) {
            Files.createDirectory(schematic.toPath());
        }

        schematic = new File(schematic, VelocityConfig.RECONNECT_SCHEMATIC_NAME.get(String.class));
        fallbackServerVelocity.getComponentLogger().info(fallbackServerVelocity.getMiniMessage().deserialize("<gray>[<aqua>!<gray>] Loading schematic: " + schematic));

        Path path = schematic.toPath();

        if (!Files.exists(path)) {
            Utils.printDebug("Schematic not found", true);
            Utils.printDebug("Please add your schematic to the 'schematics' folder", true);
        } else {
            String fileName = schematic.getName();
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();

            WorldFile worldFile;

            switch (fileExtension) {
                case "SCHEMATIC":
                    fallbackServerVelocity.getComponentLogger().info(fallbackServerVelocity.getMiniMessage().deserialize("<gray>[<aqua>!<gray>] Loading schematic as a SCHEMATIC file"));
                    worldFile = factory.openWorldFile(BuiltInWorldFileType.SCHEMATIC, path);
                    break;
                case "SCHEM":
                    fallbackServerVelocity.getComponentLogger().info(fallbackServerVelocity.getMiniMessage().deserialize("<gray>[<aqua>!<gray>] Loading schematic as a WORLDEDIT_SCHEM file"));
                    worldFile = factory.openWorldFile(BuiltInWorldFileType.WORLDEDIT_SCHEM, path);
                    break;
                case "STRUCTURE":
                    fallbackServerVelocity.getComponentLogger().info(fallbackServerVelocity.getMiniMessage().deserialize("<gray>[<aqua>!<gray>] Loading schematic as a STRUCTURE file"));
                    worldFile = factory.openWorldFile(BuiltInWorldFileType.STRUCTURE, path);
                    break;
                default:
                    Utils.printDebug("Invalid schematic file", true);
                    Utils.printDebug("Please add a valid schematic file", true);
                    worldFile = null;
                    break;
            }

            if (worldFile != null) {
                int schematicX = VelocityConfig.RECONNECT_SCHEMATIC_X.get(Integer.class);
                int schematicY = VelocityConfig.RECONNECT_SCHEMATIC_Y.get(Integer.class);
                int schematicZ = VelocityConfig.RECONNECT_SCHEMATIC_Z.get(Integer.class);
                worldFile.toWorld(factory, world, schematicX, schematicY, schematicZ);

                fallbackServerVelocity.getComponentLogger().info(fallbackServerVelocity.getMiniMessage().deserialize("<gray>[<aqua>!<gray>] Schematic has been pasted at: " + schematicX + " " + schematicY + " " + schematicZ));
            }
        }
    }

    private GameMode getGameMode(String mode) {
        switch (mode) {
            case "CREATIVE":
                return GameMode.CREATIVE;
            case "ADVENTURE":
                return GameMode.ADVENTURE;
            case "SPECTATOR":
                return GameMode.SPECTATOR;
            default:
                return GameMode.SURVIVAL;
        }
    }
}


