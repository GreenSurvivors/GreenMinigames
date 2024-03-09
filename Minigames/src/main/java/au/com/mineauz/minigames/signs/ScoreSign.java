package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgSignLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.minigame.modules.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ScoreSign extends AMinigameSign {
    private final static Pattern INT_PATTERN = Pattern.compile("^[+-]?[0-9]+$");

    @Override
    public @NotNull Component getName() {
        return MinigameMessageManager.getMgMessage(MgSignLangKey.TYPE_SCORE);
    }

    @Override
    public String getCreatePermission() {
        return "minigame.sign.create.score";
    }

    @Override
    public String getUsePermission() {
        return "minigame.sign.use.score";
    }

    @Override
    public boolean signCreate(@NotNull SignChangeEvent event) {
        PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();
        if (INT_PATTERN.matcher(plainSerializer.serialize(event.line(2))).matches()) {
            event.line(1, getName());

            TeamColor col = TeamColor.matchColor(plainSerializer.serialize(event.line(3)));
            if (col != null) {
                event.line(3, col.getCompName());
            } else {
                event.line(3, Component.empty());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean signUse(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
        if (mgPlayer.isInMinigame()) {
            Minigame mg = mgPlayer.getMinigame();
            PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();
            final String scoreString = plainSerializer.serialize(sign.getSide(Side.FRONT).line(2));

            if (INT_PATTERN.matcher(scoreString).matches()) {
                int score = Integer.parseInt(scoreString);

                if (!mg.isTeamGame()) {
                    if (mgPlayer.hasClaimedScore(sign.getLocation())) {
                        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.SIGN_SCORE_ERROR_ALREADYUSED);
                        return true;
                    }
                    mgPlayer.addScore(score);
                    mg.setScore(mgPlayer, mgPlayer.getScore());
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.SIGN_SCORE_ADDSCORE,
                            Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(score)),
                            Placeholder.unparsed(MinigamePlaceHolderKey.SCORE.getKey(), String.valueOf(mgPlayer.getScore())));
                    if (mg.getMaxScore() != 0 && mg.getMaxScorePerPlayer() <= mgPlayer.getScore()) {
                        Minigames.getPlugin().getPlayerManager().endMinigame(mgPlayer);
                    }
                    mgPlayer.addClaimedScore(sign.getLocation());
                } else {
                    TeamColor steam = TeamColor.matchColor(plainSerializer.serialize(sign.getSide(Side.FRONT).line(3)));
                    Team pteam = mgPlayer.getTeam();
                    if (steam == null || !TeamsModule.getMinigameModule(mg).hasTeam(steam) || pteam.getColor() == steam) {
                        if (Minigames.getPlugin().getMinigameManager().hasClaimedScore(mg, sign.getLocation(), 0)) {
                            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.SIGN_SCORE_ERROR_ALREADYUSEDTEAM);
                            return true;
                        }
                        mgPlayer.addScore(score);
                        mg.setScore(mgPlayer, mgPlayer.getScore());

                        pteam.addScore(score);
                        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.SIGN_SCORE_ADDSCORETEAM,
                                Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(score)),
                                Placeholder.unparsed(MinigamePlaceHolderKey.SCORE.getKey(), String.valueOf(pteam.getScore())));
                        Minigames.getPlugin().getMinigameManager().addClaimedScore(mg, sign.getLocation(), 0);
                        if (mg.getMaxScore() != 0 && mg.getMaxScorePerPlayer() <= pteam.getScore()) {
                            List<MinigamePlayer> winners = new ArrayList<>(pteam.getPlayers());
                            List<MinigamePlayer> losers = new ArrayList<>(mg.getPlayers().size() - pteam.getPlayers().size());
                            for (Team t : TeamsModule.getMinigameModule(mg).getTeams()) {
                                if (t != pteam)
                                    losers.addAll(t.getPlayers());
                            }
                            Minigames.getPlugin().getPlayerManager().endMinigame(mg, winners, losers);
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void signBreak(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
        //Eh...
    }
}
