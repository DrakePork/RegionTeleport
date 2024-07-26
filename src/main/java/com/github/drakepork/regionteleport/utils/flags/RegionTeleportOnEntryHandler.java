package com.github.drakepork.regionteleport.utils.flags;

import com.github.drakepork.regionteleport.RegionTeleport;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static com.github.drakepork.regionteleport.RegionTeleport.getSpawn;


public class RegionTeleportOnEntryHandler extends FlagValueChangeHandler<String> {
    public static Factory FACTORY(Plugin plugin) { return new Factory(plugin); }
    public static class Factory extends Handler.Factory<RegionTeleportOnEntryHandler> {
        private final Plugin plugin;
        public Factory(Plugin plugin)
        {
            this.plugin = plugin;
        }
        @Override
        public RegionTeleportOnEntryHandler create(Session session) {
            return new RegionTeleportOnEntryHandler(this.plugin, session);
        }
    }

    private final Plugin plugin;

    public RegionTeleportOnEntryHandler(Plugin plugin, Session session) {
        super(session, RegionTeleport.REGIONTP_ON_ENTRY);
        this.plugin = plugin;
    }

    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, String value) {
    }

    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, String currentValue, String lastValue, MoveType moveType) {
        handleValue(player, currentValue);
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, String lastValue, MoveType moveType) {
        return true;
    }

    public void handleValue(LocalPlayer localPlayer, String value) {
        Player player = BukkitAdapter.adapt(localPlayer);
        if (player.hasPermission("regionteleport.teleport.bypass")) return;
        if(value != null && !player.hasMetadata("regionteleport-stop-teleport-looping")) {
            player.setMetadata("regionteleport-stop-teleport-looping", new FixedMetadataValue(plugin, true));
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.removeMetadata("regionteleport-stop-teleport-looping", plugin);
                }
            }.runTask(plugin);

            org.bukkit.Location loc = getSpawn(value);
            if(loc == null) return;
            player.teleportAsync(loc);

        }
    }
}
