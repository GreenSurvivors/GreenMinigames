package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MenuItemAddTeam extends MenuItem implements StringConsumer {
    private final @NotNull TeamsModule teamsModule;

    public MenuItemAddTeam(@NotNull Component name, @NotNull TeamsModule teamsModule) {
        super(MenuUtility.createType(), name);
        this.teamsModule = teamsModule;
    }

    public MenuItemAddTeam(@NotNull MinigameLangKey name, @NotNull TeamsModule teamsModule) {
        super(MenuUtility.createType(), name);
        this.teamsModule = teamsModule;
    }

    @Override
    public @NotNull ItemStack onClick() {
        MinigamePlayer mgPlayer = getMenu().getIntendedViewer();

        final @NotNull Duration reopenTime = Duration.ofSeconds(30);

        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TEAM_ADD,
            Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(), TeamColor.inputColorNamesComp(TeamColor.validColors())),
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));

        getMenu().closeAndWaitForInput(reopenTime, this);
        return ItemStack.empty();
    }


    @Override
    public void acceptString(final @NotNull String entry) {
        final @Nullable TeamColor col = TeamColor.matchColor(entry.toUpperCase().replace(" ", "_"));
        if (col != null) {
            if (!teamsModule.hasTeam(col)) {
                teamsModule.addTeam(col);
                Team team = teamsModule.getTeam(col);

                getMenu().addItem(new MenuItemTeam(team.getColoredDisplayName(), team));
            } else {
                MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMiscLangKey.TEAM_ERROR_COLOR_TAKEN);
            }

            List<TeamColor> teams = new ArrayList<>(teamsModule.getTeams().size() + 1);
            for (Team t : teamsModule.getTeams()) {
                teams.add(t.getColor());
            }
            teams.add(TeamColor.NONE);
            getMenu().removeItem(0);
            getMenu().addItem(new MenuItemList<>(ItemType.PAPER, MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_DEFAULTWINNINGTEAM_NAME), teamsModule.getDefaultWinnerCallback(), teams), 0);

            getMenu().cancelWaitForInput();
            getMenu().displayMenu();
        } else {
            getMenu().cancelWaitForInput();
            getMenu().displayMenu();

            MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMiscLangKey.TEAM_ERROR_COLOR_INVALID,
                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), entry));
        }
    }
}
