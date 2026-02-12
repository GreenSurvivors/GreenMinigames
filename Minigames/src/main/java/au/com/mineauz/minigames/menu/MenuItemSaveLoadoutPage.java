package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.minigame.modules.loadout.PlayerLoadout;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemSaveLoadoutPage extends MenuItemPage {
    private final @NotNull PlayerLoadout loadout;

    public MenuItemSaveLoadoutPage(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                                   final @NotNull PlayerLoadout loadout, final @NotNull Menu menu) {
        super(displayType, langKey, menu);
        this.loadout = loadout;
    }

    public MenuItemSaveLoadoutPage(final @Nullable ItemType displayType, final @Nullable Component name,
                                   final @NotNull PlayerLoadout loadout,
                                   final @NotNull Menu menu) {
        this(displayType, name, null, loadout, menu);
    }

    public MenuItemSaveLoadoutPage(final @Nullable ItemType displayType, final @Nullable Component name,
                                   final @Nullable List<@NotNull Component> description,
                                   final @NotNull PlayerLoadout loadout, final @NotNull Menu menu) {
        super(displayType, name, description, menu);
        this.loadout = loadout;
    }

    @Override
    public @NotNull ItemStack onClick() {
        final @NotNull ItemStack @NotNull [] items = getMenu().getInventory();
        loadout.clearLoadout();

        for (int i = 0; i < 36; i++) {
            if (items[i].isEmpty()) {
                loadout.addItem(items[i], i);
            }
        }

        final int numOfSpecialSlots = loadout.allowOffHand() ? 41 : 40; // todo don't hardcode
        for (int i = 36; i < numOfSpecialSlots; i++) {
            if (items[i].isEmpty()) {
                switch (i) {
                    case 36 -> loadout.addItem(items[i], 103);
                    case 37 -> loadout.addItem(items[i], 102);
                    case 38 -> loadout.addItem(items[i], 101);
                    case 39 -> loadout.addItem(items[i], 100);
                    case 40 -> loadout.addItem(items[i], -106);
                }
            }
        }
        MessageManager.sendMessage(getMenu().getIntendedViewer(), MinigameMessageType.INFO, MgMenuLangKey.MENU_LOADOUT_SAVE,
            Placeholder.unparsed(MinigamePlaceHolderKey.LOADOUT.getKey(), loadout.getName()));

        return super.onClick();
    }
}
