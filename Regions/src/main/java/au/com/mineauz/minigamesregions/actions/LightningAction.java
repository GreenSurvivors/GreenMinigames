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
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;
import java.util.Random;

public class LightningAction extends AAction {
    private final BooleanFlag effect = new BooleanFlag("effect", false);

    protected LightningAction(final @NotNull NamespacedKey key) {
        super(key);
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

        if (region.getWorld() == null) {
            return;
        }

        final @NotNull Random rand = new Random();
        final double xrand = rand.nextDouble() * region.getWidthX() + region.getMinX();
        final double yrand = rand.nextDouble() * region.getHeight() + region.getMinY();
        final double zrand = rand.nextDouble() * region.getWidthZ() + region.getMinZ();

        final @NotNull Location loc = new Location(region.getWorld(), xrand, yrand, zrand);

        if (effect.getFlag()) {
            region.getWorld().strikeLightningEffect(loc);
        } else {
            region.getWorld().strikeLightning(loc);
        }
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        debug(mgPlayer, node);
        if (node.getSafeLocation().getWorld() == null) {
            return;
        }

        if (effect.getFlag()) {
            node.getSafeLocation().getWorld().strikeLightningEffect(node.getSafeLocation().toLocation());
        } else {
            node.getSafeLocation().getWorld().strikeLightning(node.getSafeLocation().toLocation());
        }
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        effect.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        effect.loadValue(config);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, @NotNull Menu previous) {
        Menu m = new Menu(3, getDisplayname(), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);
        m.addItem(effect.getMenuItem(ItemType.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_LIGHTNING_EFFECT_NAME)));
        m.displayMenu(mgPlayer);
        return true;
    }
}
