package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemPage;
import au.com.mineauz.minigames.menu.MenuUtility;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HealAction extends AbstractAction {
    private final IntegerFlag heal = new IntegerFlag(1, "amount");

    @Override
    public @NotNull String getName() {
        return "HEAL";
    }

    @Override
    public @NotNull String getCategory() {
        return "World Actions";
    }

    @Override
    public void describe(@NotNull Map<@NotNull String, @NotNull Object> out) {
        out.put("Health", heal.getFlag());
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
    public void executeNodeAction(MinigamePlayer player,
                                  @NotNull Node node) {
        debug(player, node);
        execute(player);
    }

    @Override
    public void executeRegionAction(MinigamePlayer player, @NotNull Region region) {
        debug(player, region);
        execute(player);
    }

    private void execute(MinigamePlayer player) {
        if (player == null || !player.isInMinigame()) return;
        if (heal.getFlag() > 0) {
            if (player.getPlayer().getHealth() != 20) {
                double health = heal.getFlag() + player.getPlayer().getHealth();
                if (health > 20)
                    health = 20;
                player.getPlayer().setHealth(health);
            }
        } else
            player.getPlayer().damage(heal.getFlag() * -1);
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        heal.saveValue(path, config);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        heal.loadValue(path, config);
    }

    @Override
    public boolean displayMenu(MinigamePlayer player, Menu previous) {
        Menu m = new Menu(3, "Heal", player);
        m.addItem(new MenuItemPage("Back", MenuUtility.getBackMaterial(), previous), m.getSize() - 9);
        m.addItem(heal.getMenuItem("Heal Amount", Material.GOLDEN_APPLE, null, null));
        m.displayMenu(player);
        return true;
    }

}
