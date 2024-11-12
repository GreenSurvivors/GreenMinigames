package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.PlayerLoadout;
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
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class MenuItemLoadoutAdd extends MenuItem implements StringConsumer {
    private final @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts;
    private @Nullable Minigame minigame = null;

    public MenuItemLoadoutAdd(@Nullable Material displayMat, @NotNull MinigameLangKey langKey, @NotNull Map<@NotNull String,
            @NotNull PlayerLoadout> loadouts, @Nullable Minigame mgm) {
        super(displayMat, langKey);
        this.loadouts = loadouts;
        this.minigame = mgm;
    }

    public MenuItemLoadoutAdd(@Nullable Material displayMat, @Nullable Component name, @NotNull Map<@NotNull String,
            @NotNull PlayerLoadout> loadouts, @Nullable Minigame mgm) {
        super(displayMat, name);
        this.loadouts = loadouts;
        this.minigame = mgm;
    }

    public MenuItemLoadoutAdd(@Nullable Material displayMat, @Nullable Component name,
                              @Nullable List<@NotNull Component> description,
                              @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts, @Nullable Minigame mgm) {
        super(displayMat, name, description);
        this.loadouts = loadouts;
        this.minigame = mgm;
    }

    public MenuItemLoadoutAdd(@Nullable Material displayMat, @Nullable Component name,
                              @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts) {
        super(displayMat, name);
        this.loadouts = loadouts;
    }

    public MenuItemLoadoutAdd(@Nullable Material displayMat, @Nullable Component name,
                              @Nullable List<@NotNull Component> description,
                              @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts) {
        super(displayMat, name, description);
        this.loadouts = loadouts;
    }

    public MenuItemLoadoutAdd(@Nullable Material displayMat, @NotNull MgMenuLangKey langKey,
                              @NotNull Map<@NotNull String, @NotNull PlayerLoadout> loadouts) {
        super(displayMat, langKey);
        this.loadouts = loadouts;
    }

    @Override
    public @Nullable ItemStack onClick() {
        MinigamePlayer mgPlayer = getContainer().getViewer();
        mgPlayer.setNoClose(true);
        mgPlayer.getPlayer().closeInventory();
        final int reopenSeconds = 30;
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_LOADOUT_ADD_ENTERCHAT,
                Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(reopenSeconds))));
        mgPlayer.setManualEntry(this);

        getContainer().startReopenTimer(reopenSeconds);
        return null;
    }

    @Override
    public void acceptString(String string) {
        string = string.replace(" ", "_");
        if (!loadouts.containsKey(string)) {
            for (int i = 0; i < 45; i++) {
                if (!getContainer().hasMenuItem(i)) {
                    PlayerLoadout loadout = new PlayerLoadout(string);
                    loadouts.put(string, loadout);
                    List<Component> des = MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK);

                    if (minigame != null) {
                        getContainer().addItem(new MenuItemDisplayLoadout(Material.DIAMOND_SWORD, loadout.getDisplayName(), des, loadout, minigame), i);
                    } else {
                        getContainer().addItem(new MenuItemDisplayLoadout(Material.DIAMOND_SWORD, loadout.getDisplayName(), des, loadout), i);
                    }
                    break;
                }
            }

            getContainer().cancelReopenTimer();
            getContainer().displayMenu(getContainer().getViewer());
        } else {
            getContainer().cancelReopenTimer();
            getContainer().displayMenu(getContainer().getViewer());

            MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_LOADOUT_ERROR_ALREADYEXISTS,
                Placeholder.unparsed(MinigamePlaceHolderKey.LOADOUT.getKey(), string));
        }
    }
}
