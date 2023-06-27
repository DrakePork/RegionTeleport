package com.github.drakepork.regionteleport.commands.regionclear;

import com.github.drakepork.regionteleport.RegionTeleport;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RegionClearCommand implements CommandExecutor {
    private final RegionTeleport plugin;
    public RegionClearCommand(final RegionTeleport plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> hasDisplayVersions = new ArrayList<>(Arrays.asList("1.19.4", "1.20"));
        String version = plugin.getServer().getBukkitVersion().split("-")[0];
        if(hasDisplayVersions.contains(version)) {
            new RegionClearDisplay().clearRegion(plugin, sender, command, label, args);
        } else {
            new RegionClearNoDisplay().clearRegion(plugin, sender, command, label, args);
        }
        return true;
    }
}
