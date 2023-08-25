package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.templates.npc.NpcTemplate;

public class ReflectionBossInstance extends RaidBossInstance
{
	private final static int COLLAPSE_AFTER_DEATH_TIME = 5; // 5 мин

	public ReflectionBossInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);

		if(clearReflectionOnDeath())
			clearReflection();
	}

	protected boolean clearReflectionOnDeath()
	{
		return true;
	}

	/**
	 * Удаляет все спауны из рефлекшена и запускает 5ти минутный коллапс-таймер.
	 */
	protected void clearReflection()
	{
		Reflection reflection = getReflection();
		if(!reflection.isDefault())
			reflection.startCollapseTimer(COLLAPSE_AFTER_DEATH_TIME, true);
	}

	@Override
	public boolean isReflectionBoss()
	{
		return true;
	}
}