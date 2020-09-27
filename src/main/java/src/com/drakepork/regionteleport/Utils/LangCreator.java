package src.com.drakepork.regionteleport.Utils;

import com.google.inject.Inject;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import src.com.drakepork.regionteleport.RegionTeleport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LangCreator {
	private RegionTeleport plugin;

	@Inject
	public LangCreator(RegionTeleport plugin) {
		this.plugin = plugin;
	}

	public void init() {
		File lang = new File(this.plugin.getDataFolder() + File.separator + "lang.yml");
		this.plugin.getLogger().info("WHAM");
		if(!lang.exists()) {
			try {
				lang.createNewFile();
				FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
				// Global Messages
				langConf.set("global.plugin-prefix", "&f[&aRegionTeleport&f] ");
				ArrayList cmdHelp = new ArrayList();
				cmdHelp.add("&a-----=== &9RegionTP &a===-----");
				cmdHelp.add("&2/regiontp help &f- &aShows this");
				cmdHelp.add("&2/regiontp tp <region> <spawn> (-s) &f- &aTeleport all players within a region to the specified location");
				cmdHelp.add("&2/regiontp setspawn <name> &f- &aCreate new regiontp location");
				cmdHelp.add("&2/regiontp delspawn <name> &f- &aDelete a regiontp location");
				cmdHelp.add("&2/regiontp spawnlist &f- &aLists all RegionTP Locations");
				langConf.set("global.help", cmdHelp);
				langConf.set("global.no-perm", "&4Error: &cYou do not have permission to execute this command...");

				// Spawn Related Messages

				langConf.set("spawn.specify-loc-name", "&cPlease specify a location name...");
				langConf.set("spawn.wrong-usage-setspawn", "&cIncorrect usage! /regiontp setspawn <name>");
				langConf.set("spawn.wrong-usage-delspawn", "&cIncorrect usage! /regiontp delspawn <name>");
				langConf.set("spawn.no-such-spawn", "&cNo spawn with name [name] exists!");
				langConf.set("spawn.wrong-usage-spawnlist", "&cIncorrect usage! /regiontp spawnlist");
				langConf.set("spawn.successful-setspawn", "&2Spawn location with name [name] set at your location");
				langConf.set("spawn.spawn-already-exists", "&cSpawn location with name [name] already exists!");
				langConf.set("spawn.successful-delspawn", "&2Successfully deleted spawnlocation [name]");
				langConf.set("spawn.failed-delspawn", "&cFailed to delete spawnlocation [name]");
				langConf.set("spawn.list-header", "&7--=== &aSpawn Locations &7===--");
				langConf.set("spawn.list-spawn", "&f- &a[name]");

				// Teleport Related Messages

				langConf.set("teleport.wrong-usage", "&cIncorrect Usage! /regiontp tp <region <spawn> (-s)");
				langConf.set("teleport.successful-teleport", "&aSent &e[amount] &aplayer(s) from region &2[region] &ato spawnlocation &2[name]&a!");
				langConf.set("teleport.no-such-region", "&cNo region with name [name] exists in this world!");
				langConf.save(lang);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
