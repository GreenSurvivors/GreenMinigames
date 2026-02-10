package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public class HealAction extends AAction {
    private final @NotNull IntegerFlag heal = new IntegerFlag("amount", 1);

    protected HealAction(final @NotNull Key key) {
        super(key);
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
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        debug(mgPlayer, node);
        execute(mgPlayer);
    }

    @Override
    public void executeRegionAction(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        debug(mgPlayer, region);
        execute(mgPlayer);
    }

    private void execute(final @Nullable MinigamePlayer mgPlayer) {
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return;
        final Player player = mgPlayer.getPlayer();
        if (heal.getFlag() > 0) {
            if (player.getHealth() != 20) {
                double health = heal.getFlag() + player.getHealth();

                AttributeInstance healthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
                if (healthAttribute != null) {
                    health = Math.min(health, healthAttribute.getValue());
                } else {
                    health = Math.min(health, 20.0f);
                }

                player.setHealth(health);
            }
        } else {
            player.damage(heal.getFlag() * -1);
        }
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        heal.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        heal.loadValue(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(heal.getMenuItem(ItemType.GOLDEN_APPLE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_HEAL_AMOUNT_NAME), null, null));
        menu.displayMenu();
        return true;
    }
}
