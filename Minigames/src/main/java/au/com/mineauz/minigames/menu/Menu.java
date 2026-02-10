package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.time.Duration;
import java.util.*;

public class Menu {
    private final int rows;
    protected final @NotNull Minigames plugin = Minigames.getPlugin();
    private final @Nullable ItemStack @NotNull [] pageView;
    private final @NotNull TreeMap<@NotNull Integer, @NotNull AMenuItem> pageMap = new TreeMap<>(); // sorts by index
    private final @NotNull Component title;
    private final @NotNull MinigamePlayer intendedViewer;
    private boolean allowModify = false;
    private @Nullable Menu previousPage = null;
    private @Nullable Menu nextPage = null;
    private int reopenTimerTaskID = -1;
    private @MonotonicNonNull Inventory inv = null;

    public Menu(final int rows, final @NotNull MinigameLangKey langKey, final @NotNull MinigamePlayer intendedViewer) {
        this(rows, MinigameMessageManager.getMgMessage(langKey), intendedViewer);
    }

    public Menu(final @Range(from = 1, to = 6) int rows, final @NotNull Component title, final @NotNull MinigamePlayer intendedViewer) {
        this.rows = Math.clamp(rows, 1, 6);
        this.title = title;
        pageView = new ItemStack[rows * 9];
        this.intendedViewer = intendedViewer;
    }

    public @NotNull Component getTitle() {
        return title;
    }

    public boolean setItem(final @NotNull AMenuItem item, final int slot) { // todo overflow into the next page
        if (!pageMap.containsKey(slot) && slot < pageView.length) {
            item.setContainingMenu(this);
            item.setSlot(slot);
            pageMap.put(slot, item);
            if (inv != null) {
                inv.setItem(slot, item.getDisplayItem());
            }
            return true;
        }
        return false;
    }

    private boolean isNewLine(final @NotNull AMenuItem menuItem) {
        return menuItem instanceof MenuItemNewLine;
    }

    /// overflows into next page if necessary
    public void addItem(final @NotNull AMenuItem item) {
        int inc = 0;
        @NotNull Menu menu = this;
        int maxItems = 9 * (rows - 1);

        while (true) {
            if (inc >= maxItems) {
                if (menu.getNextPage() == null) {
                    menu.addPage();
                }

                menu = menu.getNextPage();
                inc = 0;
            }

            if (menu.getMenuItem(inc) == null) {
                menu.setItem(item, inc);
                break;
            } else if (isNewLine(menu.getMenuItem(inc))) {
                // jump to next line, aka where inc % 9 == 0
                inc += 9 - inc % 9;
            } else {
                inc++;
            }
        }
    }

    /**
     * Danger! if this Menu already contains items some of the new ones might not get added! <-- todo solve this!
     */
    public void addItems(final @NotNull List<@NotNull AMenuItem> items) {
        Menu curPage = this;
        int inc = 0;
        for (AMenuItem it : items) {
            if (isNewLine(it)) {
                curPage.setItem(it, inc);
                // jump to next line, aka where inc % 9 == 0
                inc += 9 - inc % 9;
            } else {
                curPage.setItem(it, inc);
                inc++;
            }
            if (inc >= (9 * (rows - 1))) {
                inc = 0;
                if (curPage.getNextPage() == null && items.indexOf(it) < items.size()) {
                    curPage.addPage();
                }
                curPage = curPage.getNextPage();
            }
        }
    }

    protected void addPage() {
        final @NotNull Menu nextPage = new Menu(rows, title, intendedViewer);
        setItem(new MenuItemPage(MenuDisplayTypes.pageNextType(), MgMenuLangKey.MENU_PAGE_NEXT, nextPage), 9 * (rows - 1) + 5);
        setNextPage(nextPage);
        nextPage.setPreviousPage(this);
        nextPage.setItem(new MenuItemPage(MenuDisplayTypes.pageBackType(), MgMenuLangKey.MENU_PAGE_PREVIOUS, this), 9 * (rows - 1) + 3);
        for (int j = 9 * (rows - 1) + 6; j < 9 * rows; j++) {
            if (getMenuItem(j) != null)
                nextPage.setItem(getMenuItem(j), j);
        }
    }

    public void removeItem(final int slot) {
        if (pageMap.containsKey(slot)) {
            pageMap.remove(slot);
            pageView[slot] = null;
            if (inv != null) {
                inv.setItem(slot, null);
            }
        }
    }

    public void clearMenu() {
        for (final int i : new ArrayList<>(pageMap.keySet())) {
            pageMap.remove(i);
            pageView[i] = null;
        }
    }

    public void addItemStack(final @NotNull ItemStack item, final int slot) {
        inv.setItem(slot, item);
    }

    private void populateMenu() {
        for (Integer key : pageMap.keySet()) {
            if (!(pageMap.get(key) instanceof MenuItemNewLine))
                pageView[key] = pageMap.get(key).getDisplayItem();
        }
    }

    private void updateAll() {
        for (final @NotNull AMenuItem item : pageMap.values()) {
            item.update();
        }
    }

    public void displayMenu() {
        updateAll();
        populateMenu();
        inv = Bukkit.createInventory(intendedViewer.getPlayer(), rows * 9, title);
        inv.setContents(pageView);
        // Some calls of displayMenu are async, which is not allowed.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            intendedViewer.getPlayer().openInventory(inv);
            intendedViewer.setMenu(this);
        });
    }

    public boolean getAllowModify() {
        return allowModify;
    }

    public void setAllowModify(boolean canModify) {
        allowModify = canModify;
    }

    public @Nullable AMenuItem getMenuItem(final int slot) {
        return pageMap.get(slot);
    }

    public boolean hasMenuItem(final int slot) {
        return pageMap.containsKey(slot);
    }

    public int getSize() {
        return rows * 9;
    }

    public @Nullable Menu getNextPage() {
        return nextPage;
    }

    public void setNextPage(final @Nullable Menu page) {
        nextPage = page;
    }

    public boolean hasNextPage() {
        return nextPage != null;
    }

    public @Nullable Menu getPreviousPage() {
        return previousPage;
    }

    public void setPreviousPage(final @Nullable Menu page) {
        previousPage = page;
    }

    public boolean hasPreviousPage() {
        return previousPage != null;
    }

    /**
     * note: This method does not make any guarantees about this menu being viewed currently by the returned player.
     * the viewer might open this menu in the future, have already closed it, or never actually see it.
    */
    public @NotNull MinigamePlayer getIntendedViewer() {
        return intendedViewer;
    }

    public void closeAndWaitForInput(final @NotNull Duration reopenIn, final @NotNull AMenuItem itemWaitingForInput) {
        intendedViewer.getPlayer().closeInventory();
        intendedViewer.setMenuItemWaitingForManualInput(itemWaitingForInput);
        reopenTimerTaskID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (itemWaitingForInput.equals(intendedViewer.getMenuItemWaitingForManualInput())) {
                intendedViewer.setMenuItemWaitingForManualInput(null);
                displayMenu();
            }
        }, reopenIn.toSeconds() * 20L);
    }

    public void cancelWaitForInput() {
        if (reopenTimerTaskID != -1) {
            intendedViewer.setMenuItemWaitingForManualInput(null);
            Bukkit.getScheduler().cancelTask(reopenTimerTaskID);
            reopenTimerTaskID = -1;
        }
    }

    public @NotNull ItemStack @NotNull [] getInventory() {
        final @NotNull ItemStack @NotNull [] result = new ItemStack[getSize()];
        Arrays.fill(result, ItemStack.empty());

        for (int i = 0; i < inv.getContents().length; i++) {
            if (!pageMap.containsKey(i)) {
                final @Nullable ItemStack itemStack = inv.getContents()[i];

                if (itemStack != null) {
                    result[i] = itemStack;
                }
            }
        }

        return result;
    }

    public @NotNull Set<@NotNull Integer> getUsedSlots() {
        return pageMap.keySet();
    }

    public record AddMenuItemResult (@NotNull Menu menuPage, int slot) { // todo once setItem with slot parameter can overflow into a new page return this for both methods
    }
}
