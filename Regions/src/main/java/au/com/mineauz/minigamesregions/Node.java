package au.com.mineauz.minigamesregions;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.script.ScriptReference;
import au.com.mineauz.minigames.script.ScriptValue;
import au.com.mineauz.minigames.script.ScriptWrapper;
import au.com.mineauz.minigamesregions.actions.ActionInterface;
import au.com.mineauz.minigamesregions.conditions.ACondition;
import au.com.mineauz.minigamesregions.executors.NodeExecutor;
import au.com.mineauz.minigamesregions.triggers.Trigger;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Node implements BaseExecutorHolder<NodeExecutor> {
    private final @NotNull String name;
    private final @NotNull Minigame minigame;
    private final List<NodeExecutor> executors = new ArrayList<>();
    private Location loc;
    private boolean enabled = true;

    public Node(@NotNull String name, @NotNull Minigame minigame, @NotNull Location loc) {
        this.name = name;
        this.minigame = minigame;
        this.loc = loc;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull Location getLocation() {
        return loc.clone();
    }

    public void setLocation(@NotNull Location loc) {
        this.loc = loc.clone();
    }

    @Override
    public int addExecutor(@NotNull Trigger trigger) {
        executors.add(new NodeExecutor(trigger));
        return executors.size();
    }

    @Override
    public int addExecutor(NodeExecutor exec) {
        executors.add(exec);
        return executors.size();
    }

    @Override
    public @NotNull List<@NotNull NodeExecutor> getExecutors() {
        return executors;
    }

    @Override
    public void removeExecutor(int id) {
        if (executors.size() <= id) {
            executors.remove(id - 1);
        }
    }

    @Override
    public void removeExecutor(@NotNull NodeExecutor executor) {
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
        List<NodeExecutor> toExecute = new ArrayList<>();
        for (NodeExecutor exec : executors) {
            if (exec.getTrigger() == trigger) {
                if (checkConditions(exec, mgPlayer) && exec.canBeTriggered(mgPlayer)) {
                    toExecute.add(exec);
                }
            }
        }
        for (NodeExecutor exec : toExecute) {
            execute(exec, mgPlayer);
        }
    }

    @Override
    public boolean checkConditions(@NotNull NodeExecutor exec, @Nullable MinigamePlayer player) {
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
    public void execute(@NotNull NodeExecutor exec, @NotNull MinigamePlayer mgPlayer) {
        for (ActionInterface act : exec.getActions()) {
            if (!enabled && !act.getName().equalsIgnoreCase("SET_ENABLED")) continue;
            act.executeNodeAction(mgPlayer, this);
            if (!exec.isTriggerPerPlayer()) {
                exec.addPublicTrigger();
            } else {
                exec.addPlayerTrigger(mgPlayer);
            }
        }
    }

    @Override
    public @Nullable ScriptReference get(@NotNull String name) {
        if (name.equalsIgnoreCase("name")) {
            return ScriptValue.of(name);
        } else if (name.equalsIgnoreCase("pos")) {
            return ScriptWrapper.wrap(loc);
        } else if (name.equalsIgnoreCase("block")) {
            return ScriptWrapper.wrap(loc.getBlock());
        }

        return null;
    }

    @Override
    public @NotNull Set<@NotNull String> getKeys() {
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
