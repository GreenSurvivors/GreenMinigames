package au.com.mineauz.minigames.objects;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.modules.CTFModule;
import au.com.mineauz.minigames.signs.CTFFlagSign;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Flag of Capture the Flag.
 * Technical background for {@link CTFFlagSign}
 */
public class CTFFlag {
    private final @NotNull NamespacedKey flagKey = new NamespacedKey(Minigames.getPlugin(), "is_ctf_flag");
    private final @NotNull BlockState spawnData;
    private final @NotNull List<@NotNull Component> signText;
    private final @NotNull Minigame minigame;
    private final @NotNull CTFModule ctfModule;
    private final @NotNull Location spawnLocation;
    private final @Nullable Location attachedToLocation;
    private final @Nullable Team team;
    private @Nullable Location currentLocation = null;
    private BlockState originalBlockStateBelow = null;
    private boolean atHome = true;
    private int respawnTime = 60;
    private int taskID = -1;
    private int cParticleID = -1;

    public CTFFlag(@NotNull Sign sign, @Nullable Team team, @NotNull Minigame minigame) {
        sign.setWaxed(true);

        this.spawnLocation = sign.getLocation().toBlockLocation();
        this.spawnData = spawnLocation.getBlock().getState();
        this.signText = sign.getSide(Side.FRONT).lines();
        this.team = team;
        this.minigame = minigame;
        this.ctfModule = CTFModule.getMinigameModule(minigame);
        this.respawnTime = Minigames.getPlugin().getConfig().getInt("multiplayer.ctf.flagrespawntime");

        // get the location the sign was attached to
        Block signBlock = sign.getBlock();
        if (Tag.WALL_SIGNS.isTagged(signBlock.getType())) {
            this.attachedToLocation = signBlock.getRelative(
                            ((Directional) sign.getBlockData()).getFacing().getOppositeFace()).
                    getLocation().toBlockLocation();
        } else if (Tag.STANDING_SIGNS.isTagged(signBlock.getType())) {
            this.attachedToLocation = signBlock.getRelative(BlockFace.DOWN).getLocation().toBlockLocation();
        } else { // is hanging sign and therefor not depending on a block
            attachedToLocation = null;
        }
    }

    public @NotNull Location getSpawnLocation() {
        return spawnLocation;
    }

    public @Nullable Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(final @Nullable Location newLocation) {
        this.currentLocation = newLocation;
    }

    public boolean isAtHome() {
        return atHome;
    }

    public void setAtHome(boolean atHome) {
        this.atHome = atHome;
    }

    public @Nullable Team getTeam() {
        return team;
    }

    /**
     * Sets a flag as a block in the world above or below the given location, so that is stands on the ground
     * @param location the location near where the flag should be placed
     * @return the location where the flag was placed or null if not possible
     */
    public @Nullable Location spawnFlag(final @NotNull Location location) {
        Location blockBelow = location.clone();
        blockBelow.setY(blockBelow.getBlockY() - 1);

        if (blockBelow.getBlock().getType() == Material.AIR) {
            while (blockBelow.getBlock().getType() == Material.AIR) {
                if (blockBelow.getY() > blockBelow.getWorld().getMinHeight()) {
                    blockBelow.setY(blockBelow.getY() - 1);
                } else {
                    return null;
                }
            }
        } else if (blockBelow.getBlock().getType() != Material.AIR) {
            while (blockBelow.getBlock().getType() != Material.AIR) {
                if (blockBelow.getY() < blockBelow.getWorld().getMaxHeight()) {
                    blockBelow.setY(blockBelow.getY() + 1);
                } else {
                    return null;
                }
            }
            blockBelow.setY(blockBelow.getY() - 1);
        }

        if (blockBelow.getBlock().getType() == Material.LAVA) {
            return null;
        }

        if (blockBelow.getBlock().getState() instanceof Container ||
                Tag.ALL_SIGNS.isTagged(blockBelow.getBlock().getType())) {
            blockBelow.setY(blockBelow.getY() + 1);
        }

        Location newLocation = blockBelow.clone();
        newLocation.setY(newLocation.getY() + 1);

        // Converting wall signs to normal signs, if necessary
        String standingSignName = spawnData.getType().toString()
                .replace("WALL_SIGN", "SIGN")
                .replace("WALL_HANGING_SIGN", "SIGN")
                .replace("HANGING_SIGN", "SIGN");
        Material standingSign = Material.getMaterial(standingSignName);

        newLocation.getBlock().setType(standingSign == null ? Material.OAK_SIGN : standingSign);
        Sign sign = (Sign) newLocation.getBlock().getState();

        originalBlockStateBelow = blockBelow.getBlock().getState();
        blockBelow.getBlock().setType(Material.BEDROCK);

        atHome = false;

        for (int i = 0; i < 4; i++) {
            sign.getSide(Side.FRONT).line(i, signText.get(i));
        }
        sign.update();
        setCurrentLocation(newLocation.clone());

        return newLocation;
    }

    public void removeFlag() {
        if (!atHome) {
            if (currentLocation != null) {
                Location blockBelow = currentLocation.clone();
                currentLocation.getBlock().setType(Material.AIR);

                blockBelow.setY(blockBelow.getY() - 1);
                blockBelow.getBlock().setType(originalBlockStateBelow.getType());
                originalBlockStateBelow.update();

                setCurrentLocation(null);
                stopTimer();
            }
        } else {
            spawnLocation.getBlock().setType(Material.AIR);
        }
    }

    public void respawnFlag() {
        removeFlag();
        spawnLocation.getBlock().setType(spawnData.getType());
        spawnData.update();
        setCurrentLocation(null);
        atHome = true;

        Sign sign = (Sign) spawnLocation.getBlock().getState();
        sign.setWaxed(true);

        for (int i = 0; i < 4; i++) {
            sign.getSide(Side.FRONT).line(i, signText.get(i));
        }
        sign.update();
    }

    public void stopTimer() {
        if (taskID != -1) {
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }

    public @NotNull Minigame getMinigame() {
        return minigame;
    }

    public void startReturnTimer() {
        final CTFFlag self = this;
        taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(Minigames.getPlugin(), () -> {
            final String locationID = MinigameUtils.createLocationID(currentLocation);

            if (ctfModule.hasDroppedFlag(locationID)) {
                ctfModule.removeDroppedFlag(locationID);
                String newID = MinigameUtils.createLocationID(spawnLocation);
                ctfModule.addDroppedFlag(newID, self);
            }
            respawnFlag();

            if (getTeam() != null) {
                MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.MINIGAME_FLAG_RETURNEDTEAM,
                        Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(getTeam().getDisplayName(), getTeam().getTextColor()))));
            } else {
                MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.MINIGAME_FLAG_RETURNEDNEUTRAL));
            }
            taskID = -1;
        }, respawnTime * 20L);
    }

    public void startCarrierParticleEffect(final @NotNull Player player) {
        cParticleID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Minigames.getPlugin(), () -> player.getWorld().playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 0), 15L, 15L);
    }

    public void stopCarrierParticleEffect() {
        if (cParticleID != -1) {
            Bukkit.getScheduler().cancelTask(cParticleID);
            cParticleID = -1;
        }
    }

    public @Nullable Location getAttachedToLocation() {
        return attachedToLocation;
    }

    @Contract(pure = true)
    public boolean isFlag (final @NotNull ItemStack item) {
        return item.getPersistentDataContainer().has(flagKey);
    }

    public @NotNull ItemStack getAsItem() {
        final Collection<ItemStack> drops = spawnData.getDrops(ItemStack.empty());

        if (!drops.isEmpty()) {
            final ItemStack stack = drops.iterator().next();

            if (stack.editMeta(it -> it.getPersistentDataContainer().set(flagKey, PersistentDataType.BOOLEAN, Boolean.TRUE))) {
                return stack;
            }
        }

        return ItemStack.empty();
    }
}
