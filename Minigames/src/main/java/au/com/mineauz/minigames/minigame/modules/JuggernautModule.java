package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JuggernautModule extends MinigameModule {
    private @Nullable MinigamePlayer juggernaut = null;

    public JuggernautModule(@NotNull Minigame mgm, @NotNull String name) {
        super(mgm, name);
    }

    public static JuggernautModule getMinigameModule(@NotNull Minigame mgm) {
        return ((JuggernautModule) mgm.getModule(MgModules.JUGGERNAUT.getName()));
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(@NotNull FileConfiguration config, @NotNull String path) {
    }

    @Override
    public void load(@NotNull FileConfiguration config, @NotNull String path) {
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
            team.removeEntry(team.getColor() + juggernaut.getPlayer().getDisplayName()); // todo find modern equivalent
        }
        juggernaut = player;

        if (juggernaut != null) {
            Team team = player.getMinigame().getScoreboard().getTeam("juggernaut");
            team.addEntry(team.getColor() + player.getPlayer().getDisplayName());

            MinigameMessageManager.sendMgMessage(juggernaut, MinigameMessageType.SUCCESS, MgMiscLangKey.PLAYER_JUGGERNAUT_PLAYERMSG);
            MinigameMessageManager.sendMinigameMessage(getMinigame(), MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_JUGGERNAUT_GAMEMSG,
                    Placeholder.unparsed(MinigamePlaceHolderKey.PLAYER.getKey(), juggernaut.getDisplayName(getMinigame().usePlayerDisplayNames()))
            ), MinigameMessageType.INFO, juggernaut);

            LoadoutModule lm = LoadoutModule.getMinigameModule(getMinigame());
            if (lm.hasLoadout("juggernaut")) {
                player.setLoadout(lm.getLoadout("juggernaut"));
                player.getLoadout().equipLoadout(player);
            }
        }
    }
}
