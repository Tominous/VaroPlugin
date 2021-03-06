package de.cuuky.varo.command.essentials;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.cuuky.varo.Main;
import de.cuuky.varo.configuration.messages.ConfigMessages;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.listener.helper.cancelable.CancelAbleType;
import de.cuuky.varo.listener.helper.cancelable.VaroCancelAble;

public class UnfreezeCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("varo.unfreeze")) {
			sender.sendMessage(ConfigMessages.OTHER_NO_PERMISSION.getValue());
			return false;
		}

		if(args.length != 1) {
			sender.sendMessage(Main.getPrefix() + "§7/freeze <Player/@a>");
			sender.sendMessage(Main.getPrefix() + "§7/unfreeze <Player/@a>");
			return false;
		}

		if(args[0].equalsIgnoreCase("@a")) {
			for(VaroPlayer player : VaroPlayer.getOnlinePlayer()) {
				VaroCancelAble.removeCancelAble(player, CancelAbleType.FREEZE);
			}

			sender.sendMessage(Main.getPrefix() + "Erfolgreich alle Spieler entfreezed!");
			return false;
		}

		if(Bukkit.getPlayerExact(args[0]) == null) {
			sender.sendMessage(Main.getPrefix() + "§7" + args[0] + " §7nicht gefunden!");
			return false;
		}

		Player player = Bukkit.getPlayerExact(args[0]);
		VaroPlayer vp = VaroPlayer.getPlayer(player);
		VaroCancelAble.removeCancelAble(vp, CancelAbleType.FREEZE);
		sender.sendMessage(Main.getPrefix() + "§7" + args[0] + " §7erfolgreich entfreezed!");
		return false;
	}
}