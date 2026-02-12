package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionsMain;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public abstract class ACondition implements Keyed {
    private final @NotNull BooleanFlag isInverted = new BooleanFlag("invert", false);
    protected final @NotNull Key key;

    protected ACondition(final @NotNull Key key) {
        this.key = key;
    }

    protected void addInvertMenuItem(final @NotNull Menu menu) {
        menu.setItem(isInverted.getMenuItem(ItemType.ENDER_PEARL, MessageManager.getMessage(MgMenuLangKey.MENU_INVERT_NAME)), menu.getSize() - 1);
    }

    protected void saveInvertedStatus(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        isInverted.saveValue(config);
    }

    protected void loadInvert(final @NotNull CommentedConfigurationNode config) {
        isInverted.loadValue(config);
    }

    public boolean isInverted() {
        return isInverted.getFlag();
    }

    public @NotNull Key key() {
        return key;
    }

    public abstract @NotNull Component getDisplayName();

    public abstract @NotNull IConditionCategory getCategory();

    public abstract boolean useInRegions();

    public abstract boolean useInNodes();

    public abstract boolean checkRegionCondition(final MinigamePlayer mgPlayer, final @NotNull Region region);

    public abstract boolean checkNodeCondition(final MinigamePlayer mgPlayer, final @NotNull Node node);

    public abstract void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException;

    public abstract void loadArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException;

    public abstract boolean displayMenu(final @NotNull Menu prev);

    /**
     * Returns if the condition needs a player who caused the check to happen.
     */
    public abstract boolean playerNeeded();

    public abstract @NotNull Map<@NotNull Component, @Nullable Component> describe();

    public void debug(final @NotNull Minigame mg) {
        if (Minigames.getPlugin().isDebugging()) {
            RegionsMain.getPlugin().getComponentLogger().info("Cat " + getCategory() + " : " + key().asMinimalString() +
                    " Check:" + mg.getName() + " mech: " + mg.getMechanic().key() + ", Condition:                     " + this);
        }
    }
}
