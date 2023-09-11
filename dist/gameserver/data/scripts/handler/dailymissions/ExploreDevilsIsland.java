package handler.dailymissions;

/**
 * @author Bonux
 **/
public class ExploreDevilsIsland extends DailyHunting {
	private final int[] MONSTER_IDS = {
			24025,    // Кровавый Багрянец
			24026,    // Клифер
			24027,    // Сайрона
			24028,    // Демон Воин
			24029,    // Хозяин Долин
			24030,    // Каменный Ванул
			24031,    // Парящая Смерть
			24032,    // Пророк
			24033,    // Дух Стражника
			24034,    // Ночная Сайрона
			24035,    // Даймон
			24036,    // Долорес
			24037,    // Кукла
			24038,    // Тор Скорпион
			24039,    // Жемчужный Ужас
			24040,    // Ночной Кошмар
			24046,    // Всевидящее Око
			24047,    // Всевидящее Око
			24048,    // Бессмертный Дух
			24049,    // Бессмертный Дух
			24050,    // Голодный Дух
			24051,    // Голодный Дух
			24052,    // Голодный Воитель
			24053,    // Воитель
			24054,    // Голодный Воин
			24055    // Голодный Воин
	};

	@Override
	public boolean isReusable() {
		return false;
	}

	@Override
	protected int[] getMonsterIds() {
		return MONSTER_IDS;
	}
}
