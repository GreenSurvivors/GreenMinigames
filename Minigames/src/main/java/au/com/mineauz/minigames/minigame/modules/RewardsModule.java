package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemCustom;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.reward.scheme.ARewardScheme;
import au.com.mineauz.minigames.minigame.reward.scheme.MgRewardSchemes;
import au.com.mineauz.minigames.minigame.reward.scheme.RewardSchemeRegistry;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.stats.StoredGameStats;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class RewardsModule extends AMinigameModule {
    private @NotNull ARewardScheme scheme;

    public RewardsModule(final @NotNull Minigame mgm, final @NotNull Key key) {
        super(mgm, key);

        // Default scheme
        scheme = MgRewardSchemes.STANDARD.makeScheme();
    }

    public static @Nullable RewardsModule getModule(@NotNull Minigame minigame) {
        return (RewardsModule) minigame.getModule(MgDefaultModules.REWARDS.getKey());
    }


    public @NotNull ARewardScheme getScheme() {
        return scheme;
    }

    @SuppressWarnings("unused")
    public void setRewardScheme(@NotNull ARewardScheme scheme) {
        this.scheme = scheme;
    }

    public void awardPlayer(@NotNull MinigamePlayer player, StoredGameStats data, Minigame minigame, boolean firstCompletion) {
        scheme.awardPlayer(player, data, minigame, firstCompletion);
    }

    public void awardPlayerOnLoss(final @NotNull MinigamePlayer player, final @NotNull StoredGameStats data, final @NotNull Minigame minigame) {
        scheme.awardPlayerOnLoss(player, data, minigame);
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.node("reward-scheme").set(scheme.getName());
        scheme.save(config.node("rewards"));
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        final @NotNull String name = config.node("reward-scheme").getString(MgRewardSchemes.STANDARD.getSchemeName());

        scheme = RewardSchemeRegistry.createScheme(name);
        if (scheme == null) {
            scheme = MgRewardSchemes.STANDARD.makeScheme();
        }

        scheme.load(config.node("rewards"));
    }

    @Override
    public void addEditMenuOptions(final @NotNull Menu menu) {
        final @NotNull MenuItemCustom launcher = new MenuItemCustom(ItemType.DIAMOND,
            MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_SETTINGS_NAME));
        launcher.setClick(() -> {
            final @NotNull Menu submenu = createSubMenu(menu);
            submenu.displayMenu();
            return ItemStack.empty();
        });

        menu.addItem(launcher);
    }

    private @NotNull Menu createSubMenu(final @NotNull Menu parent) {
        final Menu submenu = new Menu(6,
            MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_SETTINGS_NAME), parent.getIntendedViewer());
        scheme.addMenuItems(submenu);

        submenu.addItem(RewardSchemeRegistry.newMenuItem(ItemType.PAPER,
            MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_SCHEME_NAME), new Callback<>() {
                @Override
                public @NotNull String getValue() {
                    return scheme.getName();
                }

                @Override
                public void setValue(@NotNull String value) {
                    scheme = RewardSchemeRegistry.createScheme(value);
                    // Update the menu
                    final @NotNull Menu menu = createSubMenu(parent);
                    menu.displayMenu();
                }
            }), submenu.getSize() - 1);

        submenu.addItem(new MenuItemBack(parent), submenu.getSize() - 9);
        return submenu;
    }
}
