package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigamesregions.actions.ActionInterface;
import au.com.mineauz.minigamesregions.executors.BaseExecutor;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MenuItemAction extends MenuItem {
    private final static String DESCRIPTION_TOKEN = "Action_description";
    private final @NotNull BaseExecutor exec;
    private final @NotNull ActionInterface act;

    public MenuItemAction(@Nullable Material displayMat, @Nullable Component name,
                          @NotNull BaseExecutor exec, @NotNull ActionInterface act) {
        super(displayMat, name);
        this.exec = exec;
        this.act = act;
        updateDescription();
    }

    @Override
    public void update() {
        updateDescription();
    }

    private void updateDescription() {
        Map<Component, Component> out = act.describe();

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

        setDescriptionPart(DESCRIPTION_TOKEN, description);
    }

    @Override
    public @Nullable ItemStack onClick() {
        if (act.displayMenu(getContainer().getViewer(), getContainer())) {
            return null;
        }
        return getDisplayItem();
    }

    @Override
    public ItemStack onRightClick() {
        exec.removeAction(act);
        getContainer().removeItem(getSlot());
        return null;
    }
}
