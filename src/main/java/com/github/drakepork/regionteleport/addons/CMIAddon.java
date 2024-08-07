package com.github.drakepork.regionteleport.addons;

import com.Zrips.CMI.CMI;
import org.bukkit.Location;

import java.util.Collection;
import java.util.stream.Collectors;

public class CMIAddon {
	public Location warpLoc(String warpName) {
		if (CMI.getInstance().getWarpManager().getWarp(warpName) != null) {
			return CMI.getInstance().getWarpManager().getWarp(warpName).getLoc().getBukkitLoc();
		} else {
			return null;
		}
	}

	public Collection<String> warps() {
		return CMI.getInstance().getWarpManager().getWarps().keySet().stream().map(warp -> "cmi:" + warp).toList();
	}
}
