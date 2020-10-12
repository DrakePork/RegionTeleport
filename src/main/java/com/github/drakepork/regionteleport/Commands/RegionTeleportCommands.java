package com.github.drakepork.regionteleport.Commands;

import com.google.inject.Inject;
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.github.drakepork.regionteleport.RegionTeleport;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RegionTeleportCommands implements CommandExecutor {
	private RegionTeleport plugin;

	@Inject
	public RegionTeleportCommands(RegionTeleport plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			File lang = new File(this.plugin.getDataFolder() + File.separator
					+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
			FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
			List<String> cmdList = (List<String>) langConf.getList("global.help");
			String noPerm = this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', langConf.getString("global.no-perm")));
			String prefix = langConf.getString("global.plugin-prefix");

			File spawnloc = new File(this.plugin.getDataFolder() + File.separator + "spawnlocations.yml");
			YamlConfiguration spawnConf = YamlConfiguration.loadConfiguration(spawnloc);

			String commandHelp = "";
			for(int i = 0; i < cmdList.size(); i++) {
				commandHelp += this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', cmdList.get(i)));

				if(i + 1 < cmdList.size()) {
					commandHelp += "\n";
				}
			}
			Player player = (Player) sender;
			if(args.length < 1) {
				if(player.hasPermission("regionteleport.command.help")) {
					player.sendMessage(commandHelp);
				} else {
					player.sendMessage(noPerm);

				}
				return true;
			}

			switch(args[0].toLowerCase()) {
				case "help":
					if(player.hasPermission("regionteleport.command.help")) {
						player.sendMessage(commandHelp);
					} else {
						player.sendMessage(noPerm);
					}
					break;

				case "setspawn":
					if(player.hasPermission("regionteleport.command.setspawn")) {
						if (args.length < 2) {
							player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("spawn.specify-loc-name"))));
							return true;
						}

						if (args.length > 2) {
							player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("spawn.wrong-usage-setspawn"))));
							return true;
						}

						if (spawnConf.contains(args[1])) {
							String spawnExist = langConf.getString("spawn.spawn-already-exists").replaceAll("\\[name\\]", args[1]);
							player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + spawnExist)));
							return true;
						}

						spawnConf.set(args[1] + ".world", player.getLocation().getWorld().getName());
						spawnConf.set(args[1] + ".x", Double.valueOf(player.getLocation().getX()));
						spawnConf.set(args[1] + ".y", Double.valueOf(player.getLocation().getY()));
						spawnConf.set(args[1] + ".z", Double.valueOf(player.getLocation().getZ()));
						spawnConf.set(args[1] + ".yaw", Float.valueOf(player.getLocation().getYaw()));
						spawnConf.set(args[1] + ".pitch", Float.valueOf(player.getLocation().getPitch()));
						try {
							spawnConf.save(spawnloc);
							String setspawnSuccess = langConf.getString("spawn.successful-setspawn").replaceAll("\\[name\\]", args[1]);
							player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + setspawnSuccess)));
						} catch (IOException e) {
							e.printStackTrace();
						}
						return true;
					} else {
						player.sendMessage(noPerm);
					}
					break;
				case "delspawn":
					if(player.hasPermission("regionteleport.command.delspawn")) {
						if (args.length < 2) {
							player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("spawn.specify-loc-name"))));
							return true;
						}

						if (args.length > 2) {
							player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("spawn.wrong-usage-delspawn"))));
							return true;
						}

						if(spawnConf.contains(args[1])) {
							spawnConf.set(args[1], null);
							try {
								spawnConf.save(spawnloc);
								String delspawnSuccess = langConf.getString("spawn.successful-delspawn").replaceAll("\\[name\\]", args[1]);
								player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + delspawnSuccess)));
							} catch (IOException e) {
								e.printStackTrace();
								String delspawnFail = langConf.getString("spawn.failed-delspawn").replaceAll("\\[name\\]", args[1]);
								player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + delspawnFail)));
							}
						} else {
							String noSpawn = langConf.getString("spawn.no-such-spawn").replaceAll("\\[name\\]", args[1]);
							player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + noSpawn)));
						}

					} else {
						player.sendMessage(noPerm);
					}
					break;
				case "spawnlist":
					if(player.hasPermission("regionteleport.command.spawnlist")) {
						if (args.length > 1) {
							player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("spawn.wrong-usage-spawnlist"))));
							return true;
						}

						player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', langConf.getString("spawn.list-header"))));
						for (String spawnName : spawnConf.getKeys(false)) {
							String spawn = langConf.getString("spawn.list-spawn").replaceAll("\\[name\\]", spawnName);
							player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', spawn)));
						}
					} else {
						player.sendMessage(noPerm);
					}
					break;
				case "teleport":
				case "tp":
					if(player.hasPermission("regionteleport.command.teleport")) {
						if (args.length < 2) {
							player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("teleport.wrong-usage"))));
							return true;
						}

						if (args.length > 4) {
							player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("teleport.wrong-usage"))));
							return true;
						}

						if(spawnConf.contains(args[2])) {
							RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
							RegionManager regions = container.get(BukkitAdapter.adapt(player).getWorld());
							if (regions.getRegion(args[1]) != null) {
								int teleported = 0;
								World pWorld = player.getWorld();
								for (Player pOnline : Bukkit.getServer().getOnlinePlayers()) {
									if (args[1].equalsIgnoreCase("__global__") && pOnline.getWorld() == pWorld) {
										World w = Bukkit.getServer().getWorld(spawnConf.getString(args[2] + ".world"));
										float yaw = (float) spawnConf.getDouble(args[1] + ".yaw");
										float pitch = (float) spawnConf.getDouble(args[1] + ".pitch");
										Location location = new Location(w, spawnConf.getDouble(args[2] + ".x"), spawnConf.getDouble(args[2] + ".y"), spawnConf.getDouble(args[2] + ".z"));
										location.setYaw(yaw);
										location.setPitch(pitch);
										pOnline.teleport(location);
										teleported++;
										continue;
									} else {
										Location location = pOnline.getLocation();
										BlockVector3 v = BlockVector3.at(location.getX(), location.getY(), location.getZ());
										World world = pOnline.getWorld();
										RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
										ApplicableRegionSet set = rm.getApplicableRegions(v);
										for (ProtectedRegion r : set) {
											if (r.getId().equalsIgnoreCase(args[1])) {
												World w = Bukkit.getServer().getWorld(spawnConf.getString(args[2] + ".world"));
												float yaw = (float) spawnConf.getDouble(args[2] + ".yaw");
												float pitch = (float) spawnConf.getDouble(args[2] + ".pitch");
												Location Nlocation = new Location(w, spawnConf.getDouble(args[2] + ".x"), spawnConf.getDouble(args[2] + ".y"), spawnConf.getDouble(args[2] + ".z"));
												Nlocation.setYaw(yaw);
												Nlocation.setPitch(pitch);
												pOnline.teleport(Nlocation);
												teleported++;
											}
										}
									}
								}
								if(args.length > 4) {
									if(!args[3].equalsIgnoreCase(("-s"))) {
										player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("teleport.wrong-usage"))));
									}
								} else {
									String tpSuccess = langConf.getString("teleport.successful-teleport").replaceAll("\\[name\\]", args[2]);
									tpSuccess = tpSuccess.replaceAll("\\[region\\]", args[1]);
									tpSuccess = tpSuccess.replaceAll("\\[amount\\]", String.valueOf(teleported));
									player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + tpSuccess)));
								}
							} else {
								String noRegion = langConf.getString("teleport.no-such-region").replaceAll("\\[name\\]", args[1]);
								player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + noRegion)));
							}
						} else {
							String noSpawn = langConf.getString("spawn.no-such-spawn").replaceAll("\\[name\\]", args[2]);
							player.sendMessage(this.plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + noSpawn)));
						}

					} else {
						player.sendMessage(noPerm);
					}
					break;
				default:
					player.sendMessage(commandHelp);
					return true;
			}

		} else {

		}
		return true;
	}
}
