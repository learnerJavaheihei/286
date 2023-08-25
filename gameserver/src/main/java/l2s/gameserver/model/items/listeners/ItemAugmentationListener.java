package l2s.gameserver.model.items.listeners;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.data.xml.holder.OptionDataHolder;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.OptionDataTemplate;
import l2s.gameserver.templates.item.ItemTemplate;

/**
 * @author Bonux
**/
public final class ItemAugmentationListener extends AbstractOptionDataListener
{
	private static final ItemAugmentationListener _instance = new ItemAugmentationListener();

	public static ItemAugmentationListener getInstance()
	{
		return _instance;
	}

	@Override
	public int onEquip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return 0;

		if(!item.isAugmented())
			return 0;

		if(!actor.isPlayer())
			return 0;

		Player player = actor.getPlayer();

		int flags = 0;

		List<OptionDataTemplate> addedOptionDatas = new ArrayList<OptionDataTemplate>();

		// При несоотвествии грейда аугмент не применяется
		if(item.getTemplate().getType2() != ItemTemplate.TYPE2_WEAPON || player.getWeaponsExpertisePenalty() == 0)
		{
			int[] stats = { item.getVariation1Id(), item.getVariation2Id() };
			for(int i : stats)
			{
				OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(i);
				if(template == null)
					continue;

				addedOptionDatas.add(template);
			}
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