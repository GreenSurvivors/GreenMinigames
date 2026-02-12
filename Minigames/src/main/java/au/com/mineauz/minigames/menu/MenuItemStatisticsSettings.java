package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.stats.MinigameStat;
import au.com.mineauz.minigames.stats.MinigameStatistics;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MenuItemStatisticsSettings extends AMenuItem {
    private final @NotNull Minigame minigame;

    public MenuItemStatisticsSettings(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                                      final @NotNull Minigame minigame) {
        super(displayType, langKey);
        this.minigame = minigame;
    }

    public MenuItemStatisticsSettings(final @Nullable ItemType displayType, final @Nullable Component name,
                                      final @NotNull Minigame minigame) {
        super(displayType, name);
        this.minigame = minigame;
    }

    @Override
    public @NotNull ItemStack onClick() {
        final @NotNull Menu subMenu = new Menu(6, MessageManager.getMessage(MgMenuLangKey.MENU_STAT_SETTINGS_NAME),
            getMenu().getIntendedViewer());

        for (final @NotNull MinigameStat stat : MinigameStatistics.getAllStats().values()) {
            subMenu.addItem(new MenuItemModifyStatSetting(MenuDisplayTypes.statistics(), minigame, stat));
        }

        subMenu.setItem(new MenuItemBack(getMenu()), subMenu.getSize() - 9);
        subMenu.displayMenu();

        return super.onClick();
    }
}
