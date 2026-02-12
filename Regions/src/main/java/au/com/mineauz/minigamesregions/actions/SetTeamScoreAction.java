package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemList;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SetTeamScoreAction extends AScoreAction { // todo merge with setScoreAction
    private final @NotNull IntegerFlag score = new IntegerFlag("amount", 1);
    private final @NotNull EnumFlag<TeamColor> team = new EnumFlag<>("team", TeamColor.NONE);

    protected SetTeamScoreAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return MessageManager.getMessage(RegionLangKey.MENU_ACTION_SETTEAMSCORE_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.TEAM;
    }

    @Override
    public @NotNull Map<@NotNull Component, @NotNull Component> describe() {
        return Map.of(MessageManager.getMessage(MgMiscLangKey.STATISTIC_SCORE_NAME), Component.text(score.getFlag()),
                MessageManager.getMessage(RegionLangKey.MENU_TEAM_NAME), team.getFlag().getCompName());
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
    public void executeRegionAction(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        debug(mgPlayer, region);
        executeAction(mgPlayer);
    }

    @Override
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        debug(mgPlayer, node);
        executeAction(mgPlayer);
    }

    private void executeAction(final @Nullable MinigamePlayer player) {
        if (player != null && player.isInMinigame()) {
            if (team.getFlag().equals(TeamColor.NONE)) {
                if (player.getTeam() != null) {
                    player.getTeam().setScore(score.getFlag());
                }
            } else {
                final @Nullable TeamsModule tm = TeamsModule.getMinigameModule(player.getMinigame());
                if (tm != null && tm.hasTeam(team.getFlag())) {
                    tm.getTeam(team.getFlag()).setScore(score.getFlag());
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
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(score.getMenuItem(ItemType.STONE, MessageManager.getMessage(MgMiscLangKey.STATISTIC_SCORE_NAME),
                null, null));

        List<TeamColor> teams = new ArrayList<>(TeamColor.validColors());
        menu.addItem(new MenuItemList<>(ItemType.PAPER, RegionMessageManager.getMessage(RegionLangKey.MENU_TEAM_NAME),
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
        menu.displayMenu();
        return true;
    }
}
