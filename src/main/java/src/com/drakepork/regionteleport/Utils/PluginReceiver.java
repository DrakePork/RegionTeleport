package src.com.drakepork.regionteleport.Utils;

import src.com.drakepork.regionteleport.RegionTeleport;

public abstract class PluginReceiver {

	protected final RegionTeleport regionteleport;

	public PluginReceiver(final RegionTeleport regionteleport) {
		this.regionteleport = regionteleport;
	}
}
