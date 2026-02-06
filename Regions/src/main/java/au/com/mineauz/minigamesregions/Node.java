package au.com.mineauz.minigamesregions;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import au.com.mineauz.minigames.script.ScriptReference;
import au.com.mineauz.minigames.script.ScriptValue;
import au.com.mineauz.minigames.script.ScriptWrapper;
import au.com.mineauz.minigamesregions.actions.IAction;
import au.com.mineauz.minigamesregions.actions.RegionActions;
import au.com.mineauz.minigamesregions.conditions.ACondition;
import au.com.mineauz.minigamesregions.triggers.Trigger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Node implements ActionExecutorHolder {
    private final @NotNull String name;
    private final @NotNull Minigame minigame;
    private final @NotNull List<@NotNull ActionExecutor> executors = new ArrayList<>();
    private SafeFullLocation loc;
    private boolean enabled = true;

    public Node(@NotNull String name, @NotNull Minigame minigame, @NotNull SafeFullLocation loc) {
        this.name = name;
        this.minigame = minigame;
        this.loc = loc;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull SafeFullLocation getSafeLocation() {
        return loc;
    }

    public void setLocation(@NotNull SafeFullLocation loc) {
        this.loc = loc;
    }

    @Override
    public int addExecutor(@NotNull Trigger trigger) {
        executors.add(new ActionExecutor(trigger));
        return executors.size();
    }

    @Override
    public int addExecutor(ActionExecutor exec) {
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

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void execute(@NotNull Trigger trigger, @Nullable MinigamePlayer mgPlayer) {
        if (mgPlayer == null || mgPlayer.getMinigame() == null) return;
        if (mgPlayer.getMinigame() != null && mgPlayer.getMinigame().isSpectator(mgPlayer)) return;
        List<ActionExecutor> toExecute = new ArrayList<>();
        for (ActionExecutor exec : executors) {
            if (exec.getTrigger() == trigger) {
                if (checkConditions(exec, mgPlayer) && exec.canBeTriggered(mgPlayer)) {
                    toExecute.add(exec);
                }
            }
        }
        for (ActionExecutor exec : toExecute) {
            execute(exec, mgPlayer);
        }
    }

    @Override
    public boolean checkConditions(@NotNull ActionExecutor exec, @Nullable MinigamePlayer player) {
        for (ACondition con : exec.getConditions()) {
            boolean conditionCheck = con.checkNodeCondition(player, this);
            if (con.isInverted()) {
                conditionCheck = !conditionCheck;
            }
            if (!conditionCheck) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void execute(@NotNull ActionExecutor exec, @NotNull MinigamePlayer mgPlayer) {
        for (IAction act : exec.getActions()) {
            if (!enabled && !act.key().equals(RegionActions.SET_ENABLED.key())) {
                continue;
            }

            act.executeNodeAction(mgPlayer, this);
            if (!exec.isTriggerPerPlayer()) {
                exec.addPublicTrigger();
            } else {
                exec.addPlayerTrigger(mgPlayer);
            }
        }
    }

    @Override
    public @Nullable ScriptReference resolveReference(@NotNull String name) {
        if (name.equalsIgnoreCase("name")) {
            return ScriptValue.of(name);
        } else if (name.equalsIgnoreCase("pos")) {
            return ScriptWrapper.wrap(loc);
        } else if (name.equalsIgnoreCase("block")) {
            return ScriptWrapper.wrap(loc.getBlockAt());
        }

        return null;
    }

    @Override
    public @NotNull Set<@NotNull String> getReferenceKeys() {
        return Set.of("name", "pos", "block");
    }

    @Override
    public @NotNull String getAsString() {
        return name;
    }

    public @NotNull Minigame getMinigame() {
        return minigame;
    }
}
