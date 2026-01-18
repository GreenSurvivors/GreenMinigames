package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.actions.ActionRegistry;
import au.com.mineauz.minigamesregions.conditions.ConditionRegistry;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MenuItemRegionExecutor extends MenuItem { // todo merge with MenuItemNodeExecutor
    private static final String DESCRIPTION_TOKEN = "Executor_description";
    private final @NotNull Region region;
    private final @NotNull ActionExecutor ex;

    public MenuItemRegionExecutor(@NotNull Region region, @NotNull ActionExecutor ex) {
        super(ItemType.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_REGIONEXECUTOR_NAME));
        this.region = region;
        this.ex = ex;
        setDescriptionPart(DESCRIPTION_TOKEN, List.of(
                RegionMessageManager.getMessage(RegionLangKey.MENU_EXECUTOR_TRIGGER,
                        Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(), ex.getTrigger().getDisplayName())),
                RegionMessageManager.getMessage(RegionLangKey.MENU_EXECUTOR_ACTION,
                        Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(ex.getActions().size()))),
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK).
                        color(NamedTextColor.DARK_PURPLE),
                RegionMessageManager.getMessage(RegionLangKey.MENU_EXECUTOR_EDIT)));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NonNull ItemStack onClick() {
        final MinigamePlayer fviewer = getContainer().getViewer();
        final Menu menu = new Menu(3, RegionMessageManager.getMessage(RegionLangKey.MENU_REGIONEXECUTOR_NAME), fviewer);

        MenuItemCustom actionItem = new MenuItemCustom(ItemType.CHEST,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_NAME));
        actionItem.setClick(() -> {
            ActionRegistry.displayMenu(fviewer, ex, menu);
            return ItemStack.empty();
        });
        menu.addItem(actionItem);

        MenuItemCustom c2 = new MenuItemCustom(ItemType.CHEST,
                RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITIONS_NAME));
        c2.setClick(() -> {
            ConditionRegistry.displayMenu(fviewer, ex, menu);
            return ItemStack.empty();
        });
        menu.addItem(c2);

        menu.addItem(new MenuItemNewLine());
        if (ex.getTrigger().triggerOnPlayerAvailable()) {
            menu.addItem(new MenuItemInteger(ItemType.STONE,
                    RegionMessageManager.getMessage(RegionLangKey.MENU_EXECUTOR_TRIGGERCOUNT_NAME),
                    RegionMessageManager.getMessageList(RegionLangKey.MENU_EXECUTOR_TRIGGERCOUNT_DESCRIPTION),
                    ex.getTriggerCountCallback(), 0, null));

            menu.addItem(new MenuItemBoolean(ItemType.PLAYER_HEAD,
                    RegionMessageManager.getMessage(RegionLangKey.MENU_EXECUTOR_PERPLAYER_NAME),
                    RegionMessageManager.getMessageList(RegionLangKey.MENU_EXECUTOR_PERPLAYER_DESCRIPTION),
                    ex.getIsTriggerPerPlayerCallback()));
        }
        menu.addItem(new MenuItemBack(getContainer()), menu.getSize() - 9);
        menu.displayMenu(fviewer);
        return ItemStack.empty();
    }

    @Override
    public @NonNull ItemStack onRightClick() {
        region.removeExecutor(ex);
        getContainer().removeItem(getSlot());
        return ItemStack.empty();
    }
}
