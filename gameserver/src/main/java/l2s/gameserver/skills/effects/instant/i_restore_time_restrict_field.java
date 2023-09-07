package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.data.xml.holder.TimeRestrictFieldHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.variables.PlayerVariables;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.timerestrictfield.ExTimeRestrictFieldUserChargeResult;
import l2s.gameserver.templates.TimeRestrictFieldInfo;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author nexvill
 **/
public class i_restore_time_restrict_field extends i_abstract_effect
{
	public i_restore_time_restrict_field(EffectTemplate template)
	{
		super(template);
	}
	
	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		return effected.isPlayer();
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		final int fieldId = (int) getValue();
		
		final Player player = effected.getPlayer();
		
		final TimeRestrictFieldInfo field = TimeRestrictFieldHolder.getInstance().getFields().get(fieldId);
		if (field == null)
		{
			return;
		}
		int reflectionId = 0;
		switch (field.getFieldId())
		{
			case 2:
			{
				reflectionId = -1000;
				break;
			}
		}
		
		int remainTimeRefill = player.getVarInt(PlayerVariables.RESTRICT_FIELD_TIMELEFT + "_" + reflectionId
				+ "_refill", field.getRemainTimeMax() - field.getRemainTimeBase());
		if (remainTimeRefill > 0)
		{
			boolean isInTimeRestrictField = player.getReflection().getId() < -4 ? true : false;
			player.stopTimedHuntingZoneTask(false);
			int newRemainTimeRefill = remainTimeRefill - field.getRemainTimeBase();
			player.setVar(PlayerVariables.RESTRICT_FIELD_TIMELEFT + "_" + reflectionId + "_refill", newRemainTimeRefill);
			int remainTime = player.getVarInt(PlayerVariables.RESTRICT_FIELD_TIMELEFT + "_" + reflectionId,
					field.getRemainTimeBase());
			remainTime += field.getRemainTimeBase();
			player.setVar(PlayerVariables.RESTRICT_FIELD_TIMELEFT + "_" + reflectionId, remainTime);
			player.sendPacket(new ExTimeRestrictFieldUserChargeResult(fieldId, remainTime,
					field.getRemainTimeBase() / 60,newRemainTimeRefill));
			if (isInTimeRestrictField)
			{
				player.startTimeRestrictField();
			}
		}
		else
		{
			player.sendPacket(SystemMsg.YOU_WILL_EXCEED_THE_MAX_AMOUNT_OF_TIME_FOR_THE_HUNTING_ZONE_SO_YOU_CANNOT_ADD_ANY_MORE_TIME);
		}
	}
}