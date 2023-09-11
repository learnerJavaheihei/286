package npc.model;

import instances.BalthusKnightBaium;
import instances.BalthusKnightZaken;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ChatUtils;
import l2s.gameserver.utils.ItemFunctions;

import instances.BalthusKnightAntharas;

/**
 * @author Bonux
**/
public class RashInstance extends NpcInstance
{
	private static final int RUSHS_SUPPLIES_ANTHARAS = 90999;	// Припасы Лаш - Антарас
	private static final int RUSHS_SUPPLIES_BAIUM = 91000;	// Припасы Лаша - Баюм
	private static final int RUSHS_SUPPLIES_ZAKEN = 91007;	// Припасы Лаша - Баюм
	private static final int HIGH_GRADE_XP_SCROLL_TICKET = 90947;	// Купон на Свиток Опыта Высокого Ранга
	private static final int MAGICAL_TABLET = 90045;	// Магическая Табличка
	private static final int DAMAGED_ANTHARAS_EARRING = 90993;	// Поврежденная Серьга Антараса
	private static final int DAMAGED_BAIUM_RING = 90994;	// Поврежденное Кольцо Баюма
	private static final int DAMAGED_ZAKEN_EARING = 91006;	// Поврежденное Серьга Закена
	private static final int ANTHARAS_EARRING = 90992;	// Серьга Антараса
	private static final int BAIUM_RING = 49580;	// Кольцо Баюма
	private static final int ZAKEN_EARING = 90763;	// Серьга Закена

	private static final int FIRST_MSG_TIMER_ID = 5000;
	private static final int SECOND_MSG_TIMER_ID = 5001;
	private static final int THIRD_MSG_TIMER_ID = 5002;

	public RashInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		getAI().addTimer(FIRST_MSG_TIMER_ID, 10000);
	}

	@Override
	public void onTimerFired(int timerId)
	{
		if(timerId == FIRST_MSG_TIMER_ID)
		{
			ChatUtils.say(this, NpcString.THIS_WE_BROUGHT_THIS_TO_SUPPORT_THE_BACKUP_BUT_WE_COULD_GIVE_THESE_TO_YOU);
			getAI().addTimer(SECOND_MSG_TIMER_ID, 5000);
		}
		else if(timerId == SECOND_MSG_TIMER_ID)
		{
			ChatUtils.say(this, NpcString.COURAGEOUS_ONES_WHO_SUPPORTED_ANTHARAS_FORCE_COME_AND_TAKE_THE_KINGDOMS_REWARD);
			getAI().addTimer(THIRD_MSG_TIMER_ID, 5000);
		}
		else if(timerId == THIRD_MSG_TIMER_ID)
		{
			ChatUtils.say(this, NpcString.ARE_THERE_THOSE_WHO_DIDNT_RECEIVE_THE_REWARDS_YET_COME_AND_GET_IT_FROM_ME);
			getAI().addTimer(THIRD_MSG_TIMER_ID, 10000);
		}
		super.onTimerFired(timerId);
	}

	@Override
	public String getHtmlFilename(int val, Player player)
	{
		String filename;
		if(val == 0)
		{
			Reflection reflection = getReflection();
			if(reflection instanceof BalthusKnightAntharas)
			{
				BalthusKnightAntharas balthusKnightAntharas = (BalthusKnightAntharas) reflection;
				if(balthusKnightAntharas.isRewardReceived(player))
					filename = getNpcId() + "a_received.htm";
				else
					filename = getNpcId() + "a.htm";
			}
			else if(reflection instanceof BalthusKnightBaium)
			{
				BalthusKnightBaium balthusKnightBaium = (BalthusKnightBaium) reflection;
				if(balthusKnightBaium.isRewardReceived(player))
					filename = getNpcId() + "b_received.htm";
				else
					filename = getNpcId() + "b.htm";
			}
			else if(reflection instanceof BalthusKnightZaken)
			{
				BalthusKnightZaken balthusKnightZaken = (BalthusKnightZaken) reflection;
				if(balthusKnightZaken.isRewardReceived(player))
					filename = getNpcId() + "c_received.htm";
				else
					filename = getNpcId() + "c.htm";
			}
			else
				filename = super.getHtmlFilename(val, player);
		}
		else
			filename = getNpcId() + "-" + val + ".htm";

		return filename;
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == -7001)
		{
			Reflection reflection = getReflection();
			if(!(reflection instanceof BalthusKnightAntharas))
				return;

			if(reply == 1)
			{
				BalthusKnightAntharas balthusKnightAntharas = (BalthusKnightAntharas) reflection;
				if(balthusKnightAntharas.isRewardReceived(player))
				{
					showChatWindow(player, "default/" + getNpcId() + "-already_received.htm", false);
					return;
				}

				int level = player.getLevel();
				if(level >= 70 && level <= 75)
				{
					ItemFunctions.addItem(player, HIGH_GRADE_XP_SCROLL_TICKET, Rnd.get(1, 2), true);
					ItemFunctions.addItem(player, RUSHS_SUPPLIES_ANTHARAS, 1, true);
				}
				else if(level > 75)
				{
					ItemFunctions.addItem(player, HIGH_GRADE_XP_SCROLL_TICKET, Rnd.get(1, 4), true);
					ItemFunctions.addItem(player, RUSHS_SUPPLIES_ANTHARAS, 1, true);
					ItemFunctions.addItem(player, MAGICAL_TABLET, 1, true);
				}
				balthusKnightAntharas.setRewardReceived(player);
			}
			else if(reply == 2)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3171601, player, 0);
			}
			else if(reply == 3)
			{
				ItemInstance earring = player.getInventory().getItemByItemId(DAMAGED_ANTHARAS_EARRING);
				if(earring == null || earring.getEnchantLevel() < 10)
				{
					showChatWindow(player, "default/" + getNpcId() + "-not_have_earring.htm", false);
					return;
				}

				if(!ItemFunctions.deleteItem(player, earring, 1, true))
				{
					showChatWindow(player, "default/" + getNpcId() + "-not_have_earring.htm", false);
					return;
				}
				ItemFunctions.addItem(player, ANTHARAS_EARRING, 1, true);
			}
			else if(reply == 4)
			{
				player.teleToLocation(getReflection().getReturnLoc(), ReflectionManager.MAIN);
			}
		}
		else if(ask == -8001)
		{
			Reflection reflection = getReflection();
			if(!(reflection instanceof BalthusKnightBaium))
				return;

			if(reply == 1)
			{
				BalthusKnightBaium balthusKnightBaium = (BalthusKnightBaium) reflection;
				if(balthusKnightBaium.isRewardReceived(player))
				{
					showChatWindow(player, "default/" + getNpcId() + "-already_received.htm", false);
					return;
				}

				int level = player.getLevel();
				if(level >= 70 && level <= 75)
				{
					ItemFunctions.addItem(player, HIGH_GRADE_XP_SCROLL_TICKET, Rnd.get(1, 2), true);
					ItemFunctions.addItem(player, RUSHS_SUPPLIES_BAIUM, 1, true);
				}
				else if(level > 75)
				{
					ItemFunctions.addItem(player, HIGH_GRADE_XP_SCROLL_TICKET, Rnd.get(1, 4), true);
					ItemFunctions.addItem(player, RUSHS_SUPPLIES_BAIUM, 1, true);
					ItemFunctions.addItem(player, MAGICAL_TABLET, 1, true);
				}
				balthusKnightBaium.setRewardReceived(player);
			}
			else if(reply == 2)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3171602, player, 0);
			}
			else if(reply == 3)
			{
				ItemInstance ring = player.getInventory().getItemByItemId(DAMAGED_BAIUM_RING);
				if(ring == null || ring.getEnchantLevel() < 10)
				{
					showChatWindow(player, "default/" + getNpcId() + "-not_have_ring.htm", false);
					return;
				}

				if(!ItemFunctions.deleteItem(player, ring, 1, true))
				{
					showChatWindow(player, "default/" + getNpcId() + "-not_have_ring.htm", false);
					return;
				}
				ItemFunctions.addItem(player, BAIUM_RING, 1, true);
			}
			else if(reply == 4)
			{
				player.teleToLocation(getReflection().getReturnLoc(), ReflectionManager.MAIN);
			}
		}
		else if(ask == -9001)
		{
			Reflection reflection = getReflection();
			if(!(reflection instanceof BalthusKnightZaken))
				return;

			if(reply == 1)
			{
				BalthusKnightZaken balthusKnightZaken = (BalthusKnightZaken) reflection;
				if(balthusKnightZaken.isRewardReceived(player))
				{
					showChatWindow(player, "default/" + getNpcId() + "-already_received.htm", false);
					return;
				}

				int level = player.getLevel();
				if(level >= 70 && level <= 75)
				{
					ItemFunctions.addItem(player, HIGH_GRADE_XP_SCROLL_TICKET, 2, true);
					ItemFunctions.addItem(player, RUSHS_SUPPLIES_ZAKEN, 1, true);
				}
				else if(level > 75)
				{
					ItemFunctions.addItem(player, HIGH_GRADE_XP_SCROLL_TICKET, 3, true);
					ItemFunctions.addItem(player, RUSHS_SUPPLIES_ZAKEN, 1, true);
					ItemFunctions.addItem(player, MAGICAL_TABLET, 1, true);
				}
				balthusKnightZaken.setRewardReceived(player);
			}
			else if(reply == 2)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3171602, player, 0);
			}
			else if(reply == 3)
			{
				ItemInstance ring = player.getInventory().getItemByItemId(DAMAGED_ZAKEN_EARING);
				if(ring == null || ring.getEnchantLevel() < 10)
				{
					showChatWindow(player, "default/" + getNpcId() + "-not_have_zaken.htm", false);
					return;
				}

				if(!ItemFunctions.deleteItem(player, ring, 1, true))
				{
					showChatWindow(player, "default/" + getNpcId() + "-not_have_zaken.htm", false);
					return;
				}
				ItemFunctions.addItem(player, ZAKEN_EARING, 1, true);
			}
			else if(reply == 4)
			{
				player.teleToLocation(getReflection().getReturnLoc(), ReflectionManager.MAIN);
			}
		}
		else
			super.onMenuSelect(player, ask, reply, state);
	}
}