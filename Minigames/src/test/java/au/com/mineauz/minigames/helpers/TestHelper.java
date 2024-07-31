package au.com.mineauz.minigames.helpers;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.mechanics.GameMechanicBase;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MockSign;
import au.com.mineauz.minigames.objects.SignBlockMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.BlockMock;
import be.seeseemelk.mockbukkit.block.data.BlockDataMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TestHelper {
    public static @NotNull  Minigame createMinigame(@NotNull Minigames plugin, WorldMock world, @NotNull MinigameType type, @NotNull GameMechanicBase mechanic) {
        Location start = new Location(world, 0, 21, 0);
        Minigame game = new Minigame("TestGame", MinigameType.MULTIPLAYER, start);
        game.setType(type);
        game.setMechanic(mechanic);
        game.setDeathDrops(true);
        Location quit = new Location(world, 0, 20, 0);
        game.setQuitLocation(quit);
        Location lobby = new Location(world, 0, 5., 0);
        game.setLobbyLocation(lobby);
        Location end = new Location(world, 0, 25, 0);
        game.setEndLocation(end);
        game.setEnabled(true);
        game.setStartWaitTime(5);
        game.setTimer(5);
        game.setMaxScore(3);
        game.setMaxPlayers(2);
        plugin.getMinigameManager().addMinigame(game);
        return game;
    }

    public static @NotNull BlockMock createSignBlock(@NotNull Map<@NotNull Integer, @NotNull String> lines, @NotNull WorldMock world) {
        MockSign sign = new MockSign(Material.CRIMSON_SIGN, true);
        for (Map.Entry<Integer, String> e : lines.entrySet()) {
            sign.setLine(e.getKey(), e.getValue());
        }

        BlockData bData = new BlockDataMock(Material.CRIMSON_SIGN);
        return new SignBlockMock(Material.CRIMSON_SIGN, new Location(world, 10, 40, 10), sign, bData);
    }
}
