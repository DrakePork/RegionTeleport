package com.github.drakepork.regionteleport;

import com.github.drakepork.regionteleport.addons.CMIAddon;
import com.github.drakepork.regionteleport.addons.EssentialsAddon;
import com.github.drakepork.regionteleport.commands.*;
import com.github.drakepork.regionteleport.commands.regionclearcommands.RegionClearCommand;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.drakepork.regionteleport.utils.*;

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

        File spawnLoc = new File(this.getDataFolder() + File.separator + "spawnlocations.yml");
        if(!spawnLoc.exists()) {
            try {
                spawnLoc.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(Bukkit.getPluginManager().isPluginEnabled("CMI") && this.getConfig().getBoolean("addons.cmi")) {
            cmiAddon = new CMIAddon();
            getLogger().info("Enabled CMI Addon");
        }

        if(Bukkit.getPluginManager().isPluginEnabled("Essentials") && this.getConfig().getBoolean("addons.essentials")) {
            essAddon = new EssentialsAddon();
            getLogger().info("Enabled Essentials Addon");
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderCreator(this).register();
            getLogger().info("Enabled PlaceholderAPI Placeholders");
        }


        Objects.requireNonNull(this.getCommand("regiontp")).setExecutor(new RegionTeleportCommands(this));
        Objects.requireNonNull(this.getCommand("regiontp")).setTabCompleter(new RegionTeleportTabCompleter(this));

        Objects.requireNonNull(this.getCommand("regionclear")).setExecutor(new RegionClearCommand(this));
        Objects.requireNonNull(this.getCommand("regionclear")).setTabCompleter(new RegionClearTabCompleter());
        getLogger().info("Enabled RegionTeleport - v" + getDescription().getVersion());
    }

    public void onReload() {
        this.reloadConfig();
        new ConfigCreator(this).init();
        new LangCreator(this).init();
        File spawnLoc = new File(this.getDataFolder() + File.separator + "spawnlocations.yml");
        if(!spawnLoc.exists()) {
            try {
                spawnLoc.createNewFile();
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

    public boolean isDouble(String string) {
        try {
            Double.parseDouble(string);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }


    public void tellConsole(final String message){
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public String colourMessage(String message){
        message = translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message));
        return message;
    }

    public String translateHexColorCodes(String message) {
        final Pattern hexPattern = Pattern.compile("\\{#" + "([A-Fa-f0-9]{6})" + "}");
        Matcher matcher = hexPattern.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
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
