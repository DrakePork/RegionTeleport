package src.com.drakepork.regionteleport.Commands;

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
						} catch (IOException e) {
							e.printStackTrace();
						}
						String setspawnSuccess = langConf.getString("spawn.successful-setspawn").replaceAll("\\[name\\]", args[1]);
						player.sendMessage(regionteleport.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', prefix + setspawnSuccess)));
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
