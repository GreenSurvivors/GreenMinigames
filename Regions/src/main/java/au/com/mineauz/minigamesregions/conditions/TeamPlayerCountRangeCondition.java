package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.minigame.modules.team.Team;
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

import java.util.Map;

public class TeamPlayerCountRangeCondition extends ACondition {
    private final IntegerFlag min = new IntegerFlag("min", 1);
    private final IntegerFlag max = new IntegerFlag("max", 5);

    protected TeamPlayerCountRangeCondition(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_TEAMPLAYERCOUNTRANGE_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.TEAM;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_TEAMPLAYERCOUNTRANGE_NAME),
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
        return false;
    }

    @Override
    public boolean checkRegionCondition(@NotNull MinigamePlayer mgPlayer, @NotNull Region region) {
        if (mgPlayer.getTeam() != null) {
            int count = 0;
            Team t = mgPlayer.getTeam();
            for (MinigamePlayer user : region.getPlayers()) {
                if (user.getTeam().equals(t)) {
                    count++;
                }
            }

            return (count >= min.getFlag() && count <= max.getFlag());
        }
        return false;
    }

    @Override
    public boolean checkNodeCondition(MinigamePlayer mgPlayer, @NotNull Node node) {
        return false;
    }

    @Override
    public void saveArguments(@NotNull CommentedConfigurationNode config) throws SerializationException {
        min.saveValue(config);
        max.saveValue(config);
        saveInvertedStatus(config);
    }

    @Override
    public void loadArguments(@NotNull CommentedConfigurationNode config) throws SerializationException {
        min.loadValue(config);
        max.saveValue(config);
        loadInvert(config);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer player, @NotNull Menu prev) {
        Menu m = new Menu(3, getDisplayName(), player);
        m.addItem(new MenuItemBack(prev), m.getSize() - 9);
        m.addItem(min.getMenuItem(ItemType.STONE_SLAB, RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_MIN_NAME), 1, null));
        m.addItem(max.getMenuItem(ItemType.STONE, RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_MAX_NAME), 1, null));
        addInvertMenuItem(m);
        m.displayMenu(player);
        return true;
    }

    @Override
    public boolean playerNeeded() {
        return true;
    }
}
