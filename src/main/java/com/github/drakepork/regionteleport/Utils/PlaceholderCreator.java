package com.github.drakepork.regionteleport.Utils;

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
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if(params.startsWith("player_count_w:") && params.contains("_r:")) { // regionteleport_player_count_w:<world>_r:<region>
            String worldName = params.split("_w:")[1].split("_r:")[0];
            if (Bukkit.getWorld(worldName) != null) {
                World world = Bukkit.getWorld(worldName);
                String regionName = params.split("_r:")[1];

                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                assert world != null;
                RegionManager regions = container.get(BukkitAdapter.adapt(world));
                assert regions != null;
                if (regions.getRegion(regionName) != null || regionName.equalsIgnoreCase("__global__")) {
                    int playerCount = 0;
                    for (Player pOnline : Bukkit.getServer().getOnlinePlayers()) {
                        if (regionName.equalsIgnoreCase("__global__") && pOnline.getWorld() == world) {
                            playerCount++;
                        } else {
                            Location location = pOnline.getLocation();
                            BlockVector3 v = BlockVector3.at(location.getX(), location.getY(), location.getZ());
                            ApplicableRegionSet set = regions.getApplicableRegions(v);
                            for (ProtectedRegion r : set) {
                                if (r.getId().equalsIgnoreCase(regionName)) {
                                    playerCount++;
                                }
                            }
                        }
                    }
                    return String.valueOf(playerCount);
                } else { // region not found
                    return null;
                }
            } else { // world not found
                return null;
            }
        }

        if(params.startsWith("player_count_exclude_bypass_w:") && params.contains("_r:")) { // regionteleport_player_count_exclude_bypass_w:<world>_r:<region>
            String worldName = params.split("_w:")[1].split("_r:")[0];
            if (Bukkit.getWorld(worldName) != null) {
                World world = Bukkit.getWorld(worldName);
                String regionName = params.split("_r:")[1];

                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                assert world != null;
                RegionManager regions = container.get(BukkitAdapter.adapt(world));
                assert regions != null;
                if (regions.getRegion(regionName) != null || regionName.equalsIgnoreCase("__global__")) {
                    int playerCount = 0;
                    for (Player pOnline : Bukkit.getServer().getOnlinePlayers()) {
                        if (!pOnline.hasPermission("regionteleport.teleport.bypass")) {
                            if (regionName.equalsIgnoreCase("__global__") && pOnline.getWorld() == world) {
                                playerCount++;
                            } else {
                                Location location = pOnline.getLocation();
                                BlockVector3 v = BlockVector3.at(location.getX(), location.getY(), location.getZ());
                                ApplicableRegionSet set = regions.getApplicableRegions(v);
                                for (ProtectedRegion r : set) {
                                    if (r.getId().equalsIgnoreCase(regionName)) {
                                        playerCount++;
                                    }
                                }
                            }
                        }
                    }
                    return String.valueOf(playerCount);
                } else { // region not found
                    return null;
                }
            } else { // world not found
                return null;
            }
        }

        return null;
    }
}
