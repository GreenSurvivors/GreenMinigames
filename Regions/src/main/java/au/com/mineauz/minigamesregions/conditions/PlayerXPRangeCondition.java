package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.FloatFlag;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public class PlayerXPRangeCondition extends ACondition {
    private final @NotNull FloatFlag min = new FloatFlag("min", 1.0f);
    private final @NotNull FloatFlag max = new FloatFlag("max", 1.0f);
    private final @NotNull EnumFlag<@NotNull XPCheckType> checkType = new EnumFlag<>("checkLevel", XPCheckType.LEVEL);

    protected PlayerXPRangeCondition(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return MessageManager.getMessage(RegionLangKey.MENU_CONDITION_PLAYERXPRANGE_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(MessageManager.getMessage(RegionLangKey.MENU_CONDITION_PLAYERXPRANGE_NAME),
                MessageManager.getMessage(RegionLangKey.MENU_RANGE_FORMAT,
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
    public boolean checkNodeCondition(final @Nullable MinigamePlayer mgPlayer, final @NotNull Node node) {
        return checkCondition(mgPlayer);
    }

    @Override
    public boolean checkRegionCondition(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
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
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        min.saveValue(config);
        max.saveValue(config);
        checkType.saveValue(config);
        saveInvertedStatus(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        min.loadValue(config);
        max.loadValue(config);
        checkType.loadValue(config);
        loadInvert(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu prev) {
        final @NotNull Menu menu = new Menu(3, getDisplayName(), prev.getIntendedViewer());
        menu.addItem(min.getMenuItem(ItemType.STONE_SLAB, MessageManager.getMessage(RegionLangKey.MENU_RANGE_MIN_NAME),
            0.5, 1, 0.0, null));
        menu.addItem(max.getMenuItem(ItemType.STONE, MessageManager.getMessage(RegionLangKey.MENU_RANGE_MAX_NAME),
            0.5, 1, 0.0, null));
        menu.addItem(checkType.getMenuItem(ItemType.EXPERIENCE_BOTTLE, MessageManager.getMessage(RegionLangKey.MENU_CONDITION_PLAYERXPRANGE_CHECK_LEVEL)));
        menu.setItem(new MenuItemBack(prev), menu.getSize() - 9);
        addInvertMenuItem(menu);
        menu.displayMenu();
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
