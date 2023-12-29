package me.candiesjar.fallbackserver.utils;

import com.velocitypowered.api.plugin.PluginContainer;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboFactory;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualWorld;

@UtilityClass
public class WorldUtil {

    private final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();

    @Getter
    private Limbo fallbackWorld;

    @Getter
    private final LimboFactory factory = (LimboFactory) instance.getServer().getPluginManager().getPlugin("limboapi").flatMap(PluginContainer::getInstance).orElseThrow();

    public void createWorld() {

        VirtualWorld world = factory.createVirtualWorld(
                Dimension.valueOf("OVERWORLD"),
                0, 0, 0,
                (float) 90, (float) 90
        );

        boolean shouldJoin = VelocityConfig.RECONNECT_JOIN_LIMBO.get(Boolean.class);

        fallbackWorld = factory.createLimbo(world)
                .setName("FallbackLimbo")
                .setWorldTime(6000)
                .setShouldRejoin(shouldJoin)
                .setShouldRespawn(false);
    }

}
