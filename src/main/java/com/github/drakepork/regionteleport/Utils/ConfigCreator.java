package com.github.drakepork.regionteleport.Utils;

import com.google.inject.Inject;
import org.bukkit.configuration.file.FileConfiguration;
import com.github.drakepork.regionteleport.RegionTeleport;


public class ConfigCreator {
	private RegionTeleport plugin;

	@Inject
	public ConfigCreator(RegionTeleport plugin) {
		this.plugin = plugin;
	}

	public void init() {
		FileConfiguration config = plugin.getConfig();
		config.addDefault("lang-file", "en.yml");
		config.addDefault("addons.cmi", false);
		config.addDefault("addons.essentials", false);
		config.options().copyDefaults(true);
		plugin.saveConfig();
	}
}
