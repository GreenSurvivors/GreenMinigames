package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemList;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MatchTeamCondition extends ACondition {
    private final EnumFlag<TeamColor> teamColor = new EnumFlag<>(TeamColor.RED, "team");

    protected MatchTeamCondition(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_MATCHTEAM_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.TEAM;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_TEAM_NAME), teamColor.getFlag().getCompName());
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
    public boolean checkNodeCondition(MinigamePlayer player, @NotNull Node node) {
        return player.getTeam() != null && player.getTeam().getColor() == teamColor.getFlag();
    }

    @Override
    public boolean checkRegionCondition(MinigamePlayer player, @NotNull Region region) {
        if (player == null || !player.isInMinigame()) return false;
        return player.getTeam() != null && player.getTeam().getColor() == teamColor.getFlag();
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        teamColor.saveValue(config, path);
        saveInvert(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        teamColor.loadValue(config, path);
        loadInvert(config, path);
    }

    @Override
    public boolean displayMenu(MinigamePlayer player, Menu prev) {
        Menu m = new Menu(3, getDisplayName(), player);
        m.addItem(new MenuItemBack(prev), m.getSize() - 9);

        List<TeamColor> teams = new ArrayList<>(TeamColor.validColors());

        m.addItem(new MenuItemList<>(getTeamMaterial(), RegionMessageManager.getMessage(RegionLangKey.MENU_TEAM_NAME), new Callback<TeamColor>() { // don't know why but for some reason the compiler doesn't like when I remove the redundant Teamcolor from the callback. Please let it in there for now!
            @Override
            public TeamColor getValue() {
                return teamColor.getFlag();
            }

            @Override
            public void setValue(TeamColor value) {
                teamColor.setFlag(value);
            }
        }, teams) {
            @Override
            public @NotNull ItemStack getDisplayItem() {
                ItemStack stack = super.getDisplayItem();
                stack.setType(getTeamMaterial());
                return stack;
            }
        });

        addInvertMenuItem(m);
        m.displayMenu(player);
        return true;
    }

    private Material getTeamMaterial() {
        return teamColor.getFlagOrDefault().getDisplaMaterial();
    }

    @Override
    public boolean playerNeeded() {
        return true;
    }
}
