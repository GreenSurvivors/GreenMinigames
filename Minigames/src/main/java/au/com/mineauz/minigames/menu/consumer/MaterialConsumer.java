package au.com.mineauz.minigames.menu.consumer;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public interface MaterialConsumer {
    void acceptMaterial(@NotNull Material material);
}
