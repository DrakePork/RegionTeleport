package src.com.drakepork.regionteleport.Commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import src.com.drakepork.regionteleport.RegionTeleport;
import src.com.drakepork.regionteleport.Utils.PluginReceiver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegionTeleportAutoTabCompleter extends PluginReceiver implements TabCompleter {
	public RegionTeleportAutoTabCompleter(final RegionTeleport regionteleport) {
		super(regionteleport);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			ArrayList<String> options = new ArrayList<>();
			if (command.getName().equalsIgnoreCase("regiontp")) {
				if (args.length == 1) {
					options.add("tp");
					options.add("setspawn");
					options.add("delspawn");
					options.add("list");
					options.add("help");
				} else if (args.length == 2) {
					switch (args[0].toLowerCase()) {
						case "tp":
							RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
							Map allRegions = rm.getRegions();
							for (Object region : allRegions.entrySet()) {
								options.add((String) region);
							}
							break;
						case "setspawn":
						case "delspawn":
							options.add("<name>");
							break;
					}

				} else if(args.length == 3) {
					switch (args[0].toLowerCase()) {
						case "tp":
							File spawnloc = new File(regionteleport.getDataFolder() + File.separator + "spawnlocations.yml");
							YamlConfiguration spawnConf = YamlConfiguration.loadConfiguration(spawnloc);
							for (String spawn : spawnConf.getKeys(false)) {
								options.add(spawn);
							}
							break;
					}
				} else if(args.length == 4) {
					switch (args[0].toLowerCase()) {
						case "tp":
								options.add("-s");
							break;
					}
				}
			}
			return options;
		}
		return null;
	}
}
