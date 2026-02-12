package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public class SetLivesAction extends AAction { //todo unused!
    private final @NotNull IntegerFlag amount = new IntegerFlag("amount", 1);

    protected SetLivesAction(final @NotNull NamespacedKey key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return MessageManager.getMessage(RegionLangKey.MENU_ACTION_SETLIVES_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.MINIGAME;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(MessageManager.getMessage(RegionLangKey.MENU_ACTION_SETLIVES_NAME), Component.text(amount.getFlag()));
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
        if (mgPlayer != null && mgPlayer.getMinigame() != null) {
            int lives = mgPlayer.getMinigame().getLives();

            mgPlayer.setDeaths(Math.max(0, Math.min(lives - amount.getFlag(), lives))); // todo Math.clamp
        }
    }

    @Override
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        debug(mgPlayer, node);
        if (mgPlayer.getMinigame() != null) {
            int lives = mgPlayer.getMinigame().getLives();

            mgPlayer.setDeaths(Math.max(0, Math.min(lives - amount.getFlag(), lives))); // todo Math.clamp
        }
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        amount.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        amount.loadValue(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) { // todo description that a player can't have more lives than the minigame (minigame#getLives()) can support
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(amount.getMenuItem(ItemType.TOTEM_OF_UNDYING, MessageManager.getMessage(RegionLangKey.MENU_ACTION_SETLIVES_NAME), 0, null));
        menu.displayMenu();

        return true;
    }
}
