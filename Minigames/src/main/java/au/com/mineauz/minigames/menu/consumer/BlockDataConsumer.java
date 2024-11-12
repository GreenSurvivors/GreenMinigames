package au.com.mineauz.minigames.menu.consumer;

import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

public interface BlockDataConsumer {
    void acceptBlockData(@NotNull BlockData blockData);
}
