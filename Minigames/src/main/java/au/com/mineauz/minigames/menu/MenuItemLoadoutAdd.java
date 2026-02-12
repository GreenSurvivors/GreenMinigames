package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.loadout.LoadoutModule;
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

public class MenuItemLoadoutAdd extends AMenuItem implements StringConsumer {
    private final @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts;
    private final @Nullable Minigame minigame;

    /// since no minigame was given, the loadout will be assumed to be global.
    public MenuItemLoadoutAdd(final @Nullable ItemType displayType, final @NotNull MgMenuLangKey langKey,
                              final @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts) {
        this(displayType, langKey, loadouts, null);
    }

    /// if a valid minigame is given, the new loadout will be added to the minigames {@link LoadoutModule}, elsewise the new Loadout will be global.
    public MenuItemLoadoutAdd(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                              final @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts,
                              final @Nullable Minigame minigame) {
        super(displayType, langKey);
        this.loadouts = loadouts;
        this.minigame = minigame;
    }

    /// since no minigame was given, the loadout will be assumed to be global.
    public MenuItemLoadoutAdd(final @Nullable ItemType displayType, final @Nullable Component name,
                              final @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts) {
        this(displayType, name, loadouts, null);
    }

    /// since no minigame was given, the loadout will be assumed to be global.
    public MenuItemLoadoutAdd(final @Nullable ItemType displayType, final @Nullable Component name,
                              final @Nullable List<@NotNull Component> description,
                              final @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts) {
        this(displayType, name, description, loadouts, null);
    }

    /// if a valid minigame is given, the new loadout will be added to the minigames {@link LoadoutModule}, elsewise the new Loadout will be global.
    public MenuItemLoadoutAdd(final @Nullable ItemType displayType, final @Nullable Component name,
                              final @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts,
                              final @Nullable Minigame minigame) {
        this(displayType, name, null, loadouts, minigame);
    }

    /// if a valid minigame is given, the new loadout will be added to the minigames {@link LoadoutModule}, elsewise the new Loadout will be global.
    public MenuItemLoadoutAdd(final @Nullable ItemType displayType, final @Nullable Component name,
                              final @Nullable List<@NotNull Component> description,
                              final @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts,
                              final @Nullable Minigame minigame) {
        super(displayType, name, description);
        this.loadouts = loadouts;
        this.minigame = minigame;
    }

    @Override
    public @NotNull ItemStack onClick() {
        MinigamePlayer mgPlayer = getMenu().getIntendedViewer();
        final @NotNull Duration reopenTime = Duration.ofSeconds(30);
        MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_LOADOUT_ADD_ENTERCHAT,
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
                    final @NotNull List<@NotNull Component> des = MessageManager.getMessageList(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK);

                    getMenu().setItem(new MenuItemDisplayLoadout(ItemType.DIAMOND_SWORD, loadout.getDisplayName(), des, loadout, minigame), i);
                    break;
                }
            }

            getMenu().cancelWaitForInput();
            getMenu().displayMenu();
        } else {
            getMenu().cancelWaitForInput();
            getMenu().displayMenu();

            MessageManager.sendMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_LOADOUT_ERROR_ALREADYEXISTS,
                Placeholder.unparsed(MinigamePlaceHolderKey.LOADOUT.getKey(), string));
        }
    }
}
