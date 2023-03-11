package me.candiesjar.fallbackserver.utils;

import com.velocitypowered.api.plugin.PluginContainer;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboFactory;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualWorld;

@UtilityClass
public class WorldUtil {

    private final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();
    private final LimboFactory factory = (LimboFactory) instance.getServer().getPluginManager().getPlugin("limboapi").flatMap(PluginContainer::getInstance).orElseThrow();

    @Getter
    private Limbo fallbackWorld;

    public void createWorld() {

        VirtualWorld world = factory.createVirtualWorld(
                Dimension.valueOf("OVERWORLD"),
                0, 0, 0,
                (float) 90, (float) 90
        );

        fallbackWorld = factory.createLimbo(world)
                .setName("FallbackLimbo")
                .setWorldTime(6000)
                .setShouldRejoin(false)
                .setShouldRespawn(false);
    }

}
