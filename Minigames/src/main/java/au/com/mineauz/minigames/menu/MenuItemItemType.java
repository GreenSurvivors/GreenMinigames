package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemItemType extends AMenuItem {
    private static final String DESCRIPTION_TOKEN = "ItemType_description";
    private final @NotNull Callback<@NotNull ItemType> itemTypeCallback;

    public MenuItemItemType(final @Nullable Component name, final @NotNull Callback<@NotNull ItemType> callback) {
        this(name, null, callback);
    }

    public MenuItemItemType(final @Nullable Component name,
                            final @Nullable List<@NotNull Component> description,
                            final @NotNull Callback<@NotNull ItemType> callback) {
        super(callback.getValue(), name, description);
        itemTypeCallback = callback;
    }

    @Override
    public @NotNull ItemStack onClickWithItem(final @NotNull ItemStack item) {
        itemTypeCallback.setValue(item.getType().asItemType());
        updateDescription();
        return super.onClickWithItem(item);
    }

    @Override
    public @NotNull ItemStack onShiftRightClick() {
        itemTypeCallback.setValue(ItemType.WOODEN_HOE);
        return super.onShiftRightClick();
    }

    public void updateDescription() {
        setDescriptionPart(DESCRIPTION_TOKEN, List.of(
            MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_ITEMTYPE_DESCRIPTION,
                Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(),
                    Component.translatable(itemTypeCallback.getValue().translationKey())))));

        setDisplayItem(itemTypeCallback.getValue().createItemStack());
    }
}
