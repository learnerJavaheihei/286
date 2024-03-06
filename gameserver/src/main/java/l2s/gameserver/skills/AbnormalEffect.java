package l2s.gameserver.skills;

/**
 * @author Bonux
 **/
public enum AbnormalEffect
{
	// Normal Abnormal Effects
	/*0*/NONE,
	/*1*/DOT_BLEEDING,
	/*2*/DOT_POISON,
	/*3*/DOT_FIRE, // Мигает красный круг вокруг живота персонажа.
	/*4*/DOT_WATER, // Висит и мигает кусок для на животе персонажа.
	/*5*/DOT_WIND,
	/*6*/DOT_SOIL, // Из персонажа появляется куча бардового дыма и зеленькие кружочки.
	/*7*/STUN,
	/*8*/SLEEP,
	/*9*/SILENCE,
	/*10*/ROOT,
	/*11*/PARALYZE,
	/*12*/FLESH_STONE,
	/*13*/DOT_MP, //unk
	/*14*/BIG_HEAD,
	/*15*/DOT_FIRE_AREA, // Пламя огня начиная с ног персонажа.
	/*16*/CHANGE_TEXTURE,
	/*17*/BIG_BODY,
	/*18*/FLOATING_ROOT,
	/*19*/DANCE_ROOT,
	/*20*/GHOST_STUN, // Звездочки как у стана и на ногах красный круг.
	/*21*/STEALTH,
	/*22*/SEIZURE1, // Вокруг живота синий туман с электричеством.
	/*23*/SEIZURE2, // Вокруг живота синий туман с электричеством.
	/*24*/MAGIC_SQUARE,
	/*25*/FREEZING, // Висит и мигает кусок для на животе персонажа.
	/*26*/SHAKE, // Землетрясение.
	/*27*/UNK_27, //unk
	/*28*/ULTIMATE_DEFENCE,
	/*29*/VP_UP,
	/*30*/REAL_TARGET,
	/*31*/DEATH_MARK,
	/*32*/TURN_FLEE, // Синяя морда черепа над головой.
	/*33*/INVINCIBILITY,
	/*34*/AIR_BATTLE_SLOW, // Мигает красный туманный шар вокруг живота персонажа.
	/*35*/AIR_BATTLE_ROOT,  // Мигает красный туманный шар вокруг живота персонажа.
	/*36*/CHANGE_WP, // Багет вместо оружия.
	/*37*/CHANGE_HAIR_G, // Золотая афро-прическа.
	/*38*/CHANGE_HAIR_P, // Розовая афро-прическа.
	/*39*/CHANGE_HAIR_B, // Черная афро-прическа.
	/*40*/UNK_40, // unk
	/*41*/STIGMA_OF_SILEN,
	/*42*/SPEED_DOWN, // Зеленые и белые линии запутуются в ногах.
	/*43*/FROZEN_PILLAR,
	/*44*/CHANGE_VES_S, // Переодевает персонажа в золотой веспер.
	/*45*/CHANGE_VES_C, // Переодевает персонажа в золотой веспер.
	/*46*/CHANGE_VES_D, // Переодевает персонажа в белый веспер.
	/*47*/TIME_BOMB, // Зеленый круговой дождь в тумане над головой персонажа.
	/*48*/MP_SHIELD, // Критует клиент хD
	/*49*/AIRBIND, // Поднимает в верх и держит в красном шаре персонажа.
	/*50*/CHANGEBODY, // Переодевает персонажа в Д грейд.
	/*51*/KNOCKDOWN,
	/*52*/NAVIT_ADVENT, //unk
	/*53*/KNOCKBACK,
	/*54*/CHANGE_7ANNIVERSARY,
	/*55*/ON_SPOT_MOVEMENT, //unk
	/*56*/DEPORT, // Закрывает персонажа в черной туманной клетке.
	/*57*/AURA_BUFF, // С персонажа идет зеленый дым и сверкает живот.
	/*58*/AURA_BUFF_SELF, // С персонажа идет бело-желтый дым, вокруг живота идут вспышки кольцами градиентного цвета и вокруг живота летают белые кружочки. Также персонаж стоит на красном круге.
	/*59*/AURA_DEBUFF, // С персонажа мигает красный дым и сверкает живот красными полосами.
	/*60*/AURA_DEBUFF_SELF, // Персонажа окутывают красные спиральные полосы и мигает не сильно красный дым. (При активации происходит красная вспышка)
	/*61*/HURRICANE, // (При активации происходит бело-желтая вспышка)
	/*62*/HURRICANE_SELF, // Вокруг персонажа песчаная буря.
	/*63*/BLACK_MARK, // Над головой персонажа морда красного черепа.
	/*64*/BR_SOUL_AVATAR,
	/*65*/CHANGE_GRADE_B, // Переодевает персонажа в Б грейд.
	/*66*/BR_BEAM_SWORD_ONEHAND,
	/*67*/BR_BEAM_SWORD_DUAL,
	/*68*/D_NOCHAT,
	/*69*/D_HERB_POWER,
	/*70*/D_HERB_MAGIC,
	/*71*/D_TALI_DECO_P,
	/*72*/D_TALI_DECO_B,
	/*73*/D_TALI_DECO_C, // Рука светится желтым.
	/*74*/D_TALI_DECO_D, // Рука светится красным.
	/*75*/D_TALI_DECO_E, // Рука светится синим.
	/*76*/D_TALI_DECO_F, // Рука светится Фиолетовым.
	/*77*/D_TALI_DECO_G, // Переодевает в Топ S80, одевает плащ и диадему.
	/*78*/D_CHANGESHAPE_TRANSFORM_1, // Переодевает в NG.
	/*79*/D_CHANGESHAPE_TRANSFORM_2, // Переодевает в D.
	/*80*/D_CHANGESHAPE_TRANSFORM_3, // Переодевает в C.
	/*81*/D_CHANGESHAPE_TRANSFORM_4, // Переодевает в B.
	/*82*/D_CHANGESHAPE_TRANSFORM_5, // Переодевает в A.
	/*83*/SWEET_ICE_FLAKES, // У артеас изчезает туловище, на остальных не проверялось.
	/*84*/FANTASY_ICE_FLAKES, // У артеас изчезает туловище, на остальных не проверялось.
	/*85*/SANTA_SUIT, // Переодевает в костюм деда мороза.
	/*86*/CARD_PC_DECO, // Возле персонажа летает игральная карта.
	/*87*/CHANGE_DINOS, // Переодевает в бейсбольную форму.
	/*88*/CHANGE_VALENTINE, // Оружие персонажа превращается в свадебный букет.
	/*89*/CHOCOLATE,	// Возле персонажа летает леденец.
	/*90*/CANDY,	// Возле персонажа летает конфетка.
	/*91*/COOKIE,	// Возле персонажа летает печенька.
	/*92*/EMPTY_STARS,	// Над персонажом засвечивается 5 пустых звездочек.
	/*93*/ONE_STAR,	// Над персонажом засвечивается 1я звездочка.
	/*94*/TWO_STARS,	// Над персонажом засвечивается 2я звездочка.
	/*95*/THREE_STARS,	// Над персонажом засвечивается 3я звездочка.
	/*96*/FOUR_STARS,	// Над персонажом засвечивается 4я звездочка.
	/*97*/FIVE_STARS,	// Над персонажом засвечивается 5я звездочка.
	/*98*/FACEOFF,	// Песронаж стоит в бардовом круге и над головой мигают скрещенные 2 меча.
	/*99*/FREEZING2,	// Под персонажем земля пытается замерзнуть и выростает перед ногами небольшая льдинка.
	/*100*/CHANGE_YOGI,	// Переодевает персонажа в бронь робокопа.
	/*101*/YOGI,	// Возле персонажа летает голова посоха Мастера Йоды.
	/*102*/MUSICAL_NOTE_YELLOW,	// Возле персонажа летает желтая нота.
	/*103*/MUSICAL_NOTE_BLUE,	// Возле персонажа летает синяя нота.
	/*104*/MUSICAL_NOTE_GREEN,	// Возле персонажа летает зеленая нота.
	/*105*/TENTH_ANNIVERSARY,	// Возле персонажа летает лого Lineage II.
	/*106*/STOCKING_FAIRY,	// Возле персонажа летает картинка носка.
	/*107*/TREE_FAIRY,	// Возле персонажа летает картинка елки.
	/*108*/SNOWMAN_FAIRY,	// Возле персонажа летает картинка снеговика.
	/*109*/OTHELL_ROGUE_BLUFF,	// Над головой персонажа крутятся бардовые спиральки.
	/*110*/HE_PROTECT,	// Вокруг персонажа желтый круг и персонаж стоит в красном круге.
	/*111*/SU_SUMCROSS,	// Вокруг персонажа образовывается бардовая стена с синими бликами.
	/*112*/WIND_STUN,	// Вокруг персонажа крутятся зеленые круги в сверичном виде.
	/*113*/STORM_SIGN2,	// Над головой светится красное око.
	/*114*/STIGMA_STORM,	// Над головой светится зеленое око.
	/*115*/GREEN_SPEED_UP,	// Персонаж светиться зеленым и при беге подпрыгивает.
	/*116*/RED_SPEED_UP,	// Персонаж светиться красным и при беге подпрыгивает.
	/*117*/WIND_PROTECTION,	// Персонаж светиться в зеленый и стоит с транной агрессивной стойке.
	/*118*/LOVE,	// Начинает летать сердце над головой.
	/*119*/PERFECT_STORM,	// На уровне пояса синенький маленький круговорот воздуха.
	/*120*/WIND_ILLUSION,	// Светиться синим и вокруг небольшой смерчь с листьями.
	/*121*/SAYHA_FURY,	// Темнеет персонаж с красным бликом.
	/*122*/UNK_122,
	/*123*/GREAT_GRAVITY,	// Темнеет персонаж с красной димной сверой в районе живота.
	/*124*/STEEL_MIND,	// Каждая нога окутывается зелеными полосами с звездочками и при беге подпрыгивает персонаж.
	/*125*/HOLD_LIGHTING,	// Вокруг персонажа ниже пояса летает черная и голубая цепь. Происходят синие вспышки.
	/*126*/OBLATE,	// Персонаж становится 2D. Сплюснутый))
	/*127*/SPALLATION,	// Персонаж оказывается в средине большого зеленого бликующего круга.
	/*128*/U_HE_ASPECT_AVE,	// С персонажа идет черный дым.
	/*129*/RUNWAY_ARMOR1,	// У артеас изчезает туловище, на остальных не проверялось.
	/*130*/RUNWAY_ARMOR2,	// У артеас изчезает туловище, на остальных не проверялось.
	/*131*/RUNWAY_ARMOR3,	// У артеас изчезает туловище, на остальных не проверялось.
	/*132*/RUNWAY_ARMOR4,	// Переодевает персонажа в бронь робокопа.
	/*133*/RUNWAY_ARMOR5,	// У артеас пропадает все тело, кроме головы, на остальных не проверялось.
	/*134*/RUNWAY_ARMOR6,	// У артеас пропадает все тело, кроме головы, на остальных не проверялось.
	/*135*/RUNWAY_WEAPON1,	// Оружие меняет на лазерное.
	/*136*/RUNWAY_WEAPON2,	// Оружие меняет на японское.
	/*137*/UNK_137,
	/*138*/UNK_138,
	/*139*/UNK_139,
	/*140*/UNK_140,
	/*141*/U_AVE_PALADIN_DEF,	// Вокруг персонажа крутятся светащиеся оранжевые щиты.
	/*142*/U_AVE_GUARDIAN_DEF,	// Вокруг персонажа крутятся светащиеся синие щиты.
	/*143*/U_REALTAR2_AVE,	// Над головой мигает красно-оранжевый оберег в виде тризуба.
	/*144*/U_AVE_DIVINITY,	// Персонажа переливается как эффект заточенной брони.
	/*145*/U_AVE_SHILPROTECTION,	// Вокруг персонажа образовывается красная сфера состоящая из шестиугольников.
	/*146*/U_EVENT_STAR_CA,	// Над персонажем мигают 5 звезд.
	/*147*/U_EVENT_STAR1_TA,	// Над персонажем появляется 1 звезда.
	/*148*/U_EVENT_STAR2_TA,	// Над персонажем появляется 2 звезда.
	/*149*/U_EVENT_STAR3_TA,	// Над персонажем появляется 3 звезда.
	/*150*/U_EVENT_STAR4_TA,	// Над персонажем появляется 4 звезда.
	/*151*/U_EVENT_STAR5_TA,	// Над персонажем появляется 5 звезда.
	/*152*/U_AVE_ABSORB_SHIELD,	// Вокруг персонажа образовывается белая сфера состоящая из шестиугольников.
	/*153*/U_KN_PHOENIX_AURA,	// Перед персонажем образовывается сферичный крест белый и исчезает.
	/*154*/U_KN_REVENGE_AURA,	// Над персоажем происходит фиолетовая дымовая вспышка и исчезает.
	/*155*/U_KN_EVAS_AURA,	// Из тела персонажа идет синий дымок, но в начале крутятся вокруг него синий цилиндр из шестиугольников.
	/*156*/U_KN_REMPLA_AURA,	// Из тела персонажа идет синий дымок, но в начале над ним появляются мечи.
	/*157*/U_AVE_LONGBOW,	// На оружии происходит белая вспышка и исчезает.
	/*158*/U_AVE_WIDESWORD,	// Оружие цилиндрично светиться радужными цветами.
	/*159*/U_AVE_BIGFIST,	// Кастеты светятся почти как геройские.
	/*160*/U_AVE_SHADOWSTEP,
	/*161*/U_TORNADO_AVE,
	/*162*/U_AVE_SNOW_SLOW,
	/*163*/U_AVE_SNOW_HOLD,
	/*164*/UNK_164,
	/*165*/U_AVE_TORNADO_SLOW,
	/*166*/U_AVE_ASTATINE_WATER,
	/*167*/U_BIGBD_CAT_NPC,
	/*168*/U_BIGBD_UNICORN_NPC,
	/*169*/U_BIGBD_DEMON_NPC,
	/*170*/U_BIGBD_CAT_PC,
	/*171*/U_BIGBD_UNICORN_PC,
	/*172*/U_BIGBD_DEMON_PC,
	/*173*/BIG_BODY_2,
	/*174*/BIG_BODY_3,	// персонаж уменьшаеться
	/*175*/PIRATE_SUIT,	// пират
	/*176*/DARK_ASSASSIN_SUIT,	// ассасин
	/*177*/WHITE_ASSASSIN_SUIT,	// белый ассасин
	/*178*/UNK_178,	// мушкетер
	/*179*/RED_WIZARD_SUIT,	// чародей в бардовом какой-то
	/*180*/MYSTIC_SUIT,	// джентельмен в цилиндре
	/*181*/AVE_DRAGON_ULTIMATE,
	/*182*/HALLOWEEN_SUIT, // Трансформация в Фиолетовый Наряд Хеллоуина
	/*183*/INFINITE_SHIELD1_AVE, // В очень тусклом красном шаре напоминающий футбольный
	/*184*/INFINITE_SHIELD2_AVE, // В тусклом красном шаре напоминающий футбольный
	/*185*/INFINITE_SHIELD3_AVE, // В красном шаре напоминающий футбольный
	/*186*/INFINITE_SHIELD4_AVE, // В красном шаре напоминающий футбольный
	/*187*/AVE_ABSORB2_SHIELD, // В синем шаре напоминающий футбольный
	/*188*/UNK_188, // Раздевает туловище
	/*189*/UNK_189, // Раздевает туловище
	/*190*/TALI_DECO_BAIUM, // Светится правая рука золотым
	/*191*/UNK_191, // Трансформация в Белую Династию
	/*192*/UNK_192, // Трансформация в Золотой Зубей
	/*193*/CHANGESHAPE_TRANSFORM,   // Трансформация в Мушкитера
	/*194*/ANGRY_GOLEM_AVE, // Вспыхивает солнце и начинает чар гореть
	/*195*/WA_UNBREAKABLE_SONIC_AVE,    // Свечение в виде солнца
	/*196*/HEROIC_HOLY_AVE, // Трансформация в Темного Рыцаря
	/*197*/HEROIC_SILENCE_AVE,   // Над головой сало и впышки фиолетового дыма в районе живота
	/*198*/HEROIC_FEAR_AVE_1,   // Над головой крутится синее и впышки фиолетового дыма в районе живота
	/*199*/HEROIC_FEAR_AVE_2,   // Над головой крутится синее и впышки фиолетового дыма в районе живота
	/*200*/AVE_BROOCH, // Временно светятся сиськи белым
	/*201*/UNK_201, // Временно светятся сиськи голубым
	/*202*/UNK_202,
	/*203*/UNK_203,
	/*204*/UNK_204,
	/*205*/UNK_205,
	/*206*/INFINITE_SHIELD4_AVE_2, // В красном шаре напоминающий футбольный
	/*207*/CHANGESHAPE_TRANSFORM_1,    // Раздевает и временное свечение
	/*208*/CHANGESHAPE_TRANSFORM_2,    // Раздевает и временное свечение
	/*209*/CHANGESHAPE_TRANSFORM_3,    // Раздевает и временное свечение
	/*210*/CHANGESHAPE_TRANSFORM_4,    // Раздевает и временное свечение
	/*211*/UNK_211, // Раздевает
	/*212*/UNK_212, // Раздевает
	/*213*/UNK_213, // Раздевает туловище
	/*214*/UNK_214, // Раздевает туловище
	/*215*/RO_COUNTER_TRASPIE, // Дымится черным
	/*216*/UNK_216, // Раздевает
	/*217*/RO_GHOST_REFLECT,   // Дымится фиолетовым
	/*218*/CHANGESHAPE_TRANSFORM_5,    // Переодевает в розовое платье с салатовой кофточкой
	/*219*/ICE_ELEMENTALDESTROY,   // Пронзает льдинами
	/*220*/DORMANT_USER,
	/*221*/NUWBIE_USER,
	/*222*/THIRTEENTH_BUFF,
	/*223*/UNK_223,
	/*224*/ARENA_UNSEAL_A,
	/*225*/ARENA_UNSEAL_B,
	/*226*/ARENA_UNSEAL_C,
	/*227*/ARENA_UNSEAL_D,
	/*228*/ARENA_UNSEAL_E,
	/*229*/IN_BATTLE_RHAPSODY,
	/*230*/IN_A_DECAL,
	/*231*/IN_B_DECAL,
	/*232*/CHANGESHAPE_TRANSFORM_6,
	/*233*/UNK_233,
	/*234*/CHANGESHAPE_TRANSFORM_7,
	/*235*/UNK_235,
	/*236*/UNK_236,
	/*237*/UNK_237,
	/*238*/UNK_238,
	/*239*/UNK_239,
	/*240*/UNK_240,
	/*241*/UNK_241,
	/*242*/UNK_242,
	/*243*/UNK_243,
	/*244*/UNK_244,
	/*245*/UNK_245,
	/*246*/UNK_246,
	/*247*/FOCUS_SHIELD,
	/*248*/RAISE_SHIELD,
	/*249*/TRUE_VANGUARD,
	/*250*/SHIELD_WALL,
	/*251*/UNK_251, // Летает Дракон желтый
	/*252*/DRAGON_BARRIER,				//龍之屏障
	/*253*/WHITE_CAT_SUIT, // Переодевает в костюм кошечки
	/*254*/UNK_254,
	/*255*/UNK_255,
	/*256*/UNK_256,
	/*257*/UNK_257,
	/*258*/UNK_258,
	/*259*/UNK_259,
	/*260*/UNK_260,
	/*261*/RED_CAT_SUIT, // Переодевает в костюм рыжего кота
	/*262*/PANDA_SUIT,  // Переодевает в костюм панды
	/*263*/UNK_263,
	/*264*/UNK_264,
	/*265*/UNK_265,
	/*266*/UNK_266,
	/*267*/UNK_267,
	/*268*/UNK_268,
	/*269*/UNK_269,
	/*270*/UNK_270,
	/*271*/UNK_271,
	/*272*/UNK_272,
	/*273*/DRAGON_SUIT,   // Дракон Берсерк
	/*274*/NINJA_SUIT,   // Нинзя
	/*275*/BLUE_MUSKETEER_SUIT,    // Синий Мушкетер
	/*276*/VALKYRIE_SUIT, // Валькирия
	/*277*/WOLF_BARBARIAN_SUIT, // Волчий Варвар
	/*278*/PIRATE_2_SUIT,   // Пират
	/*279*/RED_COWBOY_SUIT,   // Красный Ковбой
	/*280*/SUPREME_SUIT, // Верховный
	/*281*/RED_ROYAL_SUIT, // Красная Королева
	/*282*/WHITE_ROYAL_SUIT, // Белая Королева
	/*283*/UNK_283,
	/*284*/UNK_284,
	/*285*/UNK_285,
	/*286*/UNK_286,
	/*287*/UNK_287,
	/*288*/UNK_288,
	/*289*/UNK_289,
	/*290*/UNK_290,
	/*291*/UNK_291,
	/*292*/UNK_292,
	/*293*/UNK_293,
	/*294*/UNK_294,
	/*295*/UNK_295,
	/*296*/UNK_296,
	/*297*/UNK_297,
	/*298*/UNK_298,
	/*299*/UNK_299,
	/*300*/UNK_300,
	/*301*/UNK_301,
	/*302*/UNK_302,
	/*303*/UNK_303,
	/*304*/UNK_304,
	/*305*/UNK_305,
	/*306*/UNK_306,
	/*307*/UNK_307,
	/*308*/UNK_308,
	/*309*/UNK_309,
	/*310*/UNK_310,
	/*311*/UNK_311,
	/*312*/UNK_312,
	/*313*/UNK_313,
	/*314*/UNK_314,
	/*315*/UNK_315,
	/*316*/UNK_316,
	/*317*/UNK_317,
	/*318*/UNK_318,
	/*319*/UNK_319,
	/*320*/UNK_320,
	/*321*/UNK_321,
	/*322*/BLUE_BIKINI_SUIT, // Синий Полосатый Купальник
	/*323*/ZARICHE_PRISON,
	/*324*/UNK_324,
	/*325*/UNK_325,
	/*326*/UNK_326,
	/*327*/UNK_327,
	/*328*/HEROIC_MIRACLE,
	/*329*/UNK_329,
	/*330*/UNK_330,
	/*331*/DARK_VEIL,
	/*332*/UNK_332,
	/*333*/LIGHT_VEIL,
	/*334*/UNK_334,
	/*335*/UNK_335,
	/*336*/ZARICHE_PRISON_B,
	/*337*/BLUE_HEART,
	/*338*/ATTACK_BUFF,
	/*339*/SHIELD_BUFF,
	/*340*/BERSERKER_BUFF,
	/*341*/UNK_341,
	/*342*/GOLD_STAR_1,
	/*343*/GOLD_STAR_2,
	/*344*/GOLD_STAR_3,
	/*345*/GOLD_STAR_4,
	/*346*/GOLD_STAR_5,
	/*347*/UNK_347,
	/*348*/UNK_348,
	/*349*/UNK_349,
	/*350*/DARK_VEIL_1,
	/*351*/LIGHT_VEIL_1,
	/*352*/DEATH_EFFECT,
	/*353*/WHITE_KNIGHT,
	/*354*/UNK_354,
	/*355*/UNK_355,
	/*356*/UNK_356,
	/*357*/UNK_357,
	/*358*/UNK_358,
	/*359*/UNK_359,
	/*360*/UNK_360,
	/*361*/UNK_361,
	/*362*/UNK_362,
	/*363*/UNK_363,
	/*364*/UNK_364,
	/*365*/UNK_365,
	/*366*/UNK_366,
	/*367*/UNK_367,
	/*368*/UNK_368,
	/*369*/UNK_369,
	/*370*/UNK_370,
	/*371*/UNK_371,
	/*372*/UNK_372,
	/*373*/UNK_373,
	/*374*/UNK_374,
	/*375*/UNK_375,
	/*376*/UNK_376,
	/*377*/UNK_377,
	/*378*/UNK_378,
	/*379*/UNK_379,
	/*380*/UNK_380,
	/*381*/UNK_381,
	/*382*/UNK_382,
	/*383*/UNK_383,
	/*384*/UNK_384,
	/*385*/DEMON_SUIT, //demoneese suit
	/*386*/KAMAEL_BLACK_WINGS, //kamael has 2 dark wings
	/*387*/UNK_387,
	/*388*/BONE_PRISON,
	/*389*/UNK_389,
	/*390*/IGNITION_HUMAN,
	/*391*/IGNITION_ELF,
	/*392*/IGNITION_DARKELF,
	/*393*/ACCELERATION,
	/*394*/BURN,
	/*395*/FREEZING_AREA,
	/*396*/SHOCK,
	/*397*/PERFECT_SHIELD,
	/*398*/FROSTBITE,
	/*399*/BONE_PRISON_SQUELA,
	/*400*/UNK_400,
	/*401*/STIGMA,
	/*402*/UNK_402,
	/*403*/FORT_FLAG,
	/*404*/UNK_404,
	/*405*/UNK_405,
	/*406*/UNK_406,
	/*407*/UNK_407,
	/*408*/UNK_408,
	/*409*/UNK_409,
	/*410*/UNK_410,
	/*411*/UNK_411,
	/*412*/UNK_412,
	/*413*/UNK_413,
	/*414*/UNK_414,
	/*415*/UNK_415,
	/*416*/UNK_416,
	/*417*/UNK_417,
	/*418*/UNK_418,
	/*419*/UNK_419,
	/*420*/UNK_420,
	/*421*/UNK_421,
	/*422*/UNK_422,
	/*423*/UNK_423,
	/*424*/UNK_424,
	/*425*/UNK_425,
	/*426*/UNK_426,
	/*427*/UNK_427,
	/*428*/UNK_428,
	/*429*/UNK_429,
	/*430*/UNK_430,
	/*431*/UNK_431,
	/*432*/UNK_432,
	/*433*/UNK_433,
	/*434*/UNK_434,
	/*435*/UNK_435,
	/*436*/UNK_436,
	/*437*/UNK_437,
	/*438*/UNK_438,
	/*439*/UNK_439,
	/*440*/UNK_440,
	/*441*/UNK_441,
	/*442*/UNK_442,
	/*443*/UNK_443,
	/*444*/UNK_444,
	/*445*/UNK_445,
	/*446*/UNK_446,
	/*447*/UNK_447,
	/*448*/UNK_448,
	/*449*/UNK_449,
	/*450*/UNK_450,
	/*451*/UNK_451,
	/*452*/UNK_452,
	/*453*/UNK_453,
	/*454*/UNK_454,
	/*455*/UNK_455,
	/*456*/UNK_456,
	/*457*/UNK_457,
	/*458*/UNK_458,
	/*459*/UNK_459,
	/*460*/UNK_460,
	/*461*/UNK_461,
	/*462*/UNK_462,
	/*463*/UNK_463,
	/*481*/H_SY_BOARD_RANKER_DECO,
	/*482*/H_SY_BOARDD_DECO,
	/*483*/H_SY_BOARDC_DECO,
	/*484*/H_SY_BOARDB_DECO,
	/*485*/H_SY_BOARDA_DECO,

	BR_POWER_OF_EVA(0), //TODO: Подобрать ID.
	VP_KEEP(29), //TODO: Подобрать ID.

	U_AVE_DRAGON_ULTIMATE(700),

	UNK_10001(10001),
	UNK_10002(10002),
	UNK_10003(10003),
	UNK_10004(10004),
	UNK_10005(10005),
	UNK_10006(10006),
	UNK_10007(10007),
	UNK_10008(10008),
	UNK_10009(10009),
	UNK_10019(10019),

	BASEBALL_COSTUME(10020),	// baseball costume
	SANTA_COSTUME(10021),	// santa costume
	SANTA_1_COSTUME(10022),	// black aura kinda light
	SANTA_2_COSTUME(10023),	// black aura heavier
	SCHOOOL_UNIFORM_COSTUME(10024),	// black suit looks something japanese
	SCHOOOL_UNIFORM_1_COSTUME(10025),	// black aura heavier
	SCHOOOL_UNIFORM_2_COSTUME(10026),	// black aura even heavier
	BEACH_COSTUME(10027),	// beach costume
	BEACH_1_COSTUME(10028),	// black aura with ignition effect
	BEACH_2_COSTUME(10029),	// black aura heavier with ignition
	BEAR_COSTUME(10030),	// bear costume
	BEAR_1_COSTUME(10031),	// bear costume with black aura with ignition
	BEAR_2_COSTUME(10032),	// bear costume with black aura with ignition heavier
	CAT_COSTUME(10033),	// cat costume with sparks
	CAT_1_COSTUME(10034),	// cat costume with black aura
	CAT_2_COSTUME(10035),	// cat costume with black aura heavier
	PANDA_COSTUME(10036),	// panda costume
	PANDA_1_COSTUME(10037),	// panda costume with black aura
	PANDA_2_COSTUME(10038),	// panda costume with black aura heavier
	BASEBALL_COSTUME2(10039),	// baseball costume with sparks
	BASEBALL_1_COSTUME2(10040),	// baseball costume with black aura
	BASEBALL_2_COSTUME2(10041),	// baseball costume with black aura heavier
	BEACH_COSTUME2(10042),	// beach costume
	BEACH_1_COSTUME2(10043),	// beach costume with black aura
	BEACH_2_COSTUME2(10044),	// beach costume with black aura heavier
	SCHOOOL_UNIFORM_COSTUME2(10045),	// school uniform costume
	SCHOOOL_UNIFORM_1_COSTUME2(10046),	// school uniform  costume with black aura
	SCHOOOL_UNIFORM_2_COSTUME2(10047),	// school uniform  costume with black aura heavier
	SAMURAI_COSTUME(10048),	// samurai costume
	SAMURAI_1_COSTUME(10049),	// samurai  costume with black aura
	SAMURAI_2_COSTUME(10050),	// samurai  costume with black aura heavier
	ELITE_COSTUME(10051),	// looks like very elite armor costume
	ELITE_1_COSTUME(10052),	// elite armor  costume with black aura
	ELITE_2_COSTUME(10053),	// elite armor  costume with black aura heavier
	SAMURAI_BATTLE_COSTUME(10054),	// samurai battle costume
	SAMURAI_BATTLE_1_COSTUME(10055),	// samurai battle  costume with black aura
	SAMURAI_BATTLE_2_COSTUME(10056),	// samurai battle  costume with black aura heavier
	ROBIN_HOOD_COSTUME(10057),	// robin hood costume
	ROBIN_HOOD_1_COSTUME(10058),	// robin hood  costume with black aura
	ROBIN_HOOD_2_COSTUME(10059),	// robin hood  costume with black aura heavier
	FORMAL_WEAR_COSTUME(10060),	// formal wear costume
	FORMAL_WEAR_1_COSTUME(10061),	// formal wear  costume with black aura
	FORMAL_WEAR_2_COSTUME(10062),	// formal wear  costume with black aura heavier
	CAT_COSTUME2(10063),	// cat armor costume
	CAT_1_COSTUME2(10064),	// cat armor  costume with black aura
	CAT_2_COSTUME2(10065),	// cat armor  costume with black aura heavier
	MAGICIAN_COSTUME(10066),	// magician costume (holloween)
	MAGICIAN_1_COSTUME(10067),	// magician  costume with black aura
	MAGICIAN_2_COSTUME(10068),	// magician  costume with black aura heavier
	BEACH_COSTUME3(10069),	// beach costume
	BEACH_1_COSTUME3(10070),	// beach  costume with black aura
	BEACH_2_COSTUME3(10071),	// beach  costume with black aura heavier
	LITTLE_DEMON_COSTUME(10072),	// little demon (BDSM) costume
	LITTLE_DEMON_1_COSTUME(10073),	// little demon  costume with black aura
	LITTLE_DEMON_2_COSTUME(10074),	// little demon  costume with black aura heavier
	HEAVY_BLUE_ARMOR_COSTUME(10075),	// heavy blue armor costume
	HEAVY_BLUE_ARMOR_1_COSTUME(10076),	// heavy blue armor  costume with black aura
	HEAVY_BLUE_ARMOR_2_COSTUME(10077),	// heavy blue armor  costume with black aura heavier
	ROYAL_KNIGHT_COSTUME(10078),	// royal knight costume
	ROYAL_KNIGHT_1_COSTUME(10079),	// royal knight  costume with black aura
	ROYAL_KNIGHT_2_COSTUME(10080),	// royal knight  costume with black aura heavier
	RED_ROBIN_HOOD_COSTUME(10081),	// red robin hood costume
	RED_ROBIN_HOOD_1_COSTUME(10082),	// red robin hood  costume with black aura
	RED_ROBIN_HOOD_2_COSTUME(10083),	// red robin hood  costume with black aura heavier
	FRANCE_OFFICER_COSTUME(10084),	// france officer costume
	FRANCE_OFFICER_1_COSTUME(10085),	// france officer  costume with black aura
	FRANCE_OFFICER_2_COSTUME(10086),	// france officer  costume with black aura heavier
	BRONZE_WITCH_COSTUME(10087),	// bronze witch costume
	BRONZE_WITCH_1_COSTUME(10088),	// bronze witch  costume with black aura
	BRONZE_WITCH_2_COSTUME(10089),	// bronze witch  costume with black aura heavier
	FAT_MAGICIAN_COSTUME(10090),	// fat magician costume
	FAT_MAGICIAN_1_COSTUME(10091),	// fat magician  costume with black aura
	FAT_MAGICIAN_2_COSTUME(10092),	// fat magician  costume with black aura heavier
	PIRATE_COSTUME(10093),	// pirate costume
	PIRATE_1_COSTUME(10094),	// pirate  costume with black aura
	PIRATE_2_COSTUME(10095),	// pirate  costume with black aura heavier
	KING_COSTUME(10096),	// king costume
	KING_1_COSTUME(10097),	// king costume with black aura
	KING_2_COSTUME(10098),	// king  costume with black aura heavier
	WOLF_LEATHER_COSTUME(10099),	// wolf leather costume
	WOLF_LEATHER_1_COSTUME(10100),	// wolf leather costume with black aura
	WOLF_LEATHER_2_COSTUME(10101),	// wolf leather costume with black aura heavier
	VIKING_COSTUME(10102),	// viking costume
	VIKING_1_COSTUME(10103),	// viking costume with black aura
	VIKING_2_COSTUME(10104),	// viking costume with black aura heavier
	KELBIM_COSTUME(10105),	// black light with wings costume
	KELBIM_1_COSTUME(10106),	// black light  costume with black aura
	KELBIM_2_COSTUME(10107),	// black light  costume with black aura heavier
	CAREBIANS_PIRATE_COSTUME(10108),	// pirate aka carebians costume
	CAREBIANS_PIRATE_1_COSTUME(10109),	// pirate  costume with black aura
	CAREBIANS_PIRATE_2_COSTUME(10110),	// pirate  costume with black aura heavier
	ELF_PRINCE_COSTUME(10111),	// elf prince costume
	ELF_PRINCE_1_COSTUME(10112),	// elf prince  costume with black aura
	ELF_PRINCE_2_COSTUME(10113),	// elf prince  costume with black aura heavier
	BRIGHT_KING_COSTUME(10114),	// bright king costume
	BRIGHT_KING_1_COSTUME(10115),	// bright king costume with black aura
	BRIGHT_KING_2_COSTUME(10116),	// bright king costume with black aura heavier
	WILD_WEST_SHERIFF_COSTUME(10117),	// wild west sheriff costume
	WILD_WEST_SHERIFF_1_COSTUME(10118),	// wild west sheriff costume with black aura
	WILD_WEST_SHERIFF_2_COSTUME(10119),	// wild west sheriff  costume with black aura heavier
	MUSHKETEER_COSTUME(10120),	// mushketeer costume
	MUSHKETEER_1_COSTUME(10121),	// mushketeer costume with black aura
	MUSHKETEER_2_COSTUME(10122),	// mushketeer costume with black aura heavier
	ZAKEN_COSTUME(10123),	// zaken costume
	ZAKEN_1_COSTUME(10124),	// zaken costume with black aura
	ZAKEN_2_COSTUME(10125),	// zaken  costume with black aura heavier
	RED_MOB_COSTUME(10126),	// red mob with horns costume
	RED_MOB_1_COSTUME(10127),	// red mob  costume with black aura
	RED_MOB_2_COSTUME(10128),	// red mob  costume with black aura heavier
	STRANGE_SILVER_COSTUME(10129),	// strange silver costume
	STRANGE_SILVER_1_COSTUME(10130),	// strange silver  costume with black aura
	STRANGE_SILVER_2_COSTUME(10131),	// strange silver costume with black aura heavier
	DARK_KING_COSTUME(10132),	// dark king costume
	DARK_KING_1_COSTUME(10133),	// dark king costume with black aura
	DARK_KING_2_COSTUME(10134),	// dark king  costume with black aura heavier
	WEALTHY_BLUE_TUNIC_COSTUME(10135),	// wealthy blue tunic costume
	WEALTHY_BLUE_TUNIC_1_COSTUME(10136),	// wealthy blue tunic  costume with black aura
	WEALTHY_BLUE_TUNIC_2_COSTUME(10137);	// wealthy blue tunic  costume with black aura heavier

	public static final AbnormalEffect[] VALUES = values();

	public static AbnormalEffect valueOf(int abnormalId) {
		for(AbnormalEffect abnormalEffect : VALUES) {
			if(abnormalEffect.getId() == abnormalId)
				return abnormalEffect;
		}
		return null;
	}

	private final int _id;

	AbnormalEffect()
	{
		_id = ordinal();
	}

	AbnormalEffect(int id)
	{
		_id = id;
	}

	public final int getId()
	{
		return _id;
	}

	public final String getName()
	{
		return toString();
	}
}