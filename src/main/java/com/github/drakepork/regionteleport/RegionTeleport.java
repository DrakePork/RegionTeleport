package com.github.drakepork.regionteleport;

import com.github.drakepork.regionteleport.addons.CMIAddon;
import com.github.drakepork.regionteleport.addons.ESSAddon;
import com.github.drakepork.regionteleport.commands.*;
import com.github.drakepork.regionteleport.listeners.EntityDeath;
import com.github.drakepork.regionteleport.utils.flags.RegionTeleportOnEntryHandler;
import com.github.drakepork.regionteleport.utils.flags.RegionTeleportOnExitHandler;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.session.SessionManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.arguments.CommandAPIArgumentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.drakepork.regionteleport.utils.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class RegionTeleport extends JavaPlugin {
    public static CMIAddon cmiAddon = null;
    public static ESSAddon essAddon = null;
    public static boolean papiEnabled = false;

    public static File spawnLoc;
    public static final List<Spawn> spawns = new ArrayList<>();
    public record Spawn(String name, Location loc) { }

    public static StateFlag MOB_LOOT_DROP;
    public static StateFlag PLAYER_LOOT_DROP;
    public static StringFlag REGIONTP_ON_ENTRY;
    public static StringFlag REGIONTP_ON_EXIT;

    public static final Component prefix = MiniMessage.miniMessage().deserialize("<#8dd9c3><b>RegionTeleport</b></#8dd9c3><white> » </white>");

    @Override
    public void onLoad() {
        int pluginId = 9090;
        Metrics metrics = new Metrics(this, pluginId);
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));

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

    public static Location getSpawnLocation (YamlConfiguration spawnConf, String spawnId) {
        World w = Bukkit.getWorld(Objects.requireNonNull(spawnConf.getString(spawnId + ".world")));
        float yaw = (float) spawnConf.getDouble(spawnId + ".yaw");
        float pitch = (float) spawnConf.getDouble(spawnId + ".pitch");
        double x = spawnConf.getDouble(spawnId + ".x");
        double y = spawnConf.getDouble(spawnId + ".y");
        double z = spawnConf.getDouble(spawnId + ".z");
        return new Location(w, x, y, z, yaw, pitch);
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        new ConfigCreator(this).init();

        spawnLoc = new File( getDataFolder() + File.separator + "spawnlocations.yml");
        if(!spawnLoc.exists()) {
            try {
                spawnLoc.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            YamlConfiguration spawnConf = YamlConfiguration.loadConfiguration(spawnLoc);
            spawnConf.getKeys(false).forEach(spawn -> spawns.add(new Spawn(spawn, getSpawnLocation(spawnConf, spawn))));
        }

        new Commands(this);

        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(RegionTeleportOnEntryHandler.FACTORY(this), null);
        sessionManager.registerHandler(RegionTeleportOnExitHandler.FACTORY(this), null);

        if(Bukkit.getPluginManager().isPluginEnabled("CMI") && getConfig().getBoolean("addons.cmi")) {
            cmiAddon = new CMIAddon();
            getLogger().info("Enabled CMI Addon");
        }

        if(Bukkit.getPluginManager().isPluginEnabled("Essentials") && getConfig().getBoolean("addons.essentials")) {
            essAddon = new ESSAddon();
            getLogger().info("Enabled Essentials Addon");
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderCreator(this).register();
            papiEnabled = true;
            getLogger().info("Enabled PlaceholderAPI Placeholders");
        }

        getServer().getPluginManager().registerEvents(new EntityDeath(), this);

        getLogger().info("Enabled RegionTeleport - v" + getPluginMeta().getVersion());
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        getLogger().info("Disabled RegionTeleport - v" + getDescription().getVersion());
    }

    public void onReload() {
        reloadConfig();
        new ConfigCreator(this).init();
        spawnLoc = new File( getDataFolder() + File.separator + "spawnlocations.yml");
        if(!spawnLoc.exists()) {
            try {
                spawnLoc.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            YamlConfiguration spawnConf = YamlConfiguration.loadConfiguration(spawnLoc);
            spawns.clear();
            spawnConf.getKeys(false).forEach(spawn -> spawns.add(new Spawn(spawn, getSpawnLocation(spawnConf, spawn))));
        }

        if(Bukkit.getPluginManager().isPluginEnabled("CMI") && getConfig().getBoolean("addons.cmi")) {
            getLogger().info("Enabled CMI Addon");
            cmiAddon = new CMIAddon();
        }
        if(Bukkit.getPluginManager().isPluginEnabled("Essentials") && getConfig().getBoolean("addons.essentials")) {
            getLogger().info("Enabled Essentials Addon");
            essAddon = new ESSAddon();
        }
        getLogger().info("Reloaded RegionTeleport - v" + getDescription().getVersion());
    }

    public static Location getSpawn(String value) {
        if(!value.contains(":")) {
            Spawn spawnLoc = spawns.stream().filter(spawn -> spawn.name().equalsIgnoreCase(value)).findFirst().orElse(null);
            if(spawnLoc != null) return spawnLoc.loc();
        } else {
            String[] split = value.split(":");
            if(split[0].equalsIgnoreCase("cmi") && cmiAddon != null) {
                return cmiAddon.warpLoc(split[1]);
            } else if(split[0].equalsIgnoreCase("ess") && essAddon != null) {
                return essAddon.warpLoc(split[1]);
            }
        }
        return null;
    }
}
