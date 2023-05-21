package com.github.drakepork.regionteleport.Commands;

import com.github.drakepork.regionteleport.RegionTeleport;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegionClearTabCompleter implements TabCompleter {

    public RegionClearTabCompleter() {
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if(sender instanceof Player player) {
            ArrayList<String> options = new ArrayList<>();
            ArrayList<String> commands = new ArrayList<>();
            if(args.length == 1) {
                String[] splitIds = args[0].split(",");
                RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
                assert rm != null;
                Map<String, ProtectedRegion> allRegions = rm.getRegions();
                Set<String> keys = allRegions.keySet();
                if (!allRegions.isEmpty()) {
                    commands.addAll(keys);
                } else {
                    commands.add("__global__");
                }
                if (splitIds.length > 1 || args[0].endsWith(",")) {
                    List<String> spawnIds = new ArrayList<>();
                    List<String> allSpawns = new ArrayList<>();
                    if (args[0].endsWith(",")) {
                        spawnIds.addAll(commands);
                    } else {
                        StringUtil.copyPartialMatches(splitIds[splitIds.length - 1], commands, spawnIds);
                    }

                    for (String spawnId : spawnIds) {
                        allSpawns.add(args[0].substring(0, args[0].lastIndexOf(",")) + "," + spawnId);
                    }
                    options.addAll(allSpawns);
                } else {
                    StringUtil.copyPartialMatches(splitIds[splitIds.length - 1], commands, options);
                }
            } else if(args.length > 1) {
                commands.add("-all");
                commands.add("-monsters");
                commands.add("-animals");
                commands.add("-ambient");
                commands.add("-items");
                commands.add("-items:");
                commands.add("-vehicles");
                commands.add("-displays");
                commands.add("-specific:");
                commands.add("-npcs");
                commands.add("-npcs-only");
                commands.add("-tamed");
                commands.add("-tamed-only");
                commands.add("-named");
                commands.add("-named-only");
                commands.add("-named:");
                commands.add("-named-only:");
                StringUtil.copyPartialMatches(args[args.length - 1], commands, options);
            }
            return options;
        }
        return null;
    }
}
