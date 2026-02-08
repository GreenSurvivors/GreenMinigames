package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.loadout.PlayerLoadout;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class MenuItemLoadoutAdd extends MenuItem implements StringConsumer {
    private final @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts;
    private @Nullable Minigame minigame = null;

    public MenuItemLoadoutAdd(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey, @NotNull Map<@NotNull String,
                              @NotNull PlayerLoadout> loadouts, @Nullable Minigame mgm) {
        super(displayType, langKey);
        this.loadouts = loadouts;
        this.minigame = mgm;
    }

    public MenuItemLoadoutAdd(@Nullable ItemType displayType, @Nullable Component name, @NotNull Map<@NotNull String,
                              @NotNull PlayerLoadout> loadouts, @Nullable Minigame mgm) {
        super(displayType, name);
        this.loadouts = loadouts;
        this.minigame = mgm;
    }

    public MenuItemLoadoutAdd(@Nullable ItemType displayType, @Nullable Component name,
                              @Nullable List<@NotNull Component> description,
                              @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts, @Nullable Minigame mgm) {
        super(displayType, name, description);
        this.loadouts = loadouts;
        this.minigame = mgm;
    }

    public MenuItemLoadoutAdd(@Nullable ItemType displayType, @Nullable Component name,
                              @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts) {
        super(displayType, name);
        this.loadouts = loadouts;
    }

    public MenuItemLoadoutAdd(@Nullable ItemType displayType, @Nullable Component name,
                              @Nullable List<@NotNull Component> description,
                              @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts) {
        super(displayType, name, description);
        this.loadouts = loadouts;
    }

    public MenuItemLoadoutAdd(@Nullable ItemType displayType, @NotNull MgMenuLangKey langKey,
                              @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts) {
        super(displayType, langKey);
        this.loadouts = loadouts;
    }

    @Override
    public @NotNull ItemStack onClick() {
        MinigamePlayer mgPlayer = getMenu().getIntendedViewer();
        final @NotNull Duration reopenTime = Duration.ofSeconds(30);
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_LOADOUT_ADD_ENTERCHAT,
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));

        getMenu().closeAndWaitForInput(reopenTime, this);
        return ItemStack.empty();
    }

    @Override
    public void acceptString(@NotNull String string) {
        string = string.replace(" ", "_");
        if (!loadouts.containsKey(string)) {
            for (int i = 0; i < 45; i++) {
                if (!getMenu().hasMenuItem(i)) {
                    final @NotNull PlayerLoadout loadout = new PlayerLoadout(string);
                    loadouts.put(string, loadout);
                    List<Component> des = MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK);

                    if (minigame != null) {
                        getMenu().addItem(new MenuItemDisplayLoadout(ItemType.DIAMOND_SWORD, loadout.getDisplayName(), des, loadout, minigame), i);
                    } else {
                        getMenu().addItem(new MenuItemDisplayLoadout(ItemType.DIAMOND_SWORD, loadout.getDisplayName(), des, loadout), i);
                    }
                    break;
                }
            }

            getMenu().cancelWaitForInput();
            getMenu().displayMenu();
        } else {
            getMenu().cancelWaitForInput();
            getMenu().displayMenu();

            MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_LOADOUT_ERROR_ALREADYEXISTS,
                Placeholder.unparsed(MinigamePlaceHolderKey.LOADOUT.getKey(), string));
        }
    }
}
