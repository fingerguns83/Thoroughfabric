package net.fg83.thoroughfabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages loading and saving of TFConfig configuration settings.
 */
public class ConfigManager {
    // Configuration file name
    private static final String CONFIG_FILE_NAME = "thoroughfabric.json";
    // Path to configuration file
    private static final Path CONFIG_FILE_PATH = Paths.get("config", CONFIG_FILE_NAME);
    // Gson instance for JSON parsing and formatting
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    // Cached configuration
    private static TFConfig config;

    /**
     * Retrieves the configuration, loading it if necessary.
     *
     * @return the TFConfig configuration
     */
    public static TFConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    /**
     * Loads the configuration from a JSON file.
     * If the file does not exist, a new configuration is created and saved.
     */
    public static void loadConfig() {
        File directory = CONFIG_FILE_PATH.getParent().toFile();
        if (!directory.exists()) {
            directory.mkdirs(); // Create config directory if it doesn't exist
        }

        if (Files.exists(CONFIG_FILE_PATH)) {
            try (FileReader reader = new FileReader(CONFIG_FILE_PATH.toFile())) {
                config = GSON.fromJson(reader, TFConfig.class); // Load existing config
            } catch (IOException e) {
                throw new RuntimeException("Could not read config file", e);
            }
        } else {
            config = new TFConfig(); // Create new config if file doesn't exist
            saveConfig();
        }
    }

    /**
     * Saves the current configuration to a JSON file.
     */
    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE_PATH.toFile())) {
            GSON.toJson(config, writer); // Write config to file
        } catch (IOException e) {
            throw new RuntimeException("Could not save config file", e);
        }
    }
}