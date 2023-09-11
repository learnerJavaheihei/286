package handler.dailymissions;

/**
 * @author Bonux
 **/
public class HuntSharpTalonTigerMonster extends DailyHunting {
	private final int[] MONSTER_IDS = {
			21021    // Острый Коготь
	};

	@Override
	protected int[] getMonsterIds() {
		return MONSTER_IDS;
	}
}
