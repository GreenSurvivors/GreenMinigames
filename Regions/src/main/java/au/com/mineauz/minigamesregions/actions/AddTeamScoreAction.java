package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.Map;

public class AddTeamScoreAction extends AScoreAction { // todo merge with addScoreAction
    private final IntegerFlag score = new IntegerFlag("amount", 1);
    private final EnumFlag<TeamColor> team = new EnumFlag<>("team", TeamColor.NONE);

    protected AddTeamScoreAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SCORE_TEAMADD_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.TEAM;
    }

    @Override
    public @NotNull Map<@NotNull Component, @NotNull Component> describe() {
        return Map.of(MinigameMessageManager.getMgMessage(MgMiscLangKey.STATISTIC_SCORE_NAME), Component.text(score.getFlag()),
                RegionMessageManager.getMessage(RegionLangKey.MENU_TEAM_NAME), team.getFlag().getCompName());
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
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer,
                                    @NotNull Region region) {
        debug(mgPlayer, region);
        executeAction(mgPlayer);
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer,
                                  @NotNull Node node) {
        debug(mgPlayer, node);
        executeAction(mgPlayer);
    }

    private void executeAction(@Nullable MinigamePlayer player) {
        if (player != null && player.isInMinigame()) {
            if (team.getFlag() == TeamColor.NONE) {
                if (player.getTeam() != null) {
                    player.getTeam().addScore(score.getFlag());
                }
            } else {
                TeamsModule tm = TeamsModule.getMinigameModule(player.getMinigame());
                if (tm != null && tm.hasTeam(team.getFlag())) {
                    tm.getTeam(team.getFlag()).addScore(score.getFlag());
                }
            }

            checkScore(player);
        }
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        score.saveValue(config);
        team.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        score.loadValue(config);
        team.loadValue(config);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, @NotNull Menu previous) {
        Menu m = new Menu(3, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SCORE_TEAMADD_NAME), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);
        m.addItem(new MenuItemInteger(ItemType.STONE,
                MinigameMessageManager.getMgMessage(MgMiscLangKey.STATISTIC_SCORE_NAME), new Callback<>() {

            @Override
            public Integer getValue() {
                return score.getFlag();
            }

            @Override
            public void setValue(Integer value) {
                score.setFlag(value);
            }


        }, null, null));

        List<TeamColor> teams = List.of(TeamColor.values());
        m.addItem(new MenuItemList<>(ItemType.PAPER, RegionMessageManager.getMessage(RegionLangKey.MENU_TEAM_NAME),
                RegionMessageManager.getMessageList(RegionLangKey.MENU_TEAM_DESCRIPTION), new Callback<>() {

            @Override
            public TeamColor getValue() {
                return team.getFlag();
            }

            @Override
            public void setValue(TeamColor value) {
                team.setFlag(value);
            }
        }, teams));
        m.displayMenu(mgPlayer);
        return true;
    }
}
