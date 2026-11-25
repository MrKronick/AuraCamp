package com.mrkronick.auracamp.manager;

import com.mrkronick.auracamp.AuraCamp;
import com.mrkronick.auracamp.model.CampfireData;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.Campfire;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CampManager {

    private final AuraCamp plugin;
    private final NamespacedKey ownerKey, levelKey, shieldKey, shieldToggleKey;
    private final NamespacedKey shopFlagsKey, radiusTierKey, memberTierKey, membersKey;

    private final Set<Location> activeCamps = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastHealTime = new ConcurrentHashMap<>();

    public CampManager(AuraCamp plugin) {
        this.plugin = plugin;
        this.ownerKey = new NamespacedKey(plugin, "camp_owner");
        this.levelKey = new NamespacedKey(plugin, "camp_level");
        this.shieldKey = new NamespacedKey(plugin, "camp_shield");
        this.shieldToggleKey = new NamespacedKey(plugin, "camp_shield_toggle");
        this.shopFlagsKey = new NamespacedKey(plugin, "camp_shop_flags");
        this.radiusTierKey = new NamespacedKey(plugin, "camp_radius_tier");
        this.memberTierKey = new NamespacedKey(plugin, "camp_member_tier");
        this.membersKey = new NamespacedKey(plugin, "camp_members");
        startAuraTask();
    }

    public boolean isAuraCamp(Block block) {
        if (block == null || (block.getType() != Material.CAMPFIRE && block.getType() != Material.SOUL_CAMPFIRE)) {
            return false;
        }
        if (!(block.getState() instanceof Campfire state)) {
            return false;
        }
        return state.getPersistentDataContainer().has(levelKey, PersistentDataType.INTEGER);
    }

    public CampfireData getCampData(Block block) {
        if (!isAuraCamp(block)) return null;

        Campfire state = (Campfire) block.getState();
        PersistentDataContainer pdc = state.getPersistentDataContainer();

        String ownerStr = pdc.get(ownerKey, PersistentDataType.STRING);
        UUID owner = ownerStr != null ? UUID.fromString(ownerStr) : null;
        int level = pdc.getOrDefault(levelKey, PersistentDataType.INTEGER, 1);
        int shield = pdc.getOrDefault(shieldKey, PersistentDataType.INTEGER, 0);
        boolean shieldEnabled = pdc.getOrDefault(shieldToggleKey, PersistentDataType.BYTE, (byte) 1) == 1;

        CampfireData data = new CampfireData(block.getLocation(), owner, level, shield, shieldEnabled);
        data.setRadiusTier(pdc.getOrDefault(radiusTierKey, PersistentDataType.INTEGER, 0));
        data.setMemberTier(pdc.getOrDefault(memberTierKey, PersistentDataType.INTEGER, 0));

        int flags = pdc.getOrDefault(shopFlagsKey, PersistentDataType.INTEGER, 0);
        data.setHasRegen((flags & 1) != 0);
        data.setRegenActive((flags & 2) != 0);
        data.setHasFireRes((flags & 4) != 0);
        data.setFireResActive((flags & 8) != 0);
        data.setHasMobProt((flags & 16) != 0);
        data.setMobProtActive((flags & 32) != 0);
        data.setHasFullProt((flags & 64) != 0);
        data.setFullProtActive((flags & 128) != 0);

        String membersRaw = pdc.getOrDefault(membersKey, PersistentDataType.STRING, "");
        Set<UUID> members = new HashSet<>();
        if (owner != null) members.add(owner);
        if (!membersRaw.isEmpty()) {
            for (String s : membersRaw.split(",")) {
                try {
                    members.add(UUID.fromString(s));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        data.setMembers(members);

        return data;
    }

    public void createCamp(Block block, UUID owner) {
        if (!(block.getState() instanceof Campfire state)) return;
        PersistentDataContainer pdc = state.getPersistentDataContainer();
        if (owner != null) {
            pdc.set(ownerKey, PersistentDataType.STRING, owner.toString());
            pdc.set(membersKey, PersistentDataType.STRING, owner.toString());
        }
        pdc.set(levelKey, PersistentDataType.INTEGER, 1);
        pdc.set(shieldKey, PersistentDataType.INTEGER, getMaxShieldForLevel(1));
        pdc.set(shieldToggleKey, PersistentDataType.BYTE, (byte) 1);
        pdc.set(shopFlagsKey, PersistentDataType.INTEGER, 0);
        pdc.set(radiusTierKey, PersistentDataType.INTEGER, 0);
        pdc.set(memberTierKey, PersistentDataType.INTEGER, 0);
        state.update();

        activeCamps.add(block.getLocation());
    }

    public void saveCampData(CampfireData data) {
        Block block = data.getLocation().getBlock();
        if (!(block.getState() instanceof Campfire state)) return;

        PersistentDataContainer pdc = state.getPersistentDataContainer();
        if (data.getOwner() != null) {
            pdc.set(ownerKey, PersistentDataType.STRING, data.getOwner().toString());
        }
        pdc.set(levelKey, PersistentDataType.INTEGER, data.getLevel());
        pdc.set(shieldKey, PersistentDataType.INTEGER, data.getShieldHealth());
        pdc.set(shieldToggleKey, PersistentDataType.BYTE, (byte) (data.isShieldEnabled() ? 1 : 0));
        pdc.set(radiusTierKey, PersistentDataType.INTEGER, data.getRadiusTier());
        pdc.set(memberTierKey, PersistentDataType.INTEGER, data.getMemberTier());

        int flags = 0;
        if (data.hasRegen()) flags |= 1;
        if (data.isRegenActive()) flags |= 2;
        if (data.hasFireRes()) flags |= 4;
        if (data.isFireResActive()) flags |= 8;
        if (data.hasMobProt()) flags |= 16;
        if (data.isMobProtActive()) flags |= 32;
        if (data.hasFullProt()) flags |= 64;
        if (data.isFullProtActive()) flags |= 128;
        pdc.set(shopFlagsKey, PersistentDataType.INTEGER, flags);

        String joinedMembers = data.getMembers().stream().map(UUID::toString).collect(Collectors.joining(","));
        pdc.set(membersKey, PersistentDataType.STRING, joinedMembers);

        state.update();
        activeCamps.add(block.getLocation());
    }

    public void removeCamp(Location location) {
        activeCamps.remove(location);
    }

    public Set<Location> getActiveCamps() {
        return activeCamps;
    }

    public double getEffectiveRadius(CampfireData data) {
        if (data.getLevel() < 3 || data.getRadiusTier() == 0) {
            return plugin.getConfig().getDouble("levels." + data.getLevel() + ".radius", 6.0);
        }
        return switch (data.getRadiusTier()) {
            case 1 -> plugin.getConfig().getDouble("shop.radius-upgrades.tier-1.radius", 24.0);
            case 2 -> plugin.getConfig().getDouble("shop.radius-upgrades.tier-2.radius", 42.0);
            case 3 -> plugin.getConfig().getDouble("shop.radius-upgrades.tier-3.radius", 100.0);
            default -> 18.0;
        };
    }

    public int getMaxMembers(CampfireData data) {
        if (data.getLevel() < 3 || data.getMemberTier() == 0) {
            return plugin.getConfig().getInt("levels." + data.getLevel() + ".max-members", 3);
        }
        return switch (data.getMemberTier()) {
            case 1 -> plugin.getConfig().getInt("shop.member-upgrades.tier-1.max-members", 12);
            case 2 -> plugin.getConfig().getInt("shop.member-upgrades.tier-2.max-members", 16);
            default -> 8;
        };
    }

    public int getMaxShieldForLevel(int level) {
        return switch (level) {
            case 2 -> 3;
            case 3 -> 5;
            default -> 0;
        };
    }

    public CampfireData getProtectingCamp(Player player) {
        Location pLoc = player.getLocation();
        for (Location loc : activeCamps) {
            if (loc.getWorld() != null && loc.getWorld().equals(pLoc.getWorld())) {
                Block block = loc.getBlock();
                if (isAuraCamp(block)) {
                    CampfireData data = getCampData(block);
                    if (data != null && data.getLevel() >= 3 && data.isMember(player.getUniqueId())) {
                        double r = getEffectiveRadius(data);
                        if (Math.abs(pLoc.getX() - loc.getX()) <= r && Math.abs(pLoc.getZ() - loc.getZ()) <= r) {
                            return data;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void sendPlayerMessage(Player player, String path, boolean isActionbar) {
        String mode = plugin.getConfig().getString("message-output", "BOTH").toUpperCase();
        Component msg = isActionbar
                ? plugin.getLocaleManager().getRawMessageComponent(path)
                : plugin.getLocaleManager().getMessage(path);

        if (isActionbar) {
            if (mode.equals("ACTIONBAR") || mode.equals("BOTH")) {
                player.sendActionBar(msg);
            }
        } else {
            if (mode.equals("CHAT") || mode.equals("BOTH")) {
                player.sendMessage(msg);
            } else if (mode.equals("ACTIONBAR")) {
                player.sendActionBar(msg);
            }
        }
    }

    private void startAuraTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Location loc : activeCamps) {
                World world = loc.getWorld();
                if (world == null || !world.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) continue;

                Block block = loc.getBlock();
                if (!isAuraCamp(block)) {
                    activeCamps.remove(loc);
                    continue;
                }

                if (!(block.getBlockData() instanceof org.bukkit.block.data.type.Campfire campData) || !campData.isLit()) {
                    continue;
                }

                CampfireData data = getCampData(block);
                if (data == null) continue;

                int level = data.getLevel();
                double radius = getEffectiveRadius(data);
                double healAmount = plugin.getConfig().getDouble("levels." + level + ".heal-amount", 1.0);
                long intervalMillis = plugin.getConfig().getLong("levels." + level + ".heal-interval-seconds", 4) * 1000L;

                spawnAuraParticles(loc, level, Math.min(radius, 25.0));

                long now = System.currentTimeMillis();
                for (Player player : world.getPlayers()) {
                    if (!data.isMember(player.getUniqueId())) continue;

                    if (Math.abs(player.getLocation().getX() - loc.getX()) <= radius &&
                            Math.abs(player.getLocation().getZ() - loc.getZ()) <= radius &&
                            Math.abs(player.getLocation().getY() - loc.getY()) <= radius) {
                        processPlayerInAura(player, data, healAmount, intervalMillis, now);
                    }
                }
            }
        }, 20L, 20L);
    }

    private void processPlayerInAura(Player player, CampfireData data, double healAmount, long intervalMillis, long now) {
        if (player.isDead() || player.getHealth() <= 0) return;

        if (plugin.getCombatManager() != null && plugin.getCombatManager().isInCombat(player.getUniqueId())) {
            if (plugin.getConfig().getBoolean("pvp.disable-healing-in-combat", true)) {
                sendPlayerMessage(player, "actionbar-combat", true);
                return;
            }
        }

        if (data.getLevel() < 3) {
            String effectName = plugin.getConfig().getString("levels." + data.getLevel() + ".effect", "");
            int amplifier = plugin.getConfig().getInt("levels." + data.getLevel() + ".effect-amplifier", 0);
            PotionEffectType effectType = PotionEffectType.getByName(effectName);
            if (effectType != null) {
                player.addPotionEffect(new PotionEffect(effectType, 80, amplifier, true, false, true));
            }
        } else {
            if (data.hasRegen() && data.isRegenActive()) {
                int amp = plugin.getConfig().getInt("shop.effects.regeneration.amplifier", 0);
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, amp, true, false, true));
            }
            if (data.hasFireRes() && data.isFireResActive()) {
                int amp = plugin.getConfig().getInt("shop.effects.fire-resistance.amplifier", 0);
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, amp, true, false, true));
            }
        }

        long last = lastHealTime.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < intervalMillis) return;

        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;

        if (player.getHealth() < maxHealth) {
            double newHealth = Math.min(maxHealth, player.getHealth() + healAmount);
            player.setHealth(newHealth);
            sendPlayerMessage(player, "actionbar-healing", true);
            player.playSound(player.getLocation(), Sound.BLOCK_CAMPFIRE_CRACKLE, 0.4f, 1.2f);
            lastHealTime.put(player.getUniqueId(), now);
        }
    }

    private void spawnAuraParticles(Location center, int level, double radius) {
        World world = center.getWorld();
        if (world == null) return;
        Particle particle = switch (level) {
            case 2 -> Particle.SOUL_FIRE_FLAME;
            case 3 -> Particle.END_ROD;
            default -> Particle.FLAME;
        };

        for (int i = 0; i < 8; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double x = center.getX() + 0.5 + radius * Math.cos(angle);
            double z = center.getZ() + 0.5 + radius * Math.sin(angle);
            world.spawnParticle(particle, x, center.getY() + 0.2, z, 1, 0, 0.05, 0, 0.01);
        }
    }
}