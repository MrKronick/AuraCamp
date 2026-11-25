package com.mrkronick.auracamp.config;

import com.mrkronick.auracamp.AuraCamp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LocaleManager {

    private final AuraCamp plugin;
    private FileConfiguration localeConfig;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public LocaleManager(AuraCamp plugin) {
        this.plugin = plugin;
        loadLocale();
    }

    public void loadLocale() {
        String lang = plugin.getConfig().getString("language", "en_US");
        File localeFile = new File(plugin.getDataFolder() + "/locales", lang + ".yml");

        if (!localeFile.exists()) {
            localeFile.getParentFile().mkdirs();
            plugin.saveResource("locales/" + lang + ".yml", false);
        }

        localeConfig = YamlConfiguration.loadConfiguration(localeFile);

        InputStream defaultStream = plugin.getResource("locales/" + lang + ".yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            localeConfig.setDefaults(defaultConfig);
        }
    }

    public Component getMessage(String path) {
        String prefix = localeConfig.getString("prefix", "");
        String message = localeConfig.getString(path, "Missing: " + path);
        return miniMessage.deserialize(prefix + message);
    }

    public Component getRawMessageComponent(String path) {
        String message = localeConfig.getString(path, "Missing: " + path);
        try {
            return miniMessage.deserialize(message);
        } catch (Exception e) {
            return Component.text(message);
        }
    }

    public Component getFormattedMessage(String path, String... replacements) {
        String prefix = localeConfig.getString("prefix", "");
        String message = localeConfig.getString(path, "Missing: " + path);

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return miniMessage.deserialize(prefix + message);
    }

    public String getString(String path) {
        return localeConfig.getString(path, "");
    }

    public List<String> getStringList(String path) {
        return localeConfig.getStringList(path);
    }
}