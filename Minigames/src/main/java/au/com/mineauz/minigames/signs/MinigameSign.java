package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MinigameSign {

    @NotNull String getName();

    @Nullable String getCreatePermission();

    @NotNull String getCreatePermissionMessage();

    @Nullable String getUsePermission();

    @Nullable String getUsePermissionMessage();

    boolean signCreate(@NotNull SignChangeEvent event);

    boolean signUse(@NotNull Sign sign, @NotNull MinigamePlayer player);

    void signBreak(@NotNull Sign sign, @Nullable MinigamePlayer player);
}
