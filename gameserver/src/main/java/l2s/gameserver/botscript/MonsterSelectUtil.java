package l2s.gameserver.botscript;

import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.core.Geometry;
import l2s.gameserver.model.*;
import l2s.gameserver.model.instances.MonsterInstance;

import java.util.*;

public class MonsterSelectUtil
{
	public static int getHate(MonsterInstance mob, Player player)
	{
		AggroList.AggroInfo ai = mob.getAggroList().get(player);
		if(ai == null)
		{
			return 0;
		}
		return ai.hate;
	}

	public static MonsterInstance getMob(Creature target)
	{
		if(target != null && target instanceof MonsterInstance)
		{
			return (MonsterInstance) target;
		}
		return null;
	}

	public static MonsterInstance findHatingMeMonster(Player actor)
	{
		List<Creature> aroundObjs = World.getAroundCharacters( actor, 3000, 500);
		for(Creature creature : aroundObjs)
		{
			if(creature == null || !creature.isMonster() || MonsterSelectUtil.getHate(MonsterSelectUtil.getMob(creature), actor) <= 0)
				continue;
			return MonsterSelectUtil.getMob(creature);
		}
		return null;
	}

	public static MonsterInstance findHatingMonster(final Player actor)
	{
		Party party = actor.getParty();
		List<Player> players = party != null ? party.getPartyMembers() : Collections.singletonList(actor);
		LinkedList<MonsterInstance> mobs = new LinkedList<MonsterInstance>();
		List<Creature> aroundObjs = World.getAroundCharacters( actor, 3000, 500);
		for(Creature creature : aroundObjs)
		{
			if(creature == null || !creature.isMonster())
				continue;
			for(Player player : players)
			{
				if(MonsterSelectUtil.getHate(MonsterSelectUtil.getMob(creature), player) <= 0)
					continue;
				mobs.add(MonsterSelectUtil.getMob(creature));
			}
		}
		if(mobs.isEmpty())
		{
			return null;
		}
		// 在自己所有有仇恨的怪物列表中 优先攻击 正在攻击 没有死亡的怪物，死亡后才去寻找最近的
		Creature target = (Creature) actor.getTarget();
		if (target !=null && !target.isDead()) {
			return MonsterSelectUtil.getMob(target);
		}
		mobs.sort(new Comparator<MonsterInstance>()
		{

			@Override
			public int compare(MonsterInstance o1, MonsterInstance o2)
			{
				return Integer.compare(o1.getDistance( actor), o2.getDistance( actor));
			}
		});
		return mobs.get(0);
	}

	public static Optional<MonsterInstance> apply(Player actor)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(actor);
		MonsterInstance mob = MonsterSelectUtil.findHatingMonster(actor);
		if(mob != null && !config.getBlockTarget().containsKey(mob.getObjectId()))
		{
			return Optional.of(mob);
		}
		return World.getAroundNpc( actor, config.getFindMobMaxDistance(), config.getFindMobMaxHeight()).stream().filter(GameObject::isMonster).filter(npc -> !npc.isDead()).filter(npc -> !npc.isRaid()).filter(npc -> !config.getBlockTarget().containsKey(npc.getObjectId()) || config.getBlockTarget().get(npc.getObjectId()) < System.currentTimeMillis()).map(npc -> (MonsterInstance) npc).filter(npc -> Geometry.calc(actor,  npc)).sorted((mob1, mob2) -> Integer.compare(MonsterSelectUtil.getHate(mob1, actor), MonsterSelectUtil.getHate(mob2, actor))).sorted((mob1, mob2) -> Double.compare(actor.getDistance( mob1), actor.getDistance( mob2))).findFirst();
	}
}