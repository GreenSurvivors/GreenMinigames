package au.com.mineauz.minigames.managers.language.langkeys;

import org.jetbrains.annotations.NotNull;

public enum MgCommandLangKey implements LangKey {
    COMMAND_BACKEND_DESCRIPTION("command.backend.description"),
    COMMAND_DIVIDER_LARGE("command.divider.large"),
    COMMAND_DIVIDER_SMALL("command.divider.small"),
    COMMAND_ERROR_NOTAPLAYER("command.error.notAPlayer"),
    COMMAND_ERROR_NOTBOOL("command.error.notBool"),
    COMMAND_ERROR_NOTGAMEMECHANIC("command.set.error.notGameMechanic"),
    COMMAND_ERROR_NOTMATERIAL("command.error.notMaterial"),
    COMMAND_ERROR_NOTNUMBER("command.error.notNumber"),
    COMMAND_ERROR_NOTTEAM("command.error.notTeam"),
    COMMAND_ERROR_NOTTIME("command.error.notTime"),
    COMMAND_ERROR_RANGE("command.error.range"),
    COMMAND_ERROR_UNKNOWN_PARAM("command.error.unknown.parameter"),
    COMMAND_INFO_DESCRIPTION("command.info.description"),
    COMMAND_INFO_OUTPUT_DESCRIPTION("command.info.output.description"),
    COMMAND_INFO_OUTPUT_GAMETYPE("command.info.output.gameType"),
    COMMAND_INFO_OUTPUT_HEADER("command.info.output.header"),
    COMMAND_INFO_OUTPUT_NOMINIGAME("command.info.noMinigame"),
    COMMAND_INFO_OUTPUT_NOPLAYER("command.info.output.noPlayer"),
    COMMAND_INFO_OUTPUT_PLAYERDATA("command.info.output.playerData"),
    COMMAND_INFO_OUTPUT_PLAYERHEADER("command.info.output.playerHeader"),
    COMMAND_INFO_OUTPUT_TEAMDATA("command.info.output.teamData"),
    COMMAND_INFO_OUTPUT_TIMER("command.info.output.timer"),
    COMMAND_SET_ALLOWENDERPERLS_DESCRIPTION("command.set.allowEnderPerls.description"),
    COMMAND_SET_ALLOWENDERPERLS_SUCCESS("command.set.allowEnderPerls.success"),
    COMMAND_SET_ALLOWENDERPERLS_USAGE("command.set.allowEnderPerls.usage"),
    COMMAND_SET_BLOCKBREAK_DESCRIPTION("command.set.blockBreak.description"),
    COMMAND_SET_BLOCKBREAK_SUCCESS("command.set.blockBreak.success"),
    COMMAND_SET_BLOCKBREAK_USAGE("command.set.blockBreak.usage"),
    COMMAND_SET_BLOCKPLACE_DESCRIPTION("command.set.blockPlace.description"),
    COMMAND_SET_BLOCKPLACE_SUCCESS("command.set.blockPlace.success"),
    COMMAND_SET_BLOCKPLACE_USAGE("command.set.blockPlace.usage"),
    COMMAND_SET_BLOCKSDROP_DESCRIPTION("command.set.blocksDrop.description"),
    COMMAND_SET_BLOCKSDROP_SUCCESS("command.set.blocksDrop.success"),
    COMMAND_SET_BLOCKSDROP_USAGE("command.set.blocksDrop.usage"),
    COMMAND_SET_DEFAULTWINNER_DESCRIPTION("command.set.defaultWinner.description"),
    COMMAND_SET_DEFAULTWINNER_SUCCESS("command.set.defaultWinner.success"),
    COMMAND_SET_DEFAULTWINNER_USAGE("command.set.defaultWinner.usage"),
    COMMAND_SET_DESCRIPTION("command.set.description"),
    COMMAND_SET_DISPLAYNAME_DESCRIPTION("command.set.displayName.description"),
    COMMAND_SET_DISPLAYNAME_REMOVED("command.set.displayName.removed"),
    COMMAND_SET_DISPLAYNAME_SUCCESS("command.set.displayName.success"),
    COMMAND_SET_DISPLAYNAME_USAGE("command.set.displayName.usage"),
    COMMAND_SET_DISPLAYSCOREBOARD_DESCRIPTION("command.set.displayScoreboard.description"),
    COMMAND_SET_DISPLAYSCOREBOARD_REMOVED("command.set.displayScoreboard.removed"),
    COMMAND_SET_DISPLAYSCOREBOARD_SUCCESS("command.set.displayScoreboard.success"),
    COMMAND_SET_DISPLAYSCOREBOARD_USAGE("command.set.displayScoreboard.usage"),
    COMMAND_SET_ENABLED_DESCRIPTION("command.set.enabled.description"),
    COMMAND_SET_ENABLED_SUCCESS("command.set.enabled.success"),
    COMMAND_SET_ENABLED_USAGE("command.set.enabled.usage"),
    COMMAND_SET_END_DESCRIPTION("command.set.end.description"),
    COMMAND_SET_END_SUCCESS("command.set.end.success"),
    COMMAND_SET_END_USAGE("command.set.end.usage"),
    COMMAND_SET_FLAG_ADD("command.set.flag.add"),
    COMMAND_SET_FLAG_CLEAR("command.set.flags.clear"),
    COMMAND_SET_FLAG_DESCRIPTION("command.set.flag.description"),
    COMMAND_SET_FLAG_ERROR_NOFLAG("command.set.flag.error.noFlag"),
    COMMAND_SET_FLAG_LIST_HEADER("command.set.flag.list.header"),
    COMMAND_SET_FLAG_NOFLAGS("command.set.flag.noFlags"),
    COMMAND_SET_FLAG_REMOVE("command.set.flag.remove"),
    COMMAND_SET_FLAG_USAGE("command.set.flag.usage"),
    COMMAND_SET_FLIGHT_ALLOWED("command.set.flight.allowed"),
    COMMAND_SET_FLIGHT_DESCRIPTION("command.set.flight.description"),
    COMMAND_SET_FLIGHT_START("command.set.flight.start"),
    COMMAND_SET_FLIGHT_USAGE("command.set.flight.usage"),
    COMMAND_SET_FLOORDEGEN_CLEAR("command.set.floorDegen.clear"),
    COMMAND_SET_FLOORDEGEN_CREATE("command.set.floorDegen.create"),
    COMMAND_SET_FLOORDEGEN_DESCRIPTION("command.set.floorDegen.description"),
    COMMAND_SET_FLOORDEGEN_ERROR_NOTYPE("command.set.floorDegen.error.noType"),
    COMMAND_SET_FLOORDEGEN_TIME("command.set.floorDegen.time"),
    COMMAND_SET_FLOORDEGEN_TYPE("command.set.floorDegen.type"),
    COMMAND_SET_FLOORDEGEN_USAGE("command.set.floorDegen.usage"),
    COMMAND_SET_GAMEMECHANIC_DESCRIPTION("command.set.gameMechanic.description"),
    COMMAND_SET_GAMEMECHANIC_SUCCESS("command.set.gameMechanic.success"),
    COMMAND_SET_GAMEMECHANIC_USAGE("command.set.gameMechanic.usage"),
    COMMAND_SET_GAMEMODE_DESCRIPTION("command.set.gamemode.description"),
    COMMAND_SET_GAMEMODE_SUCCESS("command.set.gamemode.success"),
    COMMAND_SET_GAMEMODE_USAGE("command.set.gamemode.usage"),
    COMMAND_SET_GAMEOVER_DESCRIPTION("command.set.gameOver.description"),
    COMMAND_SET_GAMEOVER_HUMILIATION("command.set.gameOver.humiliation"),
    COMMAND_SET_GAMEOVER_INTERACTION("command.set.gameOver.interaction"),
    COMMAND_SET_GAMEOVER_INVINCIBLE("command.set.gameOver.invincible"),
    COMMAND_SET_GAMEOVER_TIME("command.set.gameOver.time"),
    COMMAND_SET_GAMEOVER_USAGE("command.set.gameOver.usage"),
    COMMAND_SET_GAMETYPENAME_DESCRIPTION("command.set.gameTypeName.description"),
    COMMAND_SET_GAMETYPENAME_REMOVE("command.set.gameTypeName.remove"),
    COMMAND_SET_GAMETYPENAME_SUCCESS("command.set.gameTypeName.success"),
    COMMAND_SET_GAMETYPENAME_USAGE("command.set.gameTypeName.usage"),
    COMMAND_SET_HEADER("command.set.header"),
    COMMAND_SET_HINTDELAY_DESCRIPTION("command.set.hintDelay.description"),
    COMMAND_SET_HINTDELAY_SUCCESS("command.set.hintDelay.success"),
    COMMAND_SET_HINTDELAY_USAGE("command.set.hintDelay.usage"),
    COMMAND_SET_INFECTEDPERCENT_DESCRIPTION("command.set.infectedPercent.description"),
    COMMAND_SET_INFECTEDPERCENT_SUCCESS("command.set.infectedPercent.success"),
    COMMAND_SET_INFECTEDPERCENT_USAGE("command.set.infectedPercent.usage"),
    COMMAND_SET_INFECTEDTEAM_DESCRIPTION("command.set.infectedTeam.description"),
    COMMAND_SET_INFECTEDTEAM_SUCCESS("command.set.infectedTeam.success"),
    COMMAND_SET_INFECTEDTEAM_USAGE("command.set.infectedTeam.usage"),
    COMMAND_SET_ITEMSDROP_DEATH("command.set.itemsDrop.death"),
    COMMAND_SET_ITEMSDROP_DESCRIPTION("command.set.itemsDrop.description"),
    COMMAND_SET_ITEMSDROP_DROP("command.set.itemsDrop.drop"),
    COMMAND_SET_ITEMSDROP_USAGE("command.set.itemsDrop.usage"),
    COMMAND_SET_ITEMSPICKUP_DESCRIPTION("command.set.itemsPickup.description"),
    COMMAND_SET_ITEMSPICKUP_SUCCESS("command.set.itemsPickup.success"),
    COMMAND_SET_ITEMSPICKUP_USAGE("command.set.itemsPickup.usage"),
    COMMAND_SET_LATEJOIN_DESCRIPTION("command.set.lateJoin.description"),
    COMMAND_SET_LATEJOIN_SUCCESS("command.set.lateJoin.success"),
    COMMAND_SET_LATEJOIN_USAGE("command.set.lateJoin.usage"),
    COMMAND_SET_LIVES_DESCRIPTION("command.set.lives.description"),
    COMMAND_SET_LIVES_SUCCESS("command.set.lives.success"),
    COMMAND_SET_LIVES_USAGE("command.set.lives.usage"),
    COMMAND_SET_LOADOUT_DESCRIPTION("command.set.loadout.description"),
    COMMAND_SET_LOADOUT_USAGE("command.set.loadout.usage"),
    COMMAND_SET_LOBBY_CANINTERACT_PLAYERWAIT("command.set.lobby.canInteract.playerWait"),
    COMMAND_SET_LOBBY_CANINTERACT_STARTWAIT("command.set.lobby.canInteract.startWait"),
    COMMAND_SET_LOBBY_CANMOVE_PLAYERWAIT("command.set.lobby.canMove.playerWait"),
    COMMAND_SET_LOBBY_CANMOVE_START("command.set.lobby.canMove.start"),
    COMMAND_SET_LOBBY_DESCRIPTION("command.set.lobby.description"),
    COMMAND_SET_LOBBY_LOCATION("command.set.lobby.location"),
    COMMAND_SET_LOBBY_PLAYERWAIT_SUCCESS("command.set.lobby.playerWait.success"),
    COMMAND_SET_LOBBY_TELEPORT_PLAYERWAIT("command.set.lobby.teleport.playerWait"),
    COMMAND_SET_LOBBY_TELEPORT_STARTWAIT("command.set.lobby.teleport.startWait"),
    COMMAND_SET_LOBBY_USAGE("command.set.lobby.usage"),
    COMMAND_SET_LOCATION_DESCRIPTION("command.set.location.description"),
    COMMAND_SET_LOCATION_SUCCESS("command.set.location.success"),
    COMMAND_SET_LOCATION_USAGE("command.set.location.usage"),
    COMMAND_SET_MAXHEIGHT_DESCRIPTION("command.set.maxHeight.description"),
    COMMAND_SET_MAXHEIGHT_SUCCESS("command.set.maxHeight.success"),
    COMMAND_SET_MAXHEIGHT_USAGE("command.set.maxHeight.usage"),
    COMMAND_SET_MAXPLAYERS_DESCRIPTION("command.set.maxPlayers.description"),
    COMMAND_SET_MAXPLAYERS_SUCCESS("command.set.maxPlayers.success"),
    COMMAND_SET_MAXPLAYERS_USAGE("command.set.maxPlayers.usage"),
    COMMAND_SET_MAXRADIUS_DESCRIPTION("command.set.maxRadius.description"),
    COMMAND_SET_MAXRADIUS_SUCCESS("command.set.maxRadius.success"),
    COMMAND_SET_MAXRADIUS_USAGE("command.set.maxRadius.usage"),
    COMMAND_SET_MAXSCORE_DESCRIPTION("command.set.maxScore.description"),
    COMMAND_SET_MAXSCORE_SUCCESS("command.set.maxScore.success"),
    COMMAND_SET_MAXSCORE_USAGE("command.set.maxScore.usage"),
    COMMAND_SET_MAXTREASURE_DESCRIPTION("command.set.maxTreasure.description"),
    COMMAND_SET_MAXTREASURE_SUCCESS("command.set.maxTreasure.success"),
    COMMAND_SET_MAXTREASURE_USAGE("command.set.maxTreasure.usage"),
    COMMAND_SET_MINPLAYERS_DESCRIPTION("command.set.minPlayers.description"),
    COMMAND_SET_MINPLAYERS_SUCCESS("command.set.minPlayers.success"),
    COMMAND_SET_MINPLAYERS_USAGE("command.set.minPlayers.usage"),
    COMMAND_SET_MINSCORE_DESCRIPTION("command.set.minScore.description"),
    COMMAND_SET_MINSCORE_SUCCESS("command.set.minScore.success"),
    COMMAND_SET_MINSCORE_USAGE("command.set.minScore.usage"),
    COMMAND_SET_MINTREASURE_DESCRIPTION("command.set.minTreasure.description"),
    COMMAND_SET_MINTREASURE_SUCCESS("command.set.minTreasure.success"),
    COMMAND_SET_MINTREASURE_USAGE("command.set.minTreasure.usage"),
    COMMAND_SET_MULTIPLYCHKPNTS_DESCRITON("command.set.multiplayerCheckpoints.description"),
    COMMAND_SET_MULTIPLYCHKPNTS_SUCCESS("command.set.multiPlyChkPnts.success"),
    COMMAND_SET_MULTIPLYCHKPNTS_USAGE("command.set.multiplayerCheckpoints.usage"),
    COMMAND_SET_OBJECTIVE_DESCRIPTION("command.set.objective.description"),
    COMMAND_SET_OBJECTIVE_REMOVE("command.set.objective.remove"),
    COMMAND_SET_OBJECTIVE_SUCCESS("command.set.objective.success"),
    COMMAND_SET_PAINTBALL_DAMAGE("command.set.paintball.damage"),
    COMMAND_SET_PAINTBALL_DESCRIPTION("command.set.paintball.description"),
    COMMAND_SET_PAINTBALL_MODE("command.set.paintball.mode"),
    COMMAND_SET_PAINTBALL_USAGE("command.set.paintball.usage"),
    COMMAND_SET_PRESET_DESCRIPTION("command.set.preset.description"),
    COMMAND_SET_PRESET_HEADER("command.set.preset.header"),
    COMMAND_SET_PRESET_USAGE("command.set.preset.usage"),
    COMMAND_SET_QUIT_DESCRIPTION("command.set.quit.description"),
    COMMAND_SET_QUIT_SUCCESS("command.set.quit.success"),
    COMMAND_SET_QUIT_USAGE("command.set.quit.usage"),
    COMMAND_SET_REGENAREA_DESCRIPTION("command.set.regenArea.description"),
    COMMAND_SET_REGENAREA_ERROR_NOTSELECTED("command.set.regenArea.error.notSelected"),
    COMMAND_SET_REGENAREA_LIST_HEADER("command.set.regenArea.listHeader"),
    COMMAND_SET_REGENAREA_USAGE("command.set-regenArea.usage"),
    COMMAND_SET_REGENDELAY_DESCRIPTION("command.set.regenDelay.description"),
    COMMAND_SET_REGENDELAY_SUCCESS("command.set.regenDelay.success"),
    COMMAND_SET_REGENDELAY_USAGE("command.set.regenDelay.usage"),
    COMMAND_SET_RESTARTDELAY_DESCRIPTION("command.set.restartDelay.description"),
    COMMAND_SET_RESTARTDELAY_SUCCESS("command.set.restartDelay.success"),
    COMMAND_SET_RESTARTDELAY_USAGE("command.set.restartDelay.usage"),
    COMMAND_SET_REWARD2_DESCRIPTION("command.set.reward2.description"),
    COMMAND_SET_REWARD2_ITEM_SUCCESS("command.set.reward2.item.success"),
    COMMAND_SET_REWARD2_MONEY_SUCCESS("command.set.reward2.money.success"),
    COMMAND_SET_REWARD2_USAGE("command.set.reward2.usage"),
    COMMAND_SET_REWARD_DESCRIPTION("command.set.reward.description"),
    COMMAND_SET_REWARD_ERROR_SCHEME("command.set.reward.error.scheme"),
    COMMAND_SET_REWARD_ITEM_ERROR_AIR("command.set.reward.item.error.air"),
    COMMAND_SET_REWARD_ITEM_ERROR_NOTRARITY("command.set.reward.item.error.notRarity"),
    COMMAND_SET_REWARD_ITEM_SUCCESS("command.set.reward.item.success"),
    COMMAND_SET_REWARD_MONEY_SUCCESS("command.set.reward.money.success"),
    COMMAND_SET_REWARD_USAGE("command.set.reward.usage"),
    COMMAND_SET_RNGCHEST_DESCRIPTION("command.set.randomizedChests.description"),
    COMMAND_SET_RNGCHEST_DISABLED("command.set.randomizedChests.disabled"),
    COMMAND_SET_RNGCHEST_ENABLED("command.set.randomizedChests.enabled"),
    COMMAND_SET_RNGCHEST_USAGE("command.set.randomizedChests.usage"),
    COMMAND_SET_SPECTATORSPAWN_DESCRIPTION("command.set.spectatorSpawn.description"),
    COMMAND_SET_SPECTATORSPAWN_SUCCESS("command.set.spectatorSpawn.success"),
    COMMAND_SET_SPECTATORSPAWN_USAGE("command.set.spectatorSpawn.usage"),
    COMMAND_SET_SPECTATOR_DESCRIPTION("command.set.spectator.description"),
    COMMAND_SET_SPECTATOR_SUCCESS("command.set.spectator.success"),
    COMMAND_SET_SPECTATOR_USAGE("command.set.spectator.usage"),
    COMMAND_SET_SPMAXPLAYERS_DESCRIPTION("command.set.spMaxPlayers.description"),
    COMMAND_SET_SPMAXPLAYERS_SUCCESS("command.set.spMaxPlayers.success"),
    COMMAND_SET_SPMAXPLAYERS_USAGE("command.set.spMaxPlayers.usage"),
    COMMAND_SET_STARTTIME_DESCRIPTION("command.set.startTime.description"),
    COMMAND_SET_STARTTIME_RESET("command.set.startTime.reset"),
    COMMAND_SET_STARTTIME_SUCCESS("command.set.startTime.success"),
    COMMAND_SET_STARTTIME_USAGE("command.set.startTime.usage"),
    COMMAND_SET_START_ADD_SINGLE("command.set.start.add.single"),
    COMMAND_SET_START_ADD_TEAM("command.set.start.add.team"),
    COMMAND_SET_START_CLEAR_SINGLE("command.set.start.clear.single"),
    COMMAND_SET_START_CLEAR_TEAM("command.set.start.clear.team"),
    COMMAND_SET_START_DESCRIPTION("command.set.start.description"),
    COMMAND_SET_START_USAGE("command.set.start.usage"),
    COMMAND_SET_STORECHECKPOINT_DESCRIPTION("command.set.storeCheckpoint.description"),
    COMMAND_SET_STORECHECKPOINT_SUCCESS("command.set.storeCheckpoint.success"),
    COMMAND_SET_STORECHECKPOINT_USAGE("command.set.storeCheckpoint.usage"),
    COMMAND_SET_SUBCOMMAND_ALIASES("command.set.subcommand.aliases"),
    COMMAND_SET_SUBCOMMAND_DESCRIPTION("command.set.subcommand.description"),
    COMMAND_SET_SUBCOMMAND_PARAMETERS("command.set.subcommand.parameters"),
    COMMAND_SET_SUBCOMMAND_USAGE("command.set.subcommand.usage"),
    COMMAND_SET_SURVIVORTEAM_DESCRIPTION("command.set.survivorTeam.description"),
    COMMAND_SET_SURVIVORTEAM_SUCCESS("command.set.survivorTeam.success"),
    COMMAND_SET_SURVIVORTEAM_USAGE("command.set.survivorTeam.usage"),
    COMMAND_SET_TEAM_ADD("command.set.team.add"),
    COMMAND_SET_TEAM_DESCRIPTION("command.set.team.description"),
    COMMAND_SET_TEAM_LIST("command.set.team.list"),
    COMMAND_SET_TEAM_MAXPLAYERS("command.set.team.maxPlayers"),
    COMMAND_SET_TEAM_REMOVE("command.set.team.remove"),
    COMMAND_SET_TEAM_RENAME("command.set.team.rename"),
    COMMAND_SET_TEAM_USAGE("command.set.team.usage"),
    COMMAND_SET_TIMER_DESCRIPTION("command.set.timer.description"),
    COMMAND_SET_TIMER_REMOVE("command.set.timer.remove"),
    COMMAND_SET_TIMER_SUCCESS("command.set.timer.success"),
    COMMAND_SET_TIMER_USAGE("command.set.timer.usage"),
    COMMAND_SET_TIMER_XPBAR_REMOVE("command.set.timer.xpBar.remove"),
    COMMAND_SET_TIMER_XPBAR_SUCCESS("command.set.timer.xpBar.success"),
    COMMAND_SET_TYPE_DESCRIPTION("command.set.type.description"),
    COMMAND_SET_TYPE_ERROR_NOTTYPE("command.set.type.error.notType"),
    COMMAND_SET_TYPE_SUCCESS("command.set.type.success"),
    COMMAND_SET_TYPE_USAGE("command.set.type.usage"),
    COMMAND_SET_UNLIMITEDAMMO_DESCRIPTION("command.set.unlimitedAmmo.description"),
    COMMAND_SET_UNLIMITEDAMMO_SUCCESS("command.set.unlimitedAmmo.success"),
    COMMAND_SET_UNLIMITEDAMMO_USAGE("command.set.unlimitedAmmo.usage"),
    COMMAND_SET_USAGE("command.set.usage"),
    COMMAND_SET_USEPERMISSIONS_DESCRIPTION("command.set.usePermissions.description"),
    COMMAND_SET_USEPERMISSIONS_SUCCESS("command.set.usePermissions.success"),
    COMMAND_SET_USEPERMISSIONS_USAGE("command.set.usePermissions.usage"),
    COMMAND_SET_WHITELIST_ADDED("command.set.whitelist.add"),
    COMMAND_SET_WHITELIST_CLEAR("command.set.whitelist.clear"),
    COMMAND_SET_WHITELIST_DESCRIPTION("command.set.whitelist.description"),
    COMMAND_SET_WHITELIST_LIST("command.set.whitelist.list"),
    COMMAND_SET_WHITELIST_MODE("command.set.whitelist.mode"),
    COMMAND_SET_WHITELIST_REMOVE("command.set.whitelist.remove"),
    COMMAND_SET_WHITELIST_USAGE("command.set.whitelist.usage"),
    COMMAND_SEt_OBJECTIVE_USAGE("command.set.objective.usage"),
    COMMAND_STATE_DISABLED("command.state.disabled"),
    COMMAND_STATE_ENABLED("command.state.enabled"),
    COMMAND_ERROR_NOTTYPE("command.error.noType"),
    COMMAND_CREATE_SUCCESS("command.create.success"),
    COMMAND_CREATE_ERROR_EXISTS("command.create.exists"),
    COMMAND_CREATE_DESCRIPTION("command.create.description"),
    COMMAND_CREATE_USAGE("command.create.usage");

    private final @NotNull String path;

    MgCommandLangKey(@NotNull String path) {
        this.path = path;
    }

    public @NotNull String getPath() {
        return path;
    }
}
