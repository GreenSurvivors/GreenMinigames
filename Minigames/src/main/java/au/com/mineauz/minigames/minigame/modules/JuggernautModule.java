package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

public class JuggernautModule extends MinigameModule {
    private @Nullable MinigamePlayer juggernaut = null;

    public JuggernautModule(final @NotNull Minigame mgm, final @NotNull Key key) {
        super(mgm, key);
    }

    public static JuggernautModule getMinigameModule(@NotNull Minigame mgm) {
        return ((JuggernautModule) mgm.getModule(MgModules.JUGGERNAUT.getKey()));
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) {
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) {
    }

    @Override
    public void addEditMenuOptions(@NotNull Menu menu) {
    }

    @Override
    public boolean displayMechanicSettings(@NotNull Menu previous) {
        return false;
    }

    public @Nullable MinigamePlayer getJuggernaut() {
        return juggernaut;
    }

    public void setJuggernaut(@Nullable MinigamePlayer player) {
        if (juggernaut != null) {
            Team team = juggernaut.getMinigame().getScoreboard().getTeam("juggernaut");
            juggernaut.setLoadout(null);
            team.removePlayer(juggernaut.getPlayer());
        }
        juggernaut = player;

        if (juggernaut != null) {
            Team team = player.getMinigame().getScoreboard().getTeam("juggernaut");
            team.addPlayer(player.getPlayer());

            MinigameMessageManager.sendMgMessage(juggernaut, MinigameMessageType.SUCCESS, MgMiscLangKey.PLAYER_JUGGERNAUT_PLAYERMSG);
            MinigameMessageManager.sendMinigameMessage(getMinigame(), MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_JUGGERNAUT_GAMEMSG,
                Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), juggernaut.displayName())
            ), MinigameMessageType.INFO, juggernaut);

            LoadoutModule lm = LoadoutModule.getMinigameModule(getMinigame());
            if (lm.hasLoadout("juggernaut")) {
                player.setLoadout(lm.getLoadout("juggernaut"));
                player.getLoadout().equipLoadout(player);
            }
        }
    }
}
