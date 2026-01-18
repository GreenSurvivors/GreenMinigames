package au.com.mineauz.minigamesregions;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.triggers.Trigger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ActionExecutorHolder extends ExecutableScriptObject {
    int addExecutor(@NotNull Trigger trigger);

    int addExecutor(ActionExecutor exec);

    List<ActionExecutor> getExecutors();

    void removeExecutor(int id);

    void removeExecutor(@NotNull ActionExecutor executor);

    void setEnabled(boolean enabled);

    boolean getEnabled();

    @NotNull Minigame getMinigame();

    boolean checkConditions(@NotNull ActionExecutor exec, MinigamePlayer player);

    void execute(@NotNull ActionExecutor exec, @NotNull MinigamePlayer mgPlayer);
}
