package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PlayerHealthRangeCondition extends ACondition {
    private final IntegerFlag minHealth = new IntegerFlag(20, "min");
    private final IntegerFlag maxHealth = new IntegerFlag(20, "max");

    protected PlayerHealthRangeCondition(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_PLAYERHEALTHRANGE_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_PLAYERHEALTHRANGE_NAME),
                RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_FORMAT,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MIN.getKey(), String.valueOf(minHealth.getFlag())),
                        Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), String.valueOf(maxHealth.getFlag()))));
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
    public boolean checkNodeCondition(@Nullable MinigamePlayer mgPlayer, @NotNull Node node) {
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return false;
        return mgPlayer.getPlayer().getHealth() >= minHealth.getFlag().doubleValue() &&
                mgPlayer.getPlayer().getHealth() <= maxHealth.getFlag().doubleValue();
    }

    @Override
    public boolean checkRegionCondition(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return false;
        return mgPlayer.getPlayer().getHealth() >= minHealth.getFlag().doubleValue() &&
                mgPlayer.getPlayer().getHealth() <= maxHealth.getFlag().doubleValue();
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        minHealth.saveValue(config, path);
        maxHealth.saveValue(config, path);
        saveInvert(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        minHealth.loadValue(config, path);
        maxHealth.loadValue(config, path);
        loadInvert(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer player, @NotNull Menu prev) {
        Menu m = new Menu(3, getDisplayName(), player);
        m.addItem(minHealth.getMenuItem(Material.STONE_SLAB, RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_MIN_NAME), 0, 20));
        m.addItem(maxHealth.getMenuItem(Material.STONE, RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_MAX_NAME), 0, 20));
        m.addItem(new MenuItemBack(prev), m.getSize() - 9);
        addInvertMenuItem(m);
        m.displayMenu(player);
        return true;
    }

    @Override
    public boolean playerNeeded() {
        return true;
    }
}
