package com.github.drakepork.regionteleport.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.github.drakepork.regionteleport.RegionTeleport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LangCreator {
	private RegionTeleport plugin;

	public LangCreator(RegionTeleport plugin) {
		this.plugin = plugin;
	}

	public void init() {
		File lang = new File(this.plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		try {
			FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);

			// Global Messages

			langConf.addDefault("global.plugin-prefix", "&f[&aRegionTeleport&f] ");
			ArrayList cmdHelp = new ArrayList();
			cmdHelp.add("&a-----=== &9RegionTeleport &a===-----");
			cmdHelp.add("&2/regiontp help &f- &aShows all commands");
			cmdHelp.add("&2/regiontp teleport/tp &f<region> <spawn> (-s) &f- &aTeleports all players within the region to the specified location");
			cmdHelp.add("&2/regiontp setspawn &f<name> &f- &aCreates a spawn location");
			cmdHelp.add("&2/regiontp delspawn &f<name> &f- &aDeletes a spawn location");
			cmdHelp.add("&2/regiontp spawnlist &f- &aLists all spawn Locations");
			cmdHelp.add("&2/regiontp reload &f- &aReloads the plugin");
			langConf.addDefault("global.help", cmdHelp);
			langConf.addDefault("global.no-perm", "&4Error: &cYou do not have permission to execute this command...");
			langConf.addDefault("global.reload", "&aPlugin has been reloaded!");

			// Spawn Related Messages

			langConf.addDefault("spawn.specify-loc-name", "&cPlease specify a location name...");
			langConf.addDefault("spawn.wrong-usage-setspawn", "&cIncorrect usage! /regiontp setspawn <name>");
			langConf.addDefault("spawn.wrong-usage-delspawn", "&cIncorrect usage! /regiontp delspawn <name>");
			langConf.addDefault("spawn.no-such-spawn", "&cNo spawn with name &7[name] &cexists!");
			langConf.addDefault("spawn.wrong-usage-spawnlist", "&cIncorrect usage! /regiontp spawnlist");
			langConf.addDefault("spawn.successful-setspawn", "&aSpawn location with name &2[name] &aset at your location");
			langConf.addDefault("spawn.spawn-already-exists", "&cSpawn location with name &7[name] &calready exists!");
			langConf.addDefault("spawn.successful-delspawn", "&aSuccessfully deleted spawnlocation &a[name]");
			langConf.addDefault("spawn.failed-delspawn", "&cFailed to delete spawnlocation &7[name]&c!");
			langConf.addDefault("spawn.list-header", "&7--=== &2Spawn Locations &7===--");
			langConf.addDefault("spawn.list-spawn", "&f- &a[name]");


			// Addon Related Messages

			langConf.addDefault("spawn.no-such-cmi", "&cNo CMI Warp with name &7[name] &cexists!");
			langConf.addDefault("spawn.no-such-essentials", "&cNo Essentials Warp with name &7[name] &cexists!");
			langConf.addDefault("addon.no-such-addon", "&cNo addon with name &7[name] &cexists!");
			langConf.addDefault("addon.disabled", "&cThis addon is not enabled!");

			// Teleport Related Messages

			langConf.addDefault("teleport.wrong-usage", "&cIncorrect Usage! /regiontp tp <region> <spawn> (-s)");
			langConf.addDefault("teleport.successful-teleport", "&aSent &e[amount] &aplayer(s) from region &2[region] &ato spawnlocation &2[name]&a!");
			langConf.addDefault("teleport.no-such-region", "&cNo region with name &7[name] &cexists in this world!");


			// Console Specific Messages
			langConf.addDefault("console.wrong-usage-setspawn", "&cIncorrect usage! /regiontp setspawn <name> <x> <y> <z> <world>");
			langConf.addDefault("console.no-such-world", "&cNo world with name &7[name] &cexists!");
			langConf.addDefault("console.wrong-usage-teleport", "&cIncorrect Usage! /regiontp tp <world> <region> <spawn> (-s)");
			ArrayList consoleHelp = new ArrayList();
			consoleHelp.add("&a-----=== &9RegionTeleport &a===-----");
			consoleHelp.add("&2/regiontp help &f- &aShows all commands");
			consoleHelp.add("&2/regiontp teleport/tp &f<region> <spawn> <world> (-s) &f- &aTeleports all players within the region to the specified location");
			consoleHelp.add("&2/regiontp setspawn &f<name> <x> <y> <z> <world> &f- &aCreates a spawn location");
			consoleHelp.add("&2/regiontp delspawn &f<name> &f- &aDeletes a spawn location");
			consoleHelp.add("&2/regiontp spawnlist &f- &aLists all spawn Locations");
			consoleHelp.add("&2/regiontp reload &f- &aReloads the plugin");
			langConf.addDefault("console.help", consoleHelp);

			langConf.options().copyDefaults(true);
			langConf.save(lang);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
