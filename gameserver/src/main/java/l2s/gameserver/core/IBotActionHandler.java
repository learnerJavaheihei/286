package l2s.gameserver.core;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.MonsterInstance;

import java.util.function.Predicate;

public interface IBotActionHandler extends Predicate<Player>
{
	@Override
	default public boolean test(Player actor)
	{
		boolean simpleActionDisable = actor.isActionsDisabled();
		boolean isSitting = actor.isSitting();
		boolean canMove = !isSitting && !actor.isImmobilized() && !simpleActionDisable;
		return this.doAction(actor, this.getConfig(actor), isSitting, canMove, simpleActionDisable);
	}

	public boolean doAction(Player var1, BotConfig var2, boolean var3, boolean var4, boolean var5);

	default public MonsterInstance getMonster(GameObject target)
	{
		if(target != null && target instanceof MonsterInstance)
		{
			return (MonsterInstance) target;
		}
		return null;
	}

	default public BotConfig getConfig(Player player)
	{
		return BotEngine.getInstance().getBotConfig(player);
	}

	default public boolean isActionsDisabledExcludeAttack(Player player)
	{
		return player.isBlocked() || player.isAlikeDead() || player.isStunned() || player.isSleeping() || player.isDecontrolled() || player.isCastingNow() || player.isFrozen();
	}
}