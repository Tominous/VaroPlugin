package de.cuuky.varo.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.scoreboard.DisplaySlot;

import de.cuuky.varo.Main;
import de.cuuky.varo.alert.AlertHandler;
import de.cuuky.varo.bot.discord.VaroDiscordBot;
import de.cuuky.varo.bot.discord.register.BotRegister;
import de.cuuky.varo.bot.telegram.VaroTelegramBot;
import de.cuuky.varo.broadcast.Broadcaster;
import de.cuuky.varo.configuration.ConfigFailureDetector;
import de.cuuky.varo.configuration.ConfigHandler;
import de.cuuky.varo.configuration.config.ConfigEntry;
import de.cuuky.varo.entity.player.PlayerHandler;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.entity.team.VaroTeamHandler;
import de.cuuky.varo.game.GameHandler;
import de.cuuky.varo.list.ListHandler;
import de.cuuky.varo.list.VaroList;
import de.cuuky.varo.listener.PermissionSendListener;
import de.cuuky.varo.mysql.MySQL;
import de.cuuky.varo.report.ReportHandler;
import de.cuuky.varo.scoreboard.ScoreboardHandler;
import de.cuuky.varo.serialize.VaroSerializeHandler;
import de.cuuky.varo.spawns.SpawnHandler;
import de.cuuky.varo.spigot.FileDownloader;
import de.cuuky.varo.threads.OutSideTimeChecker;
import de.cuuky.varo.utils.VaroUtils;

public class DataManager {

	private static DataManager instance;

	private static final int LABYMOD_ID = 52423, DISCORDBOT_ID = 66778, TELEGRAM_ID = 66823;

	private boolean doSave;

	private DataManager() {
		load();
		loadPlugins();

		startAutoSave();
		doSave = true;
	}

	private void copyDefaultPresets() {
		try {
			ZipInputStream zip = new ZipInputStream(new FileInputStream(Main.getInstance().getThisFile()));

			ZipEntry e = null;
			while((e = zip.getNextEntry()) != null) {
				String name = e.getName();
				e.isDirectory();
				if(name.startsWith("presets")) {
					File file = new File("plugins/Varo/" + name);
					if(e.isDirectory()) {
						file.mkdir();
						continue;
					}

					if(!file.exists()) {
						new File(file.getParent()).mkdirs();
						file.createNewFile();
					} else
						continue;

					FileOutputStream out = new FileOutputStream(file);

					byte[] byteBuff = new byte[1024];
					int bytesRead = 0;
					while((bytesRead = zip.read(byteBuff)) != -1) {
						out.write(byteBuff, 0, bytesRead);
					}

					out.flush();
					out.close();
				}
			}

			zip.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void load() {
		ConfigFailureDetector.detectConfig();

		copyDefaultPresets();
		ConfigHandler.getInstance(); // Initialization

		GameHandler.initialize(); // Initialization GameHandler
		PlayerHandler.initialize(); // Initialization PlayerHandler
		VaroTeamHandler.initialize(); // Initialization TeamHandler
		SpawnHandler.initialize(); // Initialization SpawnHandler
		ScoreboardHandler.getInstance(); // Initialization ScoreboardHandler
		ReportHandler.initialize(); // Initialization ReportHandler
		AlertHandler.initialize(); // Initialization AlertHandler
		OutSideTimeChecker.getInstance(); // Initialization TimeChecker
		MySQL.getInstance(); // Initialization MySQL
		ListHandler.getInstance(); // Initialization ListHandler
		Broadcaster.getInstance(); // Initialization Broadcaster

		Bukkit.getServer().setSpawnRadius(ConfigEntry.SPAWN_PROTECTION_RADIUS.getValueAsInt());
		VaroUtils.setWorldToTime();

		VaroPlayer.getOnlinePlayer().forEach(vp -> vp.update());
	}

	private void loadPlugins() {
		boolean discordNewDownloadFailed = false;
		if(ConfigEntry.DISCORDBOT_ENABLED.getValueAsBoolean()) {
			try {
				VaroDiscordBot.getClassName();
			} catch(NoClassDefFoundError | BootstrapMethodError ef) {
				System.out.println(Main.getConsolePrefix() + "Das Discordbot-Plugin wird automatisch heruntergeladen...");
				discordNewDownloadFailed = !loadAdditionalPlugin(DISCORDBOT_ID, "Discordbot.jar");
			}
		}

		boolean telegramNewDownloadFailed = false;
		if(ConfigEntry.TELEGRAM_ENABLED.getValueAsBoolean()) {
			try {
				VaroTelegramBot.getClassName();
			} catch(NoClassDefFoundError | BootstrapMethodError e) {
				System.out.println(Main.getConsolePrefix() + "Das Telegrambot-Plugin wird automatisch heruntergeladen...");
				telegramNewDownloadFailed = !loadAdditionalPlugin(TELEGRAM_ID, "Telegrambot.jar");
			}
		}

		boolean labymodNewDownloadFailed = false;
		if(ConfigEntry.DISABLE_LABYMOD_FUNCTIONS.getValueAsBoolean() || ConfigEntry.KICK_LABYMOD_PLAYER.getValueAsBoolean() || ConfigEntry.ONLY_LABYMOD_PLAYER.getValueAsBoolean()) {
			try {
				PermissionSendListener.getClassName();

				Bukkit.getPluginManager().registerEvents(new PermissionSendListener(), Main.getInstance());
			} catch(NoClassDefFoundError e) {
				System.out.println(Main.getConsolePrefix() + "Das Labymod-Plugin wird automatisch heruntergeladen...");
				labymodNewDownloadFailed = !loadAdditionalPlugin(LABYMOD_ID, "Labymod.jar");
			}
		}

		if(discordNewDownloadFailed || telegramNewDownloadFailed || labymodNewDownloadFailed) {
			System.out.println(Main.getConsolePrefix() + "Beim Herunterladen / Initialisieren der Plugins ist ein Fehler aufgetreten.");
			System.out.println(Main.getConsolePrefix() + "Der Server wird nun heruntergefahren. Bitte danach fahre den Server wieder hoch.");
			Bukkit.getServer().shutdown();
		}
	}

	@SuppressWarnings("deprecation")
	private void startAutoSave() {
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(Main.getInstance(), new Runnable() {

			@Override
			public void run() {
				save();
			}
		}, 12000, 12000);
	}

	public boolean loadAdditionalPlugin(int resourceId, String dataName) {
		try {
			FileDownloader fd = new FileDownloader("http://api.spiget.org/v2/resources/" + resourceId + "/download", "plugins/" + dataName);

			System.out.println(Main.getConsolePrefix() + "Downloade plugin " + dataName + "...");

			fd.startDownload();

			System.out.println(Main.getConsolePrefix() + "Donwload von " + dataName + " erfolgreich abgeschlossen!");
			
			System.out.println(Main.getConsolePrefix() + dataName + " wird nun geladen...");
			Bukkit.getPluginManager().loadPlugin(new File("plugins/" + dataName));
			System.out.println(Main.getConsolePrefix() + dataName + " wurde erfolgreich geladen!");
			return true;
		} catch(IOException | UnknownDependencyException | InvalidPluginException | InvalidDescriptionException e) {
			System.out.println(Main.getConsolePrefix() + "Es gab einen kritischen Fehler beim Download eines Plugins.");
			System.out.println(Main.getConsolePrefix() + "---------- Stack Trace ----------");
			e.printStackTrace();
			System.out.println(Main.getConsolePrefix() + "---------- Stack Trace ----------");
			return false;
		}
	}

	public void reloadConfig() {
		VaroList.reloadLists();
		ConfigHandler.getInstance().reload();
		ScoreboardHandler.getInstance().loadScores();

		for(VaroPlayer vp : VaroPlayer.getOnlinePlayer()) {
			vp.getPlayer().getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
			ScoreboardHandler.getInstance().sendScoreBoard(vp);
			if(vp.getNametag() != null)
				vp.getNametag().giveAll();
		}
	}

	public void save() {
		if(!doSave)
			return;

		VaroSerializeHandler.saveAll();
		VaroList.saveLists();

		try {
			BotRegister.saveAll();
		} catch(NoClassDefFoundError e) {}
	}

	public void setDoSave(boolean doSave) {
		this.doSave = doSave;
	}

	public static DataManager getInstance() {
		if(instance == null) {
			instance = new DataManager();
		}
		return instance;
	}
}