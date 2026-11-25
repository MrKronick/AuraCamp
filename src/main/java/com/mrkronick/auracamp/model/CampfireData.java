package com.mrkronick.auracamp.model;

import org.bukkit.Location;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CampfireData {
    private final Location location;
    private final UUID owner;
    private int level;
    private int shieldHealth;
    private boolean shieldEnabled;

    private boolean hasRegen;
    private boolean regenActive;

    private boolean hasFireRes;
    private boolean fireResActive;

    private boolean hasMobProt;
    private boolean mobProtActive;

    private boolean hasFullProt;
    private boolean fullProtActive;

    private int radiusTier;
    private int memberTier;

    private Set<UUID> members;

    public CampfireData(Location location, UUID owner, int level, int shieldHealth, boolean shieldEnabled) {
        this.location = location;
        this.owner = owner;
        this.level = level;
        this.shieldHealth = shieldHealth;
        this.shieldEnabled = shieldEnabled;
        this.members = new HashSet<>();
        if (owner != null) {
            this.members.add(owner);
        }
    }

    public Location getLocation() { return location; }
    public UUID getOwner() { return owner; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getShieldHealth() { return shieldHealth; }
    public void setShieldHealth(int shieldHealth) { this.shieldHealth = shieldHealth; }
    public boolean isShieldEnabled() { return shieldEnabled; }
    public void setShieldEnabled(boolean shieldEnabled) { this.shieldEnabled = shieldEnabled; }

    public boolean hasRegen() { return hasRegen; }
    public void setHasRegen(boolean hasRegen) { this.hasRegen = hasRegen; }
    public boolean isRegenActive() { return regenActive; }
    public void setRegenActive(boolean regenActive) { this.regenActive = regenActive; }

    public boolean hasFireRes() { return hasFireRes; }
    public void setHasFireRes(boolean hasFireRes) { this.hasFireRes = hasFireRes; }
    public boolean isFireResActive() { return fireResActive; }
    public void setFireResActive(boolean fireResActive) { this.fireResActive = fireResActive; }

    public boolean hasMobProt() { return hasMobProt; }
    public void setHasMobProt(boolean hasMobProt) { this.hasMobProt = hasMobProt; }
    public boolean isMobProtActive() { return mobProtActive; }
    public void setMobProtActive(boolean mobProtActive) { this.mobProtActive = mobProtActive; }

    public boolean hasFullProt() { return hasFullProt; }
    public void setHasFullProt(boolean hasFullProt) { this.hasFullProt = hasFullProt; }
    public boolean isFullProtActive() { return fullProtActive; }
    public void setFullProtActive(boolean fullProtActive) { this.fullProtActive = fullProtActive; }

    public int getRadiusTier() { return radiusTier; }
    public void setRadiusTier(int radiusTier) { this.radiusTier = radiusTier; }

    public int getMemberTier() { return memberTier; }
    public void setMemberTier(int memberTier) { this.memberTier = memberTier; }

    public Set<UUID> getMembers() { return members; }
    public void setMembers(Set<UUID> members) { this.members = members; }

    public boolean isMember(UUID uuid) {
        return (owner != null && owner.equals(uuid)) || members.contains(uuid);
    }
}