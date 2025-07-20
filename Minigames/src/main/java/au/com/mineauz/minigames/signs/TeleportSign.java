package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgSignLangKey;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeleportSign extends AMinigameSign {
    private final static Pattern coordPattern = Pattern.compile("(?<x>[+-]?[0-9]+),(?<y>[+-]?[0-9]+),(?<z>[+-]?[0-9]+)");
    private final static Pattern anglePattern = Pattern.compile("(?<yaw>-?[0-9]+),(?<pitch>-?[0-9]+)");

    @Override
    public @NotNull Component getName() {
        return MinigameMessageManager.getMgMessage(MgSignLangKey.TYPE_TELEPORT);
    }

    @Override
    public String getCreatePermission() {
        return "minigame.sign.create.teleport";
    }

    @Override
    public String getUsePermission() {
        return "minigame.sign.use.teleport";
    }

    @Override
    public boolean signCreate(@NotNull SignChangeEvent event) {

        if (event.line(2) == null) {
            return false;
        }

        final String serialize = PlainTextComponentSerializer.plainText().serialize(event.line(2));

        if (serialize.isEmpty()) {
            return false;
        } else {
            event.line(1, getName());
            return coordPattern.matcher(serialize).matches();
        }
    }

    @Override
    public boolean signUse(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
        final String line2 = PlainTextComponentSerializer.plainText().serialize(sign.getSide(Side.FRONT).line(2));
        Matcher coordMatcher = coordPattern.matcher(line2);
        if (coordMatcher.matches()) {
            double x = Double.parseDouble(coordMatcher.group("x"));
            double y = Double.parseDouble(coordMatcher.group("y"));
            double z = Double.parseDouble(coordMatcher.group("z"));

            Matcher angleMatcher = anglePattern.matcher(PlainTextComponentSerializer.plainText().serialize(sign.getSide(Side.FRONT).line(3)));
            if (angleMatcher.matches()) {
                float yaw = Float.parseFloat(angleMatcher.group("yaw"));
                float pitch = Float.parseFloat(angleMatcher.group("pitch"));

                mgPlayer.teleport(new Location(mgPlayer.getPlayer().getWorld(), x + 0.5, y, z + 0.5, yaw, pitch));
                return true;
            }
            mgPlayer.teleport(new Location(mgPlayer.getPlayer().getWorld(), x + 0.5, y, z + 0.5));
            return true;
        }
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.SIGN_TELEPORT_INVALID);
        return false;
    }

    @Override
    public void signBreak(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
    }
}
