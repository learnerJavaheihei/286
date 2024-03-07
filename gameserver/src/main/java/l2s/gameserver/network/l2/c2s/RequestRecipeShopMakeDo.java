package l2s.gameserver.network.l2.c2s;

import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.RecipeHolder;
import l2s.gameserver.data.xml.holder.VariationDataHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.ManufactureItem;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.RecipeShopItemInfoPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.item.*;
import l2s.gameserver.templates.item.data.ChancedItemData;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.item.support.variation.VariationStone;
import l2s.gameserver.utils.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RequestRecipeShopMakeDo extends L2GameClientPacket
{
	private int _manufacturerId;
	private int _recipeId;
	private long _price;


	@Override
	protected boolean readImpl()
	{
		_manufacturerId = readD();
		_recipeId = readD();
		_price = readQ();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player buyer = getClient().getActiveChar();
		if(buyer == null)
			return;

		if(buyer.isActionsDisabled())
		{
			buyer.sendActionFailed();
			return;
		}

		if(buyer.isInStoreMode())
		{
			buyer.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(buyer.isInTrade())
		{
			buyer.sendActionFailed();
			return;
		}

		if(buyer.isFishing())
		{
			buyer.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			return;
		}

		if(buyer.isInTrainingCamp())
		{
			buyer.sendPacket(SystemMsg.YOU_CANNOT_TAKE_OTHER_ACTION_WHILE_ENTERING_THE_TRAINING_CAMP);
			return;
		}

		if(!buyer.getPlayerAccess().UseTrade)
		{
			buyer.sendPacket(SystemMsg.SOME_LINEAGE_II_FEATURES_HAVE_BEEN_LIMITED_FOR_FREE_TRIALS_____);
			return;
		}

		Player manufacturer = (Player) buyer.getVisibleObject(_manufacturerId);
		if(manufacturer == null || manufacturer.getPrivateStoreType() != Player.STORE_PRIVATE_MANUFACTURE || !manufacturer.checkInteractionDistance(buyer))
		{
			buyer.sendActionFailed();
			return;
		}

		RecipeTemplate recipe = null;
		for(ManufactureItem mi : manufacturer.getCreateList().values())
			if(mi.getRecipeId() == _recipeId)
				if(_price == mi.getCost())
				{
					recipe = RecipeHolder.getInstance().getRecipeByRecipeId(_recipeId);
					break;
				}

		if(recipe == null)
		{
			buyer.sendActionFailed();
			return;
		}

		if(recipe.getMaterials().length == 0 || recipe.getProducts().length == 0)
		{
			manufacturer.sendPacket(SystemMsg.THE_RECIPE_IS_INCORRECT);
			buyer.sendPacket(SystemMsg.THE_RECIPE_IS_INCORRECT);
			return;
		}

		if(recipe.getLevel() > manufacturer.getSkillLevel(!recipe.isCommon() ? Skill.SKILL_CRAFTING : Skill.SKILL_COMMON_CRAFTING))
		{
			//TODO: Должно ли быть сообщение?
			buyer.sendActionFailed();
			return;
		}

		if(!manufacturer.findRecipe(_recipeId))
		{
			buyer.sendActionFailed();
			return;
		}

		int success = 0;
		if(manufacturer.getCurrentMp() < recipe.getMpConsume())
		{
			manufacturer.sendPacket(SystemMsg.NOT_ENOUGH_MP);
			buyer.sendPacket(SystemMsg.NOT_ENOUGH_MP, new RecipeShopItemInfoPacket(buyer, manufacturer, _recipeId, _price, success));
			return;
		}

		buyer.getInventory().writeLock();
		try
		{
			if(buyer.getAdena() < _price)
			{
				buyer.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfoPacket(buyer, manufacturer, _recipeId, _price, success));
				return;
			}

			ItemData[] materials = recipe.getMaterials();

			for(ItemData material : materials)
			{
				if(material.getCount() == 0)
					continue;

				ItemInstance item = buyer.getInventory().getItemByItemId(material.getId());
				if(item == null || material.getCount() > item.getCount())
				{
					buyer.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_MATERIALS_TO_PERFORM_THAT_ACTION, new RecipeShopItemInfoPacket(buyer, manufacturer, _recipeId, _price, success));
					return;
				}
			}

			if(!buyer.reduceAdena(_price, false))
			{
				buyer.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfoPacket(buyer, manufacturer, _recipeId, _price, success));
				return;
			}

			for(ItemData material : materials)
			{
				if(material.getCount() == 0)
					continue;

				buyer.getInventory().destroyItemByItemId(material.getId(), material.getCount());
				//TODO audit
				buyer.sendPacket(SystemMessagePacket.removeItems(material.getId(), material.getCount()));
			}

			long tax = TradeHelper.getTax(manufacturer, _price);
			if(tax > 0)
				_price -= tax;

			manufacturer.addAdena(_price);
		}
		finally
		{
			buyer.getInventory().writeUnlock();
		}

		manufacturer.reduceCurrentMp(recipe.getMpConsume(), null);
		manufacturer.sendStatusUpdate(false, false, StatusUpdatePacket.CUR_MP);

		ChancedItemData product = recipe.getRandomProduct();
		if(product != null)
		{
			int itemId = product.getId();
			long itemsCount = product.getCount();

			double rate = manufacturer.getStat().calc(Stats.CRAFT_CHANCE, recipe.getSuccessRate());
			rate += buyer.getPremiumAccount().getCraftChanceBonus();
			rate += buyer.getVIP().getTemplate().getCraftChanceBonus();
			rate = Math.min(100, rate);

			if(Rnd.chance(rate))
			{
				if(Rnd.chance(manufacturer.getStat().calc(Stats.CRIT_CRAFT_CHANCE, 0)))
				{
					//TODO maybe msg?
					itemsCount++;
				}

				ItemInstance item = ItemFunctions.createItem(itemId);
				item.setCount(itemsCount);

				addRefinedItem(item);
				/*製作裝備隨機強化--*/
				ItemGrade itemGrade = item.getGrade();
				if (item.isWeapon() && ((itemGrade == ItemGrade.D) || (itemGrade == ItemGrade.C)))
				{
					item.setEnchantLevel(Rnd.get(7));
					buyer.getInventory().addItem(item);
//					ItemFunctions.addItem(buyer, itemId, itemsCount, Rnd.get(7), true); //这样子会强化 0~7
				}
				else if (item.isArmor() || item.isAccessory() && ((itemGrade == ItemGrade.D) || (itemGrade == ItemGrade.C)))
				{
					item.setEnchantLevel(Rnd.get(6));
					buyer.getInventory().addItem(item);
//					ItemFunctions.addItem(buyer, itemId, itemsCount, Rnd.get(6), true); //这样子会强化 0~7
				}
				else if (item.isWeapon() && ((itemGrade == ItemGrade.B) || (itemGrade == ItemGrade.A) || (itemGrade == ItemGrade.S)))
				{
					item.setEnchantLevel(Rnd.get(5));
					buyer.getInventory().addItem(item);
//					ItemFunctions.addItem(buyer, itemId, itemsCount, Rnd.get(5), true); //这样子会强化 0~7
				}
				else if (item.isArmor() || item.isAccessory() && ((itemGrade == ItemGrade.B) || (itemGrade == ItemGrade.A) || (itemGrade == ItemGrade.S)))
				{
					item.setEnchantLevel(Rnd.get(4));
					buyer.getInventory().addItem(item);
//					ItemFunctions.addItem(buyer, itemId, itemsCount, Rnd.get(4), true); //这样子会强化 0~7
				}
				else
				{
					item.setEnchantLevel(0);
					buyer.getInventory().addItem(item);
//					ItemFunctions.addItem(buyer, itemId, itemsCount, true);
				}
				/*--製作裝備隨機強化*/
//				ItemFunctions.addItem(buyer, itemId, itemsCount, true);
				if (DropSpecialItemAnnounce.dropSpecialItems.contains(itemId)) {
					if (buyer.isGM())
						return;
					String word = DropSpecialItemAnnounce.getInstance().getWord();
					String text = word+"我在制作装备的时候意外获得了「"+ ItemHolder.getInstance().getTemplate(itemId).getName(buyer) +"」"+(itemsCount>1?",并且出现暴击产生了"+itemsCount:itemsCount)+"个";
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String date = simpleDateFormat.format(new Date(System.currentTimeMillis()));
					Announcements.announceToAll(new SystemMessage(14002).addName(buyer).addString(date).addZoneName(buyer.getLoc()).addItemName(itemId).addString(String.valueOf(itemsCount)));
//					Announcements.shout(buyer,text, ChatType.SYSTEM_MESSAGE);
					String log_content = date+"["+buyer.getName()+"]系统公告内容："+text+",获得方式:制作装备,角色ID："+buyer.getObjectId()+",道具ID："+itemId;
					ThreadPoolManager.getInstance().schedule(new LogGeneral("specialItemsAnnounce/create",log_content),1000L);
				}
				if(itemsCount > 1)
				{
					SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.C1_CREATED_S2_S3_AT_THE_PRICE_OF_S4_ADENA);
					sm.addName(manufacturer);
					sm.addItemName(itemId);
					sm.addLong(itemsCount);
					sm.addLong(_price);
					buyer.sendPacket(sm);

					sm = new SystemMessagePacket(SystemMsg.S2_S3_HAVE_BEEN_SOLD_TO_C1_FOR_S4_ADENA);
					sm.addName(buyer);
					sm.addItemName(itemId);
					sm.addLong(itemsCount);
					sm.addLong(_price);
					manufacturer.sendPacket(sm);

				}
				else
				{
					SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.C1_CREATED_S2_AFTER_RECEIVING_S3_ADENA);
					sm.addName(manufacturer);
					sm.addItemName(itemId);
					sm.addLong(_price);
					buyer.sendPacket(sm);

					sm = new SystemMessagePacket(SystemMsg.S2_IS_SOLD_TO_C1_FOR_THE_PRICE_OF_S3_ADENA);
					sm.addName(buyer);
					sm.addItemName(itemId);
					sm.addLong(_price);
					manufacturer.sendPacket(sm);
				}
				success = 1;
			}
			else
			{
				SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.C1_HAS_FAILED_TO_CREATE_S2_AT_THE_PRICE_OF_S3_ADENA);
				sm.addName(manufacturer);
				sm.addItemName(itemId);
				sm.addLong(_price);
				buyer.sendPacket(sm);

				sm = new SystemMessagePacket(SystemMsg.YOUR_ATTEMPT_TO_CREATE_S2_FOR_C1_AT_THE_PRICE_OF_S3_ADENA_HAS_FAILED);
				sm.addName(buyer);
				sm.addItemName(itemId);
				sm.addLong(_price);
				manufacturer.sendPacket(sm);
			}
		}
		else
		{
			SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.C1_HAS_FAILED_TO_CREATE_S2_AT_THE_PRICE_OF_S3_ADENA);
			sm.addName(manufacturer);
			sm.addItemName(recipe.getProducts()[0].getId());
			sm.addLong(_price);
			buyer.sendPacket(sm);

			sm = new SystemMessagePacket(SystemMsg.YOUR_ATTEMPT_TO_CREATE_S2_FOR_C1_AT_THE_PRICE_OF_S3_ADENA_HAS_FAILED);
			sm.addName(buyer);
			sm.addItemName(recipe.getProducts()[0].getId());
			sm.addLong(_price);
			manufacturer.sendPacket(sm);

		}

		buyer.sendChanges();
		buyer.sendPacket(new RecipeShopItemInfoPacket(buyer, manufacturer, _recipeId, _price, success));
	}

	public static void addRefinedItem(ItemInstance item) {
		VariationStone stone =null;
		int Stoneid = 0;
		if (item.isWeapon()) {
			Stoneid = 90007;
			stone = VariationDataHolder.getInstance().getStone(VariationType.WEAPON, Stoneid);
		}
		else if (item.isArmor()) {
			Stoneid = 90012;
			if (item.getBodyPart() == Bodypart.FEET.mask()){

				stone = VariationDataHolder.getInstance().getStone(VariationType.ARMOR_BOOTS, Stoneid);// 鞋子
			}
			else if (item.getBodyPart() == Bodypart.CHEST.mask() || item.getBodyPart() == Bodypart.FULL_ARMOR.mask())  {

				stone = VariationDataHolder.getInstance().getStone(VariationType.ARMOR_CHEST, Stoneid);// 胸甲/连体
			}
			else if (item.getBodyPart() == Bodypart.GLOVES.mask()){

				stone = VariationDataHolder.getInstance().getStone(VariationType.ARMOR_GLOVES, Stoneid);// 手套
			}
			else if (item.getBodyPart() == Bodypart.HEAD.mask()){

				stone = VariationDataHolder.getInstance().getStone(VariationType.ARMOR_HELMET, Stoneid);// 头盔
			}
		}
		else if (item.isAccessory()){
			Stoneid = 80017;
			if (item.getBodyPart() == (Bodypart.RIGHT_EAR.mask() + Bodypart.LEFT_EAR.mask()))  {
				stone = VariationDataHolder.getInstance().getStone(VariationType.ACCESSORY_RARE_EARRING, Stoneid);
			}
			else if (item.getBodyPart() == Bodypart.NECKLACE.mask())  {
				stone = VariationDataHolder.getInstance().getStone(VariationType.ACCESSORY_RARE_NECKLACE, Stoneid);
			}
			else if (item.getBodyPart() == (Bodypart.RIGHT_FINGER.mask() + Bodypart.LEFT_FINGER.mask()))  {
				stone = VariationDataHolder.getInstance().getStone(VariationType.ACCESSORY_RARE_RING, Stoneid);
			}
		}

		if (stone != null) {
			int variation1Id = VariationUtils.publicGetRandomOptionId(stone.getVariation(1));
			int variation2Id = VariationUtils.publicGetRandomOptionId(stone.getVariation(2));
			if (variation1Id != 0 && variation2Id == 0)
			{
				item.setVariationStoneId(Stoneid);
				item.setVariation1Id(variation1Id);
			}
			else if (variation1Id != 0 && variation2Id != 0)
			{
				item.setVariationStoneId(Stoneid);
				item.setVariation1Id(variation1Id);
				item.setVariation2Id(variation2Id);
			}
		}
	}
}