package au.com.mineauz.minigames.gametypes;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class MinigameTypeBase implements Listener {
    private static Minigames plugin;
    private MinigameType type;

    protected MinigameTypeBase() {
        plugin = Minigames.getPlugin();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public MinigameType getType() {
        return type;
    }

    public void setType(MinigameType type) {
        this.type = type;
    }

    public abstract boolean cannotStart(Minigame mgm, MinigamePlayer player);

    public abstract boolean teleportOnJoin(MinigamePlayer mgPlayer, Minigame mgm);

    /**
     * This should actually join the Player to the game Type
     *
     * @param player the player
     * @param mgm    the Game
     * @return True if the player joins
     */
    public abstract boolean joinMinigame(@NotNull MinigamePlayer player, @NotNull Minigame mgm);

    public abstract void quitMinigame(@NotNull MinigamePlayer player, Minigame mgm, boolean forced);

    public abstract void endMinigame(@NotNull List<@NotNull MinigamePlayer> winners,
                                     @NotNull List<@NotNull MinigamePlayer> losers, @NotNull Minigame mgm);

    public void callGeneralQuit(@NotNull MinigamePlayer player, @NotNull Minigame minigame) {
        Location location;
        if (minigame.getQuitLocation() == null) {
            location = minigame.getEndLocation();
        } else {
            location = minigame.getQuitLocation();
        }

        if (!player.getPlayer().isDead()) {
            if (location != null) {
                if (player.getPlayer().getWorld() != location.getWorld() && player.getPlayer().hasPermission("minigame.set.quit") &&
                        plugin.getConfig().getBoolean("warnings")) {
                    MinigameMessageManager.sendMgMessage(player, MinigameMessageType.WARNING, MgMiscLangKey.MINIGAME_WARNING_TELEPORT_ACROSS_WORLDS);
                }

                player.teleport(location);
            }
        } else {
            if (location != null) {
                player.setQuitPos(minigame.getQuitLocation());
            }

            player.setRequiredQuit(true);
        }
    }
}
