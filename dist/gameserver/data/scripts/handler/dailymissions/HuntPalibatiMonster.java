package handler.dailymissions;

/**
 * @author Bonux
 **/
public class HuntPalibatiMonster extends DailyHunting {
	private final int[] MONSTER_IDS = {
			20673    // Фалибати
	};

	@Override
	protected int[] getMonsterIds() {
		return MONSTER_IDS;
	}
}
