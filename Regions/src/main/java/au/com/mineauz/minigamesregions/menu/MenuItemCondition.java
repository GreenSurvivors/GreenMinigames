package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.conditions.ACondition;
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

public class MenuItemCondition extends MenuItem {
    private final @NotNull ACondition con;
    private final @NotNull ActionExecutor executor;

    public MenuItemCondition(@Nullable ItemType displayType, @Nullable Component name,
                             @NotNull ActionExecutor exec, @NotNull ACondition con) {
        super(displayType, name);
        this.executor = exec;
        this.con = con;

        updateDescription();
    }

    @Override
    public void update() {
        updateDescription();
    }

    private void updateDescription() {
        @NotNull Map<@NotNull Component, @Nullable Component> out = con.describe();

        if (out.isEmpty()) {
            return;
        }

        // Convert the description
        List<Component> description = new ArrayList<>();
        for (Entry<Component, Component> entry : out.entrySet()) {
            Component value = entry.getValue() == null ?
                    MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_ELEMENTNOTSET).
                            color(NamedTextColor.YELLOW) :
                    entry.getValue();

            Component line = RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_DESCRIPTION,
                    Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), entry.getKey()),
                    Placeholder.component(MinigamePlaceHolderKey.STATE.getKey(), value));

            description.add(MinigameUtils.limitIgnoreFormat(line, 35));
        }

        setBaseDescriptionPart(description);
    }

    @Override
    public @NonNull ItemStack onClick() {
        if (con.displayMenu(getContainer().getViewer(), getContainer())) {
            return ItemStack.empty();
        }
        return getDisplayItem();
    }

    @Override
    public @NonNull ItemStack onRightClick() {
        executor.removeCondition(con);
        getContainer().removeItem(getSlot());
        return ItemStack.empty();
    }
}
