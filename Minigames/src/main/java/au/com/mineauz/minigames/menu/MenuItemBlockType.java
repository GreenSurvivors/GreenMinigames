package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemBlockType extends AMenuItem {
    private static final String DESCRIPTION_TOKEN = "BlockType_description";
    private final @NotNull Callback<BlockType> blockTypeCallback;

    public MenuItemBlockType(final @Nullable ItemType displayType, final @Nullable Component name,
                             final @NotNull Callback<BlockType> c) {
        super(displayType, name);
        blockTypeCallback = c;
    }

    public MenuItemBlockType(final @Nullable ItemType displayType, final @Nullable Component name,
                             final @Nullable List<@NotNull Component> description,
                             final @NotNull Callback<BlockType> c) {
        super(displayType, name, description);
        blockTypeCallback = c;
    }

    @Override
    public @NotNull ItemStack onClickWithItem(final @NotNull ItemStack item) {
        blockTypeCallback.setValue(item.getType().asBlockType());
        updateDescription();
        return super.onClickWithItem(item);
    }

    @Override
    public @NotNull ItemStack onShiftRightClick() {
        blockTypeCallback.setValue(BlockType.STONE);
        return super.onShiftRightClick();
    }

    public void updateDescription() {
        setDescriptionPart(DESCRIPTION_TOKEN, List.of(
            MessageManager.getMessage(MgMenuLangKey.MENU_BLOCKTYPE_DESCRIPTION,
                Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(),
                    Component.translatable(blockTypeCallback.getValue().translationKey())))));

        setDisplayItem(blockTypeCallback.getValue().getItemType().createItemStack());
    }
}
