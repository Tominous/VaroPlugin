package de.cuuky.varo.command.varo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.utils.VaroUtils;

public class SortCommand extends VaroCommand {

	public SortCommand() {
		super("sort", "Sortiert die Spieler in ihre Löcher", "varo.sort");
	}

	@Override
	public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
		if(args.length != 0) {
			sender.sendMessage(Main.getPrefix() + "§7/sort");
			return;
		}

		VaroUtils.SortResult result = VaroUtils.sortPlayers();
		switch(result) {
		case SORTED_WELL:
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "Alle Spieler §7wurden sortiert!");
			break;
		case NO_SPAWN_WITH_TEAM:
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "Es konnte nicht für jeden Spieler ein Loche bei den Teampartnern gefunden werden!");
			break;
		case NO_SPAWN:
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "Es konnte nicht für jeden Spieler ein Loch gefunden werden!");
			break;
		}
	}
}