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
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class HealAction extends AAction {
    private final IntegerFlag heal = new IntegerFlag(1, "amount");

    protected HealAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_HEAL_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.WORLD;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_HEAL_AMOUNT_NAME), Component.text(heal.getFlag()));
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
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer,
                                  @NotNull Node node) {
        debug(mgPlayer, node);
        execute(mgPlayer);
    }

    @Override
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        debug(mgPlayer, region);
        execute(mgPlayer);
    }

    private void execute(@Nullable MinigamePlayer player) {
        if (player == null || !player.isInMinigame()) return;
        if (heal.getFlag() > 0) {
            if (player.getPlayer().getHealth() != 20) {
                double health = heal.getFlag() + player.getPlayer().getHealth();

                AttributeInstance healthAttribute = player.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (healthAttribute != null) {
                    health = Math.min(health, healthAttribute.getValue());
                } else {
                    health = Math.min(health, 20.0f);
                }

                player.getPlayer().setHealth(health);
            }
        } else {
            player.getPlayer().damage(heal.getFlag() * -1);
        }
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        heal.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        heal.loadValue(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, @NotNull Menu previous) {
        Menu m = new Menu(3, getDisplayname(), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);
        m.addItem(heal.getMenuItem(Material.GOLDEN_APPLE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_HEAL_AMOUNT_NAME), null, null));
        m.displayMenu(mgPlayer);
        return true;
    }

}
