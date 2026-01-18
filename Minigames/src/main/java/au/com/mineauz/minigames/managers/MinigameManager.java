package au.com.mineauz.minigames.managers;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.MinigameSave;
import au.com.mineauz.minigames.config.RewardsFlag;
import au.com.mineauz.minigames.events.StartGlobalMinigameEvent;
import au.com.mineauz.minigames.events.StopGlobalMinigameEvent;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.gametypes.MinigameTypeBase;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.MinigameState;
import au.com.mineauz.minigames.minigame.modules.MgModules;
import au.com.mineauz.minigames.minigame.modules.ModuleFactory;
import au.com.mineauz.minigames.minigame.modules.ResourcePackModule;
import au.com.mineauz.minigames.minigame.modules.TeamsModule;
import au.com.mineauz.minigames.minigame.reward.Rewards;
import au.com.mineauz.minigames.objects.MgRegion;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.ResourcePack;
import au.com.mineauz.minigames.recorder.RecorderData;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class MinigameManager {
    private final Minigames plugin = Minigames.getPlugin();
    private final @NotNull Map<@NotNull String, @NotNull Minigame> minigames = new HashMap<>();
    private final @NotNull Map<@NotNull MinigameType, @NotNull MinigameTypeBase> minigameTypes = new HashMap<>();
    private final @NotNull Map<@NotNull String, @NotNull RewardsFlag> rewardSigns = new HashMap<>();
    private final @NotNull Map<@NotNull Minigame, @NotNull List<@NotNull String>> claimedScoreSignsRed = new HashMap<>();
    private final @NotNull Map<@NotNull Minigame, @NotNull List<@NotNull String>> claimedScoreSignsBlue = new HashMap<>();
    private final @NotNull Map<@NotNull Key, @NotNull ModuleFactory> modules = new HashMap<>();
    private @Nullable MinigameSave rewardSignsSave;

    public MinigameManager() {
        for (ModuleFactory moduleFactory : MgModules.values()) {
            addModule(moduleFactory);
        }
    }

    public @NotNull Collection<@NotNull ModuleFactory> getModules() {
        return this.modules.values();
    }

    public void addModule(final @NotNull ModuleFactory moduleFactory) {
        this.modules.put(moduleFactory.getKey(), moduleFactory);
    }

    public void removeModule(final @NotNull Key moduleKey) {
        for (final Minigame mg : this.minigames.values()) {
            mg.removeModule(moduleKey);
        }

        this.modules.remove(moduleKey);
    }

    public void startGlobalMinigame(final @NotNull Minigame minigame, final @Nullable MinigamePlayer caller) {
        final boolean canStart = minigame.getMechanic().checkCanStart(minigame, caller);
        if (minigame.getType() == MinigameType.GLOBAL &&
            minigame.getMechanic().validTypes().contains(MinigameType.GLOBAL) &&
            canStart) {
            final StartGlobalMinigameEvent ev = new StartGlobalMinigameEvent(minigame, caller);
            Bukkit.getPluginManager().callEvent(ev);

            minigame.getMechanic().startMinigame(minigame, caller);
            final ResourcePackModule module = ResourcePackModule.getMinigameModule(minigame);
            if (module != null) {
                if (module.isEnabled()) {
                    final ResourcePack pack = plugin.getResourceManager().getResourcePack(module.getResourcePackName());
                    if (pack.isValid()) {
                        for (final MinigamePlayer player : minigame.getPlayers()) {
                            player.applyResourcePack(pack);
                        }
                    }
                }
            }
            minigame.setEnabled(true);
            minigame.saveMinigame();
        } else if (!minigame.getMechanic().validTypes().contains(MinigameType.GLOBAL)) {
            if (caller == null) {
                plugin.getComponentLogger().warn("The Minigame Type \"" + MinigameType.GLOBAL.getName() + "\" cannot use the selected Mechanic \"" + minigame.getMechanicName() + "\"!");
            } else {
                MinigameMessageManager.sendMgMessage(caller, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_INVALIDMECHANIC,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MECHANIC.getKey(), minigame.getMechanicName()),
                    Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), MinigameType.GLOBAL.getName()));
            }
        } else if (!canStart) {
            if (caller == null) {
                plugin.getComponentLogger().warn("The Game Mechanic \"" + minigame.getMechanicName() + "\" has failed to initiate!");
            } else {
                MinigameMessageManager.sendMgMessage(caller, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_MECHANICSTARTFAIL,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MECHANIC.getKey(), minigame.getMechanicName()));
            }
        }
    }

    public void stopGlobalMinigame(final @NotNull Minigame minigame, final @NotNull Audience caller) {
        if (minigame.getType() == MinigameType.GLOBAL) {
            final StopGlobalMinigameEvent ev = new StopGlobalMinigameEvent(minigame, caller);
            Bukkit.getPluginManager().callEvent(ev);

            minigame.getMechanic().stopMinigame(minigame);

            minigame.setEnabled(false);
            final ResourcePackModule module = ResourcePackModule.getMinigameModule(minigame);
            if (module != null) {
                if (module.isEnabled()) {
                    final ResourcePack pack = plugin.getResourceManager().getResourcePack("empty");
                    if (pack.isValid()) {
                        for (final MinigamePlayer player : minigame.getPlayers()) {
                            player.applyResourcePack(pack);
                        }
                    }
                }
            }
            minigame.saveMinigame();
        }
    }

    public void addMinigame(final @NotNull Minigame game) {
        this.minigames.put(game.getName(), game);
        if (plugin.includesPlaceholderAPI()) {
            plugin.getPlaceHolderManager().addGameIdentifiers(game);
        }
    }

    public @Nullable Minigame getMinigame(final @NotNull String minigame) {
        if (this.minigames.containsKey(minigame)) {
            return this.minigames.get(minigame);
        }

        for (final Map.Entry<String, Minigame> stringMinigameEntry : this.minigames.entrySet()) {
            if (minigame.equalsIgnoreCase(stringMinigameEntry.getKey()) || stringMinigameEntry.getKey().startsWith(minigame)) {
                return stringMinigameEntry.getValue();
            }
        }

        return null;
    }

    public @NotNull Map<@NotNull String, @NotNull Minigame> getAllMinigames() {
        return this.minigames;
    }

    public boolean hasMinigame(final @NotNull String minigame) {
        boolean hasmg = this.minigames.containsKey(minigame);
        if (!hasmg) {
            for (final String mg : this.minigames.keySet()) {
                if (mg.equalsIgnoreCase(minigame) || mg.toLowerCase().startsWith(minigame.toLowerCase())) {
                    hasmg = true;
                    break;
                }
            }
        }
        return hasmg;
    }

    public void removeMinigame(final @NotNull String minigame) {
        this.minigames.remove(minigame);
    }

    public void addRegenDataToRecorder(final @NotNull Minigame minigame) {
        if (minigame.hasRegenArea() && !minigame.getRecorderData().hasCreatedRegenBlocks()) {
            final RecorderData recorderData = minigame.getRecorderData();

            for (MgRegion region : recorderData.getMinigame().getRegenRegions()) {
                for (int x = (int) region.getMinX(); x <= region.getMaxX(); x++) {
                    for (int y = (int) region.getMinY(); y <= region.getMaxY(); y++) {
                        for (int z = (int) region.getMinZ(); z <= region.getMaxZ(); z++) {
                            //add block
                            recorderData.addBlock(region.getWorld().getBlockAt(x, y, z), null);
                        }
                    }
                }
            }

            recorderData.setCreatedRegenBlocks(true);
            MinigameMessageManager.debugMessage("Block Regen Data has been created for " + minigame.getName());
        }
    }

    public void addMinigameType(final @NotNull MinigameTypeBase minigameType) {
        this.minigameTypes.put(minigameType.getType(), minigameType);
        MinigameMessageManager.debugMessage("Loaded " + minigameType.getType().getName() + " minigame type."); //DEBUG
    }

    public @Nullable MinigameTypeBase minigameType(final @NotNull MinigameType name) {
        if (this.minigameTypes.containsKey(name)) {
            return this.minigameTypes.get(name);
        }
        return null;
    }

    public @NotNull Set<@NotNull MinigameType> getMinigameTypes() {
        return this.minigameTypes.keySet();
    }

    public void addRewardSign(final @NotNull Location loc) {
        final RewardsFlag flag = new RewardsFlag(MinigameUtils.createBlockLocationID(loc), new Rewards());
        this.rewardSigns.put(MinigameUtils.createBlockLocationID(loc), flag);
    }

    public @Nullable Rewards getRewardsRewardSign(final @NotNull Location loc) {
        return this.rewardSigns.get(MinigameUtils.createBlockLocationID(loc)).getFlag();
    }

    public boolean hasRewardSign(final @NotNull Location loc) {
        return this.rewardSigns.containsKey(MinigameUtils.createBlockLocationID(loc));
    }

    public void removeRewardSign(final @NotNull Location loc) throws IOException {
        final String locid = MinigameUtils.createBlockLocationID(loc);
        if (this.rewardSigns.containsKey(locid)) {
            this.rewardSigns.remove(locid);
            if (this.rewardSignsSave == null) {
                this.loadRewardSignsFile();
            }
            this.rewardSignsSave.getConfigRoot().removeChild(locid);
            this.rewardSignsSave.saveConfig();
            this.rewardSignsSave = null;
        }
    }

    public void saveRewardSigns() throws IOException {
        for (final String rew : this.rewardSigns.keySet()) {
            this.saveRewardSign(rew, false);
        }
        if (this.rewardSignsSave != null) {
            this.rewardSignsSave.saveConfig();
            this.rewardSignsSave = null;
        }
    }

    public void saveRewardSign(final @NotNull String id, final boolean saveFile) throws IOException {
        final @NotNull RewardsFlag reward = this.rewardSigns.get(id);
        if (this.rewardSignsSave == null) {
            this.loadRewardSignsFile();
        }
        final @NotNull CommentedConfigurationNode cfg = this.rewardSignsSave.getConfigRoot();
        reward.saveValue(cfg);
        if (saveFile) {
            this.rewardSignsSave.saveConfig();
            this.rewardSignsSave = null;
        }
    }

    public void loadRewardSignsFile() {
        this.rewardSignsSave = MinigameSave.forGlobalData(Path.of("rewardSigns"));
    }

    public void loadRewardSigns() throws ConfigurateException {
        if (this.rewardSignsSave == null) {
            this.loadRewardSignsFile();
        }
        final @NotNull CommentedConfigurationNode cfg = this.rewardSignsSave.getConfigRoot();
        for (final @NotNull Map.Entry<@NotNull Object, @NotNull CommentedConfigurationNode> entry : cfg.childrenMap().entrySet()) {
            final @NotNull String id = entry.getKey().toString();
            final @NotNull RewardsFlag rew = new RewardsFlag(id, new Rewards());
            rew.loadValue(cfg); // note: the entry.value will get fetched by the flag. Looks stupid here but this just stays in line with the other config flags caring about their own nodes via their names

            this.rewardSigns.put(id, rew);
        }
    }

    public boolean hasClaimedScore(final @NotNull Minigame mg, final @NotNull Location loc, final int team) {
        final String id = MinigameUtils.createBlockLocationID(loc);
        if (team == 0) {
            return this.claimedScoreSignsRed.containsKey(mg) && this.claimedScoreSignsRed.get(mg).contains(id);
        } else {
            return this.claimedScoreSignsBlue.containsKey(mg) && this.claimedScoreSignsBlue.get(mg).contains(id);
        }
    }

    public void addClaimedScore(final @NotNull Minigame mg, final @NotNull Location loc, final int team) {
        final String id = MinigameUtils.createBlockLocationID(loc);
        if (team == 0) {
            if (!this.claimedScoreSignsRed.containsKey(mg)) {
                this.claimedScoreSignsRed.put(mg, new ArrayList<>());
            }
            this.claimedScoreSignsRed.get(mg).add(id);
        } else {
            if (!this.claimedScoreSignsBlue.containsKey(mg)) {
                this.claimedScoreSignsBlue.put(mg, new ArrayList<>());
            }
            this.claimedScoreSignsBlue.get(mg).add(id);
        }
    }

    public void clearClaimedScore(final @NotNull Minigame mg) {
        this.claimedScoreSignsRed.remove(mg);
        this.claimedScoreSignsBlue.remove(mg);
    }

    public boolean minigameMechanicCheck(final @NotNull Minigame minigame, final @NotNull MinigamePlayer mgPlayer) {
        return minigame.getMechanic() == null || minigame.getMechanic().checkCanStart(minigame, mgPlayer);
    }

    public boolean minigameStartStateCheck(final @NotNull Minigame minigame, final @NotNull MinigamePlayer mgPlayer) {
        if (!minigame.isEnabled() && !mgPlayer.getPlayer().hasPermission("minigame.join.disabled")) { //todo Permission Manager
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOTENABLED);
            return false;
        } else if (!this.minigameMechanicCheck(minigame, mgPlayer)) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_MECHANICSTARTFAIL,
                Placeholder.unparsed(MinigamePlaceHolderKey.MECHANIC.getKey(), minigame.getMechanicName()));
            return false;
        } else if (minigame.getState() == MinigameState.REGENERATING) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_REGENERATING);
            return false;
        } else if (minigame.getState() == MinigameState.STARTED && !minigame.canLateJoin()) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_STARTED);
            return false;
        }
        return true;
    }

    public boolean minigameStartSetupCheck(final @NotNull Minigame minigame, final @NotNull MinigamePlayer mgPlayer) {
        if (minigame.getEndLocation() == null) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOEND);
            return false;
        } else if (minigame.getQuitLocation() == null) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOQUITLOC);
            return false;
        } else if (this.minigameType(minigame.getType()).cannotStart(minigame, mgPlayer)) { //type specific reasons we cannot start.
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_INVALIDTYPE);
            return false;
        } else if (!minigame.getMechanic().validTypes().contains(minigame.getType())) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_INVALIDTYPE);
            return false;
        } else if (minigame.getStartLocations().isEmpty() ||
            minigame.isTeamGame() && !TeamsModule.getMinigameModule(minigame).hasTeamStartLocations()) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOSTARTLOC);
            return false;
        }
        return true;
    }

    public boolean teleportPlayerOnJoin(final @NotNull Minigame minigame, final @NotNull MinigamePlayer mgPlayer) {
        if (this.minigameType(minigame.getType()) == null) {
            plugin.getComponentLogger().warn("The Minigame \"" + minigame.getName() + "\" failed the start-up checks for its Type");
        }
        return this.minigameType(minigame.getType()).teleportOnJoin(mgPlayer, minigame);
    }
}
