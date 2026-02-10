package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.minigame.modules.loadout.PlayerLoadout;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MenuItemStatusEffect extends AMenuItem {
    private static final String DESCRIPTION_TOKEN = "Potion_description";
    private final @NotNull PotionEffect eff;
    private final @NotNull PlayerLoadout loadout;

    public MenuItemStatusEffect(final @Nullable ItemType displayType, final @Nullable Component name,
                                final @NotNull PotionEffect eff, final @NotNull PlayerLoadout loadout) {
        this(displayType, name, null, eff, loadout);
    }

    public MenuItemStatusEffect(final @Nullable ItemType displayType, final @Nullable Component name,
                                final @Nullable List<@NotNull Component> description,
                                final @NotNull PotionEffect eff, final @NotNull PlayerLoadout loadout) {
        super(displayType, name, description);
        this.eff = eff;
        this.loadout = loadout;
        updateDescription();
    }

    public void updateDescription() {
        final @NotNull List<@NotNull Component> description = new ArrayList<>();
        description.add(MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_STATUSEFFECT_LEVEL,
            Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(eff.getAmplifier() + 1))));

        if (eff.isInfinite()) {
            description.add(MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_STATUSEFFECT_DURATION,
                Placeholder.component(MinigamePlaceHolderKey.NUMBER.getKey(),
                    MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_NUMBER_INFINITE))));
        } else {
            description.add(MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_STATUSEFFECT_DURATION,
                Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(eff.getDuration()))));
        }

        setDescriptionPart(DESCRIPTION_TOKEN, description);
    }

    @Override
    public @NotNull ItemStack onShiftRightClick() {
        loadout.removePotionEffect(eff);
        getMenu().removeItem(getSlot());
        return ItemStack.empty();
    }

    public @NotNull PotionEffect getEffect() {
        return eff;
    }
}
