package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.tool.MinigameTool;
import au.com.mineauz.minigames.tool.ToolMode;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemToolMode extends AMenuItem {
    private final @NotNull ToolMode mode;

    public MenuItemToolMode(final @Nullable ItemType displayType, final @Nullable Component name,
                            final @NotNull ToolMode mode) {
        this(displayType, name, null, mode);
    }

    public MenuItemToolMode(final @Nullable ItemType displayType, final @Nullable Component name,
                            final @Nullable List<@NotNull Component> description,
                            final @NotNull ToolMode mode) {
        super(displayType, name, description);
        this.mode = mode;
    }

    public @NotNull ItemStack onClick() {
        final @NotNull MinigamePlayer mgPlayer = getMenu().getIntendedViewer();
        final @NotNull MinigameTool tool = MinigameTool.getMinigameTool(mgPlayer);
        if (tool != null) {
            if (tool.getMode() != null) {
                tool.getMode().onUnsetMode(mgPlayer, tool);
            }
            tool.setMode(mode);
            tool.getMode().onSetMode(mgPlayer, tool);
        }
        return getDisplayItem();
    }
}
