package com.github.drakepork.regionteleport.commands.regionclearcommands;

import com.github.drakepork.regionteleport.RegionTeleport;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class RegionClearDisplay implements RegionClear {
    @Override
    public void clearRegion(@NotNull RegionTeleport plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        File lang = new File(plugin.getDataFolder() + File.separator
                + "lang" + File.separator + plugin.getConfig().getString("lang-file"));
        FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
        String prefix = Objects.requireNonNull(langConf.getString("region-clear.prefix"));

        // /regionclear <region> (types) (-s) (-w:[world])
        // Types: -all -monsters -animals -ambient -items -vehicles || -displays -specific:[entity names] -npcs(-only) -tamed(-only) -named(-only:[name])
        if(args.length > 1) {
            List<String> allTypes = new ArrayList<>(List.of(args));
            allTypes.remove(0);
            World world = null;
            if(sender instanceof Player player) {
                world = player.getWorld();
            }
            List<EntityType> specificTypes = new ArrayList<>();
            List<Material> specificItems = new ArrayList<>();
            boolean silent = false;
            boolean removeItems = false;
            boolean removeVehicles = false;
            boolean removeMonsters = false;
            boolean removeAnimals = false;
            boolean removeAmbient = false;
            boolean removeDisplays = false;

            boolean removeAll = false;


            String named = null;
            boolean namedOnly = false;
            boolean removeTamed = false;
            boolean tamedOnly = false;
            boolean removeNpcs = false;
            boolean npcsOnly = false;

            for(String type : allTypes) {
                String lowerType = type.toLowerCase();
                if(lowerType.contains("-w")) {
                    String[] worldName = type.split(":");
                    if(worldName.length > 1) {
                        if(Bukkit.getWorld(worldName[1]) != null) {
                            world = Objects.requireNonNull(Bukkit.getWorld(worldName[1]));
                        } else {
                            sender.sendMessage(plugin.colourMessage(prefix + Objects.requireNonNull(langConf.getString("region-clear.no-such-world")).replaceAll("\\[name]", worldName[1])));
                            return;
                        }
                    } else {
                        sender.sendMessage(plugin.colourMessage(prefix + langConf.getString("region-clear.wrong-usage-specific")));
                        return;
                    }
                } else if(lowerType.contains("-named")) {
                    String[] specificName = type.split(":");
                    if(specificName.length > 1) {
                        named = specificName[1];
                    } else {
                        named = "";
                    }
                    if(specificName[0].contains("-only")) {
                        namedOnly = true;
                    }

                } else if(lowerType.contains("-specific")) {
                    if(type.split(":").length > 1) {
                        String[] entities = type.split(":")[1].split(",");
                        for(String entity : entities) {
                            try {
                                EntityType entityType = EntityType.valueOf(entity.toUpperCase());
                                specificTypes.add(entityType);
                            } catch (IllegalArgumentException ignored) {
                                sender.sendMessage(plugin.colourMessage(prefix + Objects.requireNonNull(langConf.getString("region-clear.no-such-specific")).replaceAll("\\[name]", entity)));
                                return;
                            }
                        }
                    } else {
                        sender.sendMessage(plugin.colourMessage(prefix + langConf.getString("region-clear.wrong-usage-specific")));
                        return;
                    }
                } else if(lowerType.equalsIgnoreCase("-monsters")) {
                    removeMonsters = true;
                } else if(lowerType.equalsIgnoreCase("-animals")) {
                    removeAnimals = true;
                } else if(lowerType.contains("-items")) {
                    if(type.split(":").length > 1) {
                        String[] items = type.split(":")[1].split(",");
                        for(String item : items) {
                            if(Material.getMaterial(item.toUpperCase()) != null) {
                                specificItems.add(Material.getMaterial(item.toUpperCase()));
                            } else {
                                sender.sendMessage(plugin.colourMessage(prefix + Objects.requireNonNull(langConf.getString("region-clear.no-such-item")).replaceAll("\\[name]", item)));
                                return;
                            }
                        }
                    }
                    removeItems = true;
                } else if(lowerType.equalsIgnoreCase("-ambient")) {
                    removeAmbient = true;
                } else if(lowerType.equalsIgnoreCase("-vehicles")) {
                    removeVehicles = true;
                } else if(lowerType.contains("-npcs")) {
                    removeNpcs = true;
                    if(lowerType.contains("-only")) {
                        npcsOnly = true;
                    }
                } else if(lowerType.contains("-tamed")) {
                    removeTamed = true;
                    if(lowerType.contains("-only")) {
                        tamedOnly = true;
                    }
                } else if(lowerType.equalsIgnoreCase("-displays")) {
                    removeDisplays = true;
                } else if(lowerType.equalsIgnoreCase("-s")) {
                    silent = true;
                } else if(lowerType.equalsIgnoreCase("-all")) {
                    removeAll = true;
                }
            }
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            assert world != null;
            RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world));
            assert regionManager != null;

            List<String> regionIds = Arrays.asList(args[0].split(","));
            List<String> falseRegions = new ArrayList<>();

            boolean isGlobal = false;

            for (String region : regionIds) {
                if (!regionManager.hasRegion(region) && !region.equalsIgnoreCase("__global__")) {
                    falseRegions.add(region);
                } else if (region.equalsIgnoreCase("__global__")) {
                    regionIds = Collections.singletonList(region);
                    falseRegions = new ArrayList<>();
                    isGlobal = true;
                    break;
                }
            }

            if (falseRegions.isEmpty()) {
                List<ProtectedRegion> regions = new ArrayList<>();
                if(!isGlobal) {
                    for (String regionId : regionIds) {
                        regions.add(regionManager.getRegion(regionId));
                    }
                }

                List<Entity> entities = world.getEntities();
                boolean finalRemoveMonsters = removeMonsters;
                boolean finalRemoveAnimals = removeAnimals;
                boolean finalRemoveAmbient = removeAmbient;
                boolean finalRemoveAll = removeAll;
                boolean finalRemoveVehicles = removeVehicles;
                boolean finalRemoveItems = removeItems;
                boolean finalRemoveNpcs = removeNpcs;
                String finalNamed = named;
                boolean finalRemoveDisplays = removeDisplays;
                boolean finalRemoveTamed = removeTamed;
                boolean finalSilent = silent;
                boolean finalNpcsOnly = npcsOnly;
                boolean finalTamedOnly = tamedOnly;
                boolean finalNamedOnly = namedOnly;
                boolean finalIsGlobal = isGlobal;
                List<String> finalRegionIds = regionIds;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<Entity> entitiesToRemove = new ArrayList<>();
                        HashMap<EntityType, Integer> removals = new HashMap<>();
                        int totalAmount = 0;
                        for(Entity entity : entities) {
                            if (!(entity instanceof Player)) {
                                Location loc = entity.getLocation();
                                boolean insideRegion = regions.stream().anyMatch(region -> region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                                if (finalIsGlobal || insideRegion) {
                                    EntityType entityType = entity.getType();
                                    boolean removeEntity = finalRemoveAll;
                                    if (finalRemoveMonsters && entity instanceof Monster) {
                                        removeEntity = true;
                                    } else if (finalRemoveAnimals && entity instanceof Animals) {
                                        removeEntity = true;
                                    } else if (finalRemoveAmbient && entity instanceof Ambient) {
                                        removeEntity = true;
                                    } else if (finalRemoveVehicles && entity instanceof Vehicle && !(entity instanceof LivingEntity)) {
                                        removeEntity = true;
                                    } else if (finalRemoveItems && entity instanceof Item item) {
                                        if(specificItems.size() > 0) {
                                            if(specificItems.contains(item.getItemStack().getType())) {
                                                removeEntity = true;
                                            }
                                        } else {
                                            removeEntity = true;
                                        }
                                    } else if (finalRemoveDisplays && entity instanceof Display) {
                                        removeEntity = true;
                                    }

                                    if (!finalRemoveDisplays && entity instanceof Display) {
                                        removeEntity = false;
                                    }

                                    if (specificTypes.contains(entityType)) {
                                        removeEntity = true;
                                    }

                                    if (finalNamed == null && entity.getCustomName() != null) {
                                        removeEntity = false;
                                    } else if (finalNamed != null) {
                                        if(finalNamedOnly) {
                                            if(finalNamed.isEmpty()) {
                                                if (entity.getCustomName() == null) {
                                                    removeEntity = false;
                                                }
                                            } else {
                                                if (entity.getCustomName() == null || !entity.getCustomName().equalsIgnoreCase(finalNamed)) {
                                                    removeEntity = false;
                                                }
                                            }
                                        } else if(!finalNamed.isEmpty()) {
                                            if (entity.getCustomName() != null && !entity.getCustomName().equalsIgnoreCase(finalNamed)) {
                                                removeEntity = false;
                                            }
                                        }
                                    }

                                    if (!finalRemoveTamed && entity instanceof Tameable tamed && tamed.isTamed()) {
                                        removeEntity = false;
                                    } else if(finalRemoveTamed && finalTamedOnly && (!(entity instanceof Tameable tamed) || !tamed.isTamed())) {
                                        removeEntity = false;
                                    }

                                    if (!finalRemoveNpcs && entity.hasMetadata("NPC")) {
                                        removeEntity = false;
                                    } else if(finalRemoveNpcs && finalNpcsOnly && !entity.hasMetadata("NPC")) {
                                        removeEntity = false;
                                    }

                                    if (removeEntity) {
                                        entitiesToRemove.add(entity);
                                        int amount = 1;
                                        if (removals.containsKey(entityType)) {
                                            amount += removals.get(entityType);
                                        }
                                        removals.put(entityType, amount);
                                        totalAmount++;
                                    }
                                }
                            }
                        }
                        int finalTotalAmount = totalAmount;
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            entitiesToRemove.forEach(Entity::remove);
                            if (!finalSilent) {
                                String successMsg = Objects.requireNonNull(langConf.getString("region-clear.successful-clear")).replaceAll("\\[name]", finalRegionIds.toString());
                                successMsg = successMsg.replaceAll("\\[amount]", String.valueOf(finalTotalAmount));
                                StringBuilder entitySpecific = new StringBuilder();
                                String entitySpecificFormat = langConf.getString("region-clear.successful-clear-entity-specific");
                                int i = 1;
                                for (EntityType entityType : removals.keySet()) {
                                    entitySpecific.append(Objects.requireNonNull(entitySpecificFormat).replaceAll("\\[entity]", String.valueOf(entityType)).replaceAll("\\[amount]", String.valueOf(removals.get(entityType))));
                                    if (i != removals.size()) entitySpecific.append(" ");
                                    i++;
                                }
                                successMsg = successMsg.replaceAll("\\[entity-specific]", entitySpecific.toString());
                                sender.sendMessage(plugin.colourMessage(prefix + successMsg));
                            }
                        });
                    }
                }.runTaskAsynchronously(plugin);
            } else {
                sender.sendMessage(plugin.colourMessage(prefix + Objects.requireNonNull(langConf.getString("region-clear.no-such-region")).replaceAll("\\[name]", falseRegions.toString())));
            }
        } else {
            sender.sendMessage(plugin.colourMessage(prefix + langConf.getString("region-clear.wrong-usage")));
        }
    }
}
