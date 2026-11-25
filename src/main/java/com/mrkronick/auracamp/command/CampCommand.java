package com.mrkronick.auracamp.command;

import com.mrkronick.auracamp.AuraCamp;
import com.mrkronick.auracamp.model.CampfireData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CampCommand implements CommandExecutor, TabCompleter {

    private final AuraCamp plugin;

    public CampCommand(AuraCamp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("team.usage"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("auracamp.admin") && !sender.isOp()) {
                sender.sendMessage(plugin.getLocaleManager().getMessage("no-permission"));
                return true;
            }
            plugin.reloadConfig();
            plugin.getLocaleManager().loadLocale();
            sender.sendMessage(plugin.getLocaleManager().getMessage("reload-success"));
            return true;
        }

        if (args[0].equalsIgnoreCase("team")) {
            if (!(sender instanceof Player player)) return true;

            if (args.length < 2) {
                player.sendMessage(plugin.getLocaleManager().getMessage("team.usage"));
                return true;
            }

            Block block = findNearestCampfire(player, 6);
            if (block == null || !plugin.getCampManager().isAuraCamp(block)) {
                player.sendMessage(plugin.getLocaleManager().getMessage("team.not-standing-near"));
                return true;
            }

            CampfireData data = plugin.getCampManager().getCampData(block);
            if (data == null) return true;

            String sub = args[1].toLowerCase();

            if (sub.equals("list")) {
                int max = plugin.getCampManager().getMaxMembers(data);
                player.sendMessage(plugin.getLocaleManager().getFormattedMessage("team.list-header",
                        "%count%", String.valueOf(data.getMembers().size()), "%max%", String.valueOf(max)));
                for (UUID memberId : data.getMembers()) {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(memberId);
                    String name = op.getName() != null ? op.getName() : memberId.toString();
                    player.sendMessage(plugin.getLocaleManager().getFormattedMessage("team.list-entry", "%player%", name));
                }
                return true;
            }

            if (data.getOwner() == null || !data.getOwner().equals(player.getUniqueId())) {
                player.sendMessage(plugin.getLocaleManager().getMessage("team.not-owner"));
                return true;
            }

            if (args.length < 3) {
                player.sendMessage(plugin.getLocaleManager().getMessage("team.usage"));
                return true;
            }

            if (sub.equals("add")) {
                Player target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    player.sendMessage(plugin.getLocaleManager().getMessage("team.player-not-found"));
                    return true;
                }
                if (data.isMember(target.getUniqueId())) {
                    player.sendMessage(plugin.getLocaleManager().getMessage("team.already-in-team"));
                    return true;
                }
                int max = plugin.getCampManager().getMaxMembers(data);
                if (data.getMembers().size() >= max) {
                    player.sendMessage(plugin.getLocaleManager().getFormattedMessage("team.limit-reached", "%max%", String.valueOf(max)));
                    return true;
                }

                data.getMembers().add(target.getUniqueId());
                plugin.getCampManager().saveCampData(data);
                player.sendMessage(plugin.getLocaleManager().getFormattedMessage("team.added", "%player%", target.getName()));
                return true;
            }

            if (sub.equals("remove")) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
                if (target.getUniqueId().equals(data.getOwner()) || !data.getMembers().contains(target.getUniqueId())) {
                    player.sendMessage(plugin.getLocaleManager().getMessage("team.not-in-team"));
                    return true;
                }

                data.getMembers().remove(target.getUniqueId());
                plugin.getCampManager().saveCampData(data);
                String name = target.getName() != null ? target.getName() : args[2];
                player.sendMessage(plugin.getLocaleManager().getFormattedMessage("team.removed", "%player%", name));
                return true;
            }
        }

        return true;
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> available = new ArrayList<>();
            available.add("team");
            if (sender.hasPermission("auracamp.admin") || sender.isOp()) {
                available.add("reload");
            }
            return filterCompletions(available, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("team")) {
            return filterCompletions(Arrays.asList("add", "remove", "list"), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("team") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            return filterCompletions(names, args[2]);
        }
        return new ArrayList<>();
    }

    private List<String> filterCompletions(List<String> options, String input) {
        List<String> result = new ArrayList<>();
        String lower = input.toLowerCase();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }
}