package npc.model;

import java.util.concurrent.Future;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.network.l2.s2c.NSPacket;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import manager.FourSepulchersSpawn;

public class SepulcherMonsterInstance extends MonsterInstance
{
	public int mysteriousBoxId = 0;

	private Future<?> _victimShout = null;
	private Future<?> _victimSpawnKeyBoxTask = null;
	private Future<?> _changeImmortalTask = null;
	private Future<?> _onDeadEventTask = null;
	private final static int HALLS_KEY = 7260;

	public SepulcherMonsterInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	protected void onSpawn()
	{
		switch(getNpcId())
		{
			case 18150:
			case 18151:
			case 18152:
			case 18153:
			case 18154:
			case 18155:
			case 18156:
			case 18157:
				if(_victimSpawnKeyBoxTask != null)
					_victimSpawnKeyBoxTask.cancel(false);
				_victimSpawnKeyBoxTask = ThreadPoolManager.getInstance().schedule(new VictimSpawnKeyBox(this), 300000);
				if(_victimShout != null)
					_victimShout.cancel(false);
				_victimShout = ThreadPoolManager.getInstance().schedule(new VictimShout(this), 5000);
				break;
			case 18196:
			case 18197:
			case 18198:
			case 18199:
			case 18200:
			case 18201:
			case 18202:
			case 18203:
			case 18204:
			case 18205:
			case 18206:
			case 18207:
			case 18208:
			case 18209:
			case 18210:
			case 18211:
				break;
			case 18231:
			case 18232:
			case 18233:
			case 18234:
			case 18235:
			case 18236:
			case 18237:
			case 18238:
			case 18239:
			case 18240:
			case 18241:
			case 18242:
			case 18243:
				if(_changeImmortalTask != null)
					_changeImmortalTask.cancel(false);
				_changeImmortalTask = ThreadPoolManager.getInstance().schedule(new ChangeImmortal(this), 1600);
				break;
			case 18256:
				break;
		}
		super.onSpawn();
	}

	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);

		switch(getNpcId())
		{
			case 18120:
			case 18121:
			case 18122:
			case 18123:
			case 18124:
			case 18125:
			case 18126:
			case 18127:
			case 18128:
			case 18129:
			case 18130:
			case 18131:
			case 18141:
			case 18142:
			case 18143:
			case 18144:
			case 18158:
			case 18159:
			case 18160:
			case 18161:
			case 18162:
			case 18163:
			case 18164:
			case 18165:
			case 18183:
			case 18184:
			case 18212:
			case 18213:
			case 18214:
			case 18215:
			case 18216:
			case 18217:
			case 18218:
			case 18219:
				if(_onDeadEventTask != null)
					_onDeadEventTask.cancel(false);
				_onDeadEventTask = ThreadPoolManager.getInstance().schedule(new OnDeadEvent(this), 3500);
				break;

			case 18150:
			case 18151:
			case 18152:
			case 18153:
			case 18154:
			case 18155:
			case 18156:
			case 18157:
				if(_victimSpawnKeyBoxTask != null)
				{
					_victimSpawnKeyBoxTask.cancel(false);
					_victimSpawnKeyBoxTask = null;
				}
				if(_victimShout != null)
				{
					_victimShout.cancel(false);
					_victimShout = null;
				}
				if(_onDeadEventTask != null)
					_onDeadEventTask.cancel(false);
				_onDeadEventTask = ThreadPoolManager.getInstance().schedule(new OnDeadEvent(this), 3500);
				break;

			case 18145:
			case 18146:
			case 18147:
			case 18148:
			case 18149:
				if(FourSepulchersSpawn.isViscountMobsAnnihilated(mysteriousBoxId) && !hasPartyAKey(killer.getPlayer()))
				{
					if(_onDeadEventTask != null)
						_onDeadEventTask.cancel(false);
					_onDeadEventTask = ThreadPoolManager.getInstance().schedule(new OnDeadEvent(this), 3500);
				}
				break;

			case 18220:
			case 18221:
			case 18222:
			case 18223:
			case 18224:
			case 18225:
			case 18226:
			case 18227:
			case 18228:
			case 18229:
			case 18230:
			case 18231:
			case 18232:
			case 18233:
			case 18234:
			case 18235:
			case 18236:
			case 18237:
			case 18238:
			case 18239:
			case 18240:
				if(FourSepulchersSpawn.isDukeMobsAnnihilated(mysteriousBoxId))
				{
					if(_onDeadEventTask != null)
						_onDeadEventTask.cancel(false);
					_onDeadEventTask = ThreadPoolManager.getInstance().schedule(new OnDeadEvent(this), 3500);
				}
				break;
			case 18256:
				randomReward(killer);
		}
	}

	@Override
	public boolean hasRandomWalk()
	{
		if(getNpcId() >= 18231 && getNpcId() <= 18243)
			return false;
		return super.hasRandomWalk();
	}

	@Override
	protected void onDelete()
	{
		if(_victimSpawnKeyBoxTask != null)
		{
			_victimSpawnKeyBoxTask.cancel(false);
			_victimSpawnKeyBoxTask = null;
		}
		if(_onDeadEventTask != null)
		{
			_onDeadEventTask.cancel(false);
			_onDeadEventTask = null;
		}

		super.onDelete();
	}

	private class VictimShout implements Runnable
	{
		private final SepulcherMonsterInstance _activeChar;

		public VictimShout(SepulcherMonsterInstance activeChar)
		{
			_activeChar = activeChar;
		}

		@Override
		public void run()
		{
			if(_activeChar.isDead())
				return;

			if(!_activeChar.isVisible())
				return;

			broadcastPacket(new NSPacket(SepulcherMonsterInstance.this, ChatType.NPC_ALL, NpcString.HELP_ME));
		}
	}

	private class VictimSpawnKeyBox implements Runnable
	{
		private final SepulcherMonsterInstance _activeChar;

		public VictimSpawnKeyBox(SepulcherMonsterInstance activeChar)
		{
			_activeChar = activeChar;
		}

		@Override
		public void run()
		{
			if(_activeChar.isDead())
				return;

			if(!_activeChar.isVisible())
				return;

			FourSepulchersSpawn.spawnKeyBox(_activeChar);
			broadcastPacket(new NSPacket(SepulcherMonsterInstance.this, ChatType.NPC_ALL, NpcString.THANK_YOU_FOR_SAVING_ME));
			if(_victimShout != null)
			{
				_victimShout.cancel(false);
				_victimShout = null;
			}
		}
	}

	private class OnDeadEvent implements Runnable
	{
		SepulcherMonsterInstance _activeChar;

		public OnDeadEvent(SepulcherMonsterInstance activeChar)
		{
			_activeChar = activeChar;
		}

		@Override
		public void run()
		{
			switch(_activeChar.getNpcId())
			{
				case 18120:
				case 18121:
				case 18122:
				case 18123:
				case 18124:
				case 18125:
				case 18126:
				case 18127:
				case 18128:
				case 18129:
				case 18130:
				case 18131:
				case 18141:
				case 18142:
				case 18143:
				case 18144:
				case 18158:
				case 18159:
				case 18160:
				case 18161:
				case 18162:
				case 18163:
				case 18164:
				case 18165:
				case 18183:
				case 18184:
				case 18212:
				case 18213:
				case 18214:
				case 18215:
				case 18216:
				case 18217:
				case 18218:
				case 18219:
					FourSepulchersSpawn.spawnKeyBox(_activeChar);
					break;

				case 18150:
				case 18151:
				case 18152:
				case 18153:
				case 18154:
				case 18155:
				case 18156:
				case 18157:
					FourSepulchersSpawn.spawnExecutionerOfHalisha(_activeChar);
					break;

				case 18145:
				case 18146:
				case 18147:
				case 18148:
				case 18149:
					FourSepulchersSpawn.spawnMonster(_activeChar.mysteriousBoxId);
					break;

				case 18220:
				case 18221:
				case 18222:
				case 18223:
				case 18224:
				case 18225:
				case 18226:
				case 18227:
				case 18228:
				case 18229:
				case 18230:
				case 18231:
				case 18232:
				case 18233:
				case 18234:
				case 18235:
				case 18236:
				case 18237:
				case 18238:
				case 18239:
				case 18240:
					FourSepulchersSpawn.spawnArchonOfHalisha(_activeChar.mysteriousBoxId);
					break;
			}
		}
	}

	private class ChangeImmortal implements Runnable
	{
		private final SepulcherMonsterInstance activeChar;

		public ChangeImmortal(SepulcherMonsterInstance mob)
		{
			activeChar = mob;
		}

		@Override
		public void run()
		{
			Skill fp = SkillHolder.getInstance().getSkill(4616, 1); // Invulnerable by petrification
			fp.getEffects(activeChar, activeChar);
		}
	}

	private boolean hasPartyAKey(Player player)
	{
		if (player.getParty() == null)
			return false;
		
		for(Player m : player.getParty().getPartyMembers())
			if(ItemFunctions.getItemCount(m, HALLS_KEY) > 0)
				return true;
		return false;
	}

	private void randomReward(Creature killer) {
		int id = getNpcId();
		if(id == 18256) {
			if(Rnd.chance(20))
				dropItem(killer.getPlayer(), 57, 10000);
			else if(Rnd.chance(20))
				dropItem(killer.getPlayer(), 57, 100000);
			else if(Rnd.chance(20))
				dropItem(killer.getPlayer(), 57, 200000);
			else if(Rnd.chance(20))
				dropItem(killer.getPlayer(), 57, 1000000);
			else if(Rnd.chance(10))
				dropItem(killer.getPlayer(), 2133, 1);
			else if(Rnd.chance(15))
				dropItem(killer.getPlayer(), 91397, 2);
			else if(Rnd.chance(15))
				dropItem(killer.getPlayer(), 91398, 1);
			else if(Rnd.chance(20))
				dropItem(killer.getPlayer(), 91393, 1);
			else if(Rnd.chance(20))
				dropItem(killer.getPlayer(), 91394, 1);
			else if(Rnd.chance(20))
				dropItem(killer.getPlayer(), 91577, 1);
			else if(Rnd.chance(7))
				dropItem(killer.getPlayer(), 49786, 1);
			else if(Rnd.chance(7))
				dropItem(killer.getPlayer(), 49785, 1);
			else {
				dropItem(killer.getPlayer(), Rnd.get(6688, 6714), 1);
			}
		}
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}