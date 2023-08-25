package l2s.gameserver.model;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class GameObjectTasks
{
	public static class DeleteTask implements Runnable
	{
		private final HardReference<? extends Creature> _ref;

		public DeleteTask(Creature c)
		{
			_ref = c.getRef();
		}

		@Override
		public void run()
		{
			Creature c = _ref.get();

			if(c != null)
				c.deleteMe();
		}
	}

	// ============================ Таски для L2Player ==============================
	/** PvPFlagTask */
	public static class PvPFlagTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public PvPFlagTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;

			long diff = Math.abs(System.currentTimeMillis() - player.getLastPvPAttack());
			if(diff > Config.PVP_TIME)
				player.stopPvPFlag();
			else if(diff > Config.PVP_TIME - 20000)
				player.updatePvPFlag(2);
			else
				player.updatePvPFlag(1);
		}
	}

	/** HourlyTask */
	public static class HourlyTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public HourlyTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			// Каждый час в игре оповещаем персонажу сколько часов он играет.
			int hoursInGame = player.getHoursInGame().incrementAndGet();
			player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_BEEN_PLAYING_FOR_AN_EXTENDED_PERIOD_OF_TIME_PLEASE_CONSIDER_TAKING_A_BREAK).addNumber(hoursInGame));
		}
	}

	/** WaterTask */
	public static class WaterTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public WaterTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			if(player.isDead() || !player.isInWater())
			{
				player.stopWaterTask();
				return;
			}

			double reduceHp = player.getMaxHp() < 100 ? 1 : player.getMaxHp() / 100;
			player.reduceCurrentHp(reduceHp, player, null, false, true, true, false, false, false, false);
			player.sendPacket(new SystemMessage(SystemMessage.YOU_RECEIVED_S1_DAMAGE_BECAUSE_YOU_WERE_UNABLE_TO_BREATHE).addNumber((long) reduceHp));
			player.saveDamage(null, null, (int) reduceHp, 3);
		}
	}

	/** KickTask */
	public static class KickTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public KickTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.setOfflineMode(false);
			player.kick();
		}
	}

	/** UnJailTask */
	public static class UnJailTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public UnJailTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;

			player.fromJail();
		}
	}

	/** EndSitDownTask */
	public static class EndSitDownTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public EndSitDownTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.sittingTaskLaunched = false;
			player.getAI().clearNextAction();
		}
	}

	/** EndStandUpTask */
	public static class EndStandUpTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public EndStandUpTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.sittingTaskLaunched = false;
			player.setSitting(false);
			if(!player.getAI().setNextIntention())
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	public static class EndBreakFakeDeathTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public EndBreakFakeDeathTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.setFakeDeath(false); 
			if(!player.getAI().setNextIntention())
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}	
	// ============================ Таски для L2Character ==============================

	/** HitTask */
	public static class HitTask implements Runnable
	{
		boolean _crit, _miss, _shld, _soulshot, _unchargeSS, _notify;
		int _damage;
		int _sAtk;
		int _elementalDamage;
		boolean _elementalCrit;
		private final HardReference<? extends Creature> _charRef, _targetRef;

		public HitTask(Creature cha, Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS, boolean notify, int sAtk, int elementalDamage, boolean elementalCrit)
		{
			_charRef = cha.getRef();
			_targetRef = target.getRef();
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
			_unchargeSS = unchargeSS;
			_notify = notify;
			_sAtk = sAtk;
			_elementalDamage = elementalDamage;
			_elementalCrit = elementalCrit;
		}

		@Override
		public void run()
		{
			Creature character, target;
			if((character = _charRef.get()) == null || (target = _targetRef.get()) == null)
				return;

			if(character.isAttackAborted())
				return;

			character.onHitTimer(target, _damage, _crit, _miss, _soulshot, _shld, _unchargeSS, _elementalDamage, _elementalCrit);

			if(_notify)
				ThreadPoolManager.getInstance().schedule(new NotifyAITask(character, CtrlEvent.EVT_READY_TO_ACT), _sAtk / 2);
		}
	}

	/** Task of AI notification */
	public static class NotifyAITask implements Runnable
	{
		private final CtrlEvent _evt;
		private final Object _agr0;
		private final Object _agr1;
		private final Object _agr2;
		private final HardReference<? extends Creature> _charRef;

		public NotifyAITask(Creature cha, CtrlEvent evt, Object agr0, Object agr1, Object agr2)
		{
			_charRef = cha.getRef();
			_evt = evt;
			_agr0 = agr0;
			_agr1 = agr1;
			_agr2 = agr2;
		}

		public NotifyAITask(Creature cha, CtrlEvent evt)
		{
			this(cha, evt, null, null, null);
		}

		@Override
		public void run()
		{
			Creature character = _charRef.get();
			if(character == null || !character.hasAI())
				return;

			character.getAI().notifyEvent(_evt, _agr0, _agr1, _agr2);
		}
	}
}