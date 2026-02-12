package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MessageManager;
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

public class MenuItemAddTeam extends AMenuItem implements StringConsumer {
    private final @NotNull TeamsModule teamsModule;

    public MenuItemAddTeam(final @NotNull MinigameLangKey name, final @NotNull TeamsModule teamsModule) {
        super(MenuDisplayTypes.createType(), name);
        this.teamsModule = teamsModule;
    }

    public MenuItemAddTeam(final @NotNull Component name, final @NotNull TeamsModule teamsModule) {
        super(MenuDisplayTypes.createType(), name);
        this.teamsModule = teamsModule;
    }

    @Override
    public @NotNull ItemStack onClick() {
        final @NotNull MinigamePlayer mgPlayer = getMenu().getIntendedViewer();

        final @NotNull Duration reopenTime = Duration.ofSeconds(30);

        MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TEAM_ADD,
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
                MessageManager.sendMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMiscLangKey.TEAM_ERROR_COLOR_TAKEN);
            }

            List<TeamColor> teams = new ArrayList<>(teamsModule.getTeams().size() + 1);
            for (Team t : teamsModule.getTeams()) {
                teams.add(t.getColor());
            }
            teams.add(TeamColor.NONE);
            getMenu().removeItem(0);
            getMenu().setItem(new MenuItemList<>(ItemType.PAPER, MessageManager.getMessage(MgMenuLangKey.MENU_DEFAULTWINNINGTEAM_NAME), teamsModule.getDefaultWinnerCallback(), teams), 0);

            getMenu().cancelWaitForInput();
            getMenu().displayMenu();
        } else {
            getMenu().cancelWaitForInput();
            getMenu().displayMenu();

            MessageManager.sendMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMiscLangKey.TEAM_ERROR_COLOR_INVALID,
                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), entry));
        }
    }
}
