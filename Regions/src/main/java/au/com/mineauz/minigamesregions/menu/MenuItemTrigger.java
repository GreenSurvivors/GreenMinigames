package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.triggers.Trigger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class MenuItemTrigger extends MenuItem {
    private final @NotNull Trigger trigger;
    private final @NotNull Menu previous;
    private @Nullable Region region;
    private @Nullable Node node;

    public MenuItemTrigger(@NotNull Trigger trigger, @NotNull Region region, @NotNull Menu previous) {
        super(ItemType.LEVER, trigger.getDisplayName());
        this.trigger = trigger;
        this.region = region;
        this.previous = previous;
    }

    public MenuItemTrigger(@NotNull Trigger trigger, @NotNull Node node, @NotNull Menu previous) {
        super(ItemType.LEVER, trigger.getDisplayName());
        this.trigger = trigger;
        this.node = node;
        this.previous = previous;
    }

    @Override
    public @NonNull ItemStack onClick() {
        final @NotNull ActionExecutor exec = new ActionExecutor(trigger);
        if (region != null) {
            region.addExecutor(exec);
            previous.addItem(new MenuItemRegionExecutor(region, exec));
        } else {
            node.addExecutor(exec);
            previous.addItem(new MenuItemNodeExecutor(node, exec));
        }
        previous.displayMenu(getContainer().getViewer());
        return ItemStack.empty();
    }
}
