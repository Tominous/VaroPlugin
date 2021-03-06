package de.cuuky.varo.combatlog;

import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cuuky.varo.alert.Alert;
import de.cuuky.varo.alert.AlertType;
import de.cuuky.varo.configuration.config.ConfigEntry;
import de.cuuky.varo.configuration.messages.ConfigMessages;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.entity.player.event.BukkitEventType;
import de.cuuky.varo.entity.player.stats.stat.PlayerState;
import de.cuuky.varo.entity.player.stats.stat.Strike;
import de.cuuky.varo.game.Game;
import de.cuuky.varo.game.state.GameState;
import de.cuuky.varo.logger.logger.EventLogger;
import de.cuuky.varo.logger.logger.EventLogger.LogType;

public class CombatlogCheck {

	/*
	 * OLD CODE
	 */

	private boolean combatLog;

	public CombatlogCheck(PlayerQuitEvent event) {
		this.combatLog = false;

		check(event);
	}

	private void check(PlayerQuitEvent event) {
		if(Game.getInstance().getGameState() == GameState.END || PlayerHit.getHit(event.getPlayer()) == null) {
			return;
		}

		VaroPlayer vp = VaroPlayer.getPlayer(event.getPlayer().getName());
		PlayerHit hit = PlayerHit.getHit(event.getPlayer());

		if(hit.getOpponent() != null && hit.getOpponent().isOnline())
			PlayerHit.getHit(hit.getOpponent()).over();

		if(!vp.getStats().isAlive()) {
			return;
		}

		if(ConfigEntry.KILL_ON_COMBATLOG.getValueAsBoolean()) {
			event.getPlayer().setHealth(0);
			vp.getStats().setState(PlayerState.DEAD);
		}

		this.combatLog = true;
		punish(vp);
	}

	private void punish(VaroPlayer player) {
		player.onEvent(BukkitEventType.KICKED);
		new Alert(AlertType.COMBATLOG, player.getName() + " hat sich im Kampf ausgeloggt!");
		if(ConfigEntry.STRIKE_ON_COMBATLOG.getValueAsBoolean()) {
			player.getStats().addStrike(new Strike("CombatLog", player, "CONSOLE"));
			EventLogger.getInstance().println(LogType.ALERT, ConfigMessages.ALERT_COMBAT_LOG_STRIKE.getValue(player));
		} else
			EventLogger.getInstance().println(LogType.ALERT, ConfigMessages.ALERT_COMBAT_LOG.getValue(player));

		Bukkit.broadcastMessage(ConfigMessages.COMBAT_LOGGED_OUT.getValue(player));
	}

	public boolean isCombatLog() {
		return combatLog;
	}
}