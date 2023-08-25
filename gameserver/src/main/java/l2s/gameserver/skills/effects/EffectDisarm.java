package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectDisarm extends EffectHandler
{
	public EffectDisarm(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(!effected.isPlayer())
			return false;
		Player player = effected.getPlayer();
		// Нельзя снимать/одевать проклятое оружие и флаги
		if(player.getActiveWeaponFlagAttachment() != null)
			return false;
		return true;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		Player player = (Player) effected;

		ItemInstance wpn = player.getActiveWeaponInstance();
		if(wpn != null)
		{
			player.getInventory().unEquipItem(wpn);
			player.sendDisarmMessage(wpn);
		}
		player.getFlags().getWeaponEquipBlocked().start(this);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getWeaponEquipBlocked().stop(this);
	}
}