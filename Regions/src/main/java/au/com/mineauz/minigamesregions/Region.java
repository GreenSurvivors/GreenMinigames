package au.com.mineauz.minigamesregions;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MgRegion;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.safelocation.SafeFineLocation;
import au.com.mineauz.minigames.script.ScriptCollection;
import au.com.mineauz.minigames.script.ScriptReference;
import au.com.mineauz.minigames.script.ScriptValue;
import au.com.mineauz.minigames.script.ScriptWrapper;
import au.com.mineauz.minigamesregions.actions.IAction;
import au.com.mineauz.minigamesregions.actions.RegionActions;
import au.com.mineauz.minigamesregions.conditions.ACondition;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.triggers.MgRegTrigger;
import au.com.mineauz.minigamesregions.triggers.Trigger;
import io.papermc.paper.math.FinePosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Region extends MgRegion implements ActionExecutorHolder {
    private static final int GAME_TICK_DELAY = 1;
    private final @NotNull List<@NotNull ActionExecutor> executors = new ArrayList<>();
    private final @NotNull Set<@NotNull MinigamePlayer> players = new LinkedHashSet<>(); // sequenced set to keep track of order the players have entered
    private final @NotNull Minigame minigame; // todo how to acquire this when loaded by Configurate?
    @Setting("tickDelay")
    private long configuredDelay = 20; //todo make ingame configurable
    private transient int gameConfiguredTaskID = -1;
    private transient int gameTickTaskID = -1;
    private boolean enabled = true;

    public Region(@NotNull Minigame minigame, @NotNull String worldName, @NotNull String name, @NotNull FinePosition pos1, @NotNull FinePosition pos2) {
        super(worldName, name, pos1, pos2);

        this.minigame = minigame;
    }

    public Region(@NotNull String name, @NotNull Minigame minigame, @NotNull SafeFineLocation loc1, @NotNull SafeFineLocation loc2) {
        super(name, loc1, loc2);

        this.minigame = minigame;
    }

    public boolean playerInRegion(@NotNull MinigamePlayer mgPlayer) {
        return super.isInRegen(mgPlayer.getLocation());
    }

    public boolean locationInRegion(@NotNull Location loc) {
        return super.isInRegen(loc);
    }

    @Override
    public void updateRegion(@NotNull SafeFineLocation point1, @NotNull SafeFineLocation point2) {
        super.updateRegion(point1, point2);
        super.sortPositions();
    }

    public boolean hasPlayer(@NotNull MinigamePlayer player) {
        return players.contains(player);
    }

    public void addPlayer(@NotNull MinigamePlayer player) {
        players.add(player);
    }

    public void removePlayer(@NotNull MinigamePlayer player) {
        players.remove(player);
    }

    public @NotNull Set<@NotNull MinigamePlayer> getPlayers() {
        return players;
    }

    @Override
    public int addExecutor(@NotNull Trigger trigger) {
        executors.add(new ActionExecutor(trigger));
        return executors.size();
    }

    @Override
    public int addExecutor(final @NotNull ActionExecutor exec) {
        executors.add(exec);
        return executors.size();
    }

    @Override
    public @NotNull List<@NotNull ActionExecutor> getExecutors() {
        return executors;
    }

    @Override
    public void removeExecutor(int id) {
        if (executors.size() <= id) {
            executors.remove(id - 1);
        }
    }

    @Override
    public void removeExecutor(@NotNull ActionExecutor executor) {
        executors.remove(executor);
    }

    public void setConfiguredTickDelay(long delay) {
        removeConfiguredTask();
        configuredDelay = delay;
        gameConfiguredTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(RegionsMain.getPlugin(), () -> {
            List<MinigamePlayer> plys = new ArrayList<>(players);
            for (MinigamePlayer player : plys) {
                execute(MgRegTrigger.TIME_CONFIGURED, player);
            }
        }, 0, delay);
    }

    public long getConfiguredDelay() {
        return configuredDelay;
    }

    public void startConfigTimerTask() {
        if (gameConfiguredTaskID != -1) {
            removeConfiguredTask();
        }

        gameConfiguredTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(RegionsMain.getPlugin(), () -> {
            List<MinigamePlayer> plys = new ArrayList<>(players);
            for (MinigamePlayer player : plys) {
                execute(MgRegTrigger.TIME_CONFIGURED, player);
            }
        }, 0, configuredDelay);
    }

    public void startGameTickTask() {
        if (gameTickTaskID != -1) {
            removeGameTickTask();
        }

        gameTickTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(RegionsMain.getPlugin(),
                this::executeGameTick,
                0, GAME_TICK_DELAY);
    }

    public void removeConfiguredTask() {
        Bukkit.getScheduler().cancelTask(gameConfiguredTaskID);
    }

    public void removeGameTickTask() {
        Bukkit.getScheduler().cancelTask(gameTickTaskID);
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void execute(@NotNull Trigger trigger, @Nullable MinigamePlayer player) {
        if (player != null && player.getMinigame() != null && player.getMinigame().isSpectator(player)) return;
        List<ActionExecutor> toExecute = new ArrayList<>();
        for (ActionExecutor exec : executors) {
            if (exec.getTrigger() == trigger) {
                if (checkConditions(exec, player) && exec.canBeTriggered(player))
                    toExecute.add(exec);
            }
        }
        for (ActionExecutor exec : toExecute) {
            execute(exec, player);
        }
    }

    @Override
    public boolean checkConditions(@NotNull ActionExecutor exec, @Nullable MinigamePlayer player) {
        for (ACondition con : exec.getConditions()) {
            boolean c = con.checkRegionCondition(player, this);
            if (con.isInverted())
                c = !c;
            if (!c) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void execute(@NotNull ActionExecutor exec, @NotNull MinigamePlayer player) {
        for (final @NotNull IAction act : exec.getActions()) {
            if (!enabled && !act.key().equals(RegionActions.SET_ENABLED.key())) {
                continue;
            }

            act.executeRegionAction(player, this);
            if (!exec.isTriggerPerPlayer()) {
                exec.addPublicTrigger();
            } else {
                exec.addPlayerTrigger(player);
            }
        }
    }

    public void executeGameTick() {
        if (players.isEmpty()) {
            return;
        }
        // There is no non-player-specific condition, so we can just execute all executors.
        for (final @NotNull ActionExecutor exec : executors) {
            for (final @NotNull IAction act : exec.getActions()) {
                if (!enabled && !act.key().equals(RegionActions.SET_ENABLED.key())) {
                    continue;
                }
                try {
                    if (checkConditions(exec, null) && exec.getTrigger() == MgRegTrigger.TIME_GAMETICK) {
                        act.executeRegionAction(null, this);
                        exec.addPublicTrigger();
                    }
                } catch (Exception e) {
                    for (MinigamePlayer mgPlayer : players) {
                        MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionLangKey.TRIGGER_TICK_ERROR_CONDITION);
                    }
                }
            }
        }
    }

    @Override
    public @Nullable ScriptReference resolveReference(@NotNull String name) {
        if (name.equalsIgnoreCase("name")) {
            return ScriptValue.of(name);
        } else if (name.equalsIgnoreCase("players")) {
            return ScriptCollection.of(players);
        } else if (name.equalsIgnoreCase("min")) {
            return ScriptWrapper.wrap(this.getFirstPoint());
        } else if (name.equalsIgnoreCase("max")) {
            return ScriptWrapper.wrap(this.getSecondPoint());
        }

        return null;
    }

    @Override
    public @NotNull String getAsString() {
        return getName();
    }

    @Override
    public @NotNull Set<@NotNull String> getReferenceKeys() {
        return Set.of("name", "players", "min", "max");
    }

    @Override
    public @NotNull Minigame getMinigame() {
        return minigame;
    }
}
