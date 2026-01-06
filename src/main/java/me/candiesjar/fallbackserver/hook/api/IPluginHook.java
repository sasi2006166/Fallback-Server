package me.candiesjar.fallbackserver.hook.api;

public interface IPluginHook {
    String name();
    boolean isAvailable();
    void register();
    void unregister();
}