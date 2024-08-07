package com.github.drakepork.regionteleport.utils;

import org.bukkit.configuration.file.FileConfiguration;
import com.github.drakepork.regionteleport.RegionTeleport;

public class ConfigCreator {
	private final RegionTeleport plugin;
	public ConfigCreator(final RegionTeleport plugin) {
		this.plugin = plugin;
	}

	public void init() {
		FileConfiguration config = plugin.getConfig();
		config.addDefault("addons.cmi", false);
		config.addDefault("addons.essentials", false);
		config.options().copyDefaults(true);
		plugin.saveConfig();
	}
}
