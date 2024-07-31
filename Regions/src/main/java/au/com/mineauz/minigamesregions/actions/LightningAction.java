package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;

public class LightningAction extends AAction {
    private final BooleanFlag effect = new BooleanFlag(false, "effect");

    protected LightningAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_LIGHTNING_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.WORLD;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_LIGHTNING_EFFECT_NAME), Component.text(effect.getFlag()));
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
        Random rand = new Random();
        double xrand = rand.nextDouble() *
                (region.getSecondPoint().getBlockX() - region.getFirstPoint().getBlockX()) +
                region.getFirstPoint().getBlockX();
        double yrand = rand.nextDouble() *
                (region.getSecondPoint().getBlockY() - region.getFirstPoint().getBlockY()) +
                region.getFirstPoint().getBlockY();
        double zrand = rand.nextDouble() *
                (region.getSecondPoint().getBlockZ() - region.getFirstPoint().getBlockZ()) +
                region.getFirstPoint().getBlockZ();

        Location loc = region.getFirstPoint();
        loc.setX(xrand);
        loc.setY(yrand);
        loc.setZ(zrand);

        if (effect.getFlag()) {
            loc.getWorld().strikeLightningEffect(loc);
        } else {
            loc.getWorld().strikeLightning(loc);
        }
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        debug(mgPlayer, node);
        if (effect.getFlag()) {
            node.getLocation().getWorld().strikeLightningEffect(node.getLocation());
        } else {
            node.getLocation().getWorld().strikeLightning(node.getLocation());
        }
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        effect.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        effect.loadValue(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, @NotNull Menu previous) {
        Menu m = new Menu(3, getDisplayname(), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);
        m.addItem(effect.getMenuItem(Material.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_LIGHTNING_EFFECT_NAME)));
        m.displayMenu(mgPlayer);
        return true;
    }
}
