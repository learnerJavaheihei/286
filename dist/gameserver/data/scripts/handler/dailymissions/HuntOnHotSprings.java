package handler.dailymissions;

/**
 * @author Bonux
 **/
public class HuntOnHotSprings extends DailyHunting {
	private final int[] MONSTER_IDS = {
			21839,  // Йети Горячих Источников
			21834,  // Буйвол Горячих Источников
			21842	// Грендель Горячих Источников
	};

	@Override
	protected int[] getMonsterIds() {
		return MONSTER_IDS;
	}
}
