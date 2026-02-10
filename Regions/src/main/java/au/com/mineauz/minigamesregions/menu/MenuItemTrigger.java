package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.ActionExecutorHolder;
import au.com.mineauz.minigamesregions.triggers.Trigger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class MenuItemTrigger extends AMenuItem {
    private final @NotNull Trigger trigger;
    private final @NotNull Menu previous;
    private final @NotNull ActionExecutorHolder actionExecutorHolder;

    public MenuItemTrigger(final @NotNull Trigger trigger,
                           final @NotNull ActionExecutorHolder actionExecutorHolder,
                           final @NotNull Menu previous) {
        super(ItemType.LEVER, trigger.getDisplayName());
        this.trigger = trigger;
        this.actionExecutorHolder = actionExecutorHolder;
        this.previous = previous;
    }

    @Override
    public @NonNull ItemStack onClick() {
        final @NotNull ActionExecutor exec = new ActionExecutor(trigger);

        actionExecutorHolder.addExecutor(exec);
        previous.addItem(new MenuItemActionExecutor(actionExecutorHolder, exec));

        previous.displayMenu();
        return ItemStack.empty();
    }
}
