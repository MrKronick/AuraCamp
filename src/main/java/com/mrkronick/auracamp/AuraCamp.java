package com.mrkronick.auracamp;

import com.mrkronick.auracamp.command.CampCommand;
import com.mrkronick.auracamp.config.ConfigManager;
import com.mrkronick.auracamp.config.LocaleManager;
import com.mrkronick.auracamp.listener.CampListener;
import com.mrkronick.auracamp.listener.CombatListener;
import com.mrkronick.auracamp.manager.CampManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class AuraCamp extends JavaPlugin {

    private ConfigManager configManager;
    private LocaleManager localeManager;
    private CampManager campManager;
    private CombatListener combatListener;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.localeManager = new LocaleManager(this);
        this.combatListener = new CombatListener(this);
        this.campManager = new CampManager(this);

        getServer().getPluginManager().registerEvents(new CampListener(this), this);
        getServer().getPluginManager().registerEvents(this.combatListener, this);

        PluginCommand cpCommand = getCommand("cp");
        if (cpCommand != null) {
            CampCommand executor = new CampCommand(this);
            cpCommand.setExecutor(executor);
            cpCommand.setTabCompleter(executor);
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LocaleManager getLocaleManager() {
        return localeManager;
    }

    public CampManager getCampManager() {
        return campManager;
    }

    public CombatListener getCombatManager() {
        return combatListener;
    }
}