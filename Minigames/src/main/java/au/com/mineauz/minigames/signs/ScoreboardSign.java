package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgSignLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.ScoreboardDisplay;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.safelocation.SafeBlockLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class ScoreboardSign extends AMinigameSign {
    private static final Pattern SIZE_PATTERN = Pattern.compile("[0-9]+x[0-9]+");
    private final Minigames plugin = Minigames.getPlugin();

    @Override
    public @NotNull Component getName() {
        return MinigameMessageManager.getMgMessage(MgSignLangKey.TYPE_SCOREBOARD);
    }

    @Override
    public @Nullable String getCreatePermission() {
        return "minigame.sign.create.scoreboard";
    }

    @Override
    public @Nullable String getUsePermission() {
        return "minigame.sign.use.scoreboard";
    }

    @Override
    public boolean signCreate(final @NotNull SignChangeEvent event) {
        if (event.getBlock().getState().getBlockData() instanceof WallSign signData) {
            // Parse minigame
            final @NotNull Sign signState = (Sign) event.getBlock().getState();
            final @Nullable Minigame minigame = getMinigame(signState, event.line(2));

            if (minigame != null) {
                // Parse size
                final int width;
                final int height;

                if (event.line(3) != null) {
                    final @NotNull String line3 = PlainTextComponentSerializer.plainText().serialize(event.line(3));

                    if (!line3.isEmpty()) {
                        if (SIZE_PATTERN.matcher(line3).matches()) {
                            String[] parts = line3.split("x");
                            width = Integer.parseInt(parts[0]);
                            height = Integer.parseInt(parts[1]);
                        } else {
                            MinigameMessageManager.sendMgMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.SIGN_SCOREBOARD_ERROR_SIZE);
                            return false;
                        }
                    } else {
                        width = ScoreboardDisplay.DEFAULT_WIDTH;
                        height = ScoreboardDisplay.DEFAULT_HEIGHT;
                    }
                } else {
                    width = ScoreboardDisplay.DEFAULT_WIDTH;
                    height = ScoreboardDisplay.DEFAULT_HEIGHT;
                }

                // So we don't have to deal with even size scoreboards
                if (width % 2 == 0) {
                    MinigameMessageManager.sendMgMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.SIGN_SCOREBOARD_ERROR_UNEVENLENGTH);
                    return false;
                }

                final @NotNull BlockFace facing = signData.getFacing();

                // Add our display
                final @NotNull ScoreboardDisplay display = new ScoreboardDisplay(minigame, width, height,
                    new SafeBlockLocation(event.getBlock().getLocation()), facing);
                display.placeSigns(signData.getMaterial());

                minigame.getScoreboardData().addDisplay(display);

                // Reformat this sign for the next part
                event.line(1, getName());
                event.line(2, minigame.getDisplayName());
                setPersistentMinigame(signState, minigame);

                event.getBlock().setMetadata("Minigame", new FixedMetadataValue(plugin, minigame));
                return true;
            } else {
                MinigameMessageManager.sendMgMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOMINIGAME,
                    Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), event.line(2) == null ? Component.text("unknown") : event.line(2)));
                return false;
            }
        } else {
            MinigameMessageManager.sendMgMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.SIGN_SCOREBOARD_ERROR_WALL);
            return false;
        }
    }

    @Override
    public boolean signUse(final @NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
        Minigame minigame = getMinigame(sign);
        if (minigame == null) {
            return false;
        }

        ScoreboardDisplay display = minigame.getScoreboardData().getDisplay(sign);
        if (display == null) {
            return false;
        }

        display.displayMenu(mgPlayer);

        return false;
    }

    @Override
    public void signBreak(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
        Minigame minigame = (Minigame) sign.getBlock().getMetadata("Minigame").getFirst().value();
        if (minigame != null) {
            minigame.getScoreboardData().removeDisplay(sign.getBlock());
        }
    }
}
