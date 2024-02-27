package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 6/11/2017.
 */
public class SetLivesAction extends AAction { //todo unused!
    private final IntegerFlag amount = new IntegerFlag(1, "amount");

    protected SetLivesAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SETLIVES_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.MINIGAME;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SETLIVES_NAME), Component.text(amount.getFlag()));
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
        if (mgPlayer != null && mgPlayer.getMinigame() != null) {
            int lives = mgPlayer.getMinigame().getLives();

            mgPlayer.setDeaths(Math.max(0, Math.min(lives - amount.getFlag(), lives))); // todo Math.clamp
        }
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        debug(mgPlayer, node);
        if (mgPlayer.getMinigame() != null) {
            int lives = mgPlayer.getMinigame().getLives();

            mgPlayer.setDeaths(Math.max(0, Math.min(lives - amount.getFlag(), lives))); // todo Math.clamp
        }
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        amount.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        amount.loadValue(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu previous) { // todo description that a player can't have more lives than the minigame (minigame#getLives()) can support
        Menu menu = new Menu(3, getDisplayname(), mgPlayer);
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(amount.getMenuItem(Material.TOTEM_OF_UNDYING, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SETLIVES_NAME), 0, null));
        menu.displayMenu(mgPlayer);

        return true;
    }
}
