package com.github.drakepork.regionteleport;

import com.github.drakepork.regionteleport.addons.CMIAddon;
import com.github.drakepork.regionteleport.addons.ESSAddon;
import com.github.drakepork.regionteleport.commands.*;
import com.github.drakepork.regionteleport.commands.regionclear.RegionClearCommand;
import com.github.drakepork.regionteleport.listeners.EntityDeath;
import com.github.drakepork.regionteleport.utils.flags.RegionTeleportOnEntryHandler;
import com.github.drakepork.regionteleport.utils.flags.RegionTeleportOnExitHandler;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.session.SessionManager;
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
    public static CMIAddon cmiAddon = null;
    public static ESSAddon essAddon = null;
    public static File spawnLoc;
    public static boolean papiEnabled = false;
    public static StateFlag MOB_LOOT_DROP;
    public static StateFlag PLAYER_LOOT_DROP;
    public static StringFlag REGIONTP_ON_ENTRY;
    public static StringFlag REGIONTP_ON_EXIT;

    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag mobLootDropFlag = new StateFlag("mob-loot-drop", true);
            StateFlag playerLootDropFlag = new StateFlag("player-loot-drop", true);
            StringFlag regiontpOnEntryFlag = new StringFlag("regiontp-on-entry");
            StringFlag regiontpOnExitFlag = new StringFlag("regiontp-on-exit");
            registry.register(mobLootDropFlag);
            registry.register(playerLootDropFlag);
            registry.register(regiontpOnEntryFlag);
            registry.register(regiontpOnExitFlag);
            MOB_LOOT_DROP = mobLootDropFlag;
            PLAYER_LOOT_DROP = playerLootDropFlag;
            REGIONTP_ON_ENTRY = regiontpOnEntryFlag;
            REGIONTP_ON_EXIT = regiontpOnExitFlag;
            getLogger().info("Loaded Custom Flags");
        } catch (FlagConflictException ignored) {
        }
    }

    @Override
    public void onEnable() {
        new ConfigCreator(this).init();
        new LangCreator(this).init();
        int pluginId = 9090;
        Metrics metrics = new Metrics(this, pluginId);

        spawnLoc = new File( this.getDataFolder() + File.separator + "spawnlocations.yml");
        if(!spawnLoc.exists()) {
            try {
                spawnLoc.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(RegionTeleportOnEntryHandler.FACTORY(this), null);
        sessionManager.registerHandler(RegionTeleportOnExitHandler.FACTORY(this), null);

        if(Bukkit.getPluginManager().isPluginEnabled("CMI") && this.getConfig().getBoolean("addons.cmi")) {
            cmiAddon = new CMIAddon();
            getLogger().info("Enabled CMI Addon");
        }

        if(Bukkit.getPluginManager().isPluginEnabled("Essentials") && this.getConfig().getBoolean("addons.essentials")) {
            essAddon = new ESSAddon();
            getLogger().info("Enabled Essentials Addon");
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderCreator(this).register();
            papiEnabled = true;
            getLogger().info("Enabled PlaceholderAPI Placeholders");
        }


        getServer().getPluginManager().registerEvents(new EntityDeath(), this);

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
        spawnLoc = new File( this.getDataFolder() + File.separator + "spawnlocations.yml");
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
            essAddon = new ESSAddon();
        }
        getLogger().info("Reloaded RegionTeleport - v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled RegionTeleport - v" + getDescription().getVersion());
    }

    public static String colourMessage(String message) {
        message = translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message));
        return message;
    }

    public static String translateHexColorCodes(String message) {
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
