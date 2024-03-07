package au.com.mineauz.minigames.minigame.reward.scheme;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.menu.MenuItemList;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public final class RewardSchemeRegistry {
    private static final HashMap<String, RewardSchemeFactory> definedSchemes = new HashMap<>();

    static {
        for (RewardSchemeFactory factory : MgRewardSchemes.values()) {
            addRewardScheme(factory);
        }
    }

    public static void addRewardScheme(@NotNull RewardSchemeFactory factory) {
        definedSchemes.put(factory.getSchemeName().toLowerCase(), factory);
    }

    public static @Nullable RewardScheme createScheme(String name) {
        RewardSchemeFactory factory = definedSchemes.get(name);

        if (factory != null) {
            return factory.makeScheme();
        } else {
            return null;
        }
    }

    public static MenuItem newMenuItem(@Nullable Material displayMat, @Nullable Component name, @NotNull Callback<String> callback) {
        return new MenuItemList<>(displayMat, name, callback, new ArrayList<>(definedSchemes.keySet()));
    }
}
