package me.candiesjar.fallbackserver.hook.impl;

import me.candiesjar.fallbackserver.hook.api.IPluginHook;

public class AjQueue implements IPluginHook {
    @Override
    public String name() {
        return "";
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void register() {

    }

    @Override
    public void unregister() {

    }
}
