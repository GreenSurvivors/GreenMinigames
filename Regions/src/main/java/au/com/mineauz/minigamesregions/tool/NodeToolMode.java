package au.com.mineauz.minigamesregions.tool;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.tool.MinigameTool;
import au.com.mineauz.minigames.tool.ToolMode;
import au.com.mineauz.minigamesregions.Main;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.RegionModule;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionPlaceHolderKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NodeToolMode implements ToolMode {

    @Override
    public String getName() {
        return "NODE";
    }

    @Override
    public Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_NODE_NAME);
    }

    @Override
    public List<Component> getDescription() {
        return RegionMessageManager.getMessageList(RegionLangKey.MENU_TOOL_NODE_DESCRIPTION);
    }

    @Override
    public Material getIcon() {
        return Material.STONE_BUTTON;
    }

    @Override
    public void onSetMode(final @NotNull MinigamePlayer player, final @NotNull MinigameTool tool) {
        tool.setSetting("Node", "None");
        final Menu menu = new Menu(2, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_NODE_SELECT_NAME), player);
        if (player.isInMenu()) {
            menu.addItem(new MenuItemBack(player.getMenu()), menu.getSize() - 9);
        }

        menu.addItem(new MenuItemString(Material.PAPER, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_NODE_NAME_NAME), new Callback<>() {
            @Override
            public String getValue() {
                return tool.getSetting("Node");
            }

            @Override
            public void setValue(String value) {
                tool.setSetting("Node", value);
            }
        }));

        if (tool.getMinigame() != null) {
            // Node selection menu
            RegionModule module = RegionModule.getMinigameModule(tool.getMinigame());

            Menu nodeMenu = new Menu(6, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_NODE_LIST_NAME), player);
            List<MenuItem> items = new ArrayList<>();

            for (final Node node : module.getNodes()) {
                MenuItemCustom item = new MenuItemCustom(Material.STONE_BUTTON, Component.text(node.getName()));

                // Set the node and go back to the main menu
                item.setClick(() -> {
                    tool.setSetting("Node", node.getName());
                    menu.displayMenu(player);

                    return null;
                });

                items.add(item);
            }

            nodeMenu.addItems(items);
            nodeMenu.addItem(new MenuItemBack(menu), nodeMenu.getSize() - 9);

            menu.addItem(new MenuItemPage(Material.STONE_BUTTON, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_NODE_EDIT_NAME), nodeMenu));
        }
        menu.displayMenu(player);
    }

    @Override
    public void onUnsetMode(@NotNull MinigamePlayer mgPlayer, MinigameTool tool) {
        tool.removeSetting("Node");
    }

    @Override
    public void onLeftClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team, @NotNull PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            RegionModule mod = RegionModule.getMinigameModule(minigame);
            String name = MinigameTool.getMinigameTool(mgPlayer).getSetting("Node");

            Location loc = event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5);
            Node node = mod.getNode(name);
            if (node == null) {
                node = new Node(name, minigame, loc);
                mod.addNode(name, node);
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
                Main.getPlugin().getDisplayManager().update(node);
            }
        }
    }

    @Override
    public void onRightClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team, @NotNull PlayerInteractEvent event) {
        RegionModule mod = RegionModule.getMinigameModule(minigame);
        String name = MinigameTool.getMinigameTool(mgPlayer).getSetting("Node");

        Node node = mod.getNode(name);
        if (node == null) {
            node = new Node(name, minigame, mgPlayer.getLocation());
            mod.addNode(name, node);
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                    RegionLangKey.NODE_ADDED,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                    Placeholder.unparsed(RegionPlaceHolderKey.NODE.getKey(), name));
        } else {
            node.setLocation(mgPlayer.getLocation());
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                    RegionLangKey.NODE_EDITED,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                    Placeholder.unparsed(RegionPlaceHolderKey.NODE.getKey(), name));
            Main.getPlugin().getDisplayManager().update(node);
        }
    }

    @Override
    public void select(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team) {
        RegionModule mod = RegionModule.getMinigameModule(minigame);
        String name = MinigameTool.getMinigameTool(mgPlayer).getSetting("Node");
        if (mod.hasNode(name)) {
            Main.getPlugin().getDisplayManager().show(mod.getNode(name), mgPlayer);
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
            Main.getPlugin().getDisplayManager().hide(mod.getNode(name), mgPlayer);
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
