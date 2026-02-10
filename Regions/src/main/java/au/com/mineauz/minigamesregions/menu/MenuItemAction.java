package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.actions.IAction;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

// note: does not extends MenuItemPage, its optional for an action to have a menu.
public class MenuItemAction extends AMenuItem {
    private static final @NotNull String DESCRIPTION_TOKEN = "Action_description";
    private final @NotNull ActionExecutor exec;
    private final @NotNull IAction act;

    public MenuItemAction(final @Nullable ItemType displayType, final @Nullable Component name,
                          final @NotNull ActionExecutor exec, final @NotNull IAction act) {
        super(displayType, name);
        this.exec = exec;
        this.act = act;
        updateDescription();
    }

    @Override
    public void update() {
        updateDescription();
    }

    private void updateDescription() {
        final @NotNull Map<@NotNull Component, @Nullable Component> out = act.describe();

        if (out.isEmpty()) {
            return;
        }

        // Convert the description
        final @NotNull List<@NotNull Component> description = new ArrayList<>();
        for (final @NotNull Entry<@NotNull Component, @Nullable Component> entry : out.entrySet()) {
            final @NotNull Component value = entry.getValue() == null ?
                    MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_ELEMENTNOTSET).
                            color(NamedTextColor.YELLOW) :
                    entry.getValue();

            final @NotNull Component line = RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_DESCRIPTION,
                    Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), entry.getKey()),
                    Placeholder.component(MinigamePlaceHolderKey.STATE.getKey(), value));

            description.add(MinigameUtils.limitIgnoreFormat(line, 35));
        }

        setDescriptionPart(DESCRIPTION_TOKEN, description);
    }

    @Override
    public @NonNull ItemStack onClick() {
        if (act.displayMenu(getMenu())) {
            return ItemStack.empty();
        }
        return getDisplayItem();
    }

    @Override
    public @NonNull ItemStack onRightClick() {
        exec.removeAction(act);
        getMenu().removeItem(getSlot());
        return ItemStack.empty();
    }
}
