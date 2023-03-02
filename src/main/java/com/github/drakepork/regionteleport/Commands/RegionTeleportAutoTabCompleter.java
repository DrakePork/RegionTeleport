package com.github.drakepork.regionteleport.Commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import com.github.drakepork.regionteleport.RegionTeleport;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class RegionTeleportAutoTabCompleter implements TabCompleter {
	private final RegionTeleport plugin;

	public RegionTeleportAutoTabCompleter(final RegionTeleport plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			ArrayList<String> options = new ArrayList<>();
			ArrayList<String> commands = new ArrayList<>();
			if (command.getName().equalsIgnoreCase("regiontp")) {
				if (args.length == 1) {
					commands.add("teleport");
					commands.add("setspawn");
					commands.add("delspawn");
					commands.add("list");
					commands.add("help");
					commands.add("reload");
					StringUtil.copyPartialMatches(args[0], commands, options);
				} else if (args.length == 2) {
					String[] splitIds = args[1].split(",");
					switch (args[0].toLowerCase()) {
						case "tp":
						case "teleport":
							RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
							Map<String, ProtectedRegion> allRegions = rm.getRegions();
							Set<String> keys = allRegions.keySet();
							if(!allRegions.isEmpty()) {
								commands.addAll(keys);
							} else {
								commands.add("__global__");
							}
							break;
						case "delspawn":
							File spawnloc = new File(this.plugin.getDataFolder() + File.separator + "spawnlocations.yml");
							YamlConfiguration spawnConf = YamlConfiguration.loadConfiguration(spawnloc);
							commands.addAll(spawnConf.getKeys(false));
							break;
					}
					if(splitIds.length > 1 || args[1].endsWith(",")) {
						List<String> spawnIds = new ArrayList<>();
						List<String> allSpawns = new ArrayList<>();
						if(args[1].endsWith(",")) {
							spawnIds.addAll(commands);
						} else {
							StringUtil.copyPartialMatches(splitIds[splitIds.length - 1], commands, spawnIds);
						}

						for(String spawnId : spawnIds) {
							allSpawns.add(args[1].substring(0, args[1].lastIndexOf(",")) + "," + spawnId);
						}
						options.addAll(allSpawns);
					} else {
						StringUtil.copyPartialMatches(splitIds[splitIds.length - 1], commands, options);
					}
				} else if(args.length == 3) {
					String[] splitIds = args[2].split(",");
					switch (args[0].toLowerCase()) {
						case "tp":
						case "teleport":
							if(args[2].startsWith("cmi:") && plugin.cmiAddon != null) {
								commands.addAll(plugin.cmiAddon.warps());
							} else if(args[2].startsWith("ess:") && plugin.essAddon != null) {
								commands.addAll(plugin.essAddon.warps());
							} else {
								File spawnloc = new File(this.plugin.getDataFolder() + File.separator + "spawnlocations.yml");
								YamlConfiguration spawnConf = YamlConfiguration.loadConfiguration(spawnloc);
								commands.addAll(spawnConf.getKeys(false));
							}
							break;
					}

                    if (splitIds.length > 1 || args[2].endsWith(",")) {
                        List<String> spawnIds = new ArrayList<>();
                        List<String> allSpawns = new ArrayList<>();
                        if (args[2].endsWith(",") || args[2].endsWith(":")) {
                            spawnIds.addAll(commands);
                        } else {
                            StringUtil.copyPartialMatches(splitIds[splitIds.length - 1], commands, spawnIds);
                        }

                        for (String spawnId : spawnIds) {
                            allSpawns.add(args[2].substring(0, args[2].lastIndexOf(",")) + "," + spawnId);
                        }
                        options.addAll(allSpawns);
                    } else {
                        StringUtil.copyPartialMatches(splitIds[splitIds.length - 1], commands, options);
                    }
				} else if(args.length == 4) {
					if ("tp".equalsIgnoreCase(args[0])) {
						commands.add("-s");
					}
					StringUtil.copyPartialMatches(args[3], commands, options);
				}
			}
			Collections.sort(options);
			return options;
		}
		return null;
	}
}
