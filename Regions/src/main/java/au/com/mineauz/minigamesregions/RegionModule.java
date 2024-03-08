package au.com.mineauz.minigamesregions;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.MinigameModule;
import au.com.mineauz.minigames.minigame.modules.ModuleFactory;
import au.com.mineauz.minigames.objects.MgRegion;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.actions.ActionInterface;
import au.com.mineauz.minigamesregions.actions.ActionRegistry;
import au.com.mineauz.minigamesregions.conditions.ACondition;
import au.com.mineauz.minigamesregions.conditions.ConditionRegistry;
import au.com.mineauz.minigamesregions.executors.NodeExecutor;
import au.com.mineauz.minigamesregions.executors.RegionExecutor;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.menu.MenuItemNode;
import au.com.mineauz.minigamesregions.menu.MenuItemRegenRegion;
import au.com.mineauz.minigamesregions.menu.MenuItemRegion;
import au.com.mineauz.minigamesregions.triggers.Trigger;
import au.com.mineauz.minigamesregions.triggers.TriggerRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RegionModule extends MinigameModule {
    private final @NotNull Map<@NotNull String, @NotNull Region> regions = new HashMap<>();
    private final @NotNull Map<@NotNull String, @NotNull Node> nodes = new HashMap<>();
    private final static @NotNull ModuleFactory moduleFactory = new ModuleFactory() {
        private final String name = "Regions";

        @Override
        public @NotNull MinigameModule makeNewModule(Minigame minigame) {
            return new RegionModule(minigame, name);
        }

        @Override
        public @NotNull String getName() {
            return name;
        }
    };

    public RegionModule(@NotNull Minigame mgm, @NotNull String name) {
        super(mgm, name);
    }

    public static @Nullable RegionModule getMinigameModule(@NotNull Minigame minigame) {
        return (RegionModule) minigame.getModule(moduleFactory.getName());
    }

    public static @NotNull ModuleFactory getFactory() {
        return moduleFactory;
    }

    @Override
    public boolean useSeparateConfig() {
        return true;
    }

    @Override
    public void save(@NotNull FileConfiguration config, @NotNull String mainPath) {
        char configSeparator = config.options().pathSeparator();

        Set<String> rs = regions.keySet();
        for (String name : rs) {
            Region r = regions.get(name);
            Map<String, Object> sloc = r.getFirstPoint().serialize();
            for (String i : sloc.keySet()) {
                if (!i.equals("yaw") && !i.equals("pitch")) {
                    config.set(mainPath + configSeparator + "regions" + configSeparator + name + configSeparator + "point1" + configSeparator + i, sloc.get(i));
                }
            }
            sloc = r.getSecondPoint().serialize();
            for (String i : sloc.keySet()) {
                if (!i.equals("yaw") && !i.equals("pitch")) {
                    config.set(mainPath + configSeparator + "regions" + configSeparator + name + configSeparator + "point2" + configSeparator + i, sloc.get(i));
                }
            }

            if (r.getTickDelay() != 20) {
                config.set(mainPath + configSeparator + "regions" + configSeparator + name + configSeparator + "tickDelay", r.getTickDelay());
            }

            int c = 0;
            for (RegionExecutor ex : r.getExecutors()) {
                String executorsPath = mainPath + configSeparator + "regions" + configSeparator + name + configSeparator + "executors" + configSeparator + c;
                config.set(executorsPath + configSeparator + "trigger", ex.getTrigger().getName());
                int acc = 0;
                for (ActionInterface act : ex.getActions()) {
                    config.set(executorsPath + configSeparator + "actions" + configSeparator + acc + configSeparator + "type", act.getName());
                    act.saveArguments(config, executorsPath + configSeparator + "actions" + configSeparator + acc + configSeparator + "arguments");
                    acc++;
                }

                acc = 0;
                for (ACondition con : ex.getConditions()) {
                    config.set(executorsPath + configSeparator + "conditions" + configSeparator + acc + configSeparator + "type", con.getName());
                    con.saveArguments(config, executorsPath + configSeparator + "conditions" + configSeparator + acc + configSeparator + "arguments");
                    acc++;
                }

                if (ex.isTriggerPerPlayer()) {
                    config.set(executorsPath + configSeparator + "osTriggeredPerPlayer", ex.isTriggerPerPlayer());
                }
                if (ex.getTriggerCount() != 0) {
                    config.set(executorsPath + configSeparator + "triggerCount", ex.getTriggerCount());
                }
                c++;
            }
        }

        Set<String> ns = nodes.keySet();
        for (String name : ns) {
            Node n = nodes.get(name);
            Map<String, Object> sloc = n.getLocation().serialize();
            for (String i : sloc.keySet()) {
                config.set(mainPath + configSeparator + "nodes" + configSeparator + name + configSeparator + "point" + configSeparator + i, sloc.get(i));
            }

            int c = 0;
            for (NodeExecutor ex : n.getExecutors()) {
                String executorsPath = mainPath + configSeparator + "nodes" + configSeparator + name + configSeparator + "executors" + configSeparator + c;
                config.set(executorsPath + configSeparator + "trigger", ex.getTrigger().getName());

                int acc = 0;
                for (ActionInterface act : ex.getActions()) {
                    config.set(executorsPath + configSeparator + "actions" + configSeparator + acc + configSeparator + "type", act.getName());
                    act.saveArguments(config, executorsPath + configSeparator + "actions" + configSeparator + acc + configSeparator + "arguments");
                    acc++;
                }

                acc = 0;
                for (ACondition con : ex.getConditions()) {
                    config.set(executorsPath + configSeparator + "conditions" + configSeparator + acc + configSeparator + "type", con.getName());
                    con.saveArguments(config, executorsPath + configSeparator + "conditions" + configSeparator + acc + configSeparator + "arguments");
                    acc++;
                }

                if (ex.isTriggerPerPlayer()) {
                    config.set(executorsPath + configSeparator + "isTriggeredPerPlayer", ex.isTriggerPerPlayer());
                }
                if (ex.getTriggerCount() != 0) {
                    config.set(executorsPath + configSeparator + "triggerCount", ex.getTriggerCount());
                }
                c++;
            }
        }
    }

    @Override
    public void load(@NotNull FileConfiguration config, @NotNull String mainPath) {
        char configSeparator = config.options().pathSeparator();

        if (config.contains(mainPath + configSeparator + "regions")) {
            Set<String> rs = config.getConfigurationSection(getMinigame().getName() + configSeparator + "regions").getKeys(false);
            for (String name : rs) {
                String cloc1 = mainPath + configSeparator + "regions" + configSeparator + name + configSeparator + configSeparator + "point1" + configSeparator;
                String cloc2 = mainPath + configSeparator + "regions" + configSeparator + name + configSeparator + "point2" + configSeparator;
                World w1 = Minigames.getPlugin().getServer().getWorld(config.getString(cloc1 + "world"));
                World w2 = Minigames.getPlugin().getServer().getWorld(config.getString(cloc2 + "world"));
                double x1 = config.getDouble(cloc1 + "x");
                double x2 = config.getDouble(cloc2 + "x");
                double y1 = config.getDouble(cloc1 + "y");
                double y2 = config.getDouble(cloc2 + "y");
                double z1 = config.getDouble(cloc1 + "z");
                double z2 = config.getDouble(cloc2 + "z");
                Location loc1 = new Location(w1, x1, y1, z1);
                Location loc2 = new Location(w2, x2, y2, z2);

                regions.put(name, new Region(name, getMinigame(), loc1, loc2));
                Region region = regions.get(name);
                if (config.contains(mainPath + configSeparator + "regions" + configSeparator + name + configSeparator + "tickDelay")) {
                    region.changeTickDelay(config.getLong(mainPath + configSeparator + "regions" + configSeparator + name +
                            configSeparator + "tickDelay"));
                }
                if (config.contains(mainPath + configSeparator + "regions" + configSeparator + name + configSeparator + "executors")) {
                    Set<String> ex = config.getConfigurationSection(mainPath + configSeparator + "regions" +
                            configSeparator + name + configSeparator + "executors").getKeys(false);
                    for (String i : ex) {
                        String executorsPath = mainPath + configSeparator + "regions" + configSeparator +
                                name + configSeparator + "executors" + configSeparator + i;
                        Trigger trigger = TriggerRegistry.matchTrigger(config.getString(executorsPath + configSeparator + "trigger"));

                        if (trigger != null) {
                            RegionExecutor rex = new RegionExecutor(trigger);

                            if (config.contains(executorsPath + configSeparator + "actions")) {
                                for (String actionName : config.getConfigurationSection(executorsPath + configSeparator + "actions").getKeys(false)) {
                                    ActionInterface ai = ActionRegistry.getActionByName(config.getString(executorsPath + configSeparator +
                                            "actions" + configSeparator + actionName + configSeparator + "type"));
                                    if (ai != null) {
                                        ai.loadArguments(config, executorsPath + configSeparator + "actions" + configSeparator + actionName + configSeparator + "arguments");
                                        rex.addAction(ai);
                                    }
                                }
                            }
                            if (config.contains(executorsPath + configSeparator + "conditions")) {
                                for (String conditionName : config.getConfigurationSection(executorsPath + configSeparator + "conditions").getKeys(false)) {
                                    ACondition ci = ConditionRegistry.getConditionByName(config.getString(executorsPath + configSeparator + "conditions" +
                                            configSeparator + conditionName + configSeparator + "type"));
                                    if (ci != null) {
                                        ci.loadArguments(config, executorsPath + configSeparator + "conditions" + configSeparator +
                                                conditionName + configSeparator + "arguments");
                                        rex.addCondition(ci);
                                    }
                                }
                            }

                            if (config.contains(executorsPath + configSeparator + "isTriggeredPerPlayer")) {
                                rex.setTriggerPerPlayer(config.getBoolean(executorsPath + configSeparator + "isTriggeredPerPlayer"));
                            }
                            if (config.contains(executorsPath + configSeparator + "triggerCount")) {
                                rex.setTriggerCount(config.getInt(executorsPath + configSeparator + "triggerCount"));
                            }
                            region.addExecutor(rex);
                        } else {
                            Minigames.getCmpnntLogger().error("Couldn't load trigger in path " + executorsPath);
                        }
                    }
                }
            }
        }

        if (config.contains(mainPath + configSeparator + "nodes")) {
            Set<String> rs = config.getConfigurationSection(mainPath + configSeparator + "nodes").getKeys(false);
            for (String name : rs) {
                String cloc1 = mainPath + configSeparator + "nodes" + configSeparator + name + configSeparator + "point" + configSeparator;
                World w1 = Minigames.getPlugin().getServer().getWorld(config.getString(cloc1 + "world"));
                double x1 = config.getDouble(cloc1 + "x");
                double y1 = config.getDouble(cloc1 + "y");
                double z1 = config.getDouble(cloc1 + "z");
                float yaw = 0f;
                float pitch = 0f;

                Location loc1 = new Location(w1, x1, y1, z1, yaw, pitch);

                nodes.put(name, new Node(name, getMinigame(), loc1));
                Node n = nodes.get(name);
                if (config.contains(mainPath + configSeparator + "nodes" + configSeparator + name + configSeparator + "executors")) {
                    Set<String> ex = config.getConfigurationSection(mainPath + configSeparator + "nodes" + configSeparator + name + configSeparator + "executors").getKeys(false);
                    for (String i : ex) {
                        String executorsPath = mainPath + configSeparator + "nodes" + configSeparator + name + configSeparator + "executors" + configSeparator + i;
                        NodeExecutor rex = new NodeExecutor(TriggerRegistry.matchTrigger(config.getString(executorsPath + configSeparator + "trigger")));

                        if (config.contains(executorsPath + configSeparator + "actions")) {
                            for (String actionName : config.getConfigurationSection(executorsPath + configSeparator + "actions").getKeys(false)) {
                                ActionInterface ai = ActionRegistry.getActionByName(config.getString(executorsPath + configSeparator + "actions" +
                                        configSeparator + actionName + configSeparator + "type"));
                                if (ai != null) {
                                    ai.loadArguments(config, executorsPath + configSeparator + "actions" + configSeparator + actionName + configSeparator + "arguments");
                                    rex.addAction(ai);
                                } else {
                                    Main.getPlugin().getComponentLogger().warn("Could not load action named '" + actionName + "' in minigames config of " + getMinigame().getName());
                                }
                            }
                        }
                        if (config.contains(executorsPath + configSeparator + "conditions")) {
                            for (String conditionName : config.getConfigurationSection(executorsPath + configSeparator + "conditions").getKeys(false)) {
                                ACondition ci = ConditionRegistry.getConditionByName(config.getString(executorsPath + configSeparator +
                                        "conditions" + configSeparator + conditionName + configSeparator + "type"));
                                ci.loadArguments(config, executorsPath + configSeparator + "conditions" + configSeparator + conditionName + configSeparator + "arguments");
                                rex.addCondition(ci);
                            }
                        }

                        if (config.contains(executorsPath + configSeparator + "isTriggeredPerPlayer"))
                            rex.setTriggerPerPlayer(config.getBoolean(executorsPath + configSeparator + "isTriggeredPerPlayer"));
                        if (config.contains(executorsPath + configSeparator + "triggerCount"))
                            rex.setTriggerCount(config.getInt(executorsPath + configSeparator + "triggerCount"));
                        n.addExecutor(rex);
                    }
                }
            }
        }
    }

    public boolean hasRegion(@NotNull String name) {
        if (!regions.containsKey(name)) {
            for (String n : regions.keySet()) {
                if (n.equalsIgnoreCase(name))
                    return true;
            }
            return false;
        }
        return true;
    }

    public void addRegion(String name, Region region) {
        if (!hasRegion(name))
            regions.put(name, region);
    }

    public Region getRegion(String name) {
        if (!hasRegion(name)) {
            for (String n : regions.keySet()) {
                if (n.equalsIgnoreCase(name))
                    return regions.get(n);
            }
            return null;
        }
        return regions.get(name);
    }

    public List<Region> getRegions() {
        return new ArrayList<>(regions.values());
    }

    public void removeRegion(String name) {
        if (hasRegion(name)) {
            regions.get(name).removeTickTask();
            regions.get(name).removeGameTickTask();
            regions.remove(name);
        } else {
            for (String n : regions.keySet()) {
                if (n.equalsIgnoreCase(name)) {
                    regions.get(n).removeTickTask();
                    regions.get(n).removeGameTickTask();
                    regions.remove(n);
                    break;
                }
            }
        }
    }

    public boolean hasNode(String name) {
        if (!nodes.containsKey(name)) {
            for (String n : nodes.keySet()) {
                if (n.equalsIgnoreCase(name))
                    return true;
            }
            return false;
        }
        return true;
    }

    public void addNode(String name, Node node) {
        if (!hasNode(name))
            nodes.put(name, node);
    }

    public Node getNode(String name) {
        if (!hasNode(name)) {
            for (String n : nodes.keySet()) {
                if (n.equalsIgnoreCase(name))
                    return nodes.get(n);
            }
            return null;
        }
        return nodes.get(name);
    }

    public List<Node> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    public void removeNode(String name) {
        if (hasNode(name)) {
            nodes.remove(name);
        } else {
            for (String n : nodes.keySet()) {
                if (n.equalsIgnoreCase(name)) {
                    nodes.remove(n);
                    break;
                }
            }
        }
    }

    public void displayMenu(MinigamePlayer viewer, Menu previous) {
        Menu rm = new Menu(6, RegionMessageManager.getMessage(RegionLangKey.MENU_REGIONSNODES_NAME), viewer);
        List<MenuItem> items = new ArrayList<>(regions.size());
        for (Region region : regions.values()) {
            MenuItemRegion mir = new MenuItemRegion(Material.ENDER_CHEST, Component.text(region.getName()), region, this);
            items.add(mir);
        }
        items.add(new MenuItemNewLine());
        for (Node node : nodes.values()) {
            MenuItemNode min = new MenuItemNode(Material.CHEST, Component.text(node.getName()), node, this);
            items.add(min);
        }

        //display for regen regions
        items.add(new MenuItemNewLine());
        for (MgRegion region : getMinigame().getRegenRegions()) {
            MenuItem min = new MenuItemRegenRegion(Material.CHEST_MINECART, Component.text(region.getName()), List.of(
                    Component.text(region.getName()),
                    MinigameMessageManager.getMgMessage(MgMiscLangKey.REGION_DESCRIBE,
                            Placeholder.component(MinigamePlaceHolderKey.POSITION_1.getKey(),
                                    MinigameMessageManager.getMgMessage(MgMiscLangKey.POSITION,
                                            Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_X.getKey(), String.valueOf(region.getMinX())),
                                            Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_Y.getKey(), String.valueOf(region.getMinY())),
                                            Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_Z.getKey(), String.valueOf(region.getMinZ())))),
                            Placeholder.component(MinigamePlaceHolderKey.POSITION_2.getKey(),
                                    MinigameMessageManager.getMgMessage(MgMiscLangKey.POSITION,
                                            Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_X.getKey(), String.valueOf(region.getMaxX())),
                                            Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_Y.getKey(), String.valueOf(region.getMaxY())),
                                            Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_Z.getKey(), String.valueOf(region.getMaxZ())))))),
                    region, this);
            items.add(min);
        }
        rm.addItems(items);

        if (previous != null)
            rm.addItem(new MenuItemBack(previous), rm.getSize() - 9);
        rm.displayMenu(viewer);
    }


    @Override
    public void addEditMenuOptions(@NotNull Menu menu) {
        final MenuItemCustom menuItemCustom = new MenuItemCustom(Material.DIAMOND_BLOCK, RegionMessageManager.getMessage(RegionLangKey.MENU_REGIONSNODES_NAME));
        final Menu fmenu = menu;
        menuItemCustom.setClick(() -> {
            displayMenu(menuItemCustom.getContainer().getViewer(), fmenu);
            return null;
        });
        menu.addItem(menuItemCustom);
    }

    @Override
    public boolean displayMechanicSettings(@NotNull Menu previous) {
        return false;
    }
}
