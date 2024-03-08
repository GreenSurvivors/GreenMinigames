package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemCustom;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.reward.scheme.MgRewardSchemes;
import au.com.mineauz.minigames.minigame.reward.scheme.RewardScheme;
import au.com.mineauz.minigames.minigame.reward.scheme.RewardSchemeRegistry;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.stats.StoredGameStats;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class RewardsModule extends MinigameModule {
    private RewardScheme scheme;

    public RewardsModule(@NotNull Minigame mgm, @NotNull String name) {
        super(mgm, name);

        // Default scheme
        scheme = MgRewardSchemes.STANDARD.makeScheme();
    }

    public static RewardsModule getModule(Minigame minigame) {
        return (RewardsModule) minigame.getModule(MgModules.REWARDS.getName());
    }


    public RewardScheme getScheme() {
        return scheme;
    }

    @SuppressWarnings("unused")
    public void setRewardScheme(RewardScheme scheme) {
        this.scheme = scheme;
    }

    public void awardPlayer(MinigamePlayer player, StoredGameStats data, Minigame minigame, boolean firstCompletion) {
        scheme.awardPlayer(player, data, minigame, firstCompletion);
    }

    public void awardPlayerOnLoss(MinigamePlayer player, StoredGameStats data, Minigame minigame) {
        scheme.awardPlayerOnLoss(player, data, minigame);
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(@NotNull FileConfiguration config, @NotNull String path) {
        config.set(path + config.options().pathSeparator() + "reward-scheme", scheme.getName());
        scheme.save(config, path + config.options().pathSeparator() + "rewards");
    }

    @Override
    public void load(@NotNull FileConfiguration config, @NotNull String path) {
        String name = config.getString(path + config.options().pathSeparator() + "reward-scheme", "standard");

        scheme = RewardSchemeRegistry.createScheme(name);
        if (scheme == null) {
            scheme = MgRewardSchemes.STANDARD.makeScheme();
        }

        scheme.load(config, path + config.options().pathSeparator() + "rewards");
    }

    @Override
    public void addEditMenuOptions(final @NotNull Menu menu) {
        MenuItemCustom launcher = new MenuItemCustom(Material.DIAMOND,
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_SETTINGS_NAME));
        launcher.setClick(() -> {
            Menu submenu = createSubMenu(menu);
            submenu.displayMenu(menu.getViewer());
            return null;
        });

        menu.addItem(launcher);
    }

    private Menu createSubMenu(final Menu parent) {
        final Menu submenu = new Menu(6,
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_SETTINGS_NAME), parent.getViewer());
        scheme.addMenuItems(submenu);

        submenu.addItem(RewardSchemeRegistry.newMenuItem(Material.PAPER,
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_SCHEME_NAME), new Callback<>() {
                    @Override
                    public String getValue() {
                        return scheme.getName();
                    }

                    @Override
                    public void setValue(String value) {
                        scheme = RewardSchemeRegistry.createScheme(value);
                        // Update the menu
                        Menu menu = createSubMenu(parent);
                        menu.displayMenu(submenu.getViewer());
                    }
                }), submenu.getSize() - 1);

        submenu.addItem(new MenuItemBack(parent), submenu.getSize() - 9);
        return submenu;
    }

    @Override
    public boolean displayMechanicSettings(@NotNull Menu previous) {
        // Not used
        return false;
    }
}
