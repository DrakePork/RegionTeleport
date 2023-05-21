package com.github.drakepork.regionteleport.addons;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.commands.WarpNotFoundException;
import net.ess3.api.InvalidWorldException;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Objects;

public class EssentialsAddon {

	private Essentials getEssentials() {
		return (Essentials) Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Essentials"));
	}

	public Location warpLoc(String warpName) {
		try {
			return getEssentials().getWarps().getWarp(warpName);
		} catch (WarpNotFoundException | InvalidWorldException e) {
			return null;
		}
	}

	public boolean isWarp(String warpName) {
		return getEssentials().getWarps().isWarp(warpName);
	}

	public Collection<String> warps() {
		return getEssentials().getWarps().getList();
	}
}
