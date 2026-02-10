package au.com.mineauz.minigamesregions.tool;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuDisplayTypes;
import au.com.mineauz.minigames.menu.MenuItemSaveMinigame;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.tool.MinigameTool;
import au.com.mineauz.minigames.tool.ToolMode;
import au.com.mineauz.minigamesregions.*;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionPlaceHolderKey;
import au.com.mineauz.minigamesregions.menu.MenuItemNode;
import au.com.mineauz.minigamesregions.menu.MenuItemRegion;
import com.google.common.collect.Iterables;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

public class ExecutorHolderEditToolMode implements ToolMode {

    @Override
    public @NotNull String getName() {
        return "REGION_AND_NODE_EDITOR";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.TOOL_EXECUTORHOLDEREDIT_NAME);
    }

    @Override
    public @NotNull List<@NotNull Component> getDescription() {
        return RegionMessageManager.getMessageList(RegionLangKey.TOOL_EXECUTORHOLDEREDIT_DESCRIPTION);
    }

    @Override
    public @NotNull ItemType getIcon() {
        return ItemType.WRITABLE_BOOK;
    }

    @Override
    public void onSetMode(final @NotNull MinigamePlayer mgPlayer, final @NotNull MinigameTool tool) {
        if (tool.getMinigame() != null) {
            RegionsMain.getPlugin().getDisplayManager().hideAll(mgPlayer.getPlayer());
            RegionsMain.getPlugin().getDisplayManager().showAll(tool.getMinigame(), mgPlayer);
        }
    }

    @Override
    public void onUnsetMode(final @NotNull MinigamePlayer mgPlayer, final @NotNull MinigameTool tool) {
        if (tool.getMinigame() != null) {
            RegionsMain.getPlugin().getDisplayManager().hideAll(mgPlayer.getPlayer());
        }
    }

    @Override
    public void onLeftClick(final @NotNull MinigamePlayer mgPlayer,
                            final @NotNull Minigame minigame, final @Nullable Team team,
                            final @NotNull PlayerInteractEvent event) {
    }

    @Override
    public void onRightClick(final @NotNull MinigamePlayer mgPlayer,
                             final @NotNull Minigame minigame, final @Nullable Team team,
                             final @NotNull PlayerInteractEvent event) {
        Vector origin = event.getPlayer().getEyeLocation().toVector();
        Vector direction = event.getPlayer().getEyeLocation().getDirection().normalize();

        // Prepare region and node bounds for efficiency
        RegionModule module = RegionModule.getMinigameModule(minigame);
        Map<Region, Vector[]> regionBBs = new IdentityHashMap<>();
        for (Region region : module.getRegions()) {
            Vector point1 = region.getFirstPoint().toVector();
            Vector point2 = region.getSecondPoint().toVector();

            regionBBs.put(region, new Vector[]{Vector.getMinimum(point1, point2), Vector.getMaximum(point1, point2).add(new Vector(1, 1, 1))});
        }

        Map<Node, Vector> nodeLocs = new IdentityHashMap<>();
        for (Node node : module.getNodes()) {
            nodeLocs.put(node, node.getSafeLocation().toVector());
        }

        Set<ExecutableScriptObject> hits = Collections.newSetFromMap(new IdentityHashMap<>());

        // Raytrace the view vector
        for (double dist = 0; dist < 10; dist += 0.25) {
            Vector pos = origin.clone().add(direction.clone().multiply(dist));

            for (Entry<Region, Vector[]> regionEntry : regionBBs.entrySet()) {
                if (pos.isInAABB(regionEntry.getValue()[0], regionEntry.getValue()[1])) {
                    hits.add(regionEntry.getKey());
                }
            }

            for (Entry<Node, Vector> nodeEntry : nodeLocs.entrySet()) {
                if (pos.isInSphere(nodeEntry.getValue(), 0.4)) {
                    hits.add(nodeEntry.getKey());
                }
            }
        }

        // Tracing done, now show the results
        if (hits.size() == 1) {
            openMenu(mgPlayer, minigame, Iterables.getFirst(hits, null));
        } else if (!hits.isEmpty()) {
            openChooseMenu(mgPlayer, module, hits);
        }
    }

    private void openMenu(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame, final @NotNull ExecutableScriptObject hit) {
        final @NotNull Menu menu;
        if (hit instanceof Region region) {
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                    RegionLangKey.TOOL_REGION_EDIT,
                    Placeholder.unparsed(RegionPlaceHolderKey.REGION.getKey(), region.getName()));
            menu = MenuItemRegion.createMenu(mgPlayer, region);
        } else if (hit instanceof Node node) {
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                    RegionLangKey.TOOL_NODE_EDIT,
                    Placeholder.unparsed(RegionPlaceHolderKey.NODE.getKey(), node.getName()));
            menu = MenuItemNode.createMenu(mgPlayer, node);
        } else {
            throw new UnsupportedOperationException("Unknown ExecutableScriptObject  type!");
        }

        menu.setItem(new MenuItemSaveMinigame(MenuDisplayTypes.saveType(),
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_MINIGAME_SAVE_NAME,
                        Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName())), minigame), menu.getSize() - 9);

        menu.displayMenu();
    }

    private void openChooseMenu(final @NotNull MinigamePlayer mgPlayer, final @NotNull RegionModule module,
                                final @NotNull Set<@NotNull ExecutableScriptObject> objects) {
        final @NotNull Menu menu = new Menu(3, RegionMessageManager.getMessage(RegionLangKey.TOOL_EXECUTORHOLDEREDIT_CHOOSE_NAME), mgPlayer);

        StringBuilder options = new StringBuilder();
        for (ExecutableScriptObject object : objects) {
            if (!options.isEmpty()) {
                options.append(", ");
            }

            if (object instanceof Region region) {
                options.append(region.getName());
                final @NotNull MenuItemRegion item = new MenuItemRegion(MenuDisplayTypes.genericSubMenu(),
                    Component.text(region.getName()), region, module);
                menu.addItem(item);
            } else if (object instanceof Node node) {
                options.append(node.getName());
                final @NotNull MenuItemNode item = new MenuItemNode(ItemType.STONE_BUTTON, Component.text(node.getName()), node, module);
                menu.addItem(item);
            }
        }

        menu.setItem(new MenuItemSaveMinigame(MenuDisplayTypes.saveType(),
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_MINIGAME_SAVE_NAME,
                        Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), module.getMinigame().getDisplayName())),
                module.getMinigame()), menu.getSize() - 9);

        menu.displayMenu();

        MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                RegionLangKey.TOOL_NODEREGION_SELECTED,
                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), options.toString()));
    }

    @Override
    public void select(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame, final @Nullable Team team) {
        RegionsMain.getPlugin().getDisplayManager().showAll(minigame, mgPlayer);
    }

    @Override
    public void deselect(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame, final @Nullable Team team) {
        RegionsMain.getPlugin().getDisplayManager().hideAll(mgPlayer.getPlayer());
    }
}
