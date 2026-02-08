package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemSaveMinigame extends MenuItem {
    private final @NotNull Minigame mgm;

    public MenuItemSaveMinigame(@Nullable ItemType displayType, @Nullable Component name, @NotNull Minigame minigame) {
        super(displayType, name);
        mgm = minigame;
    }

    public MenuItemSaveMinigame(@Nullable ItemType displayType, @Nullable Component name,
                                @Nullable List<@NotNull Component> description, @NotNull Minigame minigame) {
        super(displayType, name, description);
        mgm = minigame;
    }

    @Override
    public @NotNull ItemStack onClick() {
        mgm.saveMinigame();
        MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.SUCCESS,
            MgMiscLangKey.MINIGAME_SAVED,
            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), mgm.getName()));
        return getDisplayItem();
    }
}
