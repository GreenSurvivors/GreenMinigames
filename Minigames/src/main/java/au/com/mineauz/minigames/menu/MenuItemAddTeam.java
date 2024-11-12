package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.minigame.modules.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MenuItemAddTeam extends MenuItem implements StringConsumer {
    private final @NotNull TeamsModule tm;

    public MenuItemAddTeam(@NotNull Component name, @NotNull TeamsModule tm) {
        super(MenuUtility.getCreateMaterial(), name);
        this.tm = tm;
    }

    public MenuItemAddTeam(@NotNull MinigameLangKey name, @NotNull TeamsModule tm) {
        super(MenuUtility.getCreateMaterial(), name);
        this.tm = tm;
    }

    @Override
    public @Nullable ItemStack onClick() {
        MinigamePlayer mgPlayer = getContainer().getViewer();
        mgPlayer.setNoClose(true);
        mgPlayer.getPlayer().closeInventory();

        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TEAM_ADD,
                Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(), TeamColor.validColorNamesComp()));
        mgPlayer.setManualEntry(this);

        getContainer().startReopenTimer(30);
        return null;
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
            getContainer().addItem(new MenuItemList<>(Material.PAPER, MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_DEFAULTWINNINGTEAM_NAME), tm.getDefaultWinnerCallback(), teams), 0);

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
