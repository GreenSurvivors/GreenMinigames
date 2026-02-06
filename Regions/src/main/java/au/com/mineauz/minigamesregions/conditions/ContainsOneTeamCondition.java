package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public class ContainsOneTeamCondition extends ACondition {

    protected ContainsOneTeamCondition(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_CONTAINSONETEAM_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.TEAM;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of();
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
    public boolean checkNodeCondition(@Nullable MinigamePlayer mgPlayer, @NotNull Node node) {
        return false;
    }

    @Override
    public boolean checkRegionCondition(@NotNull MinigamePlayer mgPlayer, @NotNull Region region) {
        boolean ret = true;
        Team last = mgPlayer.getTeam();
        if (last == null) return true;
        for (MinigamePlayer p : region.getPlayers()) {
            if (last != p.getTeam()) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    @Override
    public void saveArguments(@NotNull CommentedConfigurationNode config) throws SerializationException {
        saveInvertedStatus(config);
    }

    @Override
    public void loadArguments(@NotNull CommentedConfigurationNode config) {
        loadInvert(config);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer player, @NotNull Menu prev) {
        Menu menu = new Menu(3, getDisplayName(), player);
        menu.addItem(new MenuItemBack(prev), menu.getSize() - 9);
        addInvertMenuItem(menu);
        menu.displayMenu(player);
        return true;
    }

    @Override
    public boolean playerNeeded() {
        return false;
    }
}
