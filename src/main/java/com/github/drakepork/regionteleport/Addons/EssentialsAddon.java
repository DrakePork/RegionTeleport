package com.github.drakepork.regionteleport.Addons;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.commands.WarpNotFoundException;
import net.ess3.api.InvalidWorldException;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Collection;

public class EssentialsAddon {
	public Location warpLoc(String warpName) {
		Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		try {
			return ess.getWarps().getWarp(warpName);
		} catch (WarpNotFoundException | InvalidWorldException e) {
			return null;
		}
	}

	public boolean isWarp(String warpName) {
		Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		return ess.getWarps().isWarp(warpName);
	}

	public Collection<String> warps() {
		Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		return ess.getWarps().getList();
	}
}
