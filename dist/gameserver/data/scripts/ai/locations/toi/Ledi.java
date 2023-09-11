package ai.locations.toi;

import l2s.gameserver.ai.Mystic;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.AbnormalEffect;

public class Ledi extends Mystic
{
	public Ledi(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		getActor().startAbnormalEffect(AbnormalEffect.ULTIMATE_DEFENCE);
		getActor().getFlags().getDamageBlocked().start(this);
	}

	@Override
	protected void onEvtPartyDied(NpcInstance minion, Creature killer)
	{
		super.onEvtPartyDied(minion, killer);

		NpcInstance actor = getActor();
		if(!actor.getMinionList().hasAliveMinions())
		{
			actor.stopAbnormalEffect(AbnormalEffect.ULTIMATE_DEFENCE);
			actor.getFlags().getDamageBlocked().stop(this);
		}
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		NpcInstance actor = getActor();
		if(actor.getMinionList().hasAliveMinions() && !actor.getFlags().getDamageBlocked().get())
		{
			actor.startAbnormalEffect(AbnormalEffect.ULTIMATE_DEFENCE);
			actor.getFlags().getDamageBlocked().start(this);
		}
		super.onEvtAttacked(attacker, skill, damage);
	}
}