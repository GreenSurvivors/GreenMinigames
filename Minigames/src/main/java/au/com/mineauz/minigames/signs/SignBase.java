package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgSignLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SignBase implements Listener {
    private static final List<AMinigameSign> minigameSigns = new ArrayList<>();
    private static final Pattern alternativeMgmPattern = Pattern.compile("(?:\\[mgm])|(?:\\[mg])", Pattern.CASE_INSENSITIVE); // todo don't hardcode

    static {
        registerMinigameSign(new FinishSign());
        registerMinigameSign(new JoinSign());
        registerMinigameSign(new BetSign());
        registerMinigameSign(new CheckpointSign());
        registerMinigameSign(new CTFFlagSign());
        registerMinigameSign(new QuitSign());
        registerMinigameSign(new LoadoutSign());
        registerMinigameSign(new TeleportSign());
        registerMinigameSign(new SpectateSign());
        registerMinigameSign(new RewardSign());
        registerMinigameSign(new TeamSign());
        registerMinigameSign(new ScoreboardSign());
        registerMinigameSign(new ScoreSign());
    }

    public SignBase() {
        Minigames.getPlugin().getServer().getPluginManager().registerEvents(this, Minigames.getPlugin());
    }

    public static void registerMinigameSign(AMinigameSign mgSign) {
        minigameSigns.add(mgSign);
    }

    public static boolean isMinigameSign(final @Nullable Component firstLine) {
        if (firstLine != null) {
            String firstLineStr = PlainTextComponentSerializer.plainText().serialize(firstLine);

            return (MessageManager.getStrippedMessage(MgSignLangKey.MINIGAME).equalsIgnoreCase(firstLineStr) ||
                    alternativeMgmPattern.matcher(firstLineStr).matches());
        } else {
            return false;
        }
    }

    public @Nullable AMinigameSign getMgSign(final @Nullable Component secondLine) {
        if (secondLine != null) {
            // don't use a map here, with names as keys since it might be possible to reload messages via command
            String strLine = PlainTextComponentSerializer.plainText().serialize(secondLine);

            for (AMinigameSign mgSign : minigameSigns) {
                if (mgSign.isType(strLine)) {
                    return mgSign;
                }
            }
        }

        return null;
    }

    @EventHandler(ignoreCancelled = true)
    private void signPlace(@NotNull SignChangeEvent event) {
        if (isMinigameSign(event.line(0))) {
            if (event.getSide() == Side.FRONT) {
                AMinigameSign mgSign = getMgSign(event.line(1));

                if (mgSign != null) {
                    event.line(0, MessageManager.getMessage(MgSignLangKey.MINIGAME));
                    ((Sign) event.getBlock().getState()).setWaxed(true);

                    if (mgSign.getCreatePermission() != null && !event.getPlayer().hasPermission(mgSign.getCreatePermission())) {
                        event.setCancelled(true);
                        event.getBlock().breakNaturally();
                        MessageManager.sendMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOPERMISSION);
                        return;
                    }

                    if (!mgSign.signCreate(event)) {
                        event.setCancelled(true);
                        event.getBlock().breakNaturally();
                        MessageManager.sendMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.SIGN_ERROR_INVALID);
                    }
                } else {
                    MessageManager.sendMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.SIGN_ERROR_INVALID);
                    event.setCancelled(true);
                    event.getBlock().breakNaturally();
                }
            } else { //just gives an error but doesn't break the sign in case the front was important
                MessageManager.sendMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.SIGN_ERROR_BACKSIDE);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void signUse(@NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block cblock = event.getClickedBlock();
            if (cblock != null && cblock.getState() instanceof Sign sign) {
                AMinigameSign mgSign = getMgSign(sign.getSide(Side.FRONT).line(1));
                if (isMinigameSign(sign.getSide(Side.FRONT).line(0)) && mgSign != null) {

                    if (mgSign.getUsePermission() != null && !event.getPlayer().hasPermission(mgSign.getUsePermission())) {
                        event.setCancelled(true);
                        MessageManager.sendMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOPERMISSION);
                        return;
                    }

                    event.setCancelled(true);

                    mgSign.signUse(sign, Minigames.getPlugin().getPlayerManager().getMinigamePlayer(event.getPlayer()));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void signBreak(final @NotNull BlockBreakEvent event) {
        if (Tag.ALL_SIGNS.isTagged(event.getBlock().getType())) {
            Sign sign = (Sign) event.getBlock().getState();
            AMinigameSign mgSign = getMgSign(sign.getSide(Side.FRONT).line(2));
            if (isMinigameSign(sign.getSide(Side.FRONT).line(0)) && mgSign != null) {

                if (mgSign.getCreatePermission() != null && !event.getPlayer().hasPermission(mgSign.getCreatePermission())) {
                    event.setCancelled(true);
                    return;
                }
                mgSign.signBreak(sign, Minigames.getPlugin().getPlayerManager().getMinigamePlayer(event.getPlayer()));
            }
        }
    }
}
