package com.github.drakepork.regionteleport.utils;

import com.github.drakepork.regionteleport.RegionTeleport;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderCreator extends PlaceholderExpansion {
    private final RegionTeleport plugin;
    public PlaceholderCreator(final RegionTeleport plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "regionteleport";
    }

    @Override
    public @NotNull String getAuthor() {
        return "DrakePork";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }


    private String getRegionPlayerCount(String worldName, String regionName, boolean excludeBypass) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        if(regions == null) return null;

        if(regionName.equalsIgnoreCase("__global__")) return String.valueOf(world.getPlayerCount());

        ProtectedRegion region = regions.getRegion(regionName);
        if(region == null) return null;

        List<Player> players = new ArrayList<>(world.getPlayers().stream().filter(p -> {
            Location loc = p.getLocation();
            return region.contains(BukkitAdapter.adapt(loc).toVector().toBlockPoint());
        }).toList());

        if(excludeBypass) {
            players.removeIf(p -> p.hasPermission("regionteleport.teleport.bypass"));
        }
        return String.valueOf(players.size());
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if(params.startsWith("player_count_w:") && params.contains("_r:")) { // regionteleport_player_count_w:<world>_r:<region>
            String worldName = params.split("_w:")[1].split("_r:")[0];
            String regionName = params.split("_r:")[1];
            return getRegionPlayerCount(worldName, regionName, false);
        }

        if(params.startsWith("player_count_exclude_bypass_w:") && params.contains("_r:")) { // regionteleport_player_count_exclude_bypass_w:<world>_r:<region>
            String worldName = params.split("_w:")[1].split("_r:")[0];
            String regionName = params.split("_r:")[1];
            return getRegionPlayerCount(worldName, regionName, true);
        }

        return null;
    }
}
