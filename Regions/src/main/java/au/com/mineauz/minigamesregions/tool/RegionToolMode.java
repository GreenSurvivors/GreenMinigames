package au.com.mineauz.minigamesregions.tool;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.team.Team;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RegionToolMode implements ToolMode {

    @Override
    public @NotNull String getName() {
        return "REGION";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_REGION_NAME);
    }

    @Override
    public @NotNull List<@NotNull Component> getDescription() {
        return RegionMessageManager.getMessageList(RegionLangKey.MENU_TOOL_REGION_DESCRIPTION);
    }

    @Override
    public @NotNull ItemType getIcon() {
        return ItemType.DIAMOND_BLOCK;
    }

    @Override
    public void onSetMode(final @NotNull MinigamePlayer player, final @NotNull MinigameTool tool) {
        tool.setSetting("Region", "None");
        final @NotNull Menu menu = new Menu(2, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_REGION_SELECT_NAME), player);
        if (player.isInMenu()) {
            menu.setItem(new MenuItemBack(player.getMenu()), menu.getSize() - 9);
        }
        final MinigameTool ftool = tool;
        menu.addItem(new MenuItemString(ItemType.PAPER, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_REGION_NAME_NAME), new Callback<>() {

            @Override
            public @NotNull String getValue() {
                return ftool.getSetting("Region");
            }

            @Override
            public void setValue(@NotNull String value) {
                ftool.setSetting("Region", value);
            }
        }));

        if (tool.getMinigame() != null) {
            // Node selection menu
            RegionModule module = RegionModule.getMinigameModule(tool.getMinigame());

            final @NotNull Menu regionMenu = new Menu(6, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_REGION_LIST_NAME), player);
            final @NotNull List<@NotNull AMenuItem> items = new ArrayList<>();

            for (final @NotNull Region region : module.getRegions()) {
                MenuItemCustom item = new MenuItemCustom(ItemType.CHEST, Component.text(region.getName()));

                // Set the node and go back to the main menu
                item.setClick(() -> {
                    ftool.setSetting("Region", region.getName());

                    menu.displayMenu();

                    return ItemStack.empty();
                });

                items.add(item);
            }

            regionMenu.addItems(items);
            regionMenu.setItem(new MenuItemBack(menu), regionMenu.getSize() - 9);

            menu.addItem(new MenuItemPage(ItemType.CHEST, RegionMessageManager.getMessage(RegionLangKey.MENU_TOOL_REGION_EDIT_NAME), regionMenu));
        }
        menu.displayMenu();
    }

    @Override
    public void onUnsetMode(final @NotNull MinigamePlayer mgPlayer, final @NotNull MinigameTool tool) {
        tool.removeSetting("Region");
    }

    @Override
    public void onLeftClick(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame,
                            final @Nullable Team team, final @NotNull PlayerInteractEvent event) {
        if (mgPlayer.hasSelection()) {
            final @NotNull String name = MinigameTool.getMinigameTool(mgPlayer).getSetting("Region");
            final RegionModule module = RegionModule.getMinigameModule(minigame);
            final @Nullable Region region = module.getRegion(name);

            if (region == null) {
                module.addRegion(new Region(name, minigame, mgPlayer.getSelectionLocations()[0], mgPlayer.getSelectionLocations()[1]));
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
    public void onRightClick(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame,
                             final @Nullable Team team, final @NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            mgPlayer.addSelectionPoint(event.getClickedBlock().getLocation());
            if (mgPlayer.hasSelection()) {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SELECTED_REGION);
            }
        }
    }

    @Override
    public void select(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame, final @Nullable Team team) {
        final RegionModule mod = RegionModule.getMinigameModule(minigame);
        final @NotNull String name = MinigameTool.getMinigameTool(mgPlayer).getSetting("Region");
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
    public void deselect(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame, final @Nullable Team team) {
        final RegionModule mod = RegionModule.getMinigameModule(minigame);
        final @NotNull String name = MinigameTool.getMinigameTool(mgPlayer).getSetting("Region");
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
