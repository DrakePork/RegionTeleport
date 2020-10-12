package com.github.drakepork.regionteleport;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.github.drakepork.regionteleport.Commands.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.drakepork.regionteleport.Utils.*;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class RegionTeleport extends JavaPlugin {
    @Inject private LangCreator lang;
    @Inject private ConfigCreator ConfigCreator;
    @Inject private RegionTeleportCommands commands;
    @Inject private RegionTeleportAutoTabCompleter tabCompleter;

    @Override
    public void onEnable() {
        PluginReceiver module = new PluginReceiver(this);
        Injector injector = module.createInjector();
        injector.injectMembers(this);

        this.ConfigCreator.init();
        this.lang.init();
        
        int pluginId = 9090;
        Metrics metrics = new Metrics(this, pluginId);

        File spawnloc = new File(this.getDataFolder() + File.separator + "spawnlocations.yml");
        if(!spawnloc.exists()) {
            try {
                spawnloc.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.getCommand("regiontp").setExecutor(this.commands);
        this.getCommand("regiontp").setTabCompleter(this.tabCompleter);
        getLogger().info("Enabled RegionTeleport - v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled RegionTeleport - v" + getDescription().getVersion());
    }

    public String translateHexColorCodes(String message) {
        final Pattern hexPattern = Pattern.compile("\\{#" + "([A-Fa-f0-9]{6})" + "\\}");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                    + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                    + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }

}
