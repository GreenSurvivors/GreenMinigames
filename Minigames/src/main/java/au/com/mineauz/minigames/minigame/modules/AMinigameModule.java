package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.TypeDependentDisplayData;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.ModulePlaceHolderProvider;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bstats.charts.CustomChart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.SequencedCollection;

public abstract class AMinigameModule implements Keyed {
    private static @Nullable ComparableVersion minRequired = null;
    protected final @NotNull Key moduleKey;
    private final @NotNull Minigame mgm;

    protected AMinigameModule(final @NotNull Minigame mgm, final @NotNull Key moduleKey) {
        this.mgm = mgm;
        this.moduleKey = moduleKey;
    }

    public static void setVersion(@Nullable ComparableVersion version) {
        minRequired = version;
    }

    /**
     * This returns true if the Minigames version is higher than your required version
     * ie if you require version 1.13 then and Minigames is at 1.14 it will be true
     *
     * @return true if the version exceeds your version
     */
    public static boolean checkVersion() {
        return minRequired == null || !(minRequired.compareTo(Minigames.getPlugin().getVersion()) > 0);
    }

    public static void addMetricChart(CustomChart chart) {
        Minigames.getPlugin().addMetric(chart);
    }

    public static @Nullable ComparableVersion getMinRequired() {
        return minRequired;
    }

    @Override
    public @NotNull Key key() {
        return moduleKey;
    }

    public @NotNull Minigame getMinigame() {
        return mgm;
    }

    public abstract boolean useSeparateConfig();

    public abstract void save(final @NotNull CommentedConfigurationNode config) throws SerializationException;

    public abstract void load(final @NotNull CommentedConfigurationNode config) throws ConfigurateException;

    public abstract @Nullable SequencedCollection<@NotNull TypeDependentDisplayData> addEditMenuOptions(final @NotNull Menu menu);

    /**
     * You should override this method if the module should provide more placeholders for a game it services.
     *
     * @return ModulePlaceHolderProvider
     */
    public @Nullable ModulePlaceHolderProvider getModulePlaceHolders() {
        return null;
    }
}
