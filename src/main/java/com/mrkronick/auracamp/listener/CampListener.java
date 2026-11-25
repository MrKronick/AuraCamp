package com.mrkronick.auracamp.listener;

import com.mrkronick.auracamp.AuraCamp;
import com.mrkronick.auracamp.gui.CampGUI;
import com.mrkronick.auracamp.model.CampfireData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CampListener implements Listener {

    private final AuraCamp plugin;
    private final CampGUI campGUI;

    public CampListener(AuraCamp plugin) {
        this.plugin = plugin;
        this.campGUI = new CampGUI(plugin);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.CAMPFIRE || block.getType() == Material.SOUL_CAMPFIRE) {
            plugin.getCampManager().createCamp(block, event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (plugin.getCampManager().isAuraCamp(block)) {
            plugin.getCampManager().removeCamp(block.getLocation());
        }
    }

    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        if (plugin.getCampManager().isAuraCamp(block)) {
            plugin.getCampManager().removeCamp(block.getLocation());
        }
    }

    @EventHandler
    public void onMobTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        if (!(event.getTarget() instanceof Player player)) return;

        CampfireData data = plugin.getCampManager().getProtectingCamp(player);
        if (data != null) {
            boolean mobProt = (data.hasMobProt() && data.isMobProtActive()) || (data.hasFullProt() && data.isFullProtActive());
            if (mobProt) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onProtectedDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        CampfireData data = plugin.getCampManager().getProtectingCamp(victim);
        if (data != null && data.hasFullProt() && data.isFullProtActive()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        if (plugin.getCampManager().isAuraCamp(block) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            CampfireData data = plugin.getCampManager().getCampData(block);
            if (data == null) return;

            if (player.isSneaking()) {
                event.setCancelled(true);
                campGUI.openGUI(player, data);
            } else if (event.hasItem() && isExtinguishTool(event.getItem().getType())) {
                if (plugin.getConfig().getBoolean("pvp.allow-extinguish-enemy-camp", true)) {
                    if (!data.isMember(player.getUniqueId())) {
                        if (data.isShieldEnabled() && data.getShieldHealth() > 0) {
                            data.setShieldHealth(data.getShieldHealth() - 1);
                            plugin.getCampManager().saveCampData(data);
                            player.sendMessage(plugin.getLocaleManager().getMessage("enemy-extinguished"));
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    private boolean isExtinguishTool(Material mat) {
        return mat == Material.WATER_BUCKET || mat.name().endsWith("_SHOVEL");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String titleRp = plugin.getLocaleManager().getString("gui.title-rp");
        String currentTitle = ChatColor.stripColor(event.getView().getTitle());

        boolean isMainGUI = currentTitle.contains("Campfire Management") || currentTitle.contains("Управление костром") || currentTitle.contains(titleRp);
        boolean isShopGUI = currentTitle.contains("Campfire Shop") || currentTitle.contains("Магазин костра");

        if (!isMainGUI && !isShopGUI) return;

        event.setCancelled(true);
        if (event.getClickedInventory() != player.getOpenInventory().getTopInventory()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        Block block = findNearestCampfire(player, 6);
        if (block == null || !plugin.getCampManager().isAuraCamp(block)) {
            player.closeInventory();
            return;
        }

        CampfireData data = plugin.getCampManager().getCampData(block);
        if (data == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getSlot();

        if (isMainGUI) {
            if (slot == 11) {
                if (data.getLevel() < 3) {
                    int nextLevel = data.getLevel() + 1;
                    String matName = plugin.getConfig().getString("levels." + nextLevel + ".upgrade-cost.material", "COPPER_INGOT");
                    int amount = plugin.getConfig().getInt("levels." + nextLevel + ".upgrade-cost.amount", 16);
                    Material mat = Material.matchMaterial(matName);
                    if (mat == null) mat = Material.COPPER_INGOT;

                    if (player.getInventory().contains(mat, amount)) {
                        player.getInventory().removeItem(new ItemStack(mat, amount));
                        data.setLevel(nextLevel);
                        data.setShieldHealth(plugin.getCampManager().getMaxShieldForLevel(nextLevel));
                        plugin.getCampManager().saveCampData(data);

                        if (nextLevel == 3) {
                            player.sendMessage(plugin.getLocaleManager().getMessage("camp-max-unlocked"));
                        } else {
                            player.sendMessage(plugin.getLocaleManager().getFormattedMessage("camp-upgraded", "%level%", String.valueOf(nextLevel)));
                        }
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.4f);
                        campGUI.openGUI(player, data);
                    } else {
                        player.sendMessage(plugin.getLocaleManager().getFormattedMessage("not-enough-items", "%amount%", String.valueOf(amount), "%item%", matName));
                    }
                } else {
                    campGUI.openShopGUI(player, data);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                }
            }
            else if (slot == 13) {
                data.setShieldEnabled(!data.isShieldEnabled());
                plugin.getCampManager().saveCampData(data);
                player.sendMessage(plugin.getLocaleManager().getMessage(data.isShieldEnabled() ? "shield-toggled-on" : "shield-toggled-off"));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                campGUI.openGUI(player, data);
            }
            else if (slot == 15 && data.getLevel() >= 3) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType() == Material.COMPASS) {
                    ItemMeta meta = hand.getItemMeta();
                    if (meta != null) {
                        PersistentDataContainer pdc = meta.getPersistentDataContainer();
                        pdc.set(new NamespacedKey(plugin, "camp_loc"), PersistentDataType.STRING,
                                block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ());
                        hand.setItemMeta(meta);
                        player.sendMessage(plugin.getLocaleManager().getMessage("compass-bound"));
                        player.playSound(player.getLocation(), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 0.8f, 1.0f);
                        player.closeInventory();
                    }
                }
            }
        } else if (isShopGUI) {
            Material currency = Material.matchMaterial(plugin.getConfig().getString("shop.currency", "DIAMOND"));
            if (currency == null) currency = Material.DIAMOND;

            if (slot == 10) {
                if (!data.hasRegen()) {
                    int cost = plugin.getConfig().getInt("shop.effects.regeneration.cost", 5);
                    if (takeCurrency(player, currency, cost)) {
                        data.setHasRegen(true);
                        data.setRegenActive(true);
                        saveAndRefreshShop(player, data, "Regeneration");
                    }
                } else {
                    data.setRegenActive(!data.isRegenActive());
                    saveAndToggle(player, data);
                }
            }
            else if (slot == 11) {
                if (!data.hasFireRes()) {
                    int cost = plugin.getConfig().getInt("shop.effects.fire-resistance.cost", 8);
                    if (takeCurrency(player, currency, cost)) {
                        data.setHasFireRes(true);
                        data.setFireResActive(true);
                        saveAndRefreshShop(player, data, "Fire Resistance");
                    }
                } else {
                    data.setFireResActive(!data.isFireResActive());
                    saveAndToggle(player, data);
                }
            }
            else if (slot == 12) {
                if (!data.hasMobProt()) {
                    int cost = plugin.getConfig().getInt("shop.protections.mob-protection.cost", 25);
                    if (takeCurrency(player, currency, cost)) {
                        data.setHasMobProt(true);
                        data.setMobProtActive(true);
                        saveAndRefreshShop(player, data, "Anti-Mob Protection");
                    }
                } else {
                    data.setMobProtActive(!data.isMobProtActive());
                    saveAndToggle(player, data);
                }
            }
            else if (slot == 13) {
                if (!data.hasFullProt()) {
                    int cost = plugin.getConfig().getInt("shop.protections.full-protection.cost", 64);
                    if (takeCurrency(player, currency, cost)) {
                        data.setHasFullProt(true);
                        data.setFullProtActive(true);
                        saveAndRefreshShop(player, data, "Full Immunity");
                    }
                } else {
                    data.setFullProtActive(!data.isFullProtActive());
                    saveAndToggle(player, data);
                }
            }
            else if (slot == 15 && data.getRadiusTier() < 3) {
                int nextTier = data.getRadiusTier() + 1;
                int cost = plugin.getConfig().getInt("shop.radius-upgrades.tier-" + nextTier + ".cost", 16);
                if (takeCurrency(player, currency, cost)) {
                    data.setRadiusTier(nextTier);
                    saveAndRefreshShop(player, data, "Radius Tier " + nextTier);
                }
            }
            else if (slot == 16 && data.getMemberTier() < 2) {
                int nextTier = data.getMemberTier() + 1;
                int cost = plugin.getConfig().getInt("shop.member-upgrades.tier-" + nextTier + ".cost", 50);
                if (takeCurrency(player, currency, cost)) {
                    data.setMemberTier(nextTier);
                    saveAndRefreshShop(player, data, "Team Limit Tier " + nextTier);
                }
            }
            else if (slot == 22) {
                campGUI.openGUI(player, data);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            }
        }
    }

    private boolean takeCurrency(Player player, Material currency, int amount) {
        if (player.getInventory().contains(currency, amount)) {
            player.getInventory().removeItem(new ItemStack(currency, amount));
            return true;
        }
        player.sendMessage(plugin.getLocaleManager().getFormattedMessage("not-enough-items",
                "%amount%", String.valueOf(amount), "%item%", currency.name()));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f);
        return false;
    }

    private void saveAndRefreshShop(Player player, CampfireData data, String itemName) {
        plugin.getCampManager().saveCampData(data);
        player.sendMessage(plugin.getLocaleManager().getFormattedMessage("shop-purchased", "%item%", itemName));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
        campGUI.openShopGUI(player, data);
    }

    private void saveAndToggle(Player player, CampfireData data) {
        plugin.getCampManager().saveCampData(data);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
        campGUI.openShopGUI(player, data);
    }

    private Block findNearestCampfire(Player player, int radius) {
        Location loc = player.getLocation();
        int px = loc.getBlockX(), py = loc.getBlockY(), pz = loc.getBlockZ();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block b = loc.getWorld().getBlockAt(px + x, py + y, pz + z);
                    if (plugin.getCampManager().isAuraCamp(b)) return b;
                }
            }
        }
        return null;
    }
}