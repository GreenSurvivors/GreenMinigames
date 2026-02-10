package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemPage;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.MinigameState;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GameOverModule extends AMinigameModule {
    private final @NotNull Minigames plugin = Minigames.getPlugin();
    private final @NotNull TimeFlag timer = new TimeFlag("gameOver.timer", 0L); // in seconds
    private final @NotNull BooleanFlag invincible = new BooleanFlag("gameOver.invincible", false);
    private final @NotNull BooleanFlag humiliation = new BooleanFlag("gameOver.humiliation", false);
    private final @NotNull BooleanFlag interact = new BooleanFlag("gameOver.interact", false);

    private final @NotNull List<@NotNull MinigamePlayer> winners = new ArrayList<>();
    private final @NotNull List<@NotNull MinigamePlayer> losers = new ArrayList<>();
    private int task = -1;

    public GameOverModule(final @NotNull Minigame mgm, final @NotNull Key key) {
        super(mgm, key);
    }

    public static @Nullable GameOverModule getMinigameModule(final @NotNull Minigame mgm) {
        return ((GameOverModule) mgm.getModule(MgDefaultModules.GAME_OVER.getKey()));
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        timer.saveValue(config);
        invincible.saveValue(config);
        humiliation.saveValue(config);
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) {
        timer.loadValue(config);
        invincible.loadValue(config);
        humiliation.loadValue(config);
    }

    @Override
    public void addEditMenuOptions(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(6, MgMenuLangKey.MENU_GAMEOVER_NAME, previous.getIntendedViewer());
        menu.addItem(timer.getMenuItem(ItemType.CLOCK, MgMenuLangKey.MENU_GAMEOVER_TIME_NAME, 0L, null));

        menu.addItem(invincible.getMenuItem(ItemType.ENDER_PEARL, MgMenuLangKey.MENU_GAMEOVER_INVINCIBILITY_NAME));
        menu.addItem(humiliation.getMenuItem(ItemType.DIAMOND_SWORD, MgMenuLangKey.MENU_GAMEOVER_HUMILIATION_NAME,
            MgMenuLangKey.MENU_GAMEOVER_HUMILIATION_DESCRIPTION));
        menu.addItem(interact.getMenuItem(ItemType.STONE_PRESSURE_PLATE, MgMenuLangKey.MENU_GAMEOVER_INTERACT_NAME));

        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);

        previous.addItem(new MenuItemPage(ItemType.OAK_DOOR, MgMenuLangKey.MENU_GAMEOVER_NAME, menu));
    }

    public void startEndGameTimer() {
        MinigameMessageManager.sendMinigameMessage(getMinigame(), MinigameMessageManager.getMgMessage(MgMiscLangKey.MINIGAME_GAMEOVERQUIT,
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(timer.getFlag())))));
        getMinigame().setState(MinigameState.ENDED);

        final @NotNull List<@NotNull MinigamePlayer> allPlys = new ArrayList<>(winners.size() + losers.size());
        allPlys.addAll(losers);
        allPlys.addAll(winners);

        for (final @NotNull MinigamePlayer mgPlayer : allPlys) {
            if (!isInteractAllowed()) {
                mgPlayer.setCanInteract(false);
            }

            final @Nullable Player player = mgPlayer.getPlayer();
            if (player != null && isHumiliationMode() && losers.contains(mgPlayer)) {
                player.getInventory().clear();
                player.getInventory().setHelmet(null);
                player.getInventory().setChestplate(null);
                player.getInventory().setLeggings(null);
                player.getInventory().setBoots(null);

                player.clearActivePotionEffects();
            }
            if (isInvincible()) {
                mgPlayer.setInvincible(true);
            }
        }

        if (timer.getFlag() > 0) {
            if (task != -1) {
                stopEndGameTimer();
            }

            task = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                for (MinigamePlayer loser : new ArrayList<>(losers)) {
                    if (loser.isInMinigame()) {
                        plugin.getPlayerManager().quitMinigame(loser, true);
                    }
                }
                for (MinigamePlayer winner : new ArrayList<>(winners)) {
                    if (winner.isInMinigame()) {
                        plugin.getPlayerManager().quitMinigame(winner, true);
                    }
                }

                clearLosers();
                clearWinners();
            }, timer.getFlag() * 20);
        }
    }

    public void stopEndGameTimer() {
        if (task != -1) {
            Bukkit.getScheduler().cancelTask(task);
        }
    }

    public void clearWinners() {
        winners.clear();
    }

    public @NotNull List<@NotNull MinigamePlayer> getWinners() {
        return winners;
    }

    public void setWinners(final @NotNull List<@NotNull MinigamePlayer> winners) {
        this.winners.addAll(winners);
    }

    public void clearLosers() {
        losers.clear();
    }

    public @NotNull List<@NotNull MinigamePlayer> getLosers() {
        return losers;
    }

    public void setLosers(final @NotNull List<@NotNull MinigamePlayer> losers) {
        this.losers.addAll(losers);
    }

    public long getTimer() {
        return timer.getFlag();
    }

    public void setTimer(long amount) {
        timer.setFlag(amount);
    }

    public boolean isInvincible() {
        return invincible.getFlag();
    }

    public void setInvincible(final boolean bool) {
        invincible.setFlag(bool);
    }

    public boolean isHumiliationMode() {
        return humiliation.getFlag();
    }

    public void setHumiliationMode(final boolean bool) {
        humiliation.setFlag(bool);
    }

    public boolean isInteractAllowed() {
        return interact.getFlag();
    }

    public void setInteractAllowed(final boolean bool) {
        interact.setFlag(bool);
    }
}
