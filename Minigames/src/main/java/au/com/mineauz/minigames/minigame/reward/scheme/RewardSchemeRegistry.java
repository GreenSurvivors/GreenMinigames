package au.com.mineauz.minigames.minigame.reward.scheme;

import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemList;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class RewardSchemeRegistry {
    private static final Map<@NotNull String, @NotNull RewardSchemeFactory> REGISTERED_SCHEMES = new HashMap<>();

    static {
        for (final @NotNull RewardSchemeFactory factory : MgDefaultRewardSchemes.values()) {
            registerRewardScheme(factory);
        }
    }

    public static void registerRewardScheme(@NotNull RewardSchemeFactory factory) {
        REGISTERED_SCHEMES.put(factory.getSchemeName().toLowerCase(), factory);
    }

    public static @Nullable ARewardScheme makeScheme(final @NotNull String name) {
        final @Nullable RewardSchemeFactory factory = REGISTERED_SCHEMES.get(name);

        if (factory != null) {
            return factory.makeScheme();
        } else {
            return null;
        }
    }

    public static @NotNull AMenuItem newMenuItem(@Nullable ItemType displayItem, @Nullable Component name, @NotNull Callback<String> callback) {
        return new MenuItemList<>(displayItem, name, callback, new ArrayList<>(REGISTERED_SCHEMES.keySet()));
    }
}
