package au.com.mineauz.minigames.minigame.reward.scheme;

import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class RewardSchemeRegistry {
    private static final @NotNull Map<@NotNull Key, @NotNull RewardSchemeFactory> REGISTERED_SCHEMES = new HashMap<>();

    static {
        for (final @NotNull RewardSchemeFactory factory : MgDefaultRewardSchemes.values()) {
            registerRewardScheme(factory);
        }
    }

    public static void registerRewardScheme(final @NotNull RewardSchemeFactory factory) {
        REGISTERED_SCHEMES.put(factory.key(), factory);
    }

    public static @Nullable ARewardScheme makeScheme(final @NotNull Key key) {
        final @Nullable RewardSchemeFactory factory = REGISTERED_SCHEMES.get(key);

        if (factory != null) {
            return factory.makeScheme();
        } else {
            return null;
        }
    }

    public static @NotNull AMenuItem newMenuItem(final @Nullable ItemType displayItem, final @Nullable Component name, final @NotNull Callback<Key> callback) {
        return new MenuItemList<>(displayItem, name, callback, new ArrayList<>(REGISTERED_SCHEMES.keySet()));
    }
}
