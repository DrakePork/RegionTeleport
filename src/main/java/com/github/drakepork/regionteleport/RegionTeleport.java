package com.github.drakepork.regionteleport;

import com.github.drakepork.regionteleport.Addons.CMIAddon;
import com.github.drakepork.regionteleport.Addons.EssentialsAddon;
import com.github.drakepork.regionteleport.Commands.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.drakepork.regionteleport.Utils.*;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class RegionTeleport extends JavaPlugin {
    public CMIAddon cmiAddon = null;
    public EssentialsAddon essAddon = null;

    @Override
    public void onEnable() {
        new ConfigCreator(this).init();
        new LangCreator(this).init();
        
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
        if(Bukkit.getPluginManager().isPluginEnabled("CMI") && this.getConfig().getBoolean("addons.cmi")) {
            getLogger().info("Enabled CMI Addon");
            cmiAddon = new CMIAddon();
        }
        if(Bukkit.getPluginManager().isPluginEnabled("Essentials") && this.getConfig().getBoolean("addons.essentials")) {
            getLogger().info("Enabled Essentials Addon");
            essAddon = new EssentialsAddon();
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderCreator(this).register();
            getLogger().info("Enabled PlaceholderAPI Placeholders");
        }


        Objects.requireNonNull(this.getCommand("regiontp")).setExecutor(new RegionTeleportCommands(this));
        Objects.requireNonNull(this.getCommand("regiontp")).setTabCompleter(new RegionTeleportAutoTabCompleter(this));
        getLogger().info("Enabled RegionTeleport - v" + getDescription().getVersion());
    }

    public void onReload() {
        this.reloadConfig();
        new ConfigCreator(this).init();
        new LangCreator(this).init();
        File spawnloc = new File(this.getDataFolder() + File.separator + "spawnlocations.yml");
        if(!spawnloc.exists()) {
            try {
                spawnloc.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(Bukkit.getPluginManager().isPluginEnabled("CMI") && this.getConfig().getBoolean("addons.cmi")) {
            getLogger().info("Enabled CMI Addon");
            cmiAddon = new CMIAddon();
        }
        if(Bukkit.getPluginManager().isPluginEnabled("Essentials") && this.getConfig().getBoolean("addons.essentials")) {
            getLogger().info("Enabled Essentials Addon");
            essAddon = new EssentialsAddon();
        }
        getLogger().info("Reloaded RegionTeleport - v" + getDescription().getVersion());
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
