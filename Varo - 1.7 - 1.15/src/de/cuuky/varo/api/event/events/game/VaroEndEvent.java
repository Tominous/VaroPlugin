package de.cuuky.varo.api.event.events.game;

import de.cuuky.varo.api.event.VaroAPIEvent;
import de.cuuky.varo.api.objects.game.VaroAPIGame;
import de.cuuky.varo.game.Game;

public class VaroEndEvent extends VaroAPIEvent {

	private VaroAPIGame game;

	public VaroEndEvent(Game game) {
		super(true);

		this.game = new VaroAPIGame(game);
	}

	public VaroAPIGame getGame() {
		return game;
	}
}
