package au.com.mineauz.minigamesregions.tool;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.tool.MinigameTool;
import au.com.mineauz.minigames.tool.ToolMode;
import au.com.mineauz.minigamesregions.Main;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionModule;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionPlaceHolderKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RegionToolMode implements ToolMode {

    @Override
    public String getName() {
        return "REGION";
    }

    @Override
    public Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_REGION_NAME);
    }

    @Override
    public List<Component> getDescription() {
        return RegionMessageManager.getMessageList(RegionLangKey.MENU_TOOL_REGION_DESCRIPTION);
    }

    @Override
    public Material getIcon() {
        return Material.DIAMOND_BLOCK;
    }

    @Override
    public void onSetMode(final @NotNull MinigamePlayer player, @NotNull MinigameTool tool) {
        tool.setSetting("Region", "None");
        final Menu m = new Menu(2, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_REGION_SELECT_NAME), player);
        if (player.isInMenu()) {
            m.addItem(new MenuItemBack(player.getMenu()), m.getSize() - 9);
        }
        final MinigameTool ftool = tool;
        m.addItem(new MenuItemString(Material.PAPER, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_REGION_NAME_NAME), new Callback<>() {

            @Override
            public String getValue() {
                return ftool.getSetting("Region");
            }

            @Override
            public void setValue(String value) {
                ftool.setSetting("Region", value);
            }
        }));

        if (tool.getMinigame() != null) {
            // Node selection menu
            RegionModule module = RegionModule.getMinigameModule(tool.getMinigame());

            Menu regionMenu = new Menu(6, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_REGION_LIST_NAME), player);
            List<MenuItem> items = new ArrayList<>();

            for (final Region region : module.getRegions()) {
                MenuItemCustom item = new MenuItemCustom(Material.CHEST, Component.text(region.getName()));

                // Set the node and go back to the main menu
                item.setClick(() -> {
                    ftool.setSetting("Region", region.getName());

                    m.displayMenu(player);

                    return null;
                });

                items.add(item);
            }

            regionMenu.addItems(items);
            regionMenu.addItem(new MenuItemBack(m), regionMenu.getSize() - 9);

            m.addItem(new MenuItemPage(Material.CHEST, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_REGION_EDIT_NAME), regionMenu));
        }
        m.displayMenu(player);
    }

    @Override
    public void onUnsetMode(@NotNull MinigamePlayer mgPlayer, MinigameTool tool) {
        tool.removeSetting("Region");
    }

    @Override
    public void onLeftClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame,
                            @Nullable Team team, @NotNull PlayerInteractEvent event) {
        if (mgPlayer.hasSelection()) {
            String name = MinigameTool.getMinigameTool(mgPlayer).getSetting("Region");
            RegionModule module = RegionModule.getMinigameModule(minigame);
            Region region = module.getRegion(name);

            if (region == null) {
                module.addRegion(name, new Region(name, minigame, mgPlayer.getSelectionLocations()[0], mgPlayer.getSelectionLocations()[1]));
                MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                        RegionLangKey.REGION_CREATED,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(RegionPlaceHolderKey.REGION.getKey(), name));
                mgPlayer.clearSelection();
            } else {
                region.updateRegion(mgPlayer.getSelectionLocations()[0], mgPlayer.getSelectionLocations()[1]);
                Main.getPlugin().getDisplayManager().update(region);
                MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                        RegionLangKey.REGION_EDITED,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(RegionPlaceHolderKey.REGION.getKey(), name));
            }
        } else {
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionMessageManager.getBundleKey(),
                    MgMiscLangKey.TOOL_ERROR_NOREGIONSELECTED);
        }
    }

    @Override
    public void onRightClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame,
                             @Nullable Team team, @NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            mgPlayer.addSelectionPoint(event.getClickedBlock().getLocation());
            if (mgPlayer.hasSelection()) {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SELECTED_REGION);
            }
        }
    }

    @Override
    public void select(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team) {
        RegionModule mod = RegionModule.getMinigameModule(minigame);
        String name = MinigameTool.getMinigameTool(mgPlayer).getSetting("Region");
        if (mod.hasRegion(name)) {
            Main.getPlugin().getDisplayManager().show(mod.getRegion(name), mgPlayer);
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                    RegionLangKey.TOOL_REGION_SELECTED,
                    Placeholder.unparsed(RegionPlaceHolderKey.REGION.getKey(), name),
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
        } else {
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionMessageManager.getBundleKey(),
                    RegionLangKey.REGION_ERROR_NOREGION,
                    Placeholder.unparsed(RegionPlaceHolderKey.REGION.getKey(), name),
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
        }
    }

    @Override
    public void deselect(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team) {
        RegionModule mod = RegionModule.getMinigameModule(minigame);
        String name = MinigameTool.getMinigameTool(mgPlayer).getSetting("Region");
        if (mod.hasRegion(name)) {
            Main.getPlugin().getDisplayManager().hide(mod.getRegion(name), mgPlayer);
            mgPlayer.clearSelection();
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, RegionMessageManager.getBundleKey(),
                    RegionLangKey.TOOL_REGION_DESELECTED);
        } else {
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionMessageManager.getBundleKey(),
                    RegionLangKey.REGION_ERROR_NOREGION,
                    Placeholder.unparsed(RegionPlaceHolderKey.REGION.getKey(), name),
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
        }
    }
}
