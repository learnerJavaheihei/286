package l2s.gameserver.skills.skillclasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.model.instances.residences.SiegeFlagInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.FuncMul;
import l2s.gameserver.templates.StatsSet;

public class SummonSiegeFlag extends Skill
{
	public static enum FlagType
	{
		DESTROY,
		NORMAL,
		ADVANCED,
		OUTPOST
	}

	private final FlagType _flagType;
	private final double _advancedMult;

	public SummonSiegeFlag(StatsSet set)
	{
		super(set);
		_flagType = set.getEnum("flagType", FlagType.class);
		_advancedMult = set.getDouble("advancedMultiplier", 1.);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(!super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first, sendMsg, trigger))
			return false;

		if(!activeChar.isPlayer())
			return false;

		Player player = (Player) activeChar;
		if(player.getClan() == null || !player.isClanLeader())
			return false;

		switch(_flagType)
		{
			case DESTROY:
				//
				break;
			case OUTPOST:
			case NORMAL:
			case ADVANCED:
				if(player.isInZone(Zone.ZoneType.RESIDENCE))
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}

				List<SiegeEvent<?, ?>> siegeEvents = new ArrayList<SiegeEvent<?, ?>>();

				for(SiegeEvent<?, ?> siegeEvent : activeChar.getEvents(SiegeEvent.class))
				{
					List<ZoneObject> zones = siegeEvent.getObjects(SiegeEvent.FLAG_ZONES);
					for(ZoneObject zone : zones)
					{
						if(player.isInZone(zone.getZone()))
						{
							siegeEvents.add(siegeEvent);
							break;
						}
					}
				}

				if(siegeEvents.isEmpty())
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}

				boolean isAttacker = false;
				boolean haveFlag = false;
				for(SiegeEvent<?, ?> siegeEvent : siegeEvents)
				{
					SiegeClanObject siegeClan = siegeEvent.getSiegeClan(SiegeEvent.ATTACKERS, player.getClan());
					if(siegeClan != null)
					{
						isAttacker = true;
						if(siegeClan.getFlag() != null)
							haveFlag = true;
					}
				}

				if(!isAttacker)
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_THE_ENCAMPMENT_BECAUSE_YOU_ARE_NOT_A_MEMBER_OF_THE_SIEGE_CLAN_INVOLVED_IN_THE_CASTLE__FORTRESS__HIDEOUT_SIEGE, new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}

				if(haveFlag)
				{
					player.sendPacket(SystemMsg.AN_OUTPOST_OR_HEADQUARTERS_CANNOT_BE_BUILT_BECAUSE_ONE_ALREADY_EXISTS, new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}
				break;
		}
		return true;
	}

	@Override
	public void onEndCast(Creature activeChar, Set<Creature> targets)
	{
		super.onEndCast(activeChar, targets);

		if(!activeChar.isPlayer())
			return;

		final Player player = activeChar.getPlayer();

		final Clan clan = player.getClan();
		if(clan == null || !player.isClanLeader())
			return;

		switch(_flagType)
		{
			case DESTROY:
				for(SiegeEvent<?, ?> siegeEvent : activeChar.getEvents(SiegeEvent.class))
				{
					SiegeClanObject siegeClan = siegeEvent.getSiegeClan(SiegeEvent.ATTACKERS, clan);
					if(siegeClan != null)
					{
						List<ZoneObject> zones = siegeEvent.getObjects(SiegeEvent.FLAG_ZONES);
						for(ZoneObject zone : zones)
						{
							if(player.isInZone(zone.getZone()))
							{
								siegeClan.deleteFlag();
								break;
							}
						}
					}
				}
				break;
			default:
				List<SiegeEvent<?, ?>> siegeEvents = new ArrayList<SiegeEvent<?, ?>>();
				List<SiegeClanObject> siegeClans = new ArrayList<SiegeClanObject>();
				for(SiegeEvent<?, ?> siegeEvent : activeChar.getEvents(SiegeEvent.class))
				{
					SiegeClanObject siegeClan = siegeEvent.getSiegeClan(SiegeEvent.ATTACKERS, clan);
					if(siegeClan != null && siegeClan.getFlag() == null)
					{
						List<ZoneObject> zones = siegeEvent.getObjects(SiegeEvent.FLAG_ZONES);
						for(ZoneObject zone : zones)
						{
							if(player.isInZone(zone.getZone()))
							{
								siegeEvents.add(siegeEvent);
								siegeClans.add(siegeClan);
								break;
							}
						}
					}
				}

				if(siegeClans.isEmpty())
					return;

				// 35062/36590
				SiegeFlagInstance flag = (SiegeFlagInstance) NpcHolder.getInstance().getTemplate(_flagType == FlagType.OUTPOST ? 36590 : 35062).getNewInstance();
				flag.setClan(clan);

				for(SiegeEvent<?, ?> siegeEvent : siegeEvents)
					flag.addEvent(siegeEvent);

				if(_flagType == FlagType.ADVANCED)
					flag.getStat().addFuncs(new FuncMul(Stats.MAX_HP, 0x50, flag, _advancedMult, StatsSet.EMPTY));

				flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp(), true);
				flag.setHeading(player.getHeading());

				// Ставим флаг перед чаром
				int x = (int) (player.getX() + 100 * Math.cos(player.headingToRadians(player.getHeading() - 32768)));
				int y = (int) (player.getY() + 100 * Math.sin(player.headingToRadians(player.getHeading() - 32768)));
				Location loc = GeoEngine.moveCheck(player.getX(), player.getY(), player.getZ(), x, y, player.getGeoIndex());
				if(loc == null)
					loc = Location.findAroundPosition(player.getLoc(), 100, player.getGeoIndex());
				flag.spawnMe(loc);

				for(SiegeClanObject siegeClan : siegeClans)
					siegeClan.setFlag(flag);
				break;
		}
	}
}