package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.items.LockType;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author VISTALL
 * @date 14:13/16.05.2011
 */
public class EffectLockInventory extends EffectHandler
{
	private LockType _lockType;
	private int[] _lockItems;

	public EffectLockInventory(EffectTemplate template)
	{
		super(template);
		_lockType = getParams().getEnum("lockType", LockType.class);
		_lockItems = getParams().getIntegerArray("lockItems");
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		Player player = effector.getPlayer();
		player.getInventory().lockItems(_lockType, _lockItems);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		Player player = effector.getPlayer();
		player.getInventory().unlock();
	}
}
