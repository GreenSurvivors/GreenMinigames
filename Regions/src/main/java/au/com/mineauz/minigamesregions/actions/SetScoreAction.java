package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SetScoreAction extends AScoreAction {
    private final IntegerFlag amount = new IntegerFlag(1, "amount");

    protected SetScoreAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SETSCORE_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.MINIGAME;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(MinigameMessageManager.getMgMessage(MgMiscLangKey.STATISTIC_SCORE_NAME), Component.text(amount.getFlag()));
    }

    @Override
    public boolean useInRegions() {
        return true;
    }

    @Override
    public boolean useInNodes() {
        return true;
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        if (!mgPlayer.isInMinigame()) return;
        mgPlayer.setScore(amount.getFlag());
        mgPlayer.getMinigame().setScore(mgPlayer, mgPlayer.getScore());
        checkScore(mgPlayer);
    }

    @Override
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return;
        mgPlayer.setScore(amount.getFlag());
        mgPlayer.getMinigame().setScore(mgPlayer, mgPlayer.getScore());
        checkScore(mgPlayer);
    }


    @Override
    public void saveArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        amount.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        amount.saveValue(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu previous) {
        Menu menu = new Menu(3, getDisplayname(), mgPlayer);
        menu.addItem(amount.getMenuItem(Material.ENDER_PEARL,
                MinigameMessageManager.getMgMessage(MgMiscLangKey.STATISTIC_SCORE_NAME), null, null));
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.displayMenu(mgPlayer);
        return true;
    }
}
