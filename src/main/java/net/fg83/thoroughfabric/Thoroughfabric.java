package net.fg83.thoroughfabric;

import net.fabricmc.api.ModInitializer;

public class Thoroughfabric implements ModInitializer {

    public static final String MOD_ID = "thoroughfabric";

    @Override
    public void onInitialize() {
        ConfigManager.loadConfig();
    }
}
