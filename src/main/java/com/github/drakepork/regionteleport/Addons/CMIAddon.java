package com.github.drakepork.regionteleport.Addons;


import com.Zrips.CMI.CMI;
import org.bukkit.Location;

import java.util.Collection;

public class CMIAddon {
	public Location warpLoc(String[] teleportLoc) {
		if (CMI.getInstance().getWarpManager().getWarp(teleportLoc[1]) != null) {
			return CMI.getInstance().getWarpManager().getWarp(teleportLoc[1]).getLoc().getBukkitLoc();
		} else {
			return null;
		}
	}

	public Collection<String> warps() {
		return CMI.getInstance().getWarpManager().getWarps().keySet();
	}
}
