package com.mrkronick.auracamp.listener;

import com.mrkronick.auracamp.AuraCamp;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatListener implements Listener {

    private final AuraCamp plugin;
    private final Map<UUID, Long> combatTimers = new ConcurrentHashMap<>();

    public CombatListener(AuraCamp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            tagPlayer(player);
        }
        if (event.getDamager() instanceof Player player) {
            tagPlayer(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        combatTimers.remove(event.getPlayer().getUniqueId());
    }

    public void tagPlayer(Player player) {
        int seconds = plugin.getConfig().getInt("pvp.combat-tag-seconds", 15);
        combatTimers.put(player.getUniqueId(), System.currentTimeMillis() + (seconds * 1000L));
    }

    public boolean isInCombat(Player player) {
        Long expireTime = combatTimers.get(player.getUniqueId());
        if (expireTime == null) {
            return false;
        }
        if (System.currentTimeMillis() > expireTime) {
            combatTimers.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public boolean isInCombat(UUID uuid) {
        Long expireTime = combatTimers.get(uuid);
        if (expireTime == null) {
            return false;
        }
        if (System.currentTimeMillis() > expireTime) {
            combatTimers.remove(uuid);
            return false;
        }
        return true;
    }

    public void notifyCombat(Player player) {
        Component msg = plugin.getLocaleManager().getRawMessageComponent("actionbar-combat");
        player.sendActionBar(msg);
    }
}