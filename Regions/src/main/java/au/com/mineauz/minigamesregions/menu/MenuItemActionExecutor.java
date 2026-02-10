package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.ActionExecutorHolder;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.actions.ActionRegistry;
import au.com.mineauz.minigamesregions.conditions.ConditionRegistry;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MenuItemActionExecutor extends AMenuItem {
    private static final String DESCRIPTION_TOKEN = "Executor_description";
    private final @NotNull ActionExecutorHolder actionExecutorHolder;
    private final @NotNull ActionExecutor actionExecutor;

    protected static @NotNull Component getName(final @NotNull ActionExecutorHolder holder) {
        if (holder instanceof Region){
            return RegionMessageManager.getMessage(RegionLangKey.MENU_REGIONEXECUTOR_NAME);
        } else if (holder instanceof Node) {
            return RegionMessageManager.getMessage(RegionLangKey.MENU_NODEEXECUTOR_NAME);
        } else {
            throw new IllegalArgumentException("Unknown ActionExecutorHolder type: " + holder.getClass());
        }
    }

    public MenuItemActionExecutor(final @NotNull ActionExecutorHolder actionExecutorHolder, final @NotNull ActionExecutor actionExecutor) {
        super(ItemType.ENDER_PEARL, getName(actionExecutorHolder));
        this.actionExecutorHolder = actionExecutorHolder;
        this.actionExecutor = actionExecutor;
        setDescriptionPart(DESCRIPTION_TOKEN, List.of(
                RegionMessageManager.getMessage(RegionLangKey.MENU_EXECUTOR_TRIGGER,
                        Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(), actionExecutor.getTrigger().getDisplayName())),
                RegionMessageManager.getMessage(RegionLangKey.MENU_EXECUTOR_ACTION,
                        Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(actionExecutor.getActions().size()))),
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK).
                        color(NamedTextColor.DARK_PURPLE),
                RegionMessageManager.getMessage(RegionLangKey.MENU_EXECUTOR_EDIT)));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NonNull ItemStack onClick() {
        final @NotNull Menu menu = new Menu(3, getName(actionExecutorHolder),
            getMenu().getIntendedViewer());

        final @NotNull MenuItemPage actionItem = new MenuItemPage(MenuDisplayTypes.genericSubMenu(),
            RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_NAME),
            ActionRegistry.createMenu(actionExecutor, menu));
        menu.addItem(actionItem);

        final @NotNull MenuItemPage conditionsMenuItem = new MenuItemPage(MenuDisplayTypes.genericSubMenu(),
            RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITIONS_NAME),
            ConditionRegistry.createMenu(actionExecutor, menu));
        menu.addItem(conditionsMenuItem);

        menu.addItem(new MenuItemNewLine());

        if (actionExecutor.getTrigger().triggerOnPlayerAvailable()) {
            menu.addItem(new MenuItemInteger(ItemType.STONE,
                    RegionMessageManager.getMessage(RegionLangKey.MENU_EXECUTOR_TRIGGERCOUNT_NAME),
                    RegionMessageManager.getMessageList(RegionLangKey.MENU_EXECUTOR_TRIGGERCOUNT_DESCRIPTION),
                    actionExecutor.getTriggerCountCallback(), 0, null));

            menu.addItem(new MenuItemBoolean(MenuDisplayTypes.playerType(),
                    RegionMessageManager.getMessage(RegionLangKey.MENU_EXECUTOR_PERPLAYER_NAME),
                    RegionMessageManager.getMessageList(RegionLangKey.MENU_EXECUTOR_PERPLAYER_DESCRIPTION),
                    actionExecutor.getIsTriggerPerPlayerCallback()));
        }
        menu.setItem(new MenuItemBack(getMenu()), menu.getSize() - 9);
        menu.displayMenu();
        return ItemStack.empty();
    }

    @Override
    public @NonNull ItemStack onRightClick() {
        actionExecutorHolder.removeExecutor(actionExecutor);
        getMenu().removeItem(getSlot());
        return ItemStack.empty();
    }
}
