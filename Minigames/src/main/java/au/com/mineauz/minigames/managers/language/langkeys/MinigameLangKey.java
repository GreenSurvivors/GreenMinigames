package au.com.mineauz.minigames.managers.language.langkeys;

import org.jetbrains.annotations.NotNull;

public enum MinigameLangKey implements LangKey { //todo this gets rather big. Slit it into smaller digestible parts: tool, ?...
    CONFIG_BLACKLIST("config.blacklist"),
    CONFIG_WHITELIST("config.whitelist"),
    MINIGAME_ERROR_FULL("minigame.error.full"),
    MINIGAME_ERROR_INCORRECTSTART("minigame.error.incorrectStart"),
    MINIGAME_ERROR_INVALIDMECHANIC("minigame.error.invalidMechanic"),
    MINIGAME_ERROR_INVALIDTYPE("minigame.error.invalidType"),
    MINIGAME_ERROR_ISEMPTY("minigame.error.isEmpty"),
    MINIGAME_ERROR_MECHANICSTARTFAIL("minigame.error.mechanicStartFail"),
    MINIGAME_ERROR_NOCOMMAND("minigame.error.noCommand"),
    MINIGAME_ERROR_NODEFAULTTOOL("minigame.error.noDefaultTool"),
    MINIGAME_ERROR_NOEND("minigame.error.noEnd"),
    MINIGAME_ERROR_NOFLY("minigame.error.noFly"),
    MINIGAME_ERROR_NOGAMEMODE("minigame.error.noGamemode"),
    MINIGAME_ERROR_NOINFECTION("minigame.error.noInfection"),
    MINIGAME_ERROR_NOLOBY("minigame.error.noLobby"),
    MINIGAME_ERROR_NOMINIGAME("minigame.error.noMinigame"),
    MINIGAME_ERROR_NOPERMISSION("minigame.error.noPermission"),
    MINIGAME_ERROR_NOQUITLOC("minigame.error.noQuitLoc"),
    MINIGAME_ERROR_NOREVERT("minigame.error.noRevert"),
    MINIGAME_ERROR_NOSPECTATELOC("minigame.error.noSpectateLoc"),
    MINIGAME_ERROR_NOSTARTLOC("minigame.error.noStartLoc"),
    MINIGAME_ERROR_NOTEAM("minigame.error.noTeam"),
    MINIGAME_ERROR_NOTEAMASSIGNED("minigame.error.noTeamAssigned"),
    MINIGAME_ERROR_NOTELEPORTALLOWED("minigame.error.noTeleportAllowed"),
    MINIGAME_ERROR_NOTENABLED("minigame.error.notEnabled"),
    MINIGAME_ERROR_NOTSTARTED("minigame.error.notStarted"),
    MINIGAME_ERROR_NOTTEAMGAME("minigame.error.notTeamGame"),
    MINIGAME_ERROR_REGENERATING("minigame.error.regenerating"),
    MINIGAME_ERROR_STARTED("minigame.error.started"),
    MINIGAME_FLAG_RETURNEDNEUTRAL("minigame.flag.returnedNeutral"),
    MINIGAME_FLAG_RETURNEDTEAM("minigame.flag.returnedTeam"),
    MINIGAME_GAMEOVERQUIT("minigame.gameOverQuit"),
    MINIGAME_INFO_HEADER("minigame.info.header"),
    MINIGAME_INFO_LATEJOIN_DISABLED("minigame.info.lateJoin.disabled"),
    MINIGAME_INFO_LATEJOIN_ENABLED("minigame.info.lateJoin.enabled"),
    MINIGAME_INFO_LATEJOIN_MSG("minigame.info.lateJoin.msg"),
    MINIGAME_INFO_PLAYERCOUNT("minigame.info.playerCount"),
    MINIGAME_INFO_PLAYERS_TITLE("minigame.info.players.title"),
    MINIGAME_INFO_SCORE("minigame.info.score"),
    MINIGAME_INFO_STATUS_EMPTY("minigame.info.status.empty"),
    MINIGAME_INFO_STATUS_STARTED("minigame.info.status.started"),
    MINIGAME_INFO_STATUS_TITLE("minigame.info.status.title"),
    MINIGAME_INFO_STATUS_WAITINGFORPLAYERS("minigame.info.status.waitingForPlayers"),
    MINIGAME_JOIN_ERROR_ALREADYPLAYING("minigame.join.error.alreadyPlaying"),
    MINIGAME_JOIN_ERROR_NOTENOUGH_MONEY("minigame.join.error.notEnoughMoney"),
    MINIGAME_LATEJOIN("minigame.lateJoin"),
    MINIGAME_LATEJOINWAIT("minigame.lateJoinWait"),
    MINIGAME_LIVES_ERROR_NOLIVES("minigame.lives.error.noLives"),
    MINIGAME_LIVES_LIVESLEFT("minigame.lives.livesLeft"),
    MINIGAME_RESOURCEPACK_DECLINED("minigame.resourcepack.declined"),
    MINIGAME_RESOURCEPACK_FAILED("minigame.resourcepack.failed"),
    MINIGAME_RESSOURCEPACK_APPLY("minigame.resourcepack.apply"),
    MINIGAME_RESSOURCEPACK_NORESSOURCEPACK("minigame.resourcepack.noResourcepack"),
    MINIGAME_RESSOURCEPACK_REMOVE("minigames.resourcepack.remove"),
    MINIGAME_SAVED("minigame.saved"),
    MINIGAME_SCORETOWIN("minigame.scoreToWin"),
    MINIGAME_SKIPWAITTIME("minigame.skipWaitTime"),
    MINIGAME_STARTRANDOMIZED("minigame.startRandomized"),
    MINIGAME_TREASUREHUNT_ABOVE("minigame.treasurehunt.above"),
    MINIGAME_TREASUREHUNT_BELOW("minigame.treasurehunt.below"),
    MINIGAME_TREASUREHUNT_DESPAWN("minigame.treasurehunt.despawn"),
    MINIGAME_TREASUREHUNT_EAST("minigame.treasurehunt.east"),
    MINIGAME_TREASUREHUNT_ERROR_NOLOCATION("minigame.treasurehunt.error.noLocation"),
    MINIGAME_TREASUREHUNT_HINT1("minigame.treasurehunt.hint1"),
    MINIGAME_TREASUREHUNT_HINT2("minigame.treasurehunt.hint2"),
    MINIGAME_TREASUREHUNT_HINT3("minigame.treasurehunt.hint3"),
    MINIGAME_TREASUREHUNT_HINT4("minigame.treasurehunt.hint4"),
    MINIGAME_TREASUREHUNT_NORTH("minigame.treasurehunt.north"),
    MINIGAME_TREASUREHUNT_PLAYERFOUND("minigame.treasurehunt.plyFound"),
    MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE1("minigame.treasurehunt.playerSpecificHint.distance1"),
    MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE2("minigame.treasurehunt.playerSpecificHint.distance2"),
    MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE3("minigame.treasurehunt.playerSpecificHint.distance3"),
    MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE4("minigame.treasurehunt.playerSpecificHint.distance4"),
    MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE5("minigame.treasurehunt.playerSpecificHint.distance5"),
    MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE6("minigame.treasurehunt.playerSpecificHint.distance6"),
    MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_GLOBALHINTS("minigame.treasurehunt.playerSpecificHint.globalHints"),
    MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_NOHINT("minigame.treasurehunt.playerSpecificHint.noHint"),
    MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_NOUSE("minigame.treasurehunt.playerSpecificHint.noUse"),
    MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_TIMELEFT("minigame.treasurehunt.playerSpecificHint.timeLeft"),
    MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_WRONGWORLD("minigame.treasurehunt.playerSpecificHint.wrongWorld"),
    MINIGAME_TREASUREHUNT_REMOVED("minigame.treasurehunt.removed"),
    MINIGAME_TREASUREHUNT_SOUTH("minigame.treasurehunt.south"),
    MINIGAME_TREASUREHUNT_SPAWN("minigame.treasurehunt.spawn"),
    MINIGAME_TREASUREHUNT_WEST("minigame.treasurehunt.west"),
    MINIGAME_WAITINGFORPLAYERS("minigame.waitingForPlayers"),
    MINIGAME_WARNING_NOVAULT("minigame.warning.noVault"),
    MINIGAME_WARNING_TELEPORT_ACROSS_WORLDS("minigame.warning.TeleportAcrossWorlds"),
    PLAYER_BET_ERROR_NOBET("player.bet.error.NoBet"),
    PLAYER_BET_INCORRECTITEMAMOUNTINFO("player.bet.error.incorrectItemAmountInfo"),
    PLAYER_BET_INCORRECTMONEYAMOUNTINFO("player.bet.error.incorrectMoneyAmountInfo"),
    PLAYER_BET_NOTENOUGHMONEY("player.bet.error.notEnoughMoney"),
    PLAYER_BET_NOTENOUGHMONEYINFO("player.bet.error.notEnoughMoneyInfo"),
    PLAYER_BET_PLAYERMSG("player.bet.plyMsg"),
    PLAYER_BET_PLAYERNOBET("player.bet.error.NoBet"),
    PLAYER_BET_WINMONEY("player.bet.winMoney"),
    PLAYER_CHECKPOINT_DEATHREVERT("player.checkpoint.deathRevert"),
    PLAYER_CHECKPOINT_REVERT("player.checkpoint.revert"),
    PLAYER_COMPLETIONTIME("player.completionTime"),
    PLAYER_CTF_CAPTURE("player.ctf.capture"),
    PLAYER_CTF_CAPTUREFINAL("player.ctf.captureFinal"),
    PLAYER_CTF_DROPPED("player.ctf.dropped"),
    PLAYER_CTF_NEUTRAL_CAPTURE("player.ctf.captureNeutral"),
    PLAYER_CTF_NEUTRAL_CAPTUREFINAL("player.ctf.captureNeutralFinal"),
    PLAYER_CTF_NEUTRAL_DROPPED("player.ctf.droppedNeutral"),
    PLAYER_CTF_NEUTRAL_STOLE("player.ctf.stoleNeutral"),
    PLAYER_CTF_RETURNED("player.ctf.returned"),
    PLAYER_CTF_RETURNFAIL("player.ctf.returnFail"),
    PLAYER_CTF_STOLE("player.ctf.stole"),
    PLAYER_END_BROADCAST_NOBODY("player.end.broadcast.nobodyWon"),
    PLAYER_END_BROADCAST_WIN("player.end.broadcast.win"),
    PLAYER_END_TEAM_SCORE("player.end.team.score"),
    PLAYER_END_TEAM_TIE("player.end.team.tie"),
    PLAYER_END_TEAM_TIECOUNT("player.end.team.tieCount"),
    PLAYER_END_TEAM_WIN("player.end.team.win"),
    PLAYER_JOIN_JOINING("player.join.joining"),
    PLAYER_JOIN_OBJECTIVE("player.join.objective"),
    PLAYER_JOIN_PLAYERINFO("player.join.plyInfo"),
    PLAYER_JOIN_PLAYERMSG("player.join.plyMsg"),
    PLAYER_JUGGERNAUT_ERROR_TEAM("player.juggernaut.error.teamGame"),
    PLAYER_JUGGERNAUT_GAMEMSG("player.juggernaut.gameMsg"),
    PLAYER_JUGGERNAUT_PLAYERMSG("player.juggernaut.plyMsg"),
    PLAYER_KILLS_FINALKILL("player.kills.finalKill"),
    PLAYER_LOADOUT_EQUIPPED("player.loadout.equipped"),
    PLAYER_LOADOUT_ERROR_NOLOADOUT("player.loadout.error.noLoadout"),
    PLAYER_LOADOUT_NEXTRESPAWN("player.loadout.nextRespawn"),
    PLAYER_LOADOUT_TEMPORARILY("player.loadout.temporarilyEquipped"),
    PLAYER_QUIT_OUTOFLIVES("player.quit.plyOutOfLives"),
    PLAYER_QUIT_PLAYERMSG("player.quit.plyMsg"),
    PLAYER_SELECT_POS1("player.select.pos1"),
    PLAYER_SELECT_POS2("player.select.pos2"),
    PLAYER_SELECT_RESTART("player.select.restart"),
    PLAYER_SPECTATE_JOIN_MINIGAMEMSG("player.spectate.join.minigameMsg"),
    PLAYER_SPECTATE_JOIN_PLAYERHELP("player.spectate.join.plyHelp"),
    PLAYER_SPECTATE_JOIN_PLAYERMSG("player.spectate.join.plyMsg"),
    PLAYER_SPECTATE_QUIT_MINIGAMEMSG("player.spectate.quit.minigameMsg"),
    PLAYER_SPECTATE_QUIT_PLAYERMSG("player.spectate.quit.plyMsg"),
    PLAYER_TEAM_ASSIGN_ERROR_FULL("player.team.assign.full"),
    PLAYER_TEAM_ASSIGN_JOINANNOUNCE("player.team.assign.joinAnnounce"),
    PLAYER_TEAM_ASSIGN_JOINTEAM("player.team.assign.joinTeam"),
    PLAYER_TEAM_AUTOBALANCE_MINIGAMEMSG("player.team.autobalance.minigameMsg"),
    PLAYER_TEAM_AUTOBALANCE_PLYMSG("player.team.autobalance.plyMsg"),
    PRESET_INFO_NOINFO("preset.info.noInfo"),
    PRESET_LOAD_ERROR_NOTFOUND("preset.load.error.notFound"),
    PRESET_LOAD_SUCCESS("preset.load.success"),
    QUANTIFIER_NONE("quantifier.none"),
    REGION_DESCRIBE("region.describe"),
    REGION_ERROR_NOREGENREION("region.error.noRegenRegion"),
    REGION_ERROR_NOSELECTION("region.error.noSelection"),
    REGION_POSITION("region.position"),
    REGION_REGENREGION_CREATED("region.regenRegion.created"),
    REGION_REGENREGION_ERROR_LIMIT("region.regenRegion.error.limit"),
    REGION_REGENREGION_REMOVED("region.regenregion.removed"),
    REGION_REGENREGION_UPDATED("region.regenRegion.updated"),
    REGION_SELECT_POINT("region.select.point"),
    REWARDSCHEME_ERROR_DUPLICATE("rewardscheme.error.duplicate"),
    REWARDSCHEME_ERROR_INVALID("rewardscheme.error.invalid"),
    REWARD_ERROR_NOVAULT("reward.error.noVault"),
    REWARD_ITEM("reward.item"),
    REWARD_MONEY("reward.money"),
    SIGN_CHECKPOINT_FAIL("sign.checkpoint.fail"),
    SIGN_CHECKPOINT_SET("sign.checkpoint.set"),
    SIGN_ERROR_BACKSIDE("sign.error.backside"),
    SIGN_ERROR_EMPTYHAND("sign.error.emptyHand"),
    SIGN_ERROR_FULLINV("sign.error.fullInv"),
    SIGN_ERROR_INVALID("sign.error.invalid"),
    SIGN_ERROR_TEAM_INVALIDFORMAT("sign.error.team.invalidFormat"),
    SIGN_FINISH_REQUIREFLAGS("sign.finish.requireFlags"),
    SIGN_JOIN_ERROR_INVALIDMONEY("sign.join.invalidMoney"),
    SIGN_REWARD_ERROR_NONAME("sign.reward.error.noName"),
    SIGN_REWARD_SAVED("sign.reward.saved"),
    SIGN_SCOREBOARD_ERROR_SIZE("sign.scoreboard.error.size"),
    SIGN_SCOREBOARD_ERROR_UNEVENLENGTH("sign.scoreboard.error.unevenLength"),
    SIGN_SCOREBOARD_ERROR_WALL("sign.scoreboard.error.wall"),
    SIGN_SCORE_ADDSCORE("sign.score.addScore"),
    SIGN_SCORE_ADDSCORETEAM("sign.score.addScoreTeam"),
    SIGN_SCORE_ERROR_ALREADYUSED("sign.score.error.alreadyUsed"),
    SIGN_SCORE_ERROR_ALREADYUSEDTEAM("sign.score.error.alreadyUsedTeam"),
    SIGN_TEAM_ERROR_UNBALANCE("sign.team.error.unbalance"),
    SIGN_TELEPORT_INVALID("sign.teleport.invalid"),
    TEAM_ADD("team.add"),
    TEAM_ERROR_COLOR_INVALID("team.error.color.invalid"),
    TEAM_ERROR_COLOR_TAKEN("team.error.color.taken"),
    TIME_AND("time.and"),
    TIME_DAYS_LONG("time.days.long"),
    TIME_DAYS_SHORT("time.days.short"),
    TIME_HOURS_LONG("time.hours.long"),
    TIME_HOURS_SHORT("time.hours.short"),
    TIME_MINUTES_LONG("time.minutes.long"),
    TIME_MINUTES_SHORT("time.minutes.short"),
    TIME_SECONDS_LONG("time.seconds.long"),
    TIME_SECONDS_SHORT("time.seconds.short"),
    TIME_STARTUP_GO("time.startup.go"),
    TIME_STARTUP_MINIGAMESTARTS("time.startup.minigameStarts"),
    TIME_STARTUP_PAUSED("time.startup.timerPaused"),
    TIME_STARTUP_RESUMED("time.startup.timerResumed"),
    TIME_STARTUP_WAITINGFORPLAYERS("time.startup.waitingForPlayers"),
    TIME_TICKS_LONG("time.ticks.long"),
    TIME_TICKS_SHORT("time.ticks.short"),
    TIME_TIMELEFT("time.timeLeft"),
    TIME_WEEKS_LONG("time.weeks.long"),
    TIME_WEEKS_SHORT("time.weeks.short"),
    TOOL_ADDED_STARTLOCATION("tool.added.startLocation"),
    TOOL_DESELECTED_ENDLOCATION("tool.deselected.endLocation"),
    TOOL_DESELECTED_LOBBYLOCATION("tool.deselected.lobbyLocation"),
    TOOL_DESELECTED_QUITLOCATION("tool.deselected.quitLocation"),
    TOOL_DESELECTED_REGION("tool.deselected.region"),
    TOOL_DESELECTED_SPECTATORLOCATION("tool.deselected.spectatorLocation"),
    TOOL_DESELECTED_STARTLOCATION("tool.deselected.startLocation"),
    TOOL_ERROR_INMINIGAME("tool.error.inMinigame"),
    TOOL_ERROR_NODEGENAREA("tool.error.noDegenArea"),
    TOOL_ERROR_NOENDLOCATION("tool.error.noEndLocation"),
    TOOL_ERROR_NOLOBBYLOCATION("tool.error.noLobbyLocation"),
    TOOL_ERROR_NOMINIGAME("tool.error.noMinigame"),
    TOOL_ERROR_NOMODE("tool.error.noMode"),
    TOOL_ERROR_NOQUITLOCATION("tool.error.noQuitLocation"),
    TOOL_ERROR_NOREGIONSELECTED("tool.error.noRegionSelected"),
    TOOL_ERROR_NOSPECTATORLOCATION("tool.error.noSpectatorLocation"),
    TOOL_ERROR_NOSTARTLOCATION("tool.error.noStartLocation"),
    TOOL_NAME("tool.name"),
    TOOL_REMOVE_STARTLOCTION("tool.remove.startLocation"),
    TOOL_SELECTED_CUSTOM_DESCRIPTION("tool.selected.custom.description"),
    TOOL_SELECTED_ENDLOCATION("tool.selected.endLocation"),
    TOOL_SELECTED_LOBBYLOCATION("tool.selected.lobbyLocation"),
    TOOL_SELECTED_MINIGAME_DESCRIPTION("tool.selected.minigame.description"),
    TOOL_SELECTED_MINIGAME_MSG("tool.selected.minigame.msg"),
    TOOL_SELECTED_MODE_DESCRIPTION("tool.selected.mode.description"),
    TOOL_SELECTED_QUITLOCATION("tool.selected.quitLocation"),
    TOOL_SELECTED_REGENREGION("tool.selected.regenRegion"),
    TOOL_SELECTED_REGION("tool.selected.region"),
    TOOL_SELECTED_SPECTATORLOCATION("tool.selected.spectatorLocation"),
    TOOL_SELECTED_STARTLOCATION("tool.selected.startLocation"),
    TOOL_SELECTED_TEAM_DESCRIPTION("tool.selected.team.description"),
    TOOL_SET_DEGENAREA("tool.set.degenArea"),
    TOOL_SET_ENDLOCATION("tool.set.endLocation"),
    TOOL_SET_LOBBYLOCATION("tool.set.lobbyLocation"),
    TOOL_SET_QUITLOCATION("tool.set.quitLocation"),
    TOOL_SET_SPECTATORLOCATION("tool.set.spectatorLocation");

    private final @NotNull String path;

    MinigameLangKey(@NotNull String path) {
        this.path = path;
    }

    public @NotNull String getPath() {
        return path;
    }
}
