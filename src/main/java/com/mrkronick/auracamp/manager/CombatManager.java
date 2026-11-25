package com.mrkronick.auracamp.manager;

import com.mrkronick.auracamp.AuraCamp;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {

    private final AuraCamp plugin;
    private final Map<UUID, Long> combatTags = new ConcurrentHashMap<>();

    public CombatManager(AuraCamp plugin) {
        this.plugin = plugin;
    }

    public void tagPlayer(UUID playerId) {
        int seconds = plugin.getConfig().getInt("pvp.combat-tag-seconds", 15);
        long expireTime = System.currentTimeMillis() + (seconds * 1000L);
        combatTags.put(playerId, expireTime);
    }

    public boolean isInCombat(UUID playerId) {
        Long expireTime = combatTags.get(playerId);
        if (expireTime == null) {
            return false;
        }
        if (System.currentTimeMillis() > expireTime) {
            combatTags.remove(playerId);
            return false;
        }
        return true;
    }

    public void untagPlayer(UUID playerId) {
        combatTags.remove(playerId);
    }
}