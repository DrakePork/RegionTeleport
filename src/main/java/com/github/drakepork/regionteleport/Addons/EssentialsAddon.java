package com.github.drakepork.regionteleport.Addons;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.commands.WarpNotFoundException;
import net.ess3.api.InvalidWorldException;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Collection;

public class EssentialsAddon {
	public Location warpLoc(String[] teleportLoc) {
		Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		try {
			return ess.getWarps().getWarp(teleportLoc[1]);
		} catch (WarpNotFoundException | InvalidWorldException e) {
			return null;
		}
	}

	public Collection<String> warps() {
		Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		return ess.getWarps().getList();
	}
}
