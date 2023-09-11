package handler.dailymissions;

/**
 * @author Bonux
 **/
public class HuntHaritMonsters extends DailyHunting {
	private final int[] MONSTER_IDS = {
			20640,    // Ящер Харит
			20641,    // Воитель Ящеров Харит
			20642,    // Стрелок Ящеров Харит
			20643,    // Воин Ящеров Харит
			20644,    // Шаман Ящеров Харит
			20645    // Вождь Ящеров Харит
	};

	@Override
	protected int[] getMonsterIds() {
		return MONSTER_IDS;
	}
}
