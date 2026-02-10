package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.stats.MinigameStat;
import au.com.mineauz.minigames.stats.MinigameStatistics;
import au.com.mineauz.minigames.stats.StatFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class MenuItemModifyStatSetting extends AMenuItem {
    private final @NotNull Minigame minigame;
    private final @NotNull MinigameStat stat;

    public MenuItemModifyStatSetting(final @Nullable ItemType displayType, final @NotNull Minigame minigame,
                                     final @NotNull MinigameStat stat) {
        super(displayType, stat.getDisplayName());

        this.minigame = minigame;
        this.stat = stat;
    }

    @Override
    public @NotNull ItemStack onClick() {
        final @NotNull Menu subMenu = new Menu(6, MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_STAT_EDIT_NAME,
            Placeholder.component(MinigamePlaceHolderKey.STAT.getKey(), stat.getDisplayName())), getMenu().getIntendedViewer());

        subMenu.addItem(new MenuItemComponent(MenuDisplayTypes.nameType(),
            MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_DISPLAYNAME_NAME), new Callback<>() {
            @Override
            public Component getValue() {
                return minigame.getSettings(stat).getDisplayName();
            }

            @Override
            public void setValue(Component value) {
                minigame.getSettings(stat).setDisplayName(value);
            }
        }));

        if (stat != MinigameStatistics.Losses) {
            subMenu.addItem(new MenuItemList<>(ItemType.ENDER_CHEST, MgMenuLangKey.MENU_STAT_STORAGEFORMAT, new Callback<>() {
                @Override
                public @NotNull StatFormat getValue() {
                    return minigame.getSettings(stat).getFormat();
                }

                @Override
                public void setValue(StatFormat value) {
                    minigame.getSettings(stat).setFormat(value);
                }
            }, Arrays.asList(StatFormat.values())));
        }

        subMenu.setItem(new MenuItemBack(getMenu()), subMenu.getSize() - 9);
        subMenu.displayMenu();

        return super.onClick();
    }
}
