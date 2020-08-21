package src.com.drakepork.regionteleport;

import src.com.drakepork.regionteleport.Commands.RegionTeleportCommands;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import src.com.drakepork.regionteleport.Utils.ConfigCreator;
import src.com.drakepork.regionteleport.Utils.LangCreator;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class RegionTeleport extends JavaPlugin {
    FileConfiguration config = this.getConfig();
    private static RegionTeleport instance;


    public static RegionTeleport getInstance() {
        return instance;
    }

    private LangCreator lang;
    private ConfigCreator ConfigCreator;

    @Override
    public void onEnable() {
        instance = this;

        config.addDefault("plugin-prefix", "&f[&2RegionTeleport&f]");
        config.options().copyDefaults(true);
        saveConfig();

        ConfigCreator.init();
        lang.init();

        File spawnloc = new File(this.getDataFolder() + File.separator + "spawnlocations.yml");
        if(!spawnloc.exists()) {
            try {
                spawnloc.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        getCommand("regiontp").setExecutor(new RegionTeleportCommands(this));
        getLogger().info("Enabled RegionTeleport - version " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled RegionTeleport - version " + getDescription().getVersion());
        instance = null;
    }

    public String getPrefix() {
        return translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', config.getString("plugin-prefix")));
    }

    public String translateHexColorCodes(String message) {
        final Pattern hexPattern = Pattern.compile("#" + "([A-Fa-f0-9]{6})");
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
