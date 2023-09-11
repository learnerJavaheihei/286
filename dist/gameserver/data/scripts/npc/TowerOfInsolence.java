package npc;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @reworked by Bonux
**/
public class TowerOfInsolence extends ListenerHook implements OnInitScriptListener
{
	// Item's
	private static final int ENERGY_OF_INSOLENCE_ITEM_ID = 49685;	// Энергия Дерзости
	private static final int UNIDENTIFIED_STONE_ITEM_ID = 49766;	// Неопознанный Камень

	private static final int[] ENERGY_OF_INSOLENCE_NPC_IDS = {
		20977,	// Леди Эльморедена
		21081	// Могущественный Ангел Амон
	};

	private static final int[] UNIDENTIFIED_STONE_NPC_IDS = {
		20823,	// Воитель Платинового Клана
		20826,	// Стрелок Платинового Клана
		20827,	// Воин Платинового Клана
		20828,	// Шаман Платинового Клана
		20830,	// Ангел Хранитель
		20831,	// Ангел Печати
		20983,	// Латник Псоглавов
		20984,	// Воин Невольник
		20985,	// Стрелок Невольник
		21062,	// Ангел Посланник
		21064,	// Стрелок Платиновых Хранителей
		21065,	// Воин Платиновых Хранителей
		21066,	// Шаман Платиновых Хранителей
		21067,	// Архангел Хранитель
		21069, 	// Владыка Платиновых Хранителей
		21072,	// Вождь Платиновых Хранителей
		21074,	// Маг Невольник
		21079	// Тварь Валака
	};

	private static final int ENERGY_OF_INSOLENCE_MIN_DROP_COUNT = 1;	// TODO: Вынести в конфиг?
	private static final int ENERGY_OF_INSOLENCE_MAX_DROP_COUNT = 2;	// TODO: Вынести в конфиг?

	@Override
	public void onInit()
	{
		for(int npcId : ENERGY_OF_INSOLENCE_NPC_IDS)
			addHookNpc(ListenerHookType.NPC_KILL, npcId);
		for(int npcId : UNIDENTIFIED_STONE_NPC_IDS)
			addHookNpc(ListenerHookType.NPC_KILL, npcId);
	}

	@Override
	public void onNpcKill(NpcInstance npc, Player killer)
	{
		if(ArrayUtils.contains(ENERGY_OF_INSOLENCE_NPC_IDS, npc.getNpcId()))
		{
			npc.dropItem(killer, ENERGY_OF_INSOLENCE_ITEM_ID, Rnd.get(ENERGY_OF_INSOLENCE_MIN_DROP_COUNT, ENERGY_OF_INSOLENCE_MAX_DROP_COUNT));
		}

		if(ArrayUtils.contains(UNIDENTIFIED_STONE_NPC_IDS, npc.getNpcId()))
		{
			if((killer.getLevel() - npc.getLevel()) <= 9 && Rnd.chance(4))
				npc.dropItem(killer, UNIDENTIFIED_STONE_ITEM_ID, 1);
		}
	}
}