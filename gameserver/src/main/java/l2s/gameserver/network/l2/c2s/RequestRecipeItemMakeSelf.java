package l2s.gameserver.network.l2.c2s;

import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.RecipeHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.RecipeItemMakeInfoPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.RecipeTemplate;
import l2s.gameserver.templates.item.data.ChancedItemData;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.utils.DropSpecialItemAnnounce;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.LogGeneral;
import l2s.gameserver.templates.item.ItemGrade;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RequestRecipeItemMakeSelf extends L2GameClientPacket
{
	private int _recipeId;

	/**
	 * packet type id 0xB8
	 * format:		cd
	 */
	@Override
	protected boolean readImpl()
	{
		_recipeId = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isProcessingRequest())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		if(activeChar.isInTrainingCamp())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_TAKE_OTHER_ACTION_WHILE_ENTERING_THE_TRAINING_CAMP);
			return;
		}

		RecipeTemplate recipe = RecipeHolder.getInstance().getRecipeByRecipeId(_recipeId);

		if(recipe == null || recipe.getMaterials().length == 0 || recipe.getProducts().length == 0)
		{
			activeChar.sendPacket(SystemMsg.THE_RECIPE_IS_INCORRECT);
			return;
		}

		if(recipe.getLevel() > activeChar.getSkillLevel(!recipe.isCommon() ? Skill.SKILL_CRAFTING : Skill.SKILL_COMMON_CRAFTING))
		{
			//TODO: Должно ли быть сообщение?
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getCurrentMp() < recipe.getMpConsume())
		{
			activeChar.sendPacket(SystemMsg.NOT_ENOUGH_MP, new RecipeItemMakeInfoPacket(activeChar, recipe, 0));
			return;
		}

		if(!activeChar.findRecipe(_recipeId))
		{
			activeChar.sendPacket(SystemMsg.PLEASE_REGISTER_A_RECIPE, ActionFailPacket.STATIC);
			return;
		}

		activeChar.getInventory().writeLock();
		try
		{
			ItemData[] materials = recipe.getMaterials();

			for(ItemData material : materials)
			{
				if(material.getCount() == 0)
					continue;

				if(Config.ALT_GAME_UNREGISTER_RECIPE && ItemHolder.getInstance().getTemplate(material.getId()).getItemType() == EtcItemType.RECIPE)
				{
					RecipeTemplate rp = RecipeHolder.getInstance().getRecipeByRecipeItem(material.getId());
					if(activeChar.hasRecipe(rp))
						continue;
					activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_MATERIALS_TO_PERFORM_THAT_ACTION, new RecipeItemMakeInfoPacket(activeChar, recipe, 0));
					return;
				}

				ItemInstance item = activeChar.getInventory().getItemByItemId(material.getId());
				if(item == null || item.getCount() < material.getCount())
				{
					activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_MATERIALS_TO_PERFORM_THAT_ACTION, new RecipeItemMakeInfoPacket(activeChar, recipe, 0));
					return;
				}
			}

			for(ItemData material : materials)
			{
				if(material.getCount() == 0)
					continue;

				if(Config.ALT_GAME_UNREGISTER_RECIPE && ItemHolder.getInstance().getTemplate(material.getId()).getItemType() == EtcItemType.RECIPE)
					activeChar.unregisterRecipe(RecipeHolder.getInstance().getRecipeByRecipeItem(material.getId()).getId());
				else
				{
					if(!activeChar.getInventory().destroyItemByItemId(material.getId(), material.getCount()))
						continue;//TODO audit
					activeChar.sendPacket(SystemMessagePacket.removeItems(material.getId(), material.getCount()));
				}
			}
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}

		activeChar.resetWaitSitTime();
		activeChar.reduceCurrentMp(recipe.getMpConsume(), null);

		double rate = activeChar.getStat().calc(Stats.CRAFT_CHANCE, recipe.getSuccessRate());
		rate += activeChar.getPremiumAccount().getCraftChanceBonus();
		rate += activeChar.getVIP().getTemplate().getCraftChanceBonus();
		rate = Math.min(100, rate);

		int success = 0;

		ChancedItemData product = recipe.getRandomProduct();
		if(product != null)
		{
			int itemId = product.getId();
			long itemsCount = product.getCount();

			if(Rnd.chance(rate))
			{
				int count = 1;
				if(Rnd.chance(activeChar.getStat().calc(Stats.CRIT_CRAFT_CHANCE, 0)))
				{
					//TODO maybe msg?
					itemsCount++;
				}
				//TODO [G1ta0] добавить проверку на перевес
				 /*製作裝備隨機強化--*/
				ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
				ItemGrade itemGrade = item.getGrade();
				if (item.isWeapon() && ((itemGrade == ItemGrade.D) || (itemGrade == ItemGrade.C)))
				{
					ItemFunctions.addItem(activeChar, itemId, itemsCount, Rnd.get(7), true); //这样子会强化 0~7
				}
				else if (item.isArmor() || item.isAccessory() && ((itemGrade == ItemGrade.D) || (itemGrade == ItemGrade.C)))
				{
					ItemFunctions.addItem(activeChar, itemId, itemsCount, Rnd.get(6), true); //这样子会强化 0~7
				}
				else if (item.isWeapon() && ((itemGrade == ItemGrade.B) || (itemGrade == ItemGrade.A) || (itemGrade == ItemGrade.S)))
				{
					ItemFunctions.addItem(activeChar, itemId, itemsCount, Rnd.get(5), true); //这样子会强化 0~7
				}
				else if (item.isArmor() || item.isAccessory() && ((itemGrade == ItemGrade.B) || (itemGrade == ItemGrade.A) || (itemGrade == ItemGrade.S)))
				{
					ItemFunctions.addItem(activeChar, itemId, itemsCount, Rnd.get(4), true); //这样子会强化 0~7
				}
				else
				{
					ItemFunctions.addItem(activeChar, itemId, itemsCount, true);
				} 	
				/*--製作裝備隨機強化*/
				/*原始ItemFunctions.addItem(activeChar, itemId, itemsCount, true);*/
				success = 1;
				if (DropSpecialItemAnnounce.dropSpecialItems.contains(itemId)) {
					if (activeChar.isGM())
						return;

					String word = DropSpecialItemAnnounce.getInstance().getWord();
					String text = word+"我在制作装备的时候意外获得了「"+ ItemHolder.getInstance().getTemplate(itemId).getName(activeChar) +"」"+(count>1?",并且出现暴击产生了"+count:count)+"个";
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String date = simpleDateFormat.format(new Date(System.currentTimeMillis()));
					Announcements.announceToAll(new SystemMessage(14002).addName(activeChar).addString(date).addZoneName(activeChar.getLoc()).addItemName(itemId).addString(String.valueOf(count)));
//					Announcements.shout(activeChar,text, ChatType.SYSTEM_MESSAGE);
					String log_content = date+"["+activeChar.getName()+"]系统公告内容："+text+",获得方式:制作装备,角色ID："+activeChar.getObjectId()+",道具ID："+itemId;
					ThreadPoolManager.getInstance().schedule(new LogGeneral("specialItemsAnnounce/create",log_content),1000L);
				}
			}
			else
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_FAILED_TO_MANUFACTURE_S1).addItemName(itemId));
		}
		else
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_FAILED_TO_MANUFACTURE_S1).addItemName(recipe.getProducts()[0].getId()));

		activeChar.sendPacket(new RecipeItemMakeInfoPacket(activeChar, recipe, success));
	}
}