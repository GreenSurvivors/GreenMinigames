package au.com.mineauz.minigames.menu.consumer;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface EntityConsumer {
    void acceptEntity(@NotNull Entity entity);
}
