package com.github.drakepork.regionteleport.commands;

import com.github.drakepork.regionteleport.RegionTeleport;
import com.github.drakepork.regionteleport.utils.LangCreator;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

import static com.github.drakepork.regionteleport.RegionTeleport.*;

public class RegionTeleportCommands implements CommandExecutor {
	private final RegionTeleport plugin;

	public RegionTeleportCommands(final RegionTeleport plugin) {
		this.plugin = plugin;
	}

	public static Location getLocation(String spawnId, String addon) {
		if(addon.isEmpty()) {
			YamlConfiguration spawnConf = YamlConfiguration.loadConfiguration(spawnLoc);
			World w = Bukkit.getServer().getWorld(Objects.requireNonNull(spawnConf.getString(spawnId + ".world")));
			float yaw = (float) spawnConf.getDouble(spawnId + ".yaw");
			float pitch = (float) spawnConf.getDouble(spawnId + ".pitch");
			double x = spawnConf.getDouble(spawnId + ".x");
			double y = spawnConf.getDouble(spawnId + ".y");
			double z = spawnConf.getDouble(spawnId + ".z");

			return new Location(w, x, y, z, yaw, pitch);
		} else if(addon.equalsIgnoreCase("cmi")) {
			return cmiAddon.warpLoc(spawnId);
		} else if(addon.equalsIgnoreCase("ess") || addon.equalsIgnoreCase("essentials")) {
			return essAddon.warpLoc(spawnId);
		}
		return null;
	}

	public boolean isInRegion(Player player, List<String> regionIds) {
		if (regionIds.contains("__global__")) {
			return true;
		} else {
			Location playerLoc = player.getLocation();
			BlockVector3 v = BlockVector3.at(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ());
			World world = player.getWorld();
			RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
			if(rm != null) {
				ApplicableRegionSet set = rm.getApplicableRegions(v);
				for (ProtectedRegion r : set) {
					if (regionIds.contains(r.getId())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public List<String> getInvalidRegions(World world, List<String> regionIds) {
		List<String> falseRegions = new ArrayList<>();
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(BukkitAdapter.adapt(world));
		assert regions != null;
		for (String region : regionIds) {
			if (!regions.hasRegion(region) && !region.equalsIgnoreCase("__global__")) {
				falseRegions.add(region);
			} else if (region.equalsIgnoreCase("__global__")) {
				falseRegions = new ArrayList<>();
				break;
			}
		}
		return falseRegions;
	}

	public static List<String> getSpawns(String spawns) {
		if (spawns.contains(":") && spawns.split(":").length > 1) {
			spawns = spawns.split(":")[1];
		}
		return new ArrayList<>(Arrays.asList(spawns.split(",")));
	}

	public static String getAddon(String spawns) {
		if(spawns.contains(":")) {
			return spawns.split(":")[0];
		}
		return "";
	}

	public static List<String> getInvalidSpawns(List<String> spawns, String addon) {
		List<String> invalidSpawns = new ArrayList<>();
		if(addon.isEmpty()) {
			YamlConfiguration spawnConf = YamlConfiguration.loadConfiguration(spawnLoc);
			spawns.forEach(spawn -> {
				if (!spawnConf.contains(spawn)) invalidSpawns.add(spawn);
			});
		} else if(addon.equalsIgnoreCase("cmi")) {
			spawns.forEach(spawn -> {
				if (cmiAddon.warpLoc(spawn) == null) invalidSpawns.add(spawn);
			});
		} else if(addon.equalsIgnoreCase("ess") || addon.equalsIgnoreCase("essentials")) {
			spawns.forEach(spawn -> {
				if (!essAddon.isWarp(spawn)) invalidSpawns.add(spawn);
			});
		}
		return invalidSpawns;
	}

	public static String isAddonDisabled(String addon) {
		if(!addon.isEmpty()) {
			switch (addon.toLowerCase()) {
				case "cmi" -> {
					if (cmiAddon == null) {
						return colourMessage(Objects.requireNonNull(LangCreator.langConf.getString("addon.disabled")).replaceAll("\\[name]", addon));
					}
				}
				case "essentials", "ess" -> {
					if (essAddon == null) {
						return colourMessage(Objects.requireNonNull(LangCreator.langConf.getString("addon.disabled")).replaceAll("\\[name]", addon));
					}
				}
				default -> {
					return colourMessage(Objects.requireNonNull(LangCreator.langConf.getString("addon.no-such-addon")).replaceAll("\\[name]", addon));
				}
			}
		}
		return "";
	}


	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		YamlConfiguration spawnConf = YamlConfiguration.loadConfiguration(spawnLoc);
		String noPerm = Objects.requireNonNull(LangCreator.langConf.getString("global.no-perm"));
		String prefix = Objects.requireNonNull(LangCreator.langConf.getString("global.plugin-prefix"));
		ArrayList<String> cmdHelp = new ArrayList<>();
		cmdHelp.add("&a-----=== &9RegionTeleport &a===-----");
		cmdHelp.add("&2/regiontp help &f- &aShows all commands");
		cmdHelp.add("&2/regiontp teleport/tp &f<region(s)> <spawn(s)> (world) (-s) - &aTeleports all players within the region to the specified location");
		cmdHelp.add("&2/regiontp setspawn &f<name> (-x:<x>) (-y:<y>) (-z:<z>) (-w:<world>) (-yaw:<yaw>) (-p:<pitch>) &f- &aCreates a spawn location");
		cmdHelp.add("&2/regiontp delspawn &f<name> &f- &aDeletes a spawn location");
		cmdHelp.add("&2/regiontp list &f- &aLists all spawn Locations");
		cmdHelp.add("&2/regiontp reload &f- &aReloads the plugin");
		cmdHelp.add("&2/regionclear &f<region(s)> <types> (-s) (-w:<worldname>) - &aClears the specificed regions for the specified entity types");
		StringBuilder commandHelp = new StringBuilder();
		for(int i = 0; i < cmdHelp.size(); i++) {
			commandHelp.append(colourMessage(cmdHelp.get(i)));

			if(i + 1 < cmdHelp.size()) {
				commandHelp.append("\n");
			}
		}
		if(args.length < 1) {
			if(sender.hasPermission("regionteleport.command.help")) {
				sender.sendMessage(commandHelp.toString());
			} else {
				sender.sendMessage(noPerm);
			}
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "help" -> {
				if (sender.hasPermission("regionteleport.command.help")) {
					sender.sendMessage(commandHelp.toString());
				} else {
					sender.sendMessage(noPerm);
				}
			}
			case "setspawn" -> {
				if (sender.hasPermission("regionteleport.command.setspawn")) {
					if (args.length < 2) {
						sender.sendMessage(colourMessage(prefix + LangCreator.langConf.getString("spawn.specify-loc-name")));
						return true;
					}

					if (spawnConf.contains(args[1])) {
						String spawnExist = Objects.requireNonNull(LangCreator.langConf.getString("spawn.spawn-already-exists")).replaceAll("\\[name]", args[1]);
						sender.sendMessage(colourMessage(prefix + spawnExist));
						return true;
					}

					World world = null;
					double X = 0;
					double Y = 0;
					double Z = 0;
					float yaw = 0;
					float pitch = 0;

					boolean wrongUsage = false;

					if(args.length > 2) {
						List<String> variables = Arrays.stream(args).skip(2).toList();
						boolean hasX = false;
						boolean hasY = false;
						boolean hasZ = false;
						boolean hasWorld = false;
						boolean hasYaw = false;
						boolean hasPitch = false;
						for (String variable : variables) {
							if (variable.contains(":")) {
								try {
									String[] varType = variable.split(":");
									switch (varType[0].toLowerCase()) {
										case "-w" -> {
											world = Bukkit.getWorld(varType[1]);
											hasWorld = true;
											if (world == null) {
												wrongUsage = true;
											}
										}
										case "-x" -> {
											X = Double.parseDouble(varType[1]);
											hasX = true;
										}
										case "-y" -> {
											Y = Double.parseDouble(varType[1]);
											hasY = true;
										}
										case "-z" -> {
											Z = Double.parseDouble(varType[1]);
											hasZ = true;
										}
										case "-yaw" -> {
											yaw = Float.parseFloat(varType[1]);
											hasYaw = true;
										}
										case "-p" -> {
											pitch = Float.parseFloat(varType[1]);
											hasPitch = true;
										}
										default -> wrongUsage = true;
									}
								} catch (NumberFormatException e) {
									wrongUsage = true;
								}
							} else {
								wrongUsage = true;
							}
						}
						if((!hasWorld || !hasX || !hasY || !hasZ) && sender instanceof Player player) {
							if (!hasWorld) {
								world = player.getWorld();
							}
							if (!hasX) {
								X = player.getLocation().getX();
							}
							if (!hasY) {
								Y = player.getLocation().getY();
							}
							if (!hasZ) {
								Z = player.getLocation().getZ();
							}
						} else if((!hasWorld || !hasX || !hasY || !hasZ)) {
							wrongUsage = true;
						}

						if((!hasYaw || !hasPitch) && sender instanceof Player player){
							if(!hasYaw) {
								yaw = player.getLocation().getYaw();
							}
							if(!hasPitch) {
								pitch = player.getLocation().getPitch();
							}
						}
					} else if (sender instanceof Player player) {
						world = player.getWorld();
						Location loc = player.getLocation();
						X = loc.getX();
						Y = loc.getY();
						Z = loc.getZ();
						yaw = loc.getYaw();
						pitch = loc.getPitch();
					}


					if(wrongUsage || world == null) {
						sender.sendMessage(colourMessage(prefix + LangCreator.langConf.getString("spawn.wrong-usage-setspawn")));
						return true;
					}

					spawnConf.set(args[1] + ".world", world.getName());
					spawnConf.set(args[1] + ".x", X);
					spawnConf.set(args[1] + ".y", Y);
					spawnConf.set(args[1] + ".z", Z);
					spawnConf.set(args[1] + ".yaw", yaw);
					spawnConf.set(args[1] + ".pitch", pitch);
					try {
						spawnConf.save(spawnLoc);
						String setSpawnSuccess = Objects.requireNonNull(LangCreator.langConf.getString("spawn.successful-setspawn")).replaceAll("\\[name]", args[1]);
						sender.sendMessage(colourMessage(prefix + setSpawnSuccess));
					} catch (IOException e) {
						e.printStackTrace();
					}
					return true;
				} else {
					sender.sendMessage(noPerm);
				}
			}
			case "delspawn" -> {
				if (sender.hasPermission("regionteleport.command.delspawn")) {
					if (args.length < 2) {
						sender.sendMessage(colourMessage(prefix + LangCreator.langConf.getString("spawn.specify-loc-name")));
						return true;
					}

					if (args.length > 2) {
						sender.sendMessage(colourMessage(prefix + LangCreator.langConf.getString("spawn.wrong-usage-delspawn")));
						return true;
					}

					if (spawnConf.contains(args[1])) {
						spawnConf.set(args[1], null);
						try {
							spawnConf.save(spawnLoc);
							String delSpawnSuccess = Objects.requireNonNull(LangCreator.langConf.getString("spawn.successful-delspawn")).replaceAll("\\[name]", args[1]);
							sender.sendMessage(colourMessage(prefix + delSpawnSuccess));
						} catch (IOException e) {
							e.printStackTrace();
							String delSpawnFail = Objects.requireNonNull(LangCreator.langConf.getString("spawn.failed-delspawn")).replaceAll("\\[name]", args[1]);
							sender.sendMessage(colourMessage(prefix + delSpawnFail));
						}
					} else {
						String noSpawn = Objects.requireNonNull(LangCreator.langConf.getString("spawn.no-such-spawn")).replaceAll("\\[name]", args[1]);
						sender.sendMessage(colourMessage(prefix + noSpawn));
					}

				} else {
					sender.sendMessage(noPerm);
				}
			}
			case "spawnlist", "list" -> {
				if (sender.hasPermission("regionteleport.command.spawnlist")) {
					if (args.length > 1) {
						sender.sendMessage(colourMessage(prefix + LangCreator.langConf.getString("spawn.wrong-usage-spawnlist")));
						return true;
					}

					sender.sendMessage(colourMessage(LangCreator.langConf.getString("spawn.list-header")));
					for (String spawnName : spawnConf.getKeys(false)) {
						String spawn = Objects.requireNonNull(LangCreator.langConf.getString("spawn.list-spawn")).replaceAll("\\[name]", spawnName);
						sender.sendMessage(colourMessage(spawn));
					}
				} else {
					sender.sendMessage(noPerm);
				}
			}
			case "reload" -> {
				if (sender.hasPermission("regionteleport.command.reload")) {
					plugin.onReload();
					sender.sendMessage(colourMessage(LangCreator.langConf.getString("global.reload")));
				}
			}
			case "teleport", "tp" -> { //rgtp tp <region> <spawn> (world) (-s)
				if (sender.hasPermission("regionteleport.command.teleport")) {
					if (args.length < 3) {
						sender.sendMessage(colourMessage(prefix + LangCreator.langConf.getString("teleport.wrong-usage")));
						return true;
					}

					if (args.length > 5) {
						sender.sendMessage(colourMessage(prefix + LangCreator.langConf.getString("teleport.wrong-usage")));
						return true;
					}

					World world = null;
					if(args.length > 3 && !args[3].equalsIgnoreCase("-s")) {
						world = Bukkit.getWorld(args[3]);
					} else if(args.length > 4 && !args[4].equalsIgnoreCase("-s")) {
						world = Bukkit.getWorld(args[4]);
					} else if(sender instanceof Player player) {
						world = player.getWorld();
					}

					if(world == null) {
						sender.sendMessage(colourMessage(prefix + LangCreator.langConf.getString("teleport.wrong-usage")));
						return true;
					}

					boolean isGlobal = args[1].contains("__global__");

					List<String> regionIds = isGlobal ? Collections.singletonList("__global__") : Arrays.asList(args[1].split(","));
					List<String> invalidRegions = isGlobal ? new ArrayList<>() : getInvalidRegions(world, regionIds);

					if (invalidRegions.isEmpty()) {
						String addon = getAddon(args[2]);
						String checkAddon = isAddonDisabled(addon);
						if(!checkAddon.isEmpty()) {
							sender.sendMessage(checkAddon);
							return true;
						}
						List<String> spawnIds = getSpawns(args[2]);
						List<String> invalidSpawns = getInvalidSpawns(spawnIds, addon);

						Iterator<String> spawns = spawnIds.iterator();

						if (invalidSpawns.isEmpty()) {
							int teleported = 0;
							for (Player player : world.getPlayers()) {
								if (player.getWorld().equals(world))
									if (!player.hasPermission("regionteleport.teleport.bypass") && isInRegion(player, regionIds)) {
										spawns = spawns.hasNext() ? spawns : spawnIds.iterator();
										Location loc = getLocation(spawns.next(), addon);
										if(loc != null) {
											player.teleport(loc);
											teleported++;
										}
									}
								}
							boolean sendSuccess = (args.length == 3 || !args[3].equalsIgnoreCase(("-s"))) && (args.length <= 4 || !args[4].equalsIgnoreCase(("-s")));
							if(sendSuccess) {
								String tpSuccess = Objects.requireNonNull(LangCreator.langConf.getString("teleport.successful-teleport")).replaceAll("\\[name]", spawnIds.toString());
								tpSuccess = tpSuccess.replaceAll("\\[region]", regionIds.toString());
								tpSuccess = tpSuccess.replaceAll("\\[amount]", String.valueOf(teleported));
								sender.sendMessage(colourMessage(prefix + tpSuccess));
							}
						} else {
							String noSpawn = Objects.requireNonNull(LangCreator.langConf.getString("spawn.no-such-spawn")).replaceAll("\\[name]", invalidSpawns.toString());
							sender.sendMessage(colourMessage(prefix + noSpawn));
						}
					} else {
						String noRegion = Objects.requireNonNull(LangCreator.langConf.getString("teleport.no-such-region")).replaceAll("\\[name]", invalidRegions.toString());
						sender.sendMessage(colourMessage(prefix + noRegion));
					}
				} else {
					sender.sendMessage(noPerm);
				}
			}
			default -> {
				sender.sendMessage(commandHelp.toString());
				return true;
			}
		}
		return true;
	}
}
