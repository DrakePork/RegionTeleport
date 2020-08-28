package src.com.drakepork.regionteleport.Commands;

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
import src.com.drakepork.regionteleport.Utils.PluginReceiver;
import src.com.drakepork.regionteleport.RegionTeleport;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RegionTeleportCommands extends PluginReceiver implements CommandExecutor {
	public RegionTeleportCommands(final RegionTeleport regionteleport) {
		super(regionteleport);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			File lang = new File(regionteleport.getDataFolder() + File.separator + "lang.yml");
			FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
			List<String> cmdList = (List<String>) langConf.getList("global.help");
			String noPerm = regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', langConf.getString("global.no-perm")));
			String prefix = langConf.getString("global.plugin-prefix");

			File spawnloc = new File(regionteleport.getDataFolder() + File.separator + "spawnlocations.yml");
			YamlConfiguration spawnConf = YamlConfiguration.loadConfiguration(spawnloc);

			String commandHelp = null;
			for(int i = 0; i < cmdList.size(); i++) {
				commandHelp += cmdList.get(i);

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
							player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("spawn.specify-loc-name"))));
							return true;
						}

						if (args.length > 2) {
							player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("spawn.wrong-usage-setspawn"))));
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
							player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + setspawnSuccess)));
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
							player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("spawn.specify-loc-name"))));
							return true;
						}

						if (args.length > 2) {
							player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("spawn.wrong-usage-delspawn"))));
							return true;
						}

						if(spawnConf.contains(args[1])) {
							spawnConf.set(args[1], null);
							try {
								spawnConf.save(spawnloc);
								String delspawnSuccess = langConf.getString("spawn.successful-delspawn").replaceAll("\\[name\\]", args[1]);
								player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + delspawnSuccess)));
							} catch (IOException e) {
								e.printStackTrace();
								String delspawnFail = langConf.getString("spawn.failed-delspawn").replaceAll("\\[name\\]", args[1]);
								player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + delspawnFail)));
							}
						} else {
							String noSpawn = langConf.getString("spawn.no-such-spawn").replaceAll("\\[name\\]", args[1]);
							player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + noSpawn)));
						}

					} else {
						player.sendMessage(noPerm);
					}
					break;
				case "spawnlist":
					if(player.hasPermission("regionteleport.command.spawnlist")) {
						if (args.length > 2) {
							player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("spawn.wrong-usage-spawnlist"))));
							return true;
						}

						player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', langConf.getString("spawn.list-header"))));
						for (String spawnName : spawnConf.getKeys(false)) {
							String spawn = langConf.getString("spawn.list-spawn").replaceAll("\\[name\\]", spawnName);
							player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', spawn)));
						}
					} else {
						player.sendMessage(noPerm);
					}
					break;
				case "teleport":
				case "tp":
					if(player.hasPermission("regionteleport.command.teleport")) {
						if (args.length < 2) {
							player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("teleport.wrong-usage"))));
							return true;
						}

						if (args.length > 2) {
							player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("teleport.wrong-usage"))));
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
										player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + langConf.getString("teleport.wrong-usage"))));
									}
								} else {
									String noSpawn1 = langConf.getString("spawn.teleport.successful-teleport").replaceAll("\\[name\\]", args[2]);
									String noSpawn2 = noSpawn1.replaceAll("\\[region\\]", args[1]);
									String noSpawn3 = noSpawn2.replaceAll("\\[amount\\]", String.valueOf(teleported));
									player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + noSpawn3)));
								}
							} else {
								String noSpawn = langConf.getString("spawn.no-such-region").replaceAll("\\[name\\]", args[1]);
								player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + noSpawn)));
							}
						} else {
							String noSpawn = langConf.getString("spawn.no-such-spawn").replaceAll("\\[name\\]", args[2]);
							player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + noSpawn)));
						}

					} else {
						player.sendMessage(noPerm);
					}
					break;
			}

		} else {

		}
		return true;
	}
}
