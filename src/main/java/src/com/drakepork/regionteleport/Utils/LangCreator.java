package src.com.drakepork.regionteleport.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import src.com.drakepork.regionteleport.RegionTeleport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LangCreator extends PluginReceiver {
	public LangCreator (final RegionTeleport regionteleport) {
		super(regionteleport);
	}
	public void init() {
		File lang = new File(regionteleport.getDataFolder() + File.separator + "lang.yml");
		if(!lang.exists()) {
			try {
				FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
				// Global Messages
				langConf.addDefault("global.plugin-prefix", "&f[&aRegionTeleport&f] ");
				ArrayList cmdHelp = new ArrayList();
				cmdHelp.add("&a-----=== &9RegionTP &a===-----");
				cmdHelp.add("&2/regiontp tp <region> <spawn> (-s) &f- &aTeleport all players within a region to the specified location");
				cmdHelp.add("&2/regiontp setspawn <name> &f- &aCreate new regiontp location");
				cmdHelp.add("&2/regiontp delspawn <name> &f- &aDelete a regiontp location");
				cmdHelp.add("&2/regiontp list &f- &aLists all RegionTP Locations");
				langConf.addDefault("global.help", cmdHelp);
				langConf.addDefault("global.no-perm", "&4Error: &cYou do not have permission to execute this command...");

				// Spawn Related Messages

				langConf.addDefault("spawn.specify-loc-name", "&cPlease specify a location name...");
				langConf.addDefault("spawn.wrong-usage-setspawn", "&cIncorrect usage! /regiontp setspawn <name>");
				langConf.addDefault("spawn.wrong-usage-delspawn", "&cIncorrect usage! /regiontp delspawn <name>");
				langConf.addDefault("spawn.wrong-usage-spawnlist", "&cIncorrect usage! /regiontp spawnlist");
				langConf.addDefault("spawn.successful-setspawn", "&2Spawn location with name [name] set at your location");

				// Teleport Related Messages

				langConf.addDefault("teleport.wrong-usage", "&cIncorrect Usage! /regiontp tp <region <spawn>");
				langConf.addDefault("teleport.successful-teleport", "");

				langConf.save(lang);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
