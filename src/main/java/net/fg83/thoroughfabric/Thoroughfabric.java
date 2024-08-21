package net.fg83.thoroughfabric;

import net.fabricmc.api.ModInitializer;

public class Thoroughfabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ConfigManager.loadConfig();
    }
}
