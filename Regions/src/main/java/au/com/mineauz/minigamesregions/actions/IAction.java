package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.script.ScriptObject;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public interface IAction extends Keyed {

    @NotNull Component getDisplayname();

    @NotNull IActionCategory getCategory();

    @NotNull Map<@NotNull Component, @Nullable Component> describe();

    boolean useInRegions();

    boolean useInNodes();

    void executeRegionAction(MinigamePlayer mgPlayer, @NotNull Region region);

    void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node);

    void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException;

    void loadArguments(final @NotNull CommentedConfigurationNode config) throws ConfigurateException;

    boolean displayMenu(final @NotNull Menu previous);

    void debug(MinigamePlayer mgPlayer, ScriptObject scriptObject);
}
