package com.github.drakepork.regionteleport.commands.regionclear;

import com.github.drakepork.regionteleport.RegionTeleport;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface RegionClear {
    void clearRegion(@NotNull RegionTeleport plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args);
}
