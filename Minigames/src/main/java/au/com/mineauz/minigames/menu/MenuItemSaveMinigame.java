package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.MessageManager;
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

public class MenuItemSaveMinigame extends AMenuItem {
    private final @NotNull Minigame mgm;

    public MenuItemSaveMinigame(final @Nullable ItemType displayType, final @Nullable Component name,
                                final @NotNull Minigame minigame) {
        this(displayType, name, null, minigame);
    }

    public MenuItemSaveMinigame(final @Nullable ItemType displayType, final @Nullable Component name,
                                final @Nullable List<@NotNull Component> description,
                                final @NotNull Minigame minigame) {
        super(displayType, name, description);
        mgm = minigame;
    }

    @Override
    public @NotNull ItemStack onClick() {
        mgm.saveMinigame();
        MessageManager.sendMessage(getMenu().getIntendedViewer(), MinigameMessageType.SUCCESS,
            MgMiscLangKey.MINIGAME_SAVED,
            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), mgm.getName()));
        return getDisplayItem();
    }
}
