package au.com.mineauz.minigames.objects;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.MessageManager;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.modules.CTFModule;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Flag of Capture the Flag.
 * Technical background for au.com.mineauz.minigames.signs.CTFFlagSign
 */
public class CTFFlag {
    private final @NotNull NamespacedKey flagKey = new NamespacedKey(Minigames.getPlugin(), "is_ctf_flag");
    private final @NotNull BlockState spawnData;
    private final @NotNull List<@NotNull Component> signText;
    private final @NotNull Minigame minigame;
    private final @NotNull CTFModule ctfModule;
    private final @NotNull Location spawnLocation;
    private final @Nullable Location spawnAttachedToLocation;
    private final @Nullable Team team;
    private @Nullable Location currentLocation = null;
    private @Nullable Location currentAttachtedToLocation = null;
    private @Nullable BlockState currentAttachtedToOriginalBlockState = null;
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
            this.spawnAttachedToLocation = signBlock.getRelative(
                            ((Directional) sign.getBlockData()).getFacing().getOppositeFace()).
                    getLocation().toBlockLocation();
        } else if (Tag.STANDING_SIGNS.isTagged(signBlock.getType())) {
            this.spawnAttachedToLocation = signBlock.getRelative(BlockFace.DOWN).getLocation().toBlockLocation();
        } else { // is hanging sign and therefor not depending on a block
            spawnAttachedToLocation = null;
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
     * If no (horizontal) blockface was given, sets a flag as a block in the world above or below the given location, so that is stands on the ground
     * Elsewise it will set the flag as a wall attachment
     * @param location the location near where the flag should be placed
     * @return the location where the flag was placed or null if not possible
     */
    public @Nullable Location spawnFlag(final @NotNull Location location, final @Nullable BlockFace blockFace) {
        switch (blockFace) {
            case NORTH,
                 EAST,
                 SOUTH,
                 WEST -> {

                Location nextTo = location.clone();
                final Vector directionInFront = blockFace.getDirection();
                location.add(directionInFront);

                if (!nextTo.getBlock().isSolid() || !location.getBlock().isEmpty()) {
                    return null;
                }

                if (nextTo.getBlock().getState() instanceof Container ||
                    Tag.ALL_SIGNS.isTagged(nextTo.getBlock().getType())) {
                    nextTo.add(directionInFront);
                }

                final Location newLocation = nextTo.clone();
                newLocation.add(directionInFront);

                // Converting wall signs to wall signs, if necessary
                String standingSignName = spawnData.getType().toString()
                    .replace("WALL_SIGN", "")
                    .replace("WALL_HANGING_SIGN", "")
                    .replace("HANGING_SIGN", "")
                    .replace("SIGN", "") + "WALL_SIGN";
                Material wallSignType = Material.getMaterial(standingSignName);

                newLocation.getBlock().setType(wallSignType == null ? Material.OAK_WALL_SIGN : wallSignType);
                final WallSign wallSign = (WallSign) newLocation.getBlock().getBlockData();
                wallSign.setFacing(blockFace);
                newLocation.getBlock().setBlockData(wallSign, false);
                Sign sign = (Sign) newLocation.getBlock().getState();

                currentAttachtedToOriginalBlockState = nextTo.getBlock().getState();
                currentAttachtedToLocation = nextTo.clone();
                nextTo.getBlock().setType(Material.BEDROCK);

                atHome = false;

                for (int i = 0; i < 4; i++) {
                    sign.getSide(Side.FRONT).line(i, signText.get(i));
                }
                sign.update();
                setCurrentLocation(newLocation.clone());

                return newLocation;
            }
            case null, default -> {
                Location blockBelow = location.clone();
                blockBelow.setY(blockBelow.getBlockY() - 1);

                if (blockBelow.getBlock().isEmpty()) {
                    while (blockBelow.getBlock().isEmpty()) {
                        if (blockBelow.getY() > blockBelow.getWorld().getMinHeight()) {
                            blockBelow.setY(blockBelow.getY() - 1);
                        } else {
                            return null;
                        }
                    }
                } else if (!blockBelow.getBlock().isEmpty()) {
                    while (!blockBelow.getBlock().isEmpty()) {
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

                currentAttachtedToOriginalBlockState = blockBelow.getBlock().getState();
                currentAttachtedToLocation = blockBelow.clone();
                blockBelow.getBlock().setType(Material.BEDROCK);

                atHome = false;

                for (int i = 0; i < 4; i++) {
                    sign.getSide(Side.FRONT).line(i, signText.get(i));
                }
                sign.update();
                setCurrentLocation(newLocation.clone());

                return newLocation;
            }
        }
    }

    public void removeFlag() {
        if (atHome) {
            spawnLocation.getBlock().setType(Material.AIR);
        } else {
            if (currentLocation != null) {
                currentLocation.getBlock().setType(Material.AIR);

                currentAttachtedToLocation.getBlock().setType(currentAttachtedToOriginalBlockState.getType());
                currentAttachtedToOriginalBlockState.update();

                setCurrentLocation(null);
                stopTimer();
            }
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
            //TODO: Build this again with broadcasts.
            for (MinigamePlayer pl : minigame.getPlayers()) {
                if (getTeam() != null)
                    pl.sendInfoMessage(
                            MessageManager.getMinigamesMessage("minigame.flag.returnedTeam", getTeam().getChatColor() + getTeam().getDisplayName() + ChatColor.WHITE));
                else
                    pl.sendInfoMessage(MinigameUtils.getLang("minigame.flag.returnedNeutral"));
            }
            taskID = -1;
        }, respawnTime * 20L);
    }

    public void startCarrierParticleEffect(final @NotNull Player player) {
        cParticleID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Minigames.getPlugin(), () ->
            player.getWorld().playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 0), 15L, 15L
        );
    }

    public void stopCarrierParticleEffect() {
        if (cParticleID != -1) {
            Bukkit.getScheduler().cancelTask(cParticleID);
            cParticleID = -1;
        }
    }

    public @Nullable Location getSpawnAttachedToLocation() {
        return spawnAttachedToLocation;
    }

    @Contract(pure = true)
    public boolean isFlag (final @NotNull ItemStack item) {
        return item.getPersistentDataContainer().has(flagKey);
    }

    public @NotNull ItemStack getAsItem() {
        final Collection<ItemStack> drops = spawnData.getDrops(ItemStack.empty());

        if (!drops.isEmpty()) {
            final ItemStack stack = drops.iterator().next();

            if (stack.editMeta(itemMeta -> {
                itemMeta.getPersistentDataContainer().set(flagKey, PersistentDataType.BOOLEAN, Boolean.TRUE);
                itemMeta.customName(Component.text().append(signText.get(2).append()).append(Component.text(" Flag")).build());
                itemMeta.lore(List.of(Component.text("Bring this flag back home!"), Component.text("You can drop the flag by placing it.")));
            })) {
                return stack;
            }
        }

        return ItemStack.empty();
    }
}
