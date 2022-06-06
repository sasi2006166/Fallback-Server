package me.candiesjar.fallbackserver;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

@Plugin(
        id = "fallbackservervelocity",
        name = "FallbackServerVelocity",
        version = "1.0-SKIDDED",
        url = "github.com/sasi2006166",
        authors = "Candies The Skidder"
)

public class FallbackServerVelocity {


    @Inject
    public FallbackServerVelocity(ProxyServer server, Logger logger, VelocityMetrics.Factory metricsFactory, @DataDirectory Path path) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.path = path;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
