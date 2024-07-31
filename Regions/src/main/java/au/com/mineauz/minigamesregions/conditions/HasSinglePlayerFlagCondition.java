package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.StringFlag;
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

public class HasSinglePlayerFlagCondition extends ACondition {
    private final StringFlag flagName = new StringFlag("flag", "flag");

    protected HasSinglePlayerFlagCondition(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_HASSINGLEPLAYERFLAG_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_HASSINGLEPLAYERFLAG_FLAG_NAME), Component.text(flagName.getFlag()));
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
    public boolean checkRegionCondition(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        return checkCondition(mgPlayer);
    }

    @Override
    public boolean checkNodeCondition(@Nullable MinigamePlayer mgPlayer, @NotNull Node node) {
        return checkCondition(mgPlayer);
    }

    private boolean checkCondition(@Nullable MinigamePlayer player) {
        if (player == null) {
            return false;
        }
        return player.hasFlag(flagName.getFlag());
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        flagName.saveValue(config, path);
        saveInvert(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        flagName.loadValue(config, path);
        loadInvert(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer player, @NotNull Menu prev) {
        Menu m = new Menu(3, getDisplayName(), player);
        m.addItem(new MenuItemBack(prev), m.getSize() - 9);
        m.addItem(flagName.getMenuItem(Material.NAME_TAG, RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_HASSINGLEPLAYERFLAG_FLAG_NAME)));
        addInvertMenuItem(m);
        m.displayMenu(player);
        return true;
    }

    @Override
    public boolean playerNeeded() {
        return true;
    }
}
