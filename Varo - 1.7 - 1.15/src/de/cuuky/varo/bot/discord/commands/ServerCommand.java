package de.cuuky.varo.bot.discord.commands;

import java.awt.Color;

import org.bukkit.Bukkit;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import de.cuuky.varo.bot.discord.DiscordBotCommand;
import de.cuuky.varo.game.Game;

public class ServerCommand extends DiscordBotCommand {

	/*
	 * OLD CODE
	 */

	public ServerCommand() {
		super("server", new String[] { "status", "whitelist" }, "Zeigt Infos und Status des Servers");
	}

	@Override
	public void onEnable(String[] args, MessageReceivedEvent event) {
		getDiscordBot().sendMessage("IP: " + Bukkit.getServer().getIp() + ":" + Bukkit.getServer().getPort() + "\n  Whitelist: " + Bukkit.getServer().hasWhitelist() + "\n  GameState: " + Game.getInstance().getGameState().toString(), "SERVER INFO", Color.BLUE, event.getTextChannel());
	}
}
