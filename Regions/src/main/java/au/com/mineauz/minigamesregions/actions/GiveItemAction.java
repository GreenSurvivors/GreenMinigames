package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.ItemFlag;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemString;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class GiveItemAction extends AAction {
    private final ItemFlag item = new ItemFlag(new ItemStack(Material.STONE), "item");
    /*private final StringFlag type = new StringFlag("STONE", "type");
    private final IntegerFlag count = new IntegerFlag(1, "count");
    private final StringFlag name = new StringFlag(null, "name");
    private final StringFlag lore = new StringFlag(null, "lore");*/

    protected GiveItemAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_GIVEITEM_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable ComponentLike> describe() {
        out.put("Item", count.getFlag() + "x " + type.getFlag());
        out.put("Display Name", name.getFlag());
        out.put("Lore", lore.getFlag());
    }

    @Override
    public boolean useInRegions() {
        return true;
    }

    @Override
    public boolean useInNodes() {
        return true;
    }

    @Override
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        debug(mgPlayer, region);
        if (mgPlayer != null) {
            execute(mgPlayer);
        }
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        debug(mgPlayer, node);
        execute(mgPlayer);
    }

    private void execute(@NotNull MinigamePlayer player) {
        Map<Integer, ItemStack> unadded = player.getPlayer().getInventory().addItem(item.getFlag());

        if (!unadded.isEmpty()) {
            for (ItemStack i : unadded.values()) {
                player.getLocation().getWorld().dropItem(player.getLocation(), i);
            }
        }
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        item.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        type.loadValue(config, path);
        count.loadValue(config, path);
        if (config.contains(path + ".name"))
            name.loadValue(config, path);
        if (config.contains(path + ".lore")) {

            meta.setLore(List.of(lore.getFlag().split(";"))); //as the description states semicolons will be used for new lines
            lore.loadValue(config, path);
        }
    }

    @Override
    public boolean displayMenu(@NotNull final MinigamePlayer mgPlayer, Menu previous) {
        Menu m = new Menu(3, getDisplayname(), mgPlayer);

        m.addItem(new MenuItemBack(previous), m.getSize() - 9);

        MenuItemString n = (MenuItemString) name.getMenuItem(Material.NAME_TAG, "Name");
        n.setAllowNull(true);
        m.addItem(n);
        MenuItemString l = lore.getMenuItem(Material.PAPER, "Lore",
                List.of("Separate with semicolons", "for new lines"));
        l.setAllowNull(true);
        m.addItem(l);

        m.addItem(new MenuItemString(Material.STONE, "Type", new Callback<>() {
            @Override
            public String getValue() {
                return type.getFlag();
            }

            @Override
            public void setValue(String value) {
                if (Material.getMaterial(value.toUpperCase()) != null) {
                    type.setFlag(value.toUpperCase());
                } else {
                    MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionMessageManager.getBundleKey(), RegionLangKey.ERROR_INVALID_ITEMTYPE);
                }
            }
        }));
        m.addItem(count.getMenuItem(Material.STONE_SLAB, "Count", 1, 64));
        m.displayMenu(mgPlayer);
        return true;
    }
}
