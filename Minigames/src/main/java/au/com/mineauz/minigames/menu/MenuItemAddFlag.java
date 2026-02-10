package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class MenuItemAddFlag extends AMenuItem implements StringConsumer {
    private final @NotNull Minigame minigame;

    public MenuItemAddFlag(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                           final @NotNull Minigame minigame) {
        super(displayType, langKey);
        this.minigame = minigame;
    }

    public MenuItemAddFlag(final @Nullable ItemType displayType, final @NotNull Component name,
                           final @NotNull Minigame minigame) {
        this(displayType, name, null, minigame);
    }

    public MenuItemAddFlag(final @Nullable ItemType displayType, final @Nullable Component name,
                           final @Nullable List<@NotNull Component> description,
                           final @NotNull Minigame minigame) {
        super(displayType, name, description);
        this.minigame = minigame;
    }

    @Override
    public @NotNull ItemStack onClick() {
        MinigamePlayer mgPlayer = getMenu().getIntendedViewer();

        final @NotNull Duration reopenTime = Duration.ofSeconds(20);
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_FLAGADD_ENTERCHAT,
            Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(), getName()),
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));
        getMenu().closeAndWaitForInput(reopenTime, this);

        return ItemStack.empty();
    }

    @Override
    public void acceptString(final @NotNull String string) {
        minigame.addSinglePlayerFlag(string);
        getMenu().addItem(new MenuItemFlag(ItemType.OAK_SIGN, string, minigame::removeSinglePlayerFlag));

        getMenu().cancelWaitForInput();
        getMenu().displayMenu();
    }
}
