package com.mrkronick.auracamp.config;

import com.mrkronick.auracamp.AuraCamp;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final AuraCamp plugin;

    public ConfigManager(AuraCamp plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }
}