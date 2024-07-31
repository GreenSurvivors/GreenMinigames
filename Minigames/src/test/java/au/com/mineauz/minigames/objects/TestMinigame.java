package au.com.mineauz.minigames.objects;

import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.MinigameManager;
import au.com.mineauz.minigames.mechanics.GameMechanics;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.minigame.modules.TeamsModule;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 24/12/2018.
 */
public class TestMinigame extends Minigame {
    public TestMinigame(@NotNull String name, @NotNull MinigameType type, @NotNull Location start, World world, @NotNull MinigameManager manager, @Nullable Location quit, @NotNull Location end, @Nullable Location lobby) {
        super(name, type, start);
        setType(MinigameType.MULTIPLAYER);
        setMechanic(GameMechanics.MgMechanics.CTF.getMechanic());
        setDeathDrops(true);
        setQuitLocation(quit);
        setLobbyLocation(lobby);
        setEndLocation(end);
        setEnabled(true);
        setStartWaitTime(5);
        setTimer(5);
        setMaxScore(3);
        setMaxPlayers(2);
        TeamsModule.getMinigameModule(this).addTeam(TeamColor.BLUE);
        TeamsModule.getMinigameModule(this).addTeam(TeamColor.RED);
        manager.addMinigame(this);
    }
}
