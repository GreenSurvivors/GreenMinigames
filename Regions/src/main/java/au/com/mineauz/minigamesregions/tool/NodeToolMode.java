package au.com.mineauz.minigamesregions.tool;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import au.com.mineauz.minigames.tool.MinigameTool;
import au.com.mineauz.minigames.tool.ToolMode;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.RegionModule;
import au.com.mineauz.minigamesregions.RegionsMain;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionPlaceHolderKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NodeToolMode implements ToolMode {

    @Override
    public @NotNull String getName() {
        return "NODE";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_NODE_NAME);
    }

    @Override
    public @NotNull List<@NotNull Component> getDescription() {
        return RegionMessageManager.getMessageList(RegionLangKey.MENU_TOOL_NODE_DESCRIPTION);
    }

    @Override
    public @NotNull ItemType getIcon() {
        return ItemType.STONE_BUTTON;
    }

    @Override
    public void onSetMode(final @NotNull MinigamePlayer player, final @NotNull MinigameTool tool) {
        tool.setSetting("Node", "None");
        final @NotNull Menu menu = new Menu(2, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_NODE_SELECT_NAME), player);
        if (player.isInMenu()) {
            menu.setItem(new MenuItemBack(player.getMenu()), menu.getSize() - 9);
        }

        menu.addItem(new MenuItemString(ItemType.PAPER, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_NODE_NAME_NAME), new Callback<>() {
            @Override
            public @NotNull String getValue() {
                return tool.getSetting("Node");
            }

            @Override
            public void setValue(final @NotNull String value) {
                tool.setSetting("Node", value);
            }
        }));

        if (tool.getMinigame() != null) {
            // Node selection menu
            final RegionModule module = RegionModule.getMinigameModule(tool.getMinigame());

            Menu nodeMenu = new Menu(6, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_NODE_LIST_NAME), player);
            final @NotNull List<@NotNull AMenuItem> items = new ArrayList<>();

            for (final @NotNull Node node : module.getNodes()) {
                final @NotNull MenuItemCustom item = new MenuItemCustom(ItemType.STONE_BUTTON, Component.text(node.getName()));

                // Set the node and go back to the main menu
                item.setClick(() -> {
                    tool.setSetting("Node", node.getName());
                    menu.displayMenu();

                    return ItemStack.empty();
                });

                items.add(item);
            }

            nodeMenu.addItems(items);
            nodeMenu.setItem(new MenuItemBack(menu), nodeMenu.getSize() - 9);

            menu.addItem(new MenuItemPage(ItemType.STONE_BUTTON, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_NODE_EDIT_NAME), nodeMenu));
        }
        menu.displayMenu();
    }

    @Override
    public void onUnsetMode(final @NotNull MinigamePlayer mgPlayer, final @NotNull MinigameTool tool) {
        tool.removeSetting("Node");
    }

    @Override
    public void onLeftClick(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame, @Nullable Team team, @NotNull PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            final RegionModule mod = RegionModule.getMinigameModule(minigame);
            final String name = MinigameTool.getMinigameTool(mgPlayer).getSetting("Node");

            final @NotNull SafeFullLocation loc = new SafeFullLocation(event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5));
            @Nullable Node node = mod.getNode(name);
            if (node == null) {
                node = new Node(name, minigame, loc);
                mod.addNode(node);
                MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                        RegionLangKey.NODE_ADDED,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(RegionPlaceHolderKey.NODE.getKey(), name));
            } else {
                node.setLocation(loc);
                MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                        RegionLangKey.NODE_EDITED,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(RegionPlaceHolderKey.NODE.getKey(), name));
                RegionsMain.getPlugin().getDisplayManager().update(node);
            }
        }
    }

    @Override
    public void onRightClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team, @NotNull PlayerInteractEvent event) {
        RegionModule mod = RegionModule.getMinigameModule(minigame);
        String name = MinigameTool.getMinigameTool(mgPlayer).getSetting("Node");

        Node node = mod.getNode(name);
        if (node == null) {
            node = new Node(name, minigame, mgPlayer.getSafeLocation());
            mod.addNode(node);
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                    RegionLangKey.NODE_ADDED,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                    Placeholder.unparsed(RegionPlaceHolderKey.NODE.getKey(), name));
        } else {
            node.setLocation(mgPlayer.getSafeLocation());
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                    RegionLangKey.NODE_EDITED,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                    Placeholder.unparsed(RegionPlaceHolderKey.NODE.getKey(), name));
            RegionsMain.getPlugin().getDisplayManager().update(node);
        }
    }

    @Override
    public void select(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team) {
        RegionModule mod = RegionModule.getMinigameModule(minigame);
        String name = MinigameTool.getMinigameTool(mgPlayer).getSetting("Node");
        if (mod.hasNode(name)) {
            RegionsMain.getPlugin().getDisplayManager().show(mod.getNode(name), mgPlayer);
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                    RegionLangKey.TOOL_NODE_SELECTED,
                    Placeholder.unparsed(RegionPlaceHolderKey.NODE.getKey(), name));
        } else {
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionMessageManager.getBundleKey(),
                    RegionLangKey.NODE_ERROR_NONODE,
                    Placeholder.unparsed(RegionPlaceHolderKey.NODE.getKey(), name));
        }
    }

    @Override
    public void deselect(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team) {
        RegionModule mod = RegionModule.getMinigameModule(minigame);
        String name = MinigameTool.getMinigameTool(mgPlayer).getSetting("Node");
        if (mod.hasNode(name)) {
            RegionsMain.getPlugin().getDisplayManager().hide(mod.getNode(name), mgPlayer);
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                    RegionLangKey.TOOL_NODE_DESELECTED,
                    Placeholder.unparsed(RegionPlaceHolderKey.NODE.getKey(), name));
        } else {
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionMessageManager.getBundleKey(),
                    RegionLangKey.NODE_ERROR_NONODE,
                    Placeholder.unparsed(RegionPlaceHolderKey.NODE.getKey(), name));
        }
    }
}
