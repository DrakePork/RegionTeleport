package src.com.drakepork.regionteleport.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import src.com.drakepork.regionteleport.RegionTeleport;

public class ConfigCreator extends PluginReceiver {
	public ConfigCreator (final RegionTeleport regionteleport) {
		super(regionteleport);
	}

	public void init() {
		FileConfiguration config = regionteleport.getConfig();
	}
}
