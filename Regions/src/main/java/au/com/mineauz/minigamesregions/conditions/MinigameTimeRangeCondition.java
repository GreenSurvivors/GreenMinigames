package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.MinigameTimer;
import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.time.Duration;
import java.util.Map;

public class MinigameTimeRangeCondition extends ACondition {
    private final TimeFlag minTime = new TimeFlag("minTime", 5L);
    private final TimeFlag maxTime = new TimeFlag("maxTime", 10L);

    protected MinigameTimeRangeCondition(final @NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_MINIGAMETIMERANGE_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.MINIGAME;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_MINIGAMETIMERANGE_NAME),
                RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_FORMAT,
                        Placeholder.component(MinigamePlaceHolderKey.MIN.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(minTime.getFlag()), true)),
                        Placeholder.component(MinigamePlaceHolderKey.MAX.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(maxTime.getFlag()), true))));
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
    public boolean checkRegionCondition(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        return check(region.getMinigame());
    }

    @Override
    public boolean checkNodeCondition(final @Nullable MinigamePlayer mgPlayer, final @NotNull Node node) {
        return check(node.getMinigame());
    }

    private boolean check(@NotNull Minigame mg) {
        MinigameTimer timer = mg.getMinigameTimer();

        if (timer == null) {
            return false;
        } else {
            long timeLeft = timer.getTimeLeft();
            long min = minTime.getFlag();
            long max = maxTime.getFlag();
            debug(mg);
            return timeLeft >= min && timeLeft <= max;
        }
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        minTime.saveValue(config);
        maxTime.saveValue(config);
        saveInvertedStatus(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        minTime.loadValue(config);
        maxTime.loadValue(config);
        loadInvert(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu prev) {
        final @NotNull Menu menu = new Menu(3, getDisplayName(), prev.getIntendedViewer());

        menu.addItem(minTime.getMenuItem(ItemType.CLOCK, RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_MIN_NAME), 0L, null));
        menu.addItem(maxTime.getMenuItem(ItemType.CLOCK, RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_MAX_NAME), 0L, null));

        menu.setItem(new MenuItemBack(prev), menu.getSize() - 9);
        addInvertMenuItem(menu);
        menu.displayMenu();
        return true;
    }

    @Override
    public boolean playerNeeded() {
        return false;
    }
}
