package com.github.drakepork.regionteleport.commands;

import com.github.drakepork.regionteleport.RegionTeleport;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.wrappers.Rotation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.github.drakepork.regionteleport.RegionTeleport.*;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextColor.fromHexString;
import static net.kyori.adventure.text.format.TextDecoration.*;
import static net.kyori.adventure.text.format.TextDecoration.State.FALSE;

public class Commands {
    private final RegionTeleport plugin;

    public Commands(RegionTeleport plugin) {
        this.plugin = plugin;
        createTeleportCommands();
        createClearCommands();
    }

    private void helpMessage(CommandSender sender) {
        Component line = text("          ", GRAY, STRIKETHROUGH);
        Component msg = text("").append(line)
                .append(text(" RegionTeleport ", fromHexString("#8dd9c3"), BOLD))
                .append(line);
        msg = msg.append(text("\n<> - Required, () - Optional", GRAY));
        msg = msg.append(text("\n- ", DARK_GRAY).append(text("/regiontp help (page)", fromHexString("#8dd9c3")).append(text(" - ", GRAY))
                .append(text("Shows this", WHITE))).clickEvent(ClickEvent.suggestCommand("/regiontp help")));

        msg = msg.append(text("\n- ", DARK_GRAY).append(text("/regiontp reload", fromHexString("#8dd9c3")).append(text(" - ", GRAY))
                .append(text("Reloads the plugin", WHITE))).clickEvent(ClickEvent.suggestCommand("/regiontp reload")));

        msg = msg.append(text("\n- ", DARK_GRAY).append(text("/regiontp list (page)", fromHexString("#8dd9c3")).append(text(" - ", GRAY))
                .append(text("Lists all spawn locations", WHITE))).clickEvent(ClickEvent.suggestCommand("/regiontp list")));

        msg = msg.append(text("\n- ", DARK_GRAY).append(text("/regiontp setspawn <name> (x y z) (yaw pitch) (world)", fromHexString("#8dd9c3")).append(text(" - ", GRAY))
                .append(text("Sets a spawn location", WHITE))).clickEvent(ClickEvent.suggestCommand("/regiontp setspawn")));

        msg = msg.append(text("\n- ", DARK_GRAY).append(text("/regiontp delspawn <name>", fromHexString("#8dd9c3")).append(text(" - ", GRAY))
                .append(text("Delete spawn location", WHITE))).clickEvent(ClickEvent.suggestCommand("/regiontp delspawn")));

        msg = msg.append(text("\n- ", DARK_GRAY).append(text("/regiontp tp \"<regions>\" \"<spawns>\" (-s)", fromHexString("#8dd9c3")).append(text(" - ", GRAY))
                .append(text("Tps from regions to spawn locations", WHITE))).clickEvent(ClickEvent.suggestCommand("/regiontp tp")));

        msg = msg.append(text("\n- ", DARK_GRAY).append(text("/regiontp tp <world> \"<regions>\" \"<spawns>\" (-s)", fromHexString("#8dd9c3")).append(text(" - ", GRAY))
                .append(text("World specific version of above", WHITE))).clickEvent(ClickEvent.suggestCommand("/regiontp tp")));

        msg = msg.append(text("\n- ", DARK_GRAY).append(text("/regionclear \"<regions>\" \"<options>\" (-s)", fromHexString("#8dd9c3")).append(text(" - ", GRAY))
                .append(text("Clears entities from regions", WHITE))).clickEvent(ClickEvent.suggestCommand("/regionclear")));

        msg = msg.append(text("\n- ", DARK_GRAY).append(text("/regionclear <world> \"<regions>\" \"<options>\" (-s)", fromHexString("#8dd9c3")).append(text(" - ", GRAY))
                .append(text("World specific version of above", WHITE))).clickEvent(ClickEvent.suggestCommand("/regionclear")));

        msg = msg.decorationIfAbsent(STRIKETHROUGH, FALSE);
        sender.sendMessage(msg);
    }

    private void spawnList(CommandSender sender, int page) {
        int totalPages = (int) Math.ceil(spawns.size() / 10.0);
        if(totalPages == 0) totalPages = 1;
        if(page > totalPages) page = totalPages;

        List<Spawn> spawnList = new ArrayList<>(spawns);
        int toDelete = 10 * (page - 1);
        if (toDelete != 0) {
            spawnList = spawnList.subList(toDelete, spawnList.size());
        }
        Component line = text("      ", GRAY, STRIKETHROUGH);
        Component msg = text("").append(line)
                .append(text(" Spawns Locations ", fromHexString("#16f75c"), BOLD))
                .append(line);
        int totalPos = 10 * page;
        int pos = 10 * (page - 1) + 1;
        for(Spawn spawn : spawnList) {
            if(pos > totalPos) break;
            Location loc = spawn.loc();
            Component spawnMsg = text("\n" + pos + ". ", DARK_GRAY).append(text(spawn.name(), fromHexString("#8dd9c3"), BOLD))
                    .append(text(" (", DARK_GRAY)).append(text("X ", GRAY)).append(text(loc.getBlockX(), GRAY))
                    .append(text(" Y ", GRAY)).append(text(loc.getBlockY(), GRAY))
                    .append(text(" Z ", GRAY)).append(text(loc.getBlockZ(), GRAY))
                    .append(text(")", DARK_GRAY));

            if(sender instanceof Player player) {
                spawnMsg = spawnMsg.hoverEvent(HoverEvent.showText(text("Click to teleport to this location", GRAY)))
                        .clickEvent(ClickEvent.callback(audience -> player.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.COMMAND)));
            }
            msg = msg.append(spawnMsg);
            pos++;
        }

        Component pages = text(page, fromHexString("#266d27")).append(text("/", GRAY)
                .append(text(totalPages, fromHexString("#266d27"))));
        if(sender instanceof Player) {
            int nextPage = page + 1;
            int prevPage = page - 1;
            Component next = text(" Next --->", GRAY).hoverEvent(HoverEvent.showText(text(">>>", GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> spawnList(sender, nextPage)));
            Component prev = text("<--- Prev ", GRAY).hoverEvent(HoverEvent.showText(text("<<<", GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> spawnList(sender, prevPage)));

            if (page == 1 && page != totalPages) {
                msg = msg.appendNewline().append(pages).append(next);
            } else if (page != 1 && page == totalPages) {
                msg = msg.appendNewline().append(prev).append(pages);
            } else if (page != 1) {
                msg = msg.appendNewline().append(prev).append(pages).append(next);
            }
        } else {
            msg = msg.appendNewline().append(pages);
        }
        msg = msg.decorationIfAbsent(STRIKETHROUGH, FALSE);
        sender.sendMessage(msg);
    }

    private List<ProtectedRegion> getRegions(World world, List<String> regionIds) {
        List<ProtectedRegion> regions = new ArrayList<>();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        if(container == null) return regions;
        RegionManager rm = container.get(BukkitAdapter.adapt(world));
        if(rm == null) return regions;

        for(String regionId : regionIds) {
            ProtectedRegion region = rm.getRegion(regionId);
            if(region != null) {
                regions.add(region);
            }
        }
        return regions;
    }

    private List<String> getRegionSuggestions(CommandSender sender) {
        return sender instanceof Player player ? getRegionSuggestions(player.getWorld()) : new ArrayList<>();
    }

    private List<String> getRegionSuggestions(World world) {
        List<String> regions = new ArrayList<>();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        if(container == null) return regions;
        RegionManager rm = container.get(BukkitAdapter.adapt(world));
        if(rm == null) return regions;
        regions.addAll(rm.getRegions().keySet());
        if(!regions.contains("__global__")) regions.add("__global__");
        return regions;
    }

    private List<String> getSpawnSuggestions() {
        List<String> spawnList = new ArrayList<>(spawns.stream().map(Spawn::name).toList());
        if(cmiAddon != null) {
            spawnList.addAll(cmiAddon.warps());
        }
        if(essAddon != null) {
            spawnList.addAll(essAddon.warps());
        }
        return spawnList;
    }

    private void executeTeleport(CommandSender sender, List<String> regionIds, List<String> spawnIds, World world, boolean silent) {
        boolean isGlobal = regionIds.contains("__global__");
        List<ProtectedRegion> regions = isGlobal ? new ArrayList<>() : getRegions(world, regionIds);

        List<Player> players = world.getPlayers().stream().filter(player -> {
            if (player.hasPermission("regionteleport.teleport.bypass")) return false;
            if (isGlobal) return true;
            BlockVector3 loc = BukkitAdapter.asBlockVector(player.getLocation());
            return regions.stream().anyMatch(region -> region.contains(loc));
        }).toList();

        Iterator<String> spawnIterator = Stream.generate(() -> spawnIds).flatMap(List::stream).iterator();

        players.forEach(player -> {
            String spawn = spawnIterator.next();
            Location loc = getSpawn(spawn);
            if(loc == null) {
                try {
                    throw CommandAPIBukkit.failWithAdventureComponent(prefix.append(text("Failed to get spawnlocation ", RED)
                            .append(text(spawn, GRAY)).append(text("! Cancelling..", RED))));
                } catch (WrapperCommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            player.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
        });

        if(!silent) {
            sender.sendMessage(prefix.append(text("Sent ", fromHexString("#55d66d")).append(text(players.size(), GRAY))
                    .append(text(" player(s) from region(s) ", fromHexString("#55d66d"))).append(text(regionIds.toString(), GRAY))
                            .append(text(" to spawnlocation(s) ", fromHexString("#55d66d"))).append(text(spawnIds.toString(), GRAY))));
        }
    }

    private List<String> getOptionSuggestions(SuggestionInfo<CommandSender> info) {
        List<String> options = new ArrayList<>(List.of("all", "monsters", "animals", "ambient", "items", "items:", "vehicles", "displays",
                "npcs", "npcs-only", "tamed", "tamed-only", "specific:", "named", "named-only", "named-only:"));
        String currentArg = info.currentArg();
        List<String> currentArgs = Arrays.stream(currentArg.split(" ")).toList();
        currentArgs.forEach(arg -> {
            if(arg.contains(":") && !options.contains(arg)) options.add(arg.startsWith("\"") ? arg.substring(1) : arg);
        });
        return options;
    }

    private void executeClear(CommandSender sender, List<String> regionIds, List<String> options, World world, boolean silent) throws WrapperCommandSyntaxException {
        boolean isGlobal = regionIds.contains("__global__");
        List<ProtectedRegion> regions = isGlobal ? new ArrayList<>() : getRegions(world, regionIds);

        List<Entity> entities = world.getEntities();

        if(!isGlobal) {
            entities = entities.stream().filter(entity -> !(entity instanceof Player) && regions.stream().anyMatch(region ->
                    region.contains(BukkitAdapter.asBlockVector(entity.getLocation())))).toList();
        }

        boolean all = options.contains("all");
        boolean namedOnly = options.stream().anyMatch(option -> option.startsWith("named-only"));
        boolean named = namedOnly || options.contains("named");
        boolean tamedOnly = options.contains("tamed-only");
        boolean tamed = tamedOnly || options.contains("tamed");
        boolean npcsOnly = options.contains("npcs-only");
        boolean npcs = npcsOnly || options.contains("npcs");
        boolean items = options.stream().anyMatch(option -> option.startsWith("items"));

        List<EntityType> specificTypes = new ArrayList<>();
        if(options.stream().anyMatch(option -> option.startsWith("specific:"))) {
            String specifics = options.stream().filter(option -> option.startsWith("specific:")).findFirst().orElse("");
            String[] split = specifics.split(":");

            if(split.length < 2 || split[1].isEmpty()) {
                throw CommandAPIBukkit.failWithAdventureComponent(prefix.append(text("No entity types specified in specific! Example: specific:zombie,cow", RED)));
            }
            List<String> specific = Arrays.asList(split[1].split(","));
            if(!specific.isEmpty()) {
                List<String> invalid = new ArrayList<>();
                specific.forEach(type -> {
                    try {
                        EntityType entityType = EntityType.valueOf(type.toUpperCase());
                        specificTypes.add(entityType);
                    } catch (IllegalArgumentException e) {
                        invalid.add(type);
                    }
                });

                if(!invalid.isEmpty()) {
                    throw CommandAPIBukkit.failWithAdventureComponent(prefix.append(text("Invalid entity type(s) ", RED).append(text(invalid.toString(), GRAY))));
                }
            }
        }

        List<Class<?>> optionsList = getOptions(options);

        if(all) {
            optionsList.addAll(List.of(Monster.class, Animals.class, Ambient.class, Item.class, Vehicle.class));
        }

        if(items) {
            optionsList.add(Item.class);
        }

        entities = entities.stream().filter(entity -> optionsList.stream().anyMatch(type -> type.equals(Vehicle.class) ? type.isInstance(entity) && !(entity instanceof LivingEntity)
                : type.isInstance(entity)) || specificTypes.contains(entity.getType())).toList();

        if(items && options.stream().anyMatch(option -> option.startsWith("items:"))) {
            String itemString = options.stream().filter(option -> option.startsWith("items:")).findFirst().orElse("");
            if(!itemString.isEmpty()) {
                List<String> invalid = new ArrayList<>();
                List<Material> itemTypes = Arrays.stream(itemString.split(":")[1].split(",")).map(String::toUpperCase).map(mat -> {
                    Material material = Material.getMaterial(mat);
                    if(material == null) {
                        invalid.add(mat);
                    }
                    return material;
                }).toList();

                if(!invalid.isEmpty()) {
                    throw CommandAPIBukkit.failWithAdventureComponent(prefix.append(text("Invalid item type(s) ", RED).append(text(invalid.toString(), GRAY))));
                }
                if(!itemTypes.isEmpty()) {
                    entities = entities.stream().filter(entity -> !(entity instanceof Item item) || itemTypes.contains(item.getItemStack().getType())).toList();
                }
            }
        }

        if(!named) {
            entities = entities.stream().filter(entity -> entity instanceof Item item ? !item.getItemStack().getItemMeta().hasDisplayName() : entity.customName() == null).toList();
        }
        if(namedOnly) {
            entities = entities.stream().filter(entity -> entity instanceof Item item ? item.getItemStack().getItemMeta().hasDisplayName() : entity.customName() != null).toList();
            String nameString = options.stream().filter(option -> option.startsWith("named-only:")).findFirst().orElse("");

            if(!nameString.isEmpty()) {
                List<String> names = Arrays.asList(nameString.split(":")[1].split(","));
                if(!names.isEmpty()) {
                    entities = entities.stream().filter(entity -> {
                        Component compName = entity instanceof Item item ? item.getItemStack().getItemMeta().displayName() : entity.customName();
                        String name = PlainTextComponentSerializer.plainText().serialize(compName);
                        return names.contains(name);
                    }).toList();
                }
            }
        }

        if(!tamed) {
            entities = entities.stream().filter(entity -> !(entity instanceof Tameable tameable && tameable.isTamed())).toList();
        }
        if(tamedOnly) {
            entities = entities.stream().filter(entity -> entity instanceof Tameable tameable && tameable.isTamed()).toList();
        }

        if(!npcs) {
            entities = entities.stream().filter(entity -> !entity.hasMetadata("NPC")).toList();
        }
        if(npcsOnly) {
            entities = entities.stream().filter(entity -> entity.hasMetadata("NPC")).toList();
        }

        int amount = entities.size();
        HashMap<EntityType, Long> entityCount = entities.stream().collect(HashMap::new, (map, entity) -> map.merge(entity.getType(), 1L, Long::sum), HashMap::putAll);
        entities.forEach(Entity::remove);
        if(!silent) {
            sender.sendMessage(prefix.append(text("Removed from region(s) ", fromHexString("#55d66d"))).append(text(regionIds.toString(), GRAY))
                    .append(text(" a total of ", fromHexString("#55d66d"))).append(text(amount, GRAY)).append(text(" entities. ", fromHexString("#55d66d")))
                    .append(text(entityCount.toString(), GRAY)));
        }
    }

    private List<Class<?>> getOptions(List<String> options) {
        List<Class<?>> optionsList = new ArrayList<>();
        for(String option : options) {
            switch(option) {
                case "monsters" ->  optionsList.add(Monster.class);
                case "animals" ->  optionsList.add(Animals.class);
                case "ambient" -> optionsList.add(Ambient.class);
                case "vehicles" -> optionsList.add(Vehicle.class);
                case "displays" -> optionsList.add(Display.class);
            }
        }
        return optionsList;
    }

    private void createSpawn(CommandSender sender, String spawnName, Location loc, Rotation rot, World world) {
        if(spawns.stream().anyMatch(spawn -> spawn.name().equals(spawnName))) {
            sender.sendMessage(prefix.append(text("Spawn location with name ", RED).append(text(spawnName, GRAY)).append(text(" already exists!", RED))));
            return;
        }

        YamlConfiguration spawnConf = YamlConfiguration.loadConfiguration(spawnLoc);
        spawnConf.set(spawnName + ".world", world.getName());
        spawnConf.set(spawnName + ".x", loc.x());
        spawnConf.set(spawnName + ".y", loc.y());
        spawnConf.set(spawnName + ".z", loc.z());
        spawnConf.set(spawnName + ".yaw", rot.getYaw());
        spawnConf.set(spawnName + ".pitch", rot.getPitch());
        try {
            spawnConf.save(spawnLoc);
            spawns.add(new Spawn(spawnName, new Location(world, loc.x(), loc.y(), loc.z(), rot.getYaw(), rot.getPitch())));
            sender.sendMessage(prefix.append(text("Successfully set spawnlocation ", fromHexString("#55d66d")).append(text(spawnName, GRAY))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createTeleportCommands() {
        YamlConfiguration spawnConf = YamlConfiguration.loadConfiguration(spawnLoc);

        CommandAPICommand help = new CommandAPICommand("help")
                .withPermission("regionteleport.command.help")
                .executes((sender, args) -> {
                    helpMessage(sender);
                });

        CommandAPICommand setSpawn = new CommandAPICommand("setspawn")
                .withPermission("regionteleport.command.setspawn")
                .withArguments(new StringArgument("name"))
                .executesPlayer((player, args) -> {
                    createSpawn(player, (String) args.get("name"), player.getLocation(), new Rotation(player.getYaw(), player.getPitch()), player.getWorld());
                });
        CommandAPICommand setSpawnLoc = new CommandAPICommand("setspawn")
                .withPermission("regionteleport.command.setspawn")
                .withArguments(new StringArgument("name"))
                .withOptionalArguments(new LocationArgument("coordinates"))
                .withOptionalArguments(new RotationArgument("yaw/pitch"))
                .executesPlayer((player, args) -> {
                    Location loc = (Location) args.getOrDefault("coordinates", player.getLocation());
                    Rotation rot = (Rotation) args.getOrDefault("yaw/pitch", new Rotation(player.getYaw(), player.getPitch()));
                    createSpawn(player, (String) args.get("name"), loc, rot, player.getWorld());
                });
        CommandAPICommand setSpawnConsole = new CommandAPICommand("setspawn")
                .withPermission("regionteleport.command.setspawn")
                .withArguments(new StringArgument("name"))
                .withArguments(new LocationArgument("coordinates"))
                .withArguments(new RotationArgument("yaw/pitch"))
                .withArguments(new WorldArgument("world"))
                .executes((sender, args) -> {
                    createSpawn(sender, (String) args.get("name"), (Location) args.get("coordinates"), (Rotation) args.get("yaw/pitch"), (World) args.get("world"));
                });

        CommandAPICommand delSpawn = new CommandAPICommand("delspawn")
                .withPermission("regionteleport.command.delspawn")
                .withArguments(new StringArgument("name").replaceSuggestions(ArgumentSuggestions.stringsAsync(info ->
                        CompletableFuture.supplyAsync(() -> getSpawnSuggestions().toArray(new String[0])))))
                .executes((sender, args) -> {
                    String spawnName = (String) args.get("name");
                    spawnConf.set(spawnName, null);
                    try {
                        spawnConf.save(spawnLoc);
                        spawns.removeIf(spawn -> spawn.name().equals(spawnName));
                        sender.sendMessage(prefix.append(text("Successfully deleted spawnlocation ", fromHexString("#55d66d")).append(text(spawnName, GRAY))));
                    } catch (IOException e) {
                        e.printStackTrace();
                        sender.sendMessage(prefix.append(text("Failed to delete spawnlocation ", RED).append(text(spawnName, GRAY)).append(text("!", RED))));
                    }
                });

        CommandAPICommand list = new CommandAPICommand("list")
                .withPermission("regionteleport.command.list")
                .withOptionalArguments(new IntegerArgument("page", 1))
                .executes((sender, args) -> {
                    int page = (int) args.getOrDefault("page", 1);
                    spawnList(sender, page);
                });

        CommandAPICommand reload = new CommandAPICommand("reload")
                .withPermission("regionteleport.command.reload")
                .executes((sender, args) -> {
                    plugin.onReload();
                    sender.sendMessage(prefix.append(text("Successfully reloaded the plugin!", fromHexString("#55d66d"))));
                });

        CommandAPICommand teleportPlayer = new CommandAPICommand("teleport")
                .withAliases("tp")
                .withPermission("regionteleport.command.teleport")
                .withArguments(new ListArgumentBuilder<String>("region(s)")
                        .withList(info -> getRegionSuggestions(info.sender()))
                        .withMapper(String::toLowerCase)
                        .buildText()
                )
                .withArguments(new ListArgumentBuilder<String>("spawn(s)")
                        .withList(info -> getSpawnSuggestions())
                        .withMapper(String::toLowerCase)
                        .buildText()
                )
                .withOptionalArguments(new MultiLiteralArgument("silent", "-s"))
                .executesPlayer((player, args) -> {
                    List<String> regions = (List<String>) args.get("region(s)");
                    List<String> spawnList = (List<String>) args.get("spawn(s)");
                    executeTeleport(player, regions, spawnList, player.getWorld(), args.get("silent") != null);
                });

        CommandAPICommand teleportConsole = new CommandAPICommand("teleport")
                .withAliases("tp")
                .withPermission("regionteleport.command.teleport")
                .withArguments(new WorldArgument("world"))
                .withArguments(new ListArgumentBuilder<String>("region(s)")
                        .withList(info -> getRegionSuggestions((World) info.previousArgs().get(0)))
                        .withMapper(String::toLowerCase)
                        .buildText()
                )
                .withArguments(new ListArgumentBuilder<String>("spawn(s)")
                        .withList(info -> getSpawnSuggestions())
                        .withMapper(String::toLowerCase)
                        .buildText()
                )
                .withOptionalArguments(new MultiLiteralArgument("silent", "-s"))
                .executes((sender, args) -> {
                    List<String> regions = (List<String>) args.get("region(s)");
                    List<String> spawnList = (List<String>) args.get("spawn(s)");
                    World world = (World) args.get("world");
                    executeTeleport(sender, regions, spawnList, world, args.get("silent") != null);
                });

        new CommandAPICommand("regiontp")
                .withPermission("regionteleport.command.help")
                .withAliases("regionteleport")
                .withSubcommands(reload, help, setSpawn, setSpawnLoc, setSpawnConsole, delSpawn, list, teleportPlayer, teleportConsole)
                .executes((sender, args) -> {
                    helpMessage(sender);
                })
                .withUsage(
                        "/regiontp help (page)",
                        "/regiontp reload",
                        "/regiontp list (page)",
                        "/regiontp setspawn <name> (coords) (yaw/pitch) (world)",
                        "/regiontp delspawn <name>",
                        "/regiontp tp \"<regions>\" \"<spawns>\" (-s)",
                        "/regiontp tp <world> \"<regions>\" \"<spawns>\" (-s)"
                )
                .register();
    }

    private void createClearCommands() {
        new CommandAPICommand("regionclear")
                .withPermission("regionteleport.command.clear")
                .withArguments(new ListArgumentBuilder<String>("region(s)")
                        .withList(info -> getRegionSuggestions(info.sender()))
                        .withMapper(String::toLowerCase)
                        .buildText()
                )
                .withArguments(new ListArgumentBuilder<String>("options")
                        .withList(this::getOptionSuggestions)
                        .withMapper(String::toLowerCase)
                        .buildText()
                )
                .withOptionalArguments(new MultiLiteralArgument("silent", "-s"))
                .executesPlayer((player, args) -> {
                    List<String> regions = (List<String>) args.get("region(s)");
                    List<String> optionsList = (List<String>) args.get("options");
                    executeClear(player, regions, optionsList, player.getWorld(), args.get("silent") != null);
                }).register();

        new CommandAPICommand("regionclear")
                .withPermission("regionteleport.command.clear")
                .withArguments(new WorldArgument("world"))
                .withArguments(new ListArgumentBuilder<String>("region(s)")
                        .withList(info -> getRegionSuggestions((World) info.previousArgs().get(0)))
                        .withMapper(String::toLowerCase)
                        .buildText()
                )
                .withArguments(new ListArgumentBuilder<String>("options")
                        .withList(this::getOptionSuggestions)
                        .withMapper(String::toLowerCase)
                        .buildText()
                )
                .withOptionalArguments(new MultiLiteralArgument("silent", "-s"))
                .executes((sender, args) -> {
                    List<String> regions = (List<String>) args.get("region(s)");
                    List<String> optionsList = (List<String>) args.get("options");
                    World world = (World) args.get("world");
                    executeClear(sender, regions, optionsList, world, args.get("silent") != null);
                }).register();
    }
}
