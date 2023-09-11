package npc;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import l2s.commons.util.Rnd;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author Bonux
**/
public class CallOfSpirits extends ListenerHook implements OnInitScriptListener
{
	private static final TIntIntMap NPCS = new TIntIntHashMap();
	static
	{
		// Пустошь
		NPCS.put(20068, 21656);	// Монстроглаз Разрушитель / Голем
		NPCS.put(20083, 21656);	// Гранитный Голем / Голем
		// Земля Казненных
		NPCS.put(20201, 21654);	// Вурдалак / Дрэко
		NPCS.put(20202, 21654);	// Поедатель Мертвечины / Дрэко
		// Долина Ящеров
		NPCS.put(20580, 21655);	// Воин Ящеров Лито / Рейдо
		NPCS.put(20581, 21655);	// Шаман Ящеров Лито / Рейдо
		// Море Спор
		NPCS.put(20556, 21657);	// Гигантский Монстроглаз / Змей
		NPCS.put(20557, 21657);	// Жуткий Змей / Змей
		// Лес Зеркал
		NPCS.put(20643, 21658);	// Воин Ящеров Харит / Харит
		NPCS.put(20645, 21658);	// Вождь Ящеров Харит / Харит
		// Запретные Врата
		NPCS.put(20670, 21660);	// Синий Дрейк / Фалибати
		NPCS.put(20673, 21660);	// Фалибати / Фалибати
	}

	@Override
	public void onInit()
	{
		for(int npcId : NPCS.keys())
			addHookNpc(ListenerHookType.NPC_KILL, npcId);
	}

	@Override
	public void onNpcKill(NpcInstance npc, Player killer)
	{
		int spawnNpcId = NPCS.get(npc.getNpcId());
		if(spawnNpcId > 0 && Rnd.chance(7 * (killer.isInParty() ? 2 : 1)))
		{
			int count = 1;
			if(Rnd.chance(20))
				count = 2;
			else if(Rnd.chance(5))
				count = 3;

			for(int i = 0; i < count; i++)
				NpcUtils.spawnSingle(spawnNpcId, npc.getLoc(), npc.getReflection(), 60000);
		}
	}
}