package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamesKey;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GameMechanicRegistry {
    private static final @NotNull Map<@NotNull Key, @NotNull IGameMechanicFactory> registeredMechanicFactories = new HashMap<>();

    private GameMechanicRegistry () {
    }

    /**
     * Registers an AGameMechanic factory.
     *
     * @param mechanicFactory  The factory producing our mechanic to register
     * @return the factory previously registered by the same key, or null if there wasn't one
     */
    public static @Nullable IGameMechanicFactory registerGameMechanic(final @NotNull IGameMechanicFactory mechanicFactory) {
        return registeredMechanicFactories.put(mechanicFactory.getKey(), mechanicFactory);
    }

    /**
     * Unregisters a previously registered mechanic
     *
     * @param mechanicKey The key of the mechanic to unregister
     * @return true, if any mechanic was registered under the given key
     */
    public static boolean unregisterMechanic(final @NotNull Key mechanicKey) {
        return registeredMechanicFactories.remove(mechanicKey) != null;
    }

    /**
     * Retrieves a registered mechanic
     *
     * @param mechanicKey The  key to get the mechanic for
     * @return The mechanic factory or null
     */
    public static @Nullable IGameMechanicFactory getMechanicFactory(final @NotNull Key mechanicKey) {
        return registeredMechanicFactories.get(mechanicKey);
    }

    /**
     * Gets all the registered game mechanics
     *
     * @return a Collection containing the game mechanics
     */
    public static @NotNull Collection<@NotNull IGameMechanicFactory> getAllFactories() {
        return registeredMechanicFactories.values();
    }

    public static IGameMechanicFactory CUSTOM = new IGameMechanicFactory() {
        final @NotNull Key key = MinigamesKey.minigames("custom");

        @Override
        public @NotNull AGameMechanic makeNewMechanic(final @NotNull Minigames plugin, final @NotNull Minigame minigame) {
            return new CustomMechanic(plugin, key, minigame);
        }

        @Override
        public @NotNull Key getKey() {
            return key;
        }
    };

    public enum MgDefaultMechanic implements IGameMechanicFactory { // todo Custom
        KILLS("kills", PlayerKillsMechanic::new),
        CTF("ctf", CTFMechanic::new),
        INFECTION("infection", InfectionMechanic::new),
        TREASURE_HUNT("treasure_hunt", TreasureHuntMechanic::new),
        LIVES("lives", LivesMechanic::new),
        JUGGERNAUT("juggernaut", JuggernautMechanic::new);

        private final @NotNull Key key;
        private final @NotNull TriFunction<@NotNull Minigames, @NotNull Key, @NotNull Minigame, AGameMechanic> mechanicFactory;

        MgDefaultMechanic(final @NotNull String name, final @NotNull TriFunction<@NotNull Minigames, @NotNull Key, @NotNull Minigame, AGameMechanic> mechanicFactory) {
            this.key = MinigamesKey.minigames(name);
            this.mechanicFactory = mechanicFactory;
        }

        @Override
        public @NotNull AGameMechanic makeNewMechanic(final @NotNull Minigames plugin, final @NotNull Minigame minigame) {
            return this.mechanicFactory.apply(plugin, key, minigame);
        }

        @Override
        public @NotNull Key getKey() {
            return key;
        }
    }

    static {
        for (final @NotNull GameMechanicRegistry.MgDefaultMechanic mgMechanic : MgDefaultMechanic.values()) {
            registerGameMechanic(mgMechanic);
        }
        registerGameMechanic(CUSTOM);
    }
}
