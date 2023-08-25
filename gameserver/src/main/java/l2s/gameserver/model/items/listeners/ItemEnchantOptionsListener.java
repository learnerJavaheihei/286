package l2s.gameserver.model.items.listeners;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.data.xml.holder.OptionDataHolder;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.OptionDataTemplate;

/**
 * @author VISTALL
 * @date 19:34/19.05.2011
 */
public final class ItemEnchantOptionsListener extends AbstractOptionDataListener
{
	private static final ItemEnchantOptionsListener _instance = new ItemEnchantOptionsListener();

	public static ItemEnchantOptionsListener getInstance()
	{
		return _instance;
	}

	@Override
	public int onEquip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return 0;

		if(!actor.isPlayer())
			return 0;

		Player player = actor.getPlayer();

		int flags = 0;

		List<OptionDataTemplate> addedOptionDatas = new ArrayList<OptionDataTemplate>();

		for(int i : item.getEnchantOptions())
		{
			OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(i);
			if(template == null)
				continue;

			addedOptionDatas.add(template);
		}

		flags |= refreshOptionDatas(actor, item, addedOptionDatas);
		return flags;
	}

	@Override
	public int onRefreshEquip(ItemInstance item, Playable actor)
	{
		return onEquip(item.getEquipSlot(), item, actor);
	}
}
