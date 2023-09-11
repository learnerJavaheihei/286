package handler.dailymissions;

/**
 * @author Bonux
 **/
public class HuntNosMonster extends DailyHunting {
	private final int[] MONSTER_IDS = {
			20793, // Рогач
	};

	@Override
	protected int[] getMonsterIds() {
		return MONSTER_IDS;
	}
}
