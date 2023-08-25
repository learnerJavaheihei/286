package l2s.gameserver.handler.items;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.item.ItemTemplate;

/**
 * Mother class of all itemHandlers.<BR><BR>
 * an IItemHandler implementation has to be stateless
 */
public interface IItemHandler
{
	/**
	 * Make SkillEntry for ItemHandler
	 * @param itemTemplate
	 * @param skill
	 * @return maked SkillEntry
	 */
	void attachSkill(ItemTemplate itemTemplate, Skill skill);

	 /**
	 * Launch task associated to the item.
	 * @param playable
	 * @param item : L2ItemInstance designating the item to use
	 * @param ctrl
	 */
	 boolean forceUseItem(Playable playable, ItemInstance item, boolean ctrl);

	/**
	 * Launch task associated to the item.
	 * @param playable
	 * @param item : L2ItemInstance designating the item to use
	 * @param ctrl
	 */
	boolean useItem(Playable playable, ItemInstance item, boolean ctrl);

	/**
	 * Check can drop or not
	 *
	 *
	 * @param player
	 * @param item
	 * @param count
	 *@param loc  @return can drop
	 */
	void dropItem(Player player, ItemInstance item, long count, Location loc);

	/**
	 * Check if can pick up item
	 * @param playable
	 * @param item
	 * @return
	 */
	boolean pickupItem(Playable playable, ItemInstance item);

	/**
	 * Item actions after restore item
	 * @param playable
	 * @param item
	 * @return
	 */
	void onRestoreItem(Playable playable, ItemInstance item);

	/**
	 * Item actions after add item
	 * @param playable
	 * @param item
	 * @return
	 */
	void onAddItem(Playable playable, ItemInstance item);

	/**
	 * Item actions after remove item
	 * @param playable
	 * @param item
	 * @return
	 */
	void onRemoveItem(Playable playable, ItemInstance item);

	boolean isAutoUse();

	SystemMsg checkCondition(Playable playable, ItemInstance item);
}
