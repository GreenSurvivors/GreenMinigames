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
import au.com.mineauz.minigames.objects.safelocation.SafeFineLocation;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import au.com.mineauz.minigamesregions.actions.ActionRegistry;
import au.com.mineauz.minigamesregions.actions.IAction;
import au.com.mineauz.minigamesregions.conditions.ACondition;
import au.com.mineauz.minigamesregions.conditions.ConditionRegistry;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.menu.MenuItemNode;
import au.com.mineauz.minigamesregions.menu.MenuItemRegenRegion;
import au.com.mineauz.minigamesregions.menu.MenuItemRegion;
import au.com.mineauz.minigamesregions.triggers.Trigger;
import au.com.mineauz.minigamesregions.triggers.TriggerRegistry;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;

public class RegionModule extends MinigameModule {
    private final @NotNull Map<@NotNull String, @NotNull Region> regions = new HashMap<>();
    private final @NotNull Map<@NotNull String, @NotNull Node> nodes = new HashMap<>();
    private static final @NotNull ModuleFactory moduleFactory = new ModuleFactory() {
        private final Key key = new NamespacedKey(Main.getPlugin(), "regions");

        @Override
        public @NotNull MinigameModule makeNewModule(@NotNull Minigame minigame) {
            return new RegionModule(minigame, key);
        }

        @Override
        public @NotNull Key getKey() {
            return key;
        }
    };

    public RegionModule(final @NotNull Minigame mgm, final @NotNull Key key) {
        super(mgm, key);
    }

    public static @Nullable RegionModule getMinigameModule(@NotNull Minigame minigame) {
        return (RegionModule) minigame.getModule(moduleFactory.getKey());
    }

    public static @NotNull ModuleFactory getFactory() {
        return moduleFactory;
    }

    @Override
    public boolean useSeparateConfig() {
        return true;
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        for (Region region : regions.values()) {
            final @NotNull CommentedConfigurationNode regionsNode = config.node("regions", region.getName());
            regionsNode.node("point1").set(region.getFirstPoint());
            regionsNode.node("point2").set(region.getSecondPoint());

            if (region.getConfiguredDelay() != 20) {
                config.node("tickDelay").set(region.getConfiguredDelay());
            }

            saveExecutors(region, regionsNode);
        }

        // Note: I know they have similar names, but our Nodes do have nothing in common with the configurate nodes!
        for (final @NotNull Node node : nodes.values()) {
            final @NotNull CommentedConfigurationNode nodeNode = config.node("nodes", node.getName());
            nodeNode.node("point").set(node.getSafeLocation());

            saveExecutors(node, nodeNode);
        }
    }

    protected void saveExecutors(final @NotNull ActionExecutorHolder executorHolder, final @NotNull CommentedConfigurationNode nodeNode) throws SerializationException {
        int executorNumber = 0;
        for (final @NotNull ActionExecutor ex : executorHolder.getExecutors()) {
            final @NotNull CommentedConfigurationNode executorsNode = nodeNode.node("executors", executorNumber++);

            executorsNode.node("trigger").set(ex.getTrigger().getName());

            int actionNumber = 0;
            for (IAction act : ex.getActions()) {
                final @NotNull CommentedConfigurationNode actionNode = executorsNode.node("actions", actionNumber++);

                actionNode.node( "type").set(act.getKey());
                act.saveArguments(actionNode.node("arguments"));
            }

            int conditionNumber = 0;
            for (ACondition con : ex.getConditions()) {
                final @NotNull CommentedConfigurationNode conditionNode = executorsNode.node("conditions", conditionNumber++);

                conditionNode.node("type").set(con.getName());
                con.saveArguments(conditionNode.node("arguments"));
            }

            if (ex.isTriggerPerPlayer()) {
                executorsNode.node("isTriggeredPerPlayer").set(ex.isTriggerPerPlayer());
            }
            if (ex.getTriggerCount() != 0) {
                executorsNode.node("triggerCount").set(ex.getTriggerCount());
            }
        }
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) throws ConfigurateException {
        if (config.hasChild("regions")) {
            for (final @NotNull Map.Entry<@NotNull Object, @NotNull CommentedConfigurationNode> entry : config.node("regions").childrenMap().entrySet()) {
                final @NotNull String name = entry.getKey().toString();
                final @NotNull CommentedConfigurationNode regionsNode = entry.getValue();

                final @Nullable SafeFineLocation point1 = regionsNode.node("point1").get(TypeToken.get(SafeFineLocation.class));
                final @Nullable SafeFineLocation point2 = regionsNode.node("point2").get(TypeToken.get(SafeFineLocation.class));

                if (point1 == null || point2 == null) {
                    // todo log
                    continue;
                }

                final @NotNull Region region = new Region(name, getMinigame(), point1, point2);
                regions.put(name, region);

                if (regionsNode.hasChild("tickDelay")) {
                    region.setConfiguredTickDelay(regionsNode.node("tickDelay").getLong());
                }
                loadExecutorHolder(region, regionsNode);
            }
        }

        if (config.hasChild("nodes")) {
            for (final @NotNull Map.Entry<@NotNull Object, @NotNull CommentedConfigurationNode> entry : config.node("nodes").childrenMap().entrySet()) {
                final @NotNull String name = entry.getKey().toString();
                final @NotNull CommentedConfigurationNode nodeNode = entry.getValue();
                final @Nullable SafeFullLocation point = nodeNode.node("point").get(TypeToken.get(SafeFullLocation.class));

                final @NotNull Node node = new Node(name, getMinigame(), point);
                nodes.put(name,node);
                loadExecutorHolder(node, nodeNode);
            }
        }
    }

    private void loadExecutorHolder(final @NotNull ActionExecutorHolder executorHolder, final @NotNull CommentedConfigurationNode config) throws ConfigurateException {
        if (config.hasChild("executors")) {
            for (final @NotNull CommentedConfigurationNode executorNode : config.node("executors").childrenMap().values()) {
                final @Nullable Trigger trigger = TriggerRegistry.matchTrigger(executorNode.node("trigger").getString());

                if (trigger != null) {
                    final @NotNull ActionExecutor executor = new ActionExecutor(trigger);

                    if (executorNode.hasChild("actions")) {
                        for (final @NotNull CommentedConfigurationNode actionNode : executorNode.node("actions").childrenMap().values()) {

                            final @Nullable String typeStr = actionNode.node("type").getString();

                            if (typeStr != null) {
                                @Nullable IAction action = null;

                                if (!typeStr.contains(":")) { // dataFixerUpper
                                    action = ActionRegistry.getActionByName(typeStr.toLowerCase(Locale.ROOT));
                                }

                                if (action == null) {
                                    final @Nullable NamespacedKey key = NamespacedKey.fromString(typeStr.toLowerCase(Locale.ROOT), Minigames.getPlugin());

                                    if (key != null) {
                                        action = ActionRegistry.getActionByKey(key);
                                    }
                                }

                                if (action != null) {
                                    action.loadArguments(actionNode.node("arguments")); // todo catch log continue
                                    executor.addAction(action);
                                } else {
                                    // todo
                                }
                            } else {
                                // todo
                            }
                        }
                    }
                    if (executorNode.hasChild("conditions")) {
                        for (final @NotNull CommentedConfigurationNode conditionsNode : executorNode.node("conditions").childrenMap().values()) {
                            final @Nullable ACondition condition = ConditionRegistry.getConditionByName(conditionsNode.node("type").getString());
                            if (condition != null) {
                                condition.loadArguments(conditionsNode.node("arguments"));
                                executor.addCondition(condition);
                            }
                        }
                    }

                    if (executorNode.hasChild("isTriggeredPerPlayer")) {
                        executor.setTriggerPerPlayer(executorNode.node("isTriggeredPerPlayer").getBoolean());
                    }
                    if (executorNode.hasChild("triggerCount")) {
                        executor.setTriggerCount(executorNode.node("triggerCount").getInt());
                    }
                    executorHolder.addExecutor(executor);
                } else {
                    Main.getPlugin().getComponentLogger().error("Couldn't load trigger in path " + executorNode.path());
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

    public void addRegion(@NotNull String name, Region region) {
        if (!hasRegion(name))
            regions.put(name, region);
    }

    public @Nullable Region getRegion(@NotNull String name) {
        if (!hasRegion(name)) {
            for (String n : regions.keySet()) {
                if (n.equalsIgnoreCase(name))
                    return regions.get(n);
            }
            return null;
        }
        return regions.get(name);
    }

    public @NotNull List<@NotNull Region> getRegions() {
        return new ArrayList<>(regions.values());
    }

    public void removeRegion(@NotNull String name) {
        if (hasRegion(name)) {
            regions.get(name).removeConfiguredTask();
            regions.get(name).removeGameTickTask();
            regions.remove(name);
        } else {
            for (String n : regions.keySet()) {
                if (n.equalsIgnoreCase(name)) {
                    regions.get(n).removeConfiguredTask();
                    regions.get(n).removeGameTickTask();
                    regions.remove(n);
                    break;
                }
            }
        }
    }

    public boolean hasNode(@NotNull String name) {
        if (!nodes.containsKey(name)) {
            for (String n : nodes.keySet()) {
                if (n.equalsIgnoreCase(name))
                    return true;
            }
            return false;
        }
        return true;
    }

    public void addNode(final @NotNull Node node) {
        if (!hasNode(node.getName())) {
            nodes.put(node.getName(), node);
        }
    }

    public @Nullable Node getNode(@NotNull String name) {
        if (!hasNode(name)) {
            for (String n : nodes.keySet()) {
                if (n.equalsIgnoreCase(name))
                    return nodes.get(n);
            }
            return null;
        }
        return nodes.get(name);
    }

    public @NotNull List<@NotNull Node> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    public void removeNode(@NotNull String name) {
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

    public void displayMenu(@NotNull MinigamePlayer viewer, @Nullable Menu previous) {
        Menu rm = new Menu(6, RegionMessageManager.getMessage(RegionLangKey.MENU_REGIONSNODES_NAME), viewer);
        List<MenuItem> items = new ArrayList<>(regions.size());
        for (Region region : regions.values()) {
            MenuItemRegion mir = new MenuItemRegion(ItemType.ENDER_CHEST, Component.text(region.getName()), region, this);
            items.add(mir);
        }
        items.add(new MenuItemNewLine());
        for (Node node : nodes.values()) {
            MenuItemNode min = new MenuItemNode(ItemType.CHEST, Component.text(node.getName()), node, this);
            items.add(min);
        }

        //display for regen regions
        items.add(new MenuItemNewLine());
        for (MgRegion region : getMinigame().getRegenRegions()) {
            MenuItem min = new MenuItemRegenRegion(ItemType.CHEST_MINECART, Component.text(region.getName()), List.of(
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
        final MenuItemCustom menuItemCustom = new MenuItemCustom(ItemType.DIAMOND_BLOCK, RegionMessageManager.getMessage(RegionLangKey.MENU_REGIONSNODES_NAME));
        final Menu fmenu = menu;
        menuItemCustom.setClick(() -> {
            displayMenu(menuItemCustom.getContainer().getViewer(), fmenu);
            return ItemStack.empty();
        });
        menu.addItem(menuItemCustom);
    }

    @Override
    public boolean displayMechanicSettings(@NotNull Menu previous) {
        return false;
    }
}
