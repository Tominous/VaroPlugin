package de.cuuky.varo.event.events;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import de.cuuky.varo.Main;
import de.cuuky.varo.configuration.config.ConfigEntry;
import de.cuuky.varo.configuration.messages.ConfigMessages;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.entity.player.event.BukkitEventType;
import de.cuuky.varo.event.VaroEvent;
import de.cuuky.varo.logger.logger.EventLogger;
import de.cuuky.varo.logger.logger.EventLogger.LogType;

public class MassRecordingVaroEvent extends VaroEvent {

	private ArrayList<Integer[]> countdowns;
	private int scheduler;
	private int timer;
	private boolean timerEnd = false;

	public MassRecordingVaroEvent() {
		super("§aMassrecording", Material.DIAMOND_SWORD, ConfigEntry.MASS_RECORDING_TIME.getValueAsInt() == 1 ? "Lässt alle Spieler für eine Minute zusätzlich zu den normalen Folgen auf den Server" : "Lässt alle Spieler für " + ConfigEntry.MASS_RECORDING_TIME.getValueAsString() + " Minuten zusätzlich zu den normalen Folgen auf den Server");

		this.timerEnd = false;
		this.countdowns = new ArrayList<Integer[]>();
	}

	public int getCountdown(VaroPlayer vp) {
		for(Integer[] Countdown : countdowns) {
			if(vp.getId() == Countdown[0]) {
				return Countdown[1];
			}
		}
		return 0;
	}

	public int getTimer() {
		return timer;
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTask(scheduler);

		for(Integer[] Speicher : countdowns) {
			VaroPlayer vp = VaroPlayer.getPlayer(Speicher[0]);
			vp.getStats().setCountdown(Speicher[1]);
			if(Speicher[1] == ConfigEntry.PLAY_TIME.getValueAsInt() * 60) {
				if(vp.isOnline()) {
					vp.setMassRecordingKick(true);

					Bukkit.broadcastMessage(ConfigMessages.KICK_BROADCAST.getValue(vp));
					vp.onEvent(BukkitEventType.KICKED);
					vp.getPlayer().kickPlayer(ConfigMessages.KICK_MESSAGE_MASS_REC.getValue(vp));
				}
			}
		}

		if(!timerEnd) {
			for(VaroPlayer vp : VaroPlayer.getOnlinePlayer()) {
				vp.getNetworkManager().sendTitle("Ende", "Die Massenaufnahme wurde beendet.");

				EventLogger.getInstance().println(LogType.ALERT, "Die Massenaufnahme wurde vorzeitig beendet.");
			}
		} else {
			for(VaroPlayer vp : VaroPlayer.getOnlinePlayer()) {
				vp.getNetworkManager().sendTitle("Ende", "Die Massenaufnahme ist zu Ende.");

				EventLogger.getInstance().println(LogType.ALERT, "Die Massenaufnahme ist zu Ende.");
			}

		}

		countdowns.clear();
		timerEnd = false;
	}

	@Override
	public void onEnable() {
		countdowns.clear();
		timerEnd = false;

		for(VaroPlayer vp : VaroPlayer.getVaroPlayer()) {
			Integer[] save = { vp.getId(), vp.getStats().getCountdown() };
			countdowns.add(save);
			vp.getStats().setCountdown(vp.getStats().getCountdown() + 60 * ConfigEntry.MASS_RECORDING_TIME.getValueAsInt());

			vp.setalreadyHadMassProtectionTime(false);
		}

		for(VaroPlayer vp : VaroPlayer.getOnlinePlayer()) {
			vp.setalreadyHadMassProtectionTime(true);
		}

		for(VaroPlayer vp : VaroPlayer.getOnlineAndAlivePlayer()) {
			vp.getStats().addSessionPlayed();
			EventLogger.getInstance().println(LogType.JOIN_LEAVE, vp.getName() + " ist auf dem Server und nimmt an der Massenaufnahme teil.");
		}

		timer = ConfigEntry.MASS_RECORDING_TIME.getValueAsInt() * 60;

		EventLogger.getInstance().println(LogType.ALERT, ConfigEntry.MASS_RECORDING_TIME.getValueAsInt() == 1 ? "DIE MASSENAUFNAHME WURDE GESTARTET UND DAUERT EINE MINUTE!" : "DIE MASSENAUFNAHME WURDE GESTARTET UND DAUERT " + ConfigEntry.MASS_RECORDING_TIME.getValueAsInt() + " MINUTEN!");
		for(VaroPlayer vp : VaroPlayer.getOnlinePlayer()) {
			vp.getNetworkManager().sendTitle("Massenaufnahme", ConfigEntry.MASS_RECORDING_TIME.getValueAsInt() == 1 ? "Alle können für eine Minute joinen." : "Alle können für" + ConfigEntry.MASS_RECORDING_TIME.getValueAsInt() + " Minuten joinen.");
		}

		scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				if(timer < 1) {
					timerEnd = true;
					setEnabled(false);
				}
				timer -= 1;
			}
		}, 0, 20);

	}

}