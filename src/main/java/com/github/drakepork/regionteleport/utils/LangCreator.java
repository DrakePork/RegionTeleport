package com.github.drakepork.regionteleport.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.github.drakepork.regionteleport.RegionTeleport;

import java.io.File;
import java.io.IOException;

public class LangCreator {
	private final RegionTeleport plugin;

	public LangCreator(final RegionTeleport plugin) {
		this.plugin = plugin;
	}

	public void init() {
		File lang = new File(this.plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		try {
			FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);

			// Global Messages

			langConf.addDefault("global.plugin-prefix", "&f[&aRegionTeleport&f] ");
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

			langConf.addDefault("addon.no-such-addon", "&cNo addon with name &7[name] &cexists!");
			langConf.addDefault("addon.disabled", "&cThis addon is not enabled!");

			// Teleport Related Messages

			langConf.addDefault("teleport.wrong-usage", "&cIncorrect Usage! /regiontp tp <region(s)> <spawn(s)> (-s)");
			langConf.addDefault("teleport.successful-teleport", "&aSent &e[amount] &aplayer(s) from region(s) &2[region] &ato spawnlocation(s) &2[name]&a!");
			langConf.addDefault("teleport.no-such-region", "&cNo region with name &7[name] &cexists in this world!");

			// Region Clear Related Messages

			langConf.addDefault("region-clear.prefix", "&f[&aRegionClear&f] ");
			langConf.addDefault("region-clear.successful-clear", "&aRemoved from region(s) &2[name] &aa total of &e[amount] &aentities. ([entity-specific]&a)");
			langConf.addDefault("region-clear.successful-clear-entity-specific", "&6[entity]&7: &e[amount]");
			langConf.addDefault("region-clear.wrong-usage-specific", "&cIncorrect Usage! No specific entity specified..");
			langConf.addDefault("region-clear.no-such-specific", "&7[name] &cis not a valid entity!");
			langConf.addDefault("region-clear.no-such-item", "&7[name] &cis not a valid item!");
			langConf.addDefault("region-clear.no-such-world", "&7[name] &cis not a valid world!");
			langConf.addDefault("region-clear.no-world-specified", "&cYou have to specify a world with -w:<worldname>");
			langConf.addDefault("region-clear.no-such-region", "&7[name] &cis not a valid region!");
			langConf.addDefault("region-clear.wrong-usage", "&cIncorrect Usage! /regionclear <region> (types) (-s) (-w:[world])");
			langConf.addDefault("region-clear.no-such-region", "&cNo region with name &7[name] &cexists in this world!");

			// Console Related Messages

			langConf.addDefault("console.wrong-usage-setspawn", "&cIncorrect usage! /regiontp setspawn <name> <x> <y> <z> <world>");
			langConf.addDefault("console.no-such-world", "&cNo world with name &7[name] &cexists!");
			langConf.addDefault("console.wrong-usage-teleport", "&cIncorrect Usage! /regiontp tp <region(s)> <spawn(s)> <world> (-s)");

			langConf.options().copyDefaults(true);
			langConf.save(lang);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
