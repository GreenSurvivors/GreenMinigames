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

/**
 * Used when the menu item holds a ItemType.
 * <p>
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 15/11/2018.
 */
public class MenuItemItemType extends MenuItem {
    private static final String DESCRIPTION_TOKEN = "ItemType_description";
    private final @NotNull Callback<@NotNull ItemType> ItemTypeCallback;

    public MenuItemItemType(@Nullable Component name, @NotNull Callback<@NotNull ItemType> c) {
        super(c.getValue(), name);
        ItemTypeCallback = c;
    }

    public MenuItemItemType(@Nullable Component name,
                            @Nullable List<@NotNull Component> description, @NotNull Callback<@NotNull ItemType> c) {
        super(c.getValue(), name, description);
        ItemTypeCallback = c;
    }

    @Override
    public @NotNull ItemStack onClickWithItem(final @NotNull ItemStack item) {
        ItemTypeCallback.setValue(item.getType().asItemType());
        updateDescription();
        return super.onClickWithItem(item);
    }

    @Override
    public @NotNull ItemStack onShiftRightClick() {
        ItemTypeCallback.setValue(ItemType.WOODEN_HOE);
        return super.onShiftRightClick();
    }

    public void updateDescription() {
        setDescriptionPart(DESCRIPTION_TOKEN, List.of(
            MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_ITEMTYPE_DESCRIPTION,
                Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(),
                    Component.translatable(ItemTypeCallback.getValue().translationKey())))));

        setDisplayItem(ItemTypeCallback.getValue().createItemStack());
    }
}
