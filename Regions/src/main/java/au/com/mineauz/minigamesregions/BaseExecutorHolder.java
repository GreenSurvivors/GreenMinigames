package au.com.mineauz.minigamesregions;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.executors.BaseExecutor;
import au.com.mineauz.minigamesregions.triggers.Trigger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BaseExecutorHolder<T extends BaseExecutor> extends ExecutableScriptObject {
    int addExecutor(@NotNull Trigger trigger);

    int addExecutor(T exec);

    List<T> getExecutors();

    void removeExecutor(int id);

    void removeExecutor(@NotNull T executor);

    void setEnabled(boolean enabled);

    boolean getEnabled();

    @NotNull Minigame getMinigame();

    boolean checkConditions(@NotNull T exec, MinigamePlayer player);

    void execute(@NotNull T exec, @NotNull MinigamePlayer mgPlayer);
}
