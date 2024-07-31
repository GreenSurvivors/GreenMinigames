package au.com.mineauz.minigames.mechanics;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GameMechanics {
    private static final Map<@NotNull String, @NotNull GameMechanicBase> gameMechanics = new HashMap<>();

    static {
        addGameMechanic(new CustomMechanic());
        Arrays.stream(MgMechanics.values()).forEach(s -> addGameMechanic(s.getMechanic()));
    }

    /**
     * Adds a new game mechanic to Minigames
     *
     * @param mechanic A game mechanic extending GameMechanicBase
     */
    public static void addGameMechanic(@NotNull GameMechanicBase mechanic) {
        gameMechanics.put(mechanic.getMechanicName(), mechanic);
    }

    /**
     * Removes an existing game mechanic from Minigames
     *
     * @param mechanic The name of the mechanic to be removed
     * @throws NullPointerException if the mechanic cannot be found.
     */
    public static void removeGameMechanic(@NotNull String mechanic) {
        if (gameMechanics.containsKey(mechanic)) {
            HandlerList.unregisterAll(gameMechanics.get(mechanic));
            gameMechanics.remove(mechanic);
        } else
            throw new NullPointerException("No GameMechanic of that name has been added!");
    }

    /**
     * Gets a specific game mechanic by name
     *
     * @param mechanic The name of the mechanic
     * @return A game mechanic extending GameMechanicBase or Null if none found.
     */
    public static @Nullable GameMechanicBase getGameMechanic(@NotNull String mechanic) {
        if (gameMechanics.containsKey(mechanic)) {
            return gameMechanics.get(mechanic);
        }
        return null;
    }

    /**
     * Gets all the registered game mechanics in Minigames
     *
     * @return a Set containing the game mechanics
     */
    public static @NotNull Set<@NotNull GameMechanicBase> getGameMechanics() {
        return new HashSet<>(gameMechanics.values());
    }

    public static @Nullable GameMechanicBase matchGameMechanic(@NotNull String name) {
        for (Map.Entry<String, GameMechanicBase> entry : gameMechanics.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public enum MgMechanics {
        KILLS(new PlayerKillsMechanic()),
        CTF(new CTFMechanic()),
        INFECTION(new InfectionMechanic()),
        TREASUREHUNT(new TreasureHuntMechanic()),
        LIVES(new LivesMechanic()),
        JUGGERNAUT(new JuggernautMechanic());

        private final GameMechanicBase mechanic;

        MgMechanics(GameMechanicBase mechanic) {
            this.mechanic = mechanic;
        }

        public GameMechanicBase getMechanic() {
            return this.mechanic;
        }

        @Override
        public @NotNull String toString() {
            return mechanic.getMechanicName();
        }
    }
}
