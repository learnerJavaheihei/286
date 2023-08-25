package l2s.gameserver.model.pledge;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.TimeUtils;

/**
 * @author GodWorld
 * @reworked by Bonux
**/
public class ClanWar
{
	public static enum ClanWarState
	{
		PREPARATION, 
		REJECTED, 
		MUTUAL, 
		WIN, 
		LOSS, 
		TIE;
	}

	public static enum WarProgress
	{
		VERY_LOW, 
		LOW, 
		NORMAL, 
		HIGH, 
		VERY_HIGH;
	}

	public static enum ClanWarPeriod
	{
		NEW, 
		PREPARATION, 
		MUTUAL, 
		PEACE;
	}

	public static final long PREPARATION_PERIOD_DURATION = TimeUnit.MILLISECONDS.convert(Config.CLAN_WAR_PREPARATION_DAYS_PERIOD, TimeUnit.DAYS);
	public static final long INACTIVITY_TIME_DURATION = TimeUnit.MILLISECONDS.convert(Config.CLAN_WAR_INACTIVITY_DAYS_PERIOD, TimeUnit.DAYS);
	public static final long PEACE_DURATION = TimeUnit.MILLISECONDS.convert(Config.CLAN_WAR_PEACE_DAYS_PERIOD, TimeUnit.DAYS);

	private final Clan _attackerClan;
	private final Clan _attackedClan;
	private ClanWarPeriod _period;
	private int _currentPeriodStartTime;
	private int _lastKillTime;
	private Future<?> _currentPeriodTask;
	private AtomicInteger _attackersKillCounter = new AtomicInteger();
	private AtomicInteger _attackedKillCounter = new AtomicInteger();

	public ClanWar(Clan attackerClan, Clan attackedClan, ClanWarPeriod period, int currentPeriodStartTime, int lastKillTime, int attackersKillCounter, int attackedKillCounter)
	{
		_attackerClan = attackerClan;
		_attackedClan = attackedClan;
		_period = period;
		_currentPeriodStartTime = currentPeriodStartTime;
		_lastKillTime = lastKillTime;
		_attackersKillCounter.set(attackersKillCounter);
		_attackedKillCounter.set(attackedKillCounter);

		_attackerClan.addWar(_attackedClan.getClanId(), this);
		_attackedClan.addWar(_attackerClan.getClanId(), this);
	}

	public void onKill(Player killer, Player victim)
	{
		Clan killerClan = killer.getClan();
		if(killerClan == null)
			return;

		Clan victimClan = victim.getClan();
		if(victimClan == null)
			return;

		if(getOpposingClan(killerClan) != victimClan)
			return;

		if(_period == ClanWarPeriod.MUTUAL)
		{
			if(victimClan.getReputationScore() > 0)
				killerClan.incReputation(Config.CLAN_WAR_REPUTATION_SCORE_PER_KILL, false, "ClanWar");

			if(killerClan.getReputationScore() > 0)
				victimClan.incReputation(-Config.CLAN_WAR_REPUTATION_SCORE_PER_KILL, false, "ClanWar");

			_lastKillTime = (int) (System.currentTimeMillis() / 1000L);

			if(isAttacker(killerClan))
				_attackersKillCounter.incrementAndGet();
			else if(isAttacked(killerClan))
				_attackedKillCounter.incrementAndGet();

			save(false);

			// TODO: Проверить, должно ли посылаться убийце\убитому.
			victimClan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.BECAUSE_C1_WAS_KILLED_BY_A_CLAN_MEMBER_OF_S2_CLAN_REPUTATION_DECREASED_BY_1).addName(victim).addString(killerClan.getName()));
			killerClan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.BECAUSE_A_CLAN_MEMBER_OF_S1_WAS_KILLED_BY_C2_CLAN_REPUTATION_INCREASED_BY_1).addString(victimClan.getName()).addName(killer));
		}
		else if(_period == ClanWarPeriod.PREPARATION && isAttacker(victimClan))
		{
			// убил в PvP/PK 5 или более игроков клана, который кинул приглашение в войну кланов (противник не должен быть в состоянии хаоса);
			if(!victim.isPK())
			{
				int killCount = _attackedKillCounter.incrementAndGet();
				if(killCount < Config.CLAN_WAR_KILLS_COUNT_TO_CONFIRM_MUTUAL_WAR)
				{
					SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.A_CLAN_MEMBER_OF_S1_WAS_KILLED_BY_YOUR_CLAN_MEMBER_IF_YOUR_CLAN_KILLS_S2_MEMBERS_OF_CLAN_S1_A_CLAN_WAR_WITH_CLAN_S1_WILL_START);
					sm.addString(victimClan.getName());
					sm.addInteger(Config.CLAN_WAR_KILLS_COUNT_TO_CONFIRM_MUTUAL_WAR - killCount);
					killerClan.broadcastToOnlineMembers(sm);
					save(false);
				}
				else
					setPeriod(ClanWarPeriod.MUTUAL);
			}
		}
	}

	public int getPointDiff(Clan clan)
	{
		return isAttacker(clan) ? (getAttackersKillCounter() - getAttackedKillCounter()) : (isAttacked(clan) ? (getAttackedKillCounter() - getAttackersKillCounter()) : 0);
	}

	public WarProgress calculateWarProgress(Clan clan)
	{
		int pointDiff = getPointDiff(clan);
		if(pointDiff <= -50)
			return WarProgress.VERY_LOW;

		if(pointDiff > -50 && pointDiff <= -20)
			return WarProgress.LOW;

		if(pointDiff > -20 && pointDiff <= 19)
			return WarProgress.NORMAL;

		if(pointDiff > 19 && pointDiff <= 49)
			return WarProgress.HIGH;

		return WarProgress.VERY_HIGH;
	}

	public ClanWarState getClanWarState(Clan clan)
	{
		if(_period == ClanWarPeriod.PREPARATION)
			return ClanWarState.PREPARATION;

		if(_period == ClanWarPeriod.MUTUAL)
			return ClanWarState.MUTUAL;

		if(_period == ClanWarPeriod.PEACE)
		{
			int points = getPointDiff(clan);
			if(points == 0)
				return ClanWarState.TIE;

			if(points < 0)
				return ClanWarState.LOSS;

			return ClanWarState.WIN;
		}
		return ClanWarState.REJECTED;
	}

	public boolean isAttacker(Clan clan)
	{
		return _attackerClan == clan;
	}

	public boolean isAttacked(Clan clan)
	{
		return _attackedClan == clan;
	}

	public Clan getAttackerClan()
	{
		return _attackerClan;
	}

	public Clan getAttackedClan()
	{
		return _attackedClan;
	}

	public int getAttackerClanId()
	{
		return _attackerClan.getClanId();
	}

	public int getAttackedClanId()
	{
		return _attackedClan.getClanId();
	}

	public Clan getOpposingClan(Clan clan)
	{
		return isAttacker(clan) ? _attackedClan : (isAttacked(clan) ? _attackerClan : null);
	}

	public int getAttackersKillCounter()
	{
		return _attackersKillCounter.get();
	}

	public int getAttackedKillCounter()
	{
		return _attackedKillCounter.get();
	}

	public int getLastKillTime()
	{
		return _lastKillTime;
	}

	public int getKillToStart()
	{
		return _period == ClanWarPeriod.PREPARATION ? Config.CLAN_WAR_KILLS_COUNT_TO_CONFIRM_MUTUAL_WAR - getAttackedKillCounter() : 0;
	}

	public ClanWarPeriod getPeriod()
	{
		return _period;
	}

	public int getPeriodDuration()
	{
		switch(_period)
		{
			case PREPARATION:
			{
				long clientCorrection = TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS) - PREPARATION_PERIOD_DURATION;
				return (int) (((System.currentTimeMillis() + clientCorrection) / 1000) - _currentPeriodStartTime);
			}
			case PEACE:
			{
				long clientCorrection = TimeUnit.MILLISECONDS.convert(5, TimeUnit.DAYS) - PEACE_DURATION;
				return (int) (((System.currentTimeMillis() + clientCorrection) / 1000) - _currentPeriodStartTime);
			}
		}
		return 0;
	}

	public int getCurrentPeriodStartTime()
	{
		return _currentPeriodStartTime;
	}

	public void restore()
	{
		onChange();
	}

	public boolean start()
	{
		if(_period == ClanWarPeriod.NEW)
		{
			_period = ClanWarPeriod.PREPARATION;

			getAttackerClan().broadcastClanStatus(false, false, true);
			getAttackerClan().broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.YOU_HAVE_DECLARED_A_CLAN_WAR_WITH_S1).addString(getAttackedClan().getName()));

			getAttackedClan().broadcastClanStatus(false, false, true);
			getAttackedClan().broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.S1_HAS_DECLARED_A_CLAN_WAR_THE_WAR_WILL_AUTOMATICALLY_START_IF_YOU_KILL_S1_CLAN_MEMBERS_5_TIMES_WITHIN_A_WEEK).addString(getAttackerClan().getName()));

			onChange();
			return true;
		}
		return false;
	}

	public void accept(Clan requestor)
	{
		if(isAttacked(requestor))
			setPeriod(ClanWarPeriod.MUTUAL);
	}

	public void cancel(Clan requester)
	{
		Clan winnerClan = getOpposingClan(requester);

		if(Config.CLAN_WAR_CANCEL_REPUTATION_PENALTY > 0)
			requester.incReputation(-Config.CLAN_WAR_CANCEL_REPUTATION_PENALTY, true, "ClanWar");

		requester.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN).addString(winnerClan.getName()));

		winnerClan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.YOU_HAVE_WON_THE_WAR_OVER_THE_S1_CLAN).addString(requester.getName()));

		setPeriod(ClanWarPeriod.PEACE);
	}

	public void setPeriod(ClanWarPeriod period)
	{
		if(_period == period)
			return;

		if(_period == ClanWarPeriod.MUTUAL && period == ClanWarPeriod.PREPARATION)
			Log.add("Cannot change clan war period from mutual (when both sides fighting) to preparation.", Log.ClanWar);

		_period = period;
		_currentPeriodStartTime = (int) (System.currentTimeMillis() / 1000L);

		if(period == ClanWarPeriod.MUTUAL)
		{
			getAttackerClan().broadcastClanStatus(false, false, true);
			getAttackerClan().broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.A_CLAN_WAR_WITH_CLAN_S1_HAS_STARTED).addString(getAttackedClan().getName()));

			getAttackedClan().broadcastClanStatus(false, false, true);
			getAttackedClan().broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.A_CLAN_WAR_WITH_CLAN_S1_HAS_STARTED).addString(getAttackerClan().getName()));
		}
		else if(period == ClanWarPeriod.PEACE)
		{
			getAttackerClan().broadcastClanStatus(false, false, true);
			getAttackerClan().broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.WAR_WITH_THE_S1_CLAN_HAS_ENDED).addString(getAttackedClan().getName()));

			getAttackedClan().broadcastClanStatus(false, false, true);
			getAttackedClan().broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.WAR_WITH_THE_S1_CLAN_HAS_ENDED).addString(getAttackerClan().getName()));
		}
		onChange();
	}

	private void onChange()
	{
		if(_currentPeriodTask != null)
		{
			_currentPeriodTask.cancel(true);
			_currentPeriodTask = null;
		}

		if(_period == ClanWarPeriod.PREPARATION)
		{
			long mutualPeriodStartTime = Math.max(0, PREPARATION_PERIOD_DURATION - (System.currentTimeMillis() - (_currentPeriodStartTime * 1000L)));

			_currentPeriodTask = ThreadPoolManager.getInstance().schedule(() -> setPeriod(ClanWarPeriod.MUTUAL), mutualPeriodStartTime);

			if(mutualPeriodStartTime > 0)
				Log.add("Clan war between clans with ID " + getAttackerClan().getClanId() + " and " + getAttackedClan().getClanId() + " in preparation mode. Scheduled for mutual period at " + TimeUtils.toSimpleFormat(System.currentTimeMillis() + mutualPeriodStartTime), Log.ClanWar);
		}
		else if(_period == ClanWarPeriod.MUTUAL)
		{
			long taskDelay = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);

			_currentPeriodTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() ->{
				long lastKillTimeDuration = System.currentTimeMillis() - (_lastKillTime * 1000L);
				if(lastKillTimeDuration > INACTIVITY_TIME_DURATION)
					setPeriod(ClanWarPeriod.PEACE);
			}, taskDelay, taskDelay);

			if(_lastKillTime > 0)
				Log.add("Last kill in clan war between clans with ID " + getAttackerClan().getClanId() + " and " + getAttackedClan().getClanId() + " wat at " + TimeUtils.toSimpleFormat(_lastKillTime * 1000L) + ". Scheduled inactivity check per each hour.", Log.ClanWar);
			else
				Log.add("Last kill in clan war between clans with ID " + getAttackerClan().getClanId() + " and " + getAttackedClan().getClanId() + " has never happened. Scheduled inactivity check per each hour.", Log.ClanWar);
		}
		else if(_period == ClanWarPeriod.PEACE)
		{
			long peaceDurationRemain = Math.max(0, PEACE_DURATION - (System.currentTimeMillis() - (_currentPeriodStartTime * 1000L)));

			_currentPeriodTask = ThreadPoolManager.getInstance().schedule(() ->{
				getAttackerClan().deleteWar(getAttackedClanId());
				//getAttackerClan().broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.THE_WAR_AGAINST_S1_CLAN_HAS_BEEN_STOPPED).addString(getAttackedClan().getName())); TODO: Надо ли?

				getAttackedClan().deleteWar(getAttackerClanId());
				//getAttackedClan().broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.THE_CLAN_S1_HAS_DECIDED_TO_STOP_THE_WAR).addString(getAttackerClan().getName())); TODO: Надо ли?

				ClanTable.getInstance().deleteClanWar(ClanWar.this);
			}, peaceDurationRemain);

			if(peaceDurationRemain > 0)
				Log.add("Clan war between clans " + getAttackerClan().getName() + " and " + getAttackedClan().getName() + " has end. CW scheduled for deletion at " + TimeUtils.toSimpleFormat(System.currentTimeMillis() + peaceDurationRemain) + ".", Log.ClanWar);
		}
		save(true);
	}

	private void save(boolean force)
	{
		ClanTable.getInstance().storeClanWar(this, force);
	}
}