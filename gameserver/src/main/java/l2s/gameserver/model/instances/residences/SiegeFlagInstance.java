package l2s.gameserver.model.instances.residences;

import org.apache.commons.lang3.StringUtils;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.npc.NpcTemplate;

public class SiegeFlagInstance extends NpcInstance
{
	private Clan _owner;
	private long _lastAnnouncedAttackedTime = 0;

	public SiegeFlagInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
		setHasChatWindow(false);
	}

	@Override
	public String getName()
	{
		return _owner.getName();
	}

	@Override
	public Clan getClan()
	{
		return _owner;
	}

	@Override
	public String getTitle()
	{
		return StringUtils.EMPTY;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Player player = attacker.getPlayer();
		if(player == null || isInvulnerable())
			return false;
		Clan clan = player.getClan();
		return clan == null || _owner != clan;
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return true;
	}

	@Override
	protected void onDeath(Creature killer)
	{
		for(SiegeEvent<?, ?> siegeEvent : getEvents(SiegeEvent.class))
		{
			SiegeClanObject siegeClan = siegeEvent.getSiegeClan(SiegeEvent.ATTACKERS, _owner);
			if(siegeClan != null)
				siegeClan.setFlag(null);
		}
		super.onDeath(killer);
	}

	@Override
	protected void onReduceCurrentHp(final double damage, final Creature attacker, Skill skill, final boolean awake, final boolean standUp, boolean directHp, boolean isDot)
	{
		if(System.currentTimeMillis() - _lastAnnouncedAttackedTime > 120000)
		{
			_lastAnnouncedAttackedTime = System.currentTimeMillis();
			_owner.broadcastToOnlineMembers(SystemMsg.YOUR_BASE_IS_BEING_ATTACKED);
		}

		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, isDot);
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isPeaceNpc()
	{
		return false;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isThrowAndKnockImmune()
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
	public boolean isHealBlocked()
	{
		return true;
	}

	@Override
	public boolean isEffectImmune(Creature caster)
	{
		return true;
	}

	public void setClan(Clan owner)
	{
		_owner = owner;
	}
}