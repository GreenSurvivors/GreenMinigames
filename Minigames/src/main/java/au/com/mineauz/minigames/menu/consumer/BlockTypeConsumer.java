package au.com.mineauz.minigames.menu.consumer;

import org.bukkit.block.BlockType;
import org.jetbrains.annotations.NotNull;

public interface BlockTypeConsumer {
    void acceptBlockType(@NotNull BlockType blockType);
}
