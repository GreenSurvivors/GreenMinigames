package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.FloatFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Main;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public class VelocityAction extends AAction {
    private final @NotNull FloatFlag x = new FloatFlag("xv", 0f);
    private final @NotNull FloatFlag y = new FloatFlag("yv", 5f);
    private final @NotNull FloatFlag z = new FloatFlag("zv", 0f);

    protected VelocityAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_VELOCITY_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_VELOCITY_NAME),
                MinigameMessageManager.getMgMessage(MgMiscLangKey.POSITION,
                        Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_X.getKey(), String.valueOf(x.getFlag())),
                        Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_Y.getKey(), String.valueOf(y.getFlag())),
                        Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_Z.getKey(), String.valueOf(z.getFlag()))));
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
    public void executeRegionAction(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        debug(mgPlayer, region);
        execute(mgPlayer);
    }

    @Override
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        debug(mgPlayer, node);
        execute(mgPlayer);
    }

    private void execute(final @Nullable MinigamePlayer mgPlayer) {
        if (mgPlayer == null) return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> mgPlayer.getPlayer().setVelocity(new Vector(x.getFlag(), y.getFlag(), z.getFlag())));
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        x.saveValue(config);
        y.saveValue(config);
        z.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        x.loadValue(config);
        y.loadValue(config);
        z.loadValue(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(x.getMenuItem(ItemType.STONE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_VELOCITY_X_NAME), 0.5d, 1d, null, null));
        menu.addItem(y.getMenuItem(ItemType.STONE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_VELOCITY_Y_NAME), 0.5d, 1d, null, null));
        menu.addItem(z.getMenuItem(ItemType.STONE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_VELOCITY_Z_NAME), 0.5d, 1d, null, null));
        menu.displayMenu();
        return true;
    }
}
