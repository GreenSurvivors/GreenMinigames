package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.FloatFlag;
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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PlayerXPRangeCondition extends ACondition {
    private final FloatFlag min = new FloatFlag("min", 1.0f);
    private final FloatFlag max = new FloatFlag("max", 1.0f);
    private final EnumFlag<XPCheckType> checkType = new EnumFlag<>("checkLevel", XPCheckType.LEVEL);

    protected PlayerXPRangeCondition(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_PLAYERXPRANGE_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_PLAYERXPRANGE_NAME),
                RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_FORMAT,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MIN.getKey(), String.valueOf(min.getFlag())),
                        Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), String.valueOf(max.getFlag()))));
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
        return checkCondition(mgPlayer);
    }

    @Override
    public boolean checkRegionCondition(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        return checkCondition(mgPlayer);
    }

    private boolean checkCondition(final @Nullable MinigamePlayer mgPlayer) {
        if (mgPlayer != null && mgPlayer.isInMinigame()) {
            switch (checkType.getFlag()) {
                case TOTAL_XP -> {
                    final float xp = mgPlayer.getPlayer().calculateTotalExperiencePoints();

                    return xp >= min.getFlag() && xp <= max.getFlag();
                }
                case LEVEL -> {
                    final Player player = mgPlayer.getPlayer();
                    float xpLevel = player.getLevel() + player.getExp();

                    return xpLevel >= min.getFlag() && xpLevel <= max.getFlag();
                }
            }
        }

        return false;
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        min.saveValue(config, path);
        max.saveValue(config, path);
        checkType.saveValue(config, path);
        saveInvert(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        min.loadValue(config, path);
        max.loadValue(config, path);
        checkType.loadValue(config, path);
        loadInvert(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer player, @NotNull Menu prev) {
        final Menu menu = new Menu(3, getDisplayName(), player);
        menu.addItem(min.getMenuItem(Material.STONE_SLAB, RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_MIN_NAME)
                , 0.5, 1, 0.0, null));
        menu.addItem(max.getMenuItem(Material.STONE, RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_MAX_NAME)
                , 0.5, 1, 0.0, null));
        menu.addItem(checkType.getMenuItem(Material.EXPERIENCE_BOTTLE, RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_PLAYERXPRANGE_CHECK_LEVEL)));
        menu.addItem(new MenuItemBack(prev), menu.getSize() - 9);
        addInvertMenuItem(menu);
        menu.displayMenu(player);
        return true;
    }

    @Override
    public boolean playerNeeded() {
        return true;
    }

    public enum XPCheckType {
        TOTAL_XP,
        LEVEL
    }
}
