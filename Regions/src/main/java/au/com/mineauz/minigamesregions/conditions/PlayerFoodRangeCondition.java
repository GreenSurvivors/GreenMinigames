package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.IntegerFlag;
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
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public class PlayerFoodRangeCondition extends ACondition {
    private final @NotNull IntegerFlag min = new IntegerFlag("min", 20);
    private final @NotNull IntegerFlag max = new IntegerFlag("max", 20);

    protected PlayerFoodRangeCondition(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return MessageManager.getMessage(RegionLangKey.MENU_CONDITION_PLAYERFOODRANGE_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(MessageManager.getMessage(RegionLangKey.MENU_CONDITION_PLAYERFOODRANGE_NAME),
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
    public boolean checkNodeCondition(final @Nullable MinigamePlayer mgPlayer, final @Nullable Node node) {
        return checkCondition(mgPlayer);
    }

    @Override
    public boolean checkRegionCondition(final @Nullable MinigamePlayer mgPlayer, final @Nullable Region region) {
        return checkCondition(mgPlayer);
    }

    private boolean checkCondition(final @Nullable MinigamePlayer player) {
        return player != null && player.isInMinigame();
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        min.saveValue(config);
        max.saveValue(config);
        saveInvertedStatus(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        min.loadValue(config);
        max.loadValue(config);
        loadInvert(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu prev) {
        final @NotNull Menu menu = new Menu(3, getDisplayName(), prev.getIntendedViewer());
        menu.addItem(min.getMenuItem(ItemType.STONE_SLAB, MessageManager.getMessage(RegionLangKey.MENU_RANGE_MIN_NAME), 0, 20));
        menu.addItem(max.getMenuItem(ItemType.STONE, MessageManager.getMessage(RegionLangKey.MENU_RANGE_MAX_NAME), 0, 20));
        menu.setItem(new MenuItemBack(prev), menu.getSize() - 9);
        addInvertMenuItem(menu);
        menu.displayMenu();
        return true;
    }

    @Override
    public boolean playerNeeded() {
        return true;
    }
}
