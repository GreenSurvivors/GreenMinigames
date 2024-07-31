package au.com.mineauz.minigamesregions.executors;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.actions.ActionInterface;
import au.com.mineauz.minigamesregions.conditions.ACondition;
import au.com.mineauz.minigamesregions.triggers.Trigger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseExecutor {
    private final @NotNull Trigger trigger;
    private final @NotNull List<@NotNull ACondition> conditions = new ArrayList<>();
    private final @NotNull List<@NotNull ActionInterface> actions = new ArrayList<>();
    private final @NotNull Map<@NotNull String, @NotNull Integer> triggers = new HashMap<>();
    private boolean triggerPerPlayer = false;
    private int triggerCount = 0;

    public BaseExecutor(@NotNull Trigger trigger) {
        this.trigger = trigger;
    }

    public @NotNull Trigger getTrigger() {
        return trigger;
    }

    public @NotNull List<@NotNull ACondition> getConditions() {
        return conditions;
    }

    public void addCondition(@NotNull ACondition condition) {
        conditions.add(condition);
    }

    public void removeCondition(ACondition condition) {
        conditions.remove(condition);
    }

    public @NotNull List<@NotNull ActionInterface> getActions() {
        return actions;
    }

    public void addAction(@NotNull ActionInterface action) {
        actions.add(action);
    }

    public void removeAction(ActionInterface action) {
        actions.remove(action);
    }

    public int getTriggerCount() {
        return triggerCount;
    }

    public void setTriggerCount(int count) {
        triggerCount = count;
    }

    public @NotNull Callback<@NotNull Integer> getTriggerCountCallback() {
        return new Callback<>() {

            @Override
            public @NotNull Integer getValue() {
                return getTriggerCount();
            }

            @Override
            public void setValue(@NotNull Integer value) {
                setTriggerCount(value);
            }
        };
    }

    public boolean isTriggerPerPlayer() {
        return triggerPerPlayer;
    }

    public void setTriggerPerPlayer(boolean perPlayer) {
        triggerPerPlayer = perPlayer;
    }

    public @NotNull Callback<Boolean> getIsTriggerPerPlayerCallback() {
        return new Callback<>() {

            @Override
            public @NotNull Boolean getValue() {
                return isTriggerPerPlayer();
            }

            @Override
            public void setValue(@NotNull Boolean value) {
                setTriggerPerPlayer(value);
            }
        };
    }

    public void addPublicTrigger() {
        if (!triggers.containsKey("public"))
            triggers.put("public", 0);
        triggers.put("public", triggers.get("public") + 1);
    }

    public void addPlayerTrigger(@NotNull MinigamePlayer player) {
        String uuid = player.getUUID().toString();
        if (!triggers.containsKey(uuid))
            triggers.put(uuid, 0);
        triggers.put(uuid, triggers.get(uuid) + 1);
    }

    public boolean canBeTriggered(@NotNull MinigamePlayer player) {
        if (triggerCount != 0) {
            if (!triggerPerPlayer) {
                return triggers.get("public") == null ||
                        triggers.get("public") < triggerCount;
            } else {
                return triggers.get(player.getUUID().toString()) == null ||
                        triggers.get(player.getUUID().toString()) < triggerCount;
            }
        }
        return true;
    }

    public void clearTriggers() {
        triggers.clear();
    }

    public void removeTrigger(@NotNull MinigamePlayer player) {
        triggers.remove(player.getUUID().toString());
    }
}
