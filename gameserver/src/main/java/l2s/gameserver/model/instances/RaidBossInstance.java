package l2s.gameserver.model.instances;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectTasks;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.HeroDiary;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.NpcUtils;

public class RaidBossInstance extends MonsterInstance
{
	private class BerserkTask implements Runnable
	{
		private final boolean _prepare;

		public BerserkTask(boolean prepare)
		{
			_prepare = prepare;
		}

		@Override
		public void run()
		{
			if(_prepare)
			{
				_raidBerserkTask = ThreadPoolManager.getInstance().schedule(new BerserkTask(false), TimeUnit.MINUTES.toMillis(5));
				broadcastPacket(new ExShowScreenMessage(NpcString._5_MINUTES_UNTIL_RAID_BOSS_GOES_BERSERK, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, true));
			}
			else
			{
				_raidBerserkTask = null;
				altUseSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, RAID_BERSERK_SKILL_ID, 1), RaidBossInstance.this);
			}
		}
	}

	private static final int RAID_BERSERK_SKILL_ID = 15458;

	private ScheduledFuture<?> _raidBerserkTask = null;

	private final boolean _canRaidBerserk;
	private final boolean _spawnDeathKnight;

	public RaidBossInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);

		_canRaidBerserk = getParameter("can_raid_berserk", !isBoss() && !isReflectionBoss());
		_spawnDeathKnight = getParameter("spawn_death_knight", true);
	}

	@Override
	public boolean isRaid()
	{
		return true;
	}

	@Override
	public double getRewardRate(Player player)
	{
		return Config.RATE_DROP_ITEMS_RAIDBOSS; // ПА не увеличивает дроп с рейдов
	}

	@Override
	public double getDropChanceMod(Player player)
	{
		return Config.DROP_CHANCE_MODIFIER_RAIDBOSS; // ПА не увеличивает дроп с боссов
	}

	@Override
	public double getDropCountMod(Player player)
	{
		return Config.DROP_COUNT_MODIFIER_RAIDBOSS; // ПА не увеличивает дроп с боссов
	}

	@Override
	protected void onDeath(Creature killer)
	{
		stopRaidBerserkTask();

		if(isReflectionBoss())
		{
			super.onDeath(killer);
			return;
		}

		if(killer != null && killer.isPlayable())
		{
			Player player = killer.getPlayer();
			if(player.isInParty())
			{
				for(Player member : player.getParty().getPartyMembers())
					if(member.isHero())
						Hero.getInstance().addHeroDiary(member.getObjectId(), HeroDiary.ACTION_RAID_KILLED, getNpcId());
				player.getParty().broadCast(SystemMsg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
			}
			else
			{
				if(player.isHero())
					Hero.getInstance().addHeroDiary(player.getObjectId(), HeroDiary.ACTION_RAID_KILLED, getNpcId());
				player.sendPacket(SystemMsg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
			}

			Quest q = QuestHolder.getInstance().getQuest(508);
			if(q != null)
			{
				if(player.getClan() != null && player.getClan().getLeader().isOnline())
				{
					QuestState st = player.getClan().getLeader().getPlayer().getQuestState(q);
					if(st != null)
						st.getQuest().onKill(this, st);
				}
			}
		}

		int boxId = 0;
		switch(getNpcId())
		{
			case 25035: // Shilens Messenger Cabrio
				boxId = 31027;
				break;
			case 25054: // Demon Kernon
				boxId = 31028;
				break;
			case 25126: // Golkonda, the Longhorn General
				boxId = 31029;
				break;
			case 25220: // Death Lord Hallate
				boxId = 31030;
				break;
		}

		if(boxId != 0)
		{
			NpcTemplate boxTemplate = NpcHolder.getInstance().getTemplate(boxId);
			if(boxTemplate != null)
			{
				final NpcInstance box = new NpcInstance(IdFactory.getInstance().getNextId(), boxTemplate, StatsSet.EMPTY);
				box.spawnMe(getLoc());
				box.setSpawnedLoc(getLoc());
				box.startDeleteTask(60000L);
			}
		}
		
		if(killer != null && killer.getPlayer() != null && Config.RAID_DROP_GLOBAL_ITEMS)
		{
			if(getLevel() >= Config.MIN_RAID_LEVEL_TO_DROP)
			{
				for(Config.RaidGlobalDrop drop_inf : Config.RAID_GLOBAL_DROP)
				{
					int id = drop_inf.getId();
					long count = drop_inf.getCount();
					double chance = drop_inf.getChance();
					if(Rnd.chance(chance))
						ItemFunctions.addItem(killer.getPlayer(), id, count, true);
				}
			}
		}

		if(_spawnDeathKnight && !isBoss() && getReflection().isMain() && Rnd.chance(10))
		{
			int knightId = 0;
			if(getLevel() >= 20 && getLevel() < 30)
				knightId = 25787;
			else if(getLevel() >= 30 && getLevel() < 40)
				knightId = 25788;
			else if(getLevel() >= 40 && getLevel() < 50)
				knightId = 25789;
			else if(getLevel() >= 50 && getLevel() < 60)
				knightId = 25790;
			else if(getLevel() >= 60 && getLevel() < 70)
				knightId = 25791;
			else if(getLevel() >= 70 && getLevel() < 80)
				knightId = 25792;

			if(knightId > 0)
			{
				NpcInstance npc = NpcUtils.spawnSingle(knightId, getLoc(), getReflection(), 900000L); // TODO: Проверить процесс деспавна на оффе.
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 1000);
			}
		}	

		super.onDeath(killer);

		RaidBossSpawnManager.getInstance().onBossDeath(this);
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		addSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4045, 1)); // Resist Full Magic Attack
		RaidBossSpawnManager.getInstance().onBossSpawned(this);
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public boolean isThrowAndKnockImmune()
	{
		return true;
	}

	@Override
	public boolean isTransformImmune()
	{
		return true;
	}

	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}

	@Override
	public void onZoneEnter(Zone zone)
	{
		if(!zone.checkIfInZone(getSpawnedLoc().getX(), getSpawnedLoc().getY(), getSpawnedLoc().getZ()))
		{
			if(zone.getType() == ZoneType.peace_zone || zone.getType() == ZoneType.battle_zone || zone.getType() == ZoneType.SIEGE)
				getAI().returnHomeAndRestore(isRunning());
		}
	}

	@Override
	protected void onDespawn()
	{
		super.onDespawn();
		stopRaidBerserkTask();
	}

	public void startRaidBerserkTask()
	{
		
		/*移除BOSS狂暴if(!_canRaidBerserk)*/
			return;

		/*移除BOSS狂暴
		if(_raidBerserkTask != null)
			return;

		if(getAbnormalList().contains(RAID_BERSERK_SKILL_ID))
			return;

		_raidBerserkTask = ThreadPoolManager.getInstance().schedule(new BerserkTask(true), TimeUnit.MINUTES.toMillis(10));
		*/
	}

	public void stopRaidBerserkTask()
	{
		if(_raidBerserkTask != null)
		{
			_raidBerserkTask.cancel(false);
			_raidBerserkTask = null;
		}
	}
}