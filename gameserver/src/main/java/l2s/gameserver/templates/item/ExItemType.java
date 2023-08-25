package l2s.gameserver.templates.item;

public enum ExItemType
{
	/*0*/NONE_0(-1),

	// Оружие
	/*1*/SWORD(0), // Одноручный меч
	/*2*/MAGIC_SWORD(0), // Магический одноручный меч
	/*3*/DAGGER(0), // Кинжал
	/*4*/RAPIER(0), // Рапира
	/*5*/BIG_SWORD(0), // Двуручный меч
	/*6*/ANCIENT_SWORD(0), // Древний меч
	/*7*/DUAL_SWORD(0), // Парные клинки
	/*8*/DUAL_DAGGER(0), // Парные кинжалы
	/*9*/BLUNT_WEAPON(0), // Одноручное дробящее
	/*10*/MAGIC_BLUNT_WEAPON(0), // Одноручное магическое дробящее
	/*11*/BIG_BLUNT_WEAPON(0), // Двуручное дробящее
	/*12*/BIG_MAGIC_BLUNT_WEAPON(0), // Двуручное магическое дробящее
	/*13*/DUAL_BLUNT_WEAPON(0), // Парное дробящее
	/*14*/BOW(0), // Лук
	/*15*/CROSSBOW(0), // Арбалет
	/*16*/HAND_TO_HAND(0), // Кастеты
	/*17*/POLE(0), // Древковые
	/*18*/OTHER_WEAPON(0), // Другое оружие

	// Доспехи
	/*19*/HELMET(1), // Шлем
	/*20*/UPPER_PIECE(1), // Верхняя часть доспехов
	/*21*/LOWER_PIECE(1), // Нижняя часть доспехов
	/*22*/FULL_BODY(1), // Костюм
	/*23*/GLOVES(1), // Перчатки
	/*24*/FEET(1), // Обувь
	/*25*/SHIELD(1), // Щит
	/*26*/SIGIL(1), // Символ
	/*27*/PENDANT(1), // Подвеска
	/*28*/CLOAK(1), // Плащ

	// Аксессуары
	/*29*/RING(2), // Кольцо
	/*30*/EARRING(2), // Серьга
	/*31*/NECKLACE(2), // Ожерелье
	/*32*/BELT(2), // Пояс
	/*33*/BRACELET(2), // Браслет
	/*34*/AGATHION(2), // Браслет
	/*35*/HAIR_ACCESSORY(2), // Головной убор

	// Припасы
	/*36*/POTION(3), // Зелье
	/*37*/SCROLL_ENCHANT_WEAPON(3), // Свиток: Модифицировать Оружие
	/*38*/SCROLL_ENCHANT_ARMOR(3), // Свиток: Модифицировать Доспех
	/*39*/SCROLL_OTHER(3), // Другой свиток
	/*40*/SOULSHOT(3), // Заряды Душ
	/*41*/SPIRITSHOT(3), // Заряды Духа
	/*42*/NONE_41(-1),

	// Для питомцев
	/*43*/PET_EQUIPMENT(4), // Доспехи питомца
	/*44*/PET_SUPPLIES(4), // Припасы питомца

	// Остальное
	/*45*/CRYSTAL(5), // Кристалл
	/*46*/RECIPE(5), // Рецепт
	/*47*/CRAFTING_MAIN_INGRIDIENTS(5), // Основные материалы для изготовления предметов
	/*48*/LIFE_STONE(5), // Камень Жизни
	/*49*/SOUL_CRYSTAL(5), // Кристалл Души
	/*50*/ATTRIBUTE_STONE(5), // Кристалл Стихии
	/*51*/WEAPON_ENCHANT_STONE(5), // Камень: Модифицировать Оружие
	/*52*/ARMOR_ENCHANT_STONE(5), // Камень:  Модифицировать Доспех
	/*53*/SPELLBOOK(5), // Книга заклинаний
	/*54*/GEMSTONE(5), // Самоцветы
	/*55*/POUCH(5), // Кошель
	/*56*/PIN(5), // Заколка
	/*57*/MAGIC_RUNE_CLIP(5), // Магическая Заколка
	/*58*/MAGIC_ORNAMENT(5), // Магическое Украшение
	/*59*/DYES(5), // Цвет
	/*60*/OTHER_ITEMS(5); // Другие Предметы

	public static final ExItemType[] VALUES = values();

	private int _mask;

	ExItemType(int mask)
	{
		_mask = mask;
	}

	public int mask()
	{
		return _mask;
	}
}