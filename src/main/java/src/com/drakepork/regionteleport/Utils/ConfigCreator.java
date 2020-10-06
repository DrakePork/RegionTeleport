package src.com.drakepork.regionteleport.Utils;

import com.google.inject.Inject;
import org.bukkit.configuration.file.FileConfiguration;
import src.com.drakepork.regionteleport.RegionTeleport;


public class ConfigCreator {
	private RegionTeleport plugin;

	@Inject
	public ConfigCreator(RegionTeleport plugin) {
		this.plugin = plugin;
	}

	public void init() {
		FileConfiguration config = this.plugin.getConfig();
		config.addDefault("lang-file", "en.yml");
		config.options().copyDefaults(true);
		this.plugin.saveConfig();
	}
}
