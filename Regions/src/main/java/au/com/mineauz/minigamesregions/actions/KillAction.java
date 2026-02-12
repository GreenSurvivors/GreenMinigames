package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.Map;

public class KillAction extends AAction {

    protected KillAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return MessageManager.getMessage(RegionLangKey.MENU_ACTION_KILL_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.WORLD;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of();
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
        if (!mgPlayer.isInMinigame()) return;
        if (!mgPlayer.isLiving())
            mgPlayer.getPlayer().setHealth(0.0);
    }

    @Override
    public void executeRegionAction(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return;
        debug(mgPlayer, region);
        if (mgPlayer.isLiving()) {
            mgPlayer.getPlayer().setHealth(0.0);
        }
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) {
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        return false;
    }
}
