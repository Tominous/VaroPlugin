package de.cuuky.varo.listener.logging;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import de.cuuky.varo.configuration.config.ConfigEntry;
import de.cuuky.varo.logger.logger.BlockLogger;

public class DestroyedBlocksListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onOreBreak(BlockBreakEvent event) {
		if(!ConfigEntry.BLOCK_DESTROY_LOGGER.getValueAsBoolean())
			return;

		if(event.isCancelled())
			return;

		BlockLogger.getInstance().println(event.getBlock(), event.getPlayer());
	}
}