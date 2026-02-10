package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class MenuItemFlag extends AMenuItem {
    private final @NotNull String flag;
    final @NotNull Consumer<@NotNull String> removeConsumer;

    public MenuItemFlag(final @Nullable ItemType displayType, final @NotNull String flag,
                        final @NotNull Consumer<@NotNull String> removeConsumer) {
        this(displayType, null, flag, removeConsumer);
    }

    public MenuItemFlag(final @Nullable ItemType displayType,
                        final @Nullable List<@NotNull Component> description,
                        final @NotNull String flag,
                        final @NotNull Consumer<@NotNull String> removeConsumer) {
        super(displayType, Component.text(flag), description);
        this.flag = flag;
        this.removeConsumer = removeConsumer;
    }

    @Override
    public @NotNull ItemStack onShiftRightClick() {
        MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.INFO, MgMenuLangKey.MENU_FLAG_REMOVED,
            Placeholder.unparsed(MinigamePlaceHolderKey.FLAG.getKey(), flag));
        removeConsumer.accept(flag);

        getMenu().removeItem(getSlot());
        return ItemStack.empty();
    }
}
