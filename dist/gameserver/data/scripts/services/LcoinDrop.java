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

			double CHANCE = 1; // 初始化为1
			int itemCount = 1; // 初始数量为1
			int playerLevel = topDamager.getLevel();
			if (playerLevel < 21) {
				// 玩家等级小于21级，不掉落LCOIN
				return;
			}
			if (monster.getLevel() < 21) {
				CHANCE = 0.001;
			}
			if (monster.getLevel() >= 22  &&  monster.getLevel() < 31) {
			CHANCE = 1.;
			}
			if (monster.getLevel() >= 31  &&  monster.getLevel() < 46) {
			CHANCE = 1.5;
			}
			if (monster.getLevel() >= 46  &&  monster.getLevel() < 66) {
			CHANCE = 2.;
			}
			if (monster.getLevel() >= 66  &&  monster.getLevel() < 77) {
			CHANCE = 2.5;
			}
			if (monster.getLevel() >= 77  &&  monster.getLevel() < 85) {
			CHANCE = 3.;
			}
			if (monster.getLevel() >= 85) {
				CHANCE = 3.5;
			}

			if (topDamager.getPlayer().getInventory().getCountOf(91694) >= 1) {
				CHANCE *= 2; // 如果背包中有道具91694，将概率乘以2
				itemCount *= 2; // 如果背包中有道具91694，将数量乘以2
			}

			if(Rnd.chance(CHANCE)) {
				topDamager.getPlayer().getInventory().addItem(LCOIN, itemCount);
				topDamager.getPlayer().sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_OBTAINED_S1).addItemName(LCOIN));
				topDamager.getPlayer().sendMessage("恭喜您，获得[lcoin幣] "+itemCount+"个！");
			}
		}
	}

	private static final int LCOIN = 91663; // L币掉落（会员币）概率

	@Override
	public void onInit()
	{
		CharListenerList.addGlobal(new DeathListener());
	}
}
