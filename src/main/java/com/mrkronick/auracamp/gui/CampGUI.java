package com.mrkronick.auracamp.gui;

import com.mrkronick.auracamp.AuraCamp;
import com.mrkronick.auracamp.model.CampfireData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CampGUI {

    private final AuraCamp plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public CampGUI(AuraCamp plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player, CampfireData data) {
        int level = data.getLevel();
        boolean useRp = plugin.getConfig().getBoolean("use-resource-pack", false);

        Component title;
        if (useRp) {
            title = plugin.getLocaleManager().getRawMessageComponent("gui.title-rp");
        } else {
            String rawTitle = plugin.getLocaleManager().getString("gui.title-vanilla").replace("%level%", String.valueOf(level));
            title = miniMessage.deserialize(rawTitle);
        }

        Inventory inv = Bukkit.createInventory(null, 27, title);
        fillBackground(inv);

        if (level < 3) {
            String matName = plugin.getConfig().getString("levels." + (level + 1) + ".upgrade-cost.material", "COPPER_INGOT");
            int amount = plugin.getConfig().getInt("levels." + (level + 1) + ".upgrade-cost.amount", 16);
            Material mat = Material.matchMaterial(matName);
            if (mat == null) mat = Material.COPPER_INGOT;

            ItemStack upgradeItem = new ItemStack(mat);
            ItemMeta upMeta = upgradeItem.getItemMeta();
            if (upMeta != null) {
                upMeta.displayName(plugin.getLocaleManager().getRawMessageComponent("gui.upgrade-name"));
                List<Component> lore = new ArrayList<>();
                for (String line : plugin.getLocaleManager().getStringList("gui.upgrade-lore")) {
                    String parsed = line.replace("%level%", String.valueOf(level))
                            .replace("%amount%", String.valueOf(amount))
                            .replace("%item%", mat.name());
                    lore.add(miniMessage.deserialize(parsed));
                }
                upMeta.lore(lore);
                upgradeItem.setItemMeta(upMeta);
            }
            inv.setItem(11, upgradeItem);
        } else {
            ItemStack shopBtn = new ItemStack(Material.NETHER_STAR);
            ItemMeta shopMeta = shopBtn.getItemMeta();
            if (shopMeta != null) {
                shopMeta.displayName(plugin.getLocaleManager().getRawMessageComponent("gui.shop-button-name"));
                List<Component> lore = new ArrayList<>();
                for (String line : plugin.getLocaleManager().getStringList("gui.shop-button-lore")) {
                    lore.add(miniMessage.deserialize(line));
                }
                shopMeta.lore(lore);
                shopBtn.setItemMeta(shopMeta);
            }
            inv.setItem(11, shopBtn);
        }

        ItemStack shieldItem = new ItemStack(data.isShieldEnabled() ? Material.SHIELD : Material.BARRIER);
        ItemMeta shieldMeta = shieldItem.getItemMeta();
        if (shieldMeta != null) {
            shieldMeta.displayName(plugin.getLocaleManager().getRawMessageComponent(data.isShieldEnabled() ? "gui.shield-on-name" : "gui.shield-off-name"));
            List<Component> shieldLore = new ArrayList<>();
            for (String line : plugin.getLocaleManager().getStringList("gui.shield-lore")) {
                shieldLore.add(miniMessage.deserialize(line));
            }
            shieldMeta.lore(shieldLore);
            shieldItem.setItemMeta(shieldMeta);
        }
        inv.setItem(13, shieldItem);

        if (level >= 3 && plugin.getConfig().getBoolean("levels.3.allow-compass-bind", true)) {
            ItemStack compassItem = new ItemStack(Material.COMPASS);
            ItemMeta compMeta = compassItem.getItemMeta();
            if (compMeta != null) {
                compMeta.displayName(plugin.getLocaleManager().getRawMessageComponent("gui.compass-name"));
                List<Component> compassLore = new ArrayList<>();
                for (String line : plugin.getLocaleManager().getStringList("gui.compass-lore")) {
                    compassLore.add(miniMessage.deserialize(line));
                }
                compMeta.lore(compassLore);
                compassItem.setItemMeta(compMeta);
            }
            inv.setItem(15, compassItem);
        }

        player.openInventory(inv);
    }

    public void openShopGUI(Player player, CampfireData data) {
        String rawTitle = plugin.getLocaleManager().getString("gui.title-shop");
        Inventory inv = Bukkit.createInventory(null, 27, miniMessage.deserialize(rawTitle));
        fillBackground(inv);

        int regenCost = plugin.getConfig().getInt("shop.effects.regeneration.cost", 5);
        inv.setItem(10, createShopItem(Material.GHAST_TEAR, "shop-items.regen-name", data.hasRegen(), data.isRegenActive(), regenCost));

        int fireCost = plugin.getConfig().getInt("shop.effects.fire-resistance.cost", 8);
        inv.setItem(11, createShopItem(Material.MAGMA_CREAM, "shop-items.fire-res-name", data.hasFireRes(), data.isFireResActive(), fireCost));

        int mobCost = plugin.getConfig().getInt("shop.protections.mob-protection.cost", 25);
        inv.setItem(12, createShopItem(Material.ZOMBIE_HEAD, "shop-items.mob-prot-name", data.hasMobProt(), data.isMobProtActive(), mobCost));

        int fullCost = plugin.getConfig().getInt("shop.protections.full-protection.cost", 64);
        inv.setItem(13, createShopItem(Material.TOTEM_OF_UNDYING, "shop-items.full-prot-name", data.hasFullProt(), data.isFullProtActive(), fullCost));

        int rTier = data.getRadiusTier();
        if (rTier < 3) {
            int nextTier = rTier + 1;
            int rCost = plugin.getConfig().getInt("shop.radius-upgrades.tier-" + nextTier + ".cost", 16);
            double nextRad = plugin.getConfig().getDouble("shop.radius-upgrades.tier-" + nextTier + ".radius", 24.0);
            String name = plugin.getLocaleManager().getString("shop-items.radius-name").replace("%radius%", String.valueOf((int) nextRad));
            inv.setItem(15, createTierItem(Material.ENDER_PEARL, name, rCost, false));
        } else {
            String name = plugin.getLocaleManager().getString("shop-items.radius-name").replace("%radius%", "100");
            inv.setItem(15, createTierItem(Material.ENDER_EYE, name, 0, true));
        }

        int mTier = data.getMemberTier();
        if (mTier < 2) {
            int nextTier = mTier + 1;
            int mCost = plugin.getConfig().getInt("shop.member-upgrades.tier-" + nextTier + ".cost", 50);
            int nextMax = plugin.getConfig().getInt("shop.member-upgrades.tier-" + nextTier + ".max-members", 12);
            String name = plugin.getLocaleManager().getString("shop-items.members-name").replace("%max%", String.valueOf(nextMax));
            inv.setItem(16, createTierItem(Material.PLAYER_HEAD, name, mCost, false));
        } else {
            String name = plugin.getLocaleManager().getString("shop-items.members-name").replace("%max%", "16");
            inv.setItem(16, createTierItem(Material.BEACON, name, 0, true));
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(plugin.getLocaleManager().getRawMessageComponent("gui.back-button"));
            back.setItemMeta(backMeta);
        }
        inv.setItem(22, back);

        player.openInventory(inv);
    }

    private ItemStack createShopItem(Material mat, String namePath, boolean unlocked, boolean active, int cost) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getLocaleManager().getRawMessageComponent(namePath));
            String statusRaw;
            if (!unlocked) {
                statusRaw = plugin.getLocaleManager().getString("shop-items.status-locked").replace("%cost%", String.valueOf(cost));
            } else {
                statusRaw = plugin.getLocaleManager().getString(active ? "shop-items.status-unlocked-active" : "shop-items.status-unlocked-inactive");
            }
            List<Component> lore = new ArrayList<>();
            for (String line : statusRaw.split("\n")) {
                lore.add(miniMessage.deserialize(line));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createTierItem(Material mat, String rawName, int cost, boolean maxed) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(rawName));
            String statusRaw = maxed
                    ? plugin.getLocaleManager().getString("shop-items.status-maxed")
                    : plugin.getLocaleManager().getString("shop-items.status-locked").replace("%cost%", String.valueOf(cost));
            List<Component> lore = new ArrayList<>();
            for (String line : statusRaw.split("\n")) {
                lore.add(miniMessage.deserialize(line));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillBackground(Inventory inv) {
        ItemStack background = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgMeta = background.getItemMeta();
        if (bgMeta != null) {
            bgMeta.displayName(Component.text(" "));
            background.setItemMeta(bgMeta);
        }
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, background);
        }
    }
}