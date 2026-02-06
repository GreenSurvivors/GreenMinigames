package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemList;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MatchTeamCondition extends ACondition {
    private final EnumFlag<TeamColor> teamColor = new EnumFlag<>("team", TeamColor.RED);

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
    public boolean checkNodeCondition(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        return mgPlayer.getTeam() != null && mgPlayer.getTeam().getColor() == teamColor.getFlag();
    }

    @Override
    public boolean checkRegionCondition(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return false;
        return mgPlayer.getTeam() != null && mgPlayer.getTeam().getColor() == teamColor.getFlag();
    }

    @Override
    public void saveArguments(@NotNull CommentedConfigurationNode config) throws SerializationException {
        teamColor.saveValue(config);
        saveInvertedStatus(config);
    }

    @Override
    public void loadArguments(@NotNull CommentedConfigurationNode config) {
        teamColor.loadValue(config);
        loadInvert(config);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer player, @NotNull Menu prev) {
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
                return stack.withType(getTeamMaterial().asMaterial());
            }
        });

        addInvertMenuItem(m);
        m.displayMenu(player);
        return true;
    }

    private @NotNull ItemType getTeamMaterial() {
        return teamColor.getFlagOrDefault().getDisplayType();
    }

    @Override
    public boolean playerNeeded() {
        return true;
    }
}
