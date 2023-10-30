package services;

import l2s.commons.util.Rnd;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.utils.Functions;

/**
 * @author SanyaDC
**/
public class LcoinDrop implements OnInitScriptListener
{
	private static class DeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature cha, Creature killer)
		{
			if(!cha.isMonster())
				return;

			MonsterInstance monster = (MonsterInstance) cha;

			Creature topDamager = monster.getAggroList().getTopDamager(killer);
			if(topDamager == null || !topDamager.isPlayable())
				return;

			if(!Functions.SimpleCheckDrop(monster, topDamager))
				return;

			if(Rnd.chance(CHANCE)) {
				topDamager.getPlayer().getInventory().addItem(LCOIN, 1);
				topDamager.getPlayer().sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_OBTAINED_S1).addItemName(LCOIN));
			}

		}
	}
	private static final int LCOIN = 91663; // L币掉落（会员币）概率
	private static final double CHANCE = 1;

	@Override
	public void onInit()
	{
		CharListenerList.addGlobal(new DeathListener());
	}
}