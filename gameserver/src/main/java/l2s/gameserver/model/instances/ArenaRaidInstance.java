package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.instances.ReflectionBossInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author Bonux
 * Миньоны спавнятся после первой атаки против РБ
 * Данный тип РБ не имеет штрафа уровня (урон, паралич и т.д.)
**/
public class ArenaRaidInstance extends ReflectionBossInstance
{
	public ArenaRaidInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	protected boolean clearReflectionOnDeath()
	{
		return false;
	}

	@Override
	public boolean isArenaRaid()
	{
		return true;
	}
}