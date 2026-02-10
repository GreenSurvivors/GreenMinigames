package au.com.mineauz.minigames.tool;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.display.IDisplayObject;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.objects.MgRegion;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.RegenRegionChangeResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RegenAreaMode implements ToolMode {
    private static final @NotNull String SETTING_KEY = "Region";
    private final @NotNull Map<@NotNull UUID, @NotNull IDisplayObject> displayedRegions = new HashMap<>();

    @Override
    public @NotNull String getName() {
        return "REGEN_AREA";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_TOOL_REGENAREA_NAME);
    }

    @Override
    public @NotNull List<@NotNull Component> getDescription() {
        return MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_TOOL_REGENAREA_DESCRIPTION);
    }

    @Override
    public @NotNull ItemType getIcon() {
        return ItemType.OAK_SAPLING;
    }

    @Override
    public void onSetMode(final @NotNull MinigamePlayer mgPlayer, final @NotNull MinigameTool tool) {
        tool.setSetting(SETTING_KEY, "None");
        final @NotNull Menu menu = new Menu(2, MgMenuLangKey.MENU_TOOL_REGENAREA_SELECT_NAME, mgPlayer);

        if (mgPlayer.isInMenu()) {
            menu.setItem(new MenuItemBack(mgPlayer.getMenu()), menu.getSize() - 9);
        }

        menu.addItem(new MenuItemString(ItemType.PAPER, MgMenuLangKey.MENU_TOOL_REGENAREA_REGIONNAME_NAME, new Callback<>() {

            @Override
            public @NotNull String getValue() {
                return tool.getSetting(SETTING_KEY);
            }

            @Override
            public void setValue(@NotNull String value) {
                tool.setSetting(SETTING_KEY, value);
            }
        }));

        if (tool.getMinigame() != null) {
            Menu regionMenu = new Menu(6, MgMenuLangKey.MENU_TOOL_REGENAREA_REGIONS_NAME, mgPlayer);
            final @NotNull List<@NotNull AMenuItem> menuItems = new ArrayList<>();

            for (final MgRegion region : tool.getMinigame().getRegenRegions()) {
                MenuItemCustom customMenuItem = new MenuItemCustom(ItemType.CHEST, Component.text(region.getName()));

                // Set the region area and go back to the main menu
                customMenuItem.setClick(() -> {
                    tool.setSetting(SETTING_KEY, region.getName());

                    menu.displayMenu();

                    return ItemStack.empty();
                });

                menuItems.add(customMenuItem);
            }

            regionMenu.addItems(menuItems);
            regionMenu.setItem(new MenuItemBack(menu), regionMenu.getSize() - 9);

            menu.addItem(new MenuItemPage(ItemType.CHEST, MgMenuLangKey.MENU_TOOL_REGENAREA_REGIONEDIT_NAME, regionMenu));
        }
        menu.displayMenu();
    }

    @Override
    public void onUnsetMode(final @NotNull MinigamePlayer mgPlayer, final @NotNull MinigameTool tool) {
        tool.removeSetting(SETTING_KEY);
    }

    @Override
    public void onLeftClick(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame,
                            final @Nullable Team team, final @NotNull PlayerInteractEvent event) {
        if (mgPlayer.hasSelection()) {
            final @NotNull String name = MinigameTool.getMinigameTool(mgPlayer).getSetting(SETTING_KEY); //todo expose Settings
            final @Nullable MgRegion region = minigame.getRegenRegion(name);

            final @NotNull RegenRegionChangeResult result = minigame.setRegenRegion(new MgRegion(name, mgPlayer.getSelectionLocations()[0], mgPlayer.getSelectionLocations()[1]));

            if (result.success()) {
                if (region == null) {
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.REGION_REGENREGION_CREATED,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.REGION.getKey(), name),
                        Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(result.numOfBlocksTotal())),
                        Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), String.valueOf(minigame.getRegenBlocklimit())));
                } else {
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.REGION_REGENREGION_UPDATED,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.REGION.getKey(), name),
                        Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(result.numOfBlocksTotal())),
                        Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), String.valueOf(minigame.getRegenBlocklimit())));
                }

                mgPlayer.clearSelection();
            } else {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.REGION_REGENREGION_ERROR_LIMIT,
                    Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(result.numOfBlocksTotal())),
                    Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), String.valueOf(minigame.getRegenBlocklimit())));
            }
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NOREGIONSELECTED);
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
        final @NotNull String name = MinigameTool.getMinigameTool(mgPlayer).getSetting(SETTING_KEY);
        if (minigame.getRegenRegion(name) != null) {
            displayedRegions.put(mgPlayer.getUUID(),
                Minigames.getPlugin().getDisplayManager().displayCuboid(mgPlayer.getPlayer(), minigame.getRegenRegion(name)));
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SELECTED_REGENREGION,
                Placeholder.unparsed(MinigamePlaceHolderKey.REGION.getKey(), name),
                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.REGION_ERROR_NOREGENREION,
                Placeholder.unparsed(MinigamePlaceHolderKey.REGION.getKey(), name));
        }
    }

    @Override
    public void deselect(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame, final @Nullable Team team) {
        String name = MinigameTool.getMinigameTool(mgPlayer).getSetting(SETTING_KEY);
        if (minigame.getRegenRegion(name) != null) {

            IDisplayObject displayed = displayedRegions.get(mgPlayer.getUUID());
            if (displayed != null) {
                displayed.remove();
            }

            mgPlayer.clearSelection();

            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_DESELECTED_REGION);
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.REGION_ERROR_NOREGENREION,
                Placeholder.unparsed(MinigamePlaceHolderKey.REGION.getKey(), name));
        }
    }
}
