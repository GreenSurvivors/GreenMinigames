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

public class MenuItemAddFlag extends MenuItem implements StringConsumer {
    private final @NotNull Minigame mgm;

    public MenuItemAddFlag(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey, @NotNull Minigame mgm) {
        super(displayType, langKey);
        this.mgm = mgm;
    }

    public MenuItemAddFlag(@Nullable ItemType displayType, @NotNull Component name, @NotNull Minigame mgm) {
        super(displayType, name);
        this.mgm = mgm;
    }

    public MenuItemAddFlag(@Nullable ItemType displayType, @Nullable Component name, List<@NotNull Component> description,
                           @NotNull Minigame mgm) {
        super(displayType, name, description);
        this.mgm = mgm;
    }

    @Override
    public @NotNull ItemStack onClick() {
        MinigamePlayer mgPlayer = getContainer().getViewer();
        mgPlayer.setNoClose(true);
        mgPlayer.getPlayer().closeInventory();

        final @NotNull Duration reopenTime = Duration.ofSeconds(20);
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_FLAGADD_ENTERCHAT,
            Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(), getName()),
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));
        mgPlayer.setManualEntry(this);
        getContainer().startReopenTimer(reopenTime);

        return ItemStack.empty();
    }

    @Override
    public void acceptString(@NotNull String string) {
        mgm.addSinglePlayerFlag(string);
        getContainer().addItem(new MenuItemFlag(ItemType.OAK_SIGN, string, mgm.getSinglePlayerFlags()));

        getContainer().cancelReopenTimer();
        getContainer().displayMenu(getContainer().getViewer());
    }
}
