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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MenuItemAddTeam extends MenuItem implements StringConsumer {
    private final @NotNull TeamsModule tm;

    public MenuItemAddTeam(@NotNull Component name, @NotNull TeamsModule tm) {
        super(MenuUtility.createType(), name);
        this.tm = tm;
    }

    public MenuItemAddTeam(@NotNull MinigameLangKey name, @NotNull TeamsModule tm) {
        super(MenuUtility.createType(), name);
        this.tm = tm;
    }

    @Override
    public @NotNull ItemStack onClick() {
        MinigamePlayer mgPlayer = getContainer().getViewer();
        mgPlayer.setNoClose(true);
        mgPlayer.getPlayer().closeInventory();

        final @NotNull Duration reopenTime = Duration.ofSeconds(30);

        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TEAM_ADD,
            Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(), TeamColor.inputColorNamesComp(TeamColor.validColors())),
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));
        mgPlayer.setManualEntry(this);

        getContainer().startReopenTimer(reopenTime);
        return ItemStack.empty();
    }


    @Override
    public void acceptString(@NotNull String entry) {
        TeamColor col = TeamColor.matchColor(entry.toUpperCase().replace(" ", "_"));
        if (col != null) {
            if (!tm.hasTeam(col)) {
                tm.addTeam(col);
                Team team = tm.getTeam(col);

                getContainer().addItem(new MenuItemTeam(team.getColoredDisplayName(), team));
            } else {
                MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR, MgMiscLangKey.TEAM_ERROR_COLOR_TAKEN);
            }

            List<TeamColor> teams = new ArrayList<>(tm.getTeams().size() + 1);
            for (Team t : tm.getTeams()) {
                teams.add(t.getColor());
            }
            teams.add(TeamColor.NONE);
            getContainer().removeItem(0);
            getContainer().addItem(new MenuItemList<>(ItemType.PAPER, MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_DEFAULTWINNINGTEAM_NAME), tm.getDefaultWinnerCallback(), teams), 0);

            getContainer().cancelReopenTimer();
            getContainer().displayMenu(getContainer().getViewer());
        } else {
            getContainer().cancelReopenTimer();
            getContainer().displayMenu(getContainer().getViewer());

            MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR, MgMiscLangKey.TEAM_ERROR_COLOR_INVALID,
                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), entry));
        }
    }
}
