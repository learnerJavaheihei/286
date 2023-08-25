package l2s.gameserver.model.items.listeners;

import java.util.List;

import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.OptionDataTemplate;

import org.napile.primitive.maps.IntObjectMap;

/**
 * @author Bonux
**/
public abstract class AbstractOptionDataListener implements OnEquipListener
{
	@Override
	public int onUnequip(int slot, ItemInstance item, Playable actor)
	{
		return removeOptionDatas(actor, item.removeEquippedOptionDatas(this));
	}

	protected int refreshOptionDatas(Playable actor, ItemInstance item, List<OptionDataTemplate> optionDatas)
	{
		if(!actor.isPlayer())
			return 0;

		Player player = actor.getPlayer();

		int flags = 0;

		IntObjectMap<OptionDataTemplate> removedOptionDatas = item.removeEquippedOptionDatas(this);

		for(OptionDataTemplate optionData : optionDatas)
		{
			if(player.addOptionData(optionData) != optionData)
			{
				flags |= Inventory.UPDATE_STATS_FLAG;

				if(!optionData.getSkills().isEmpty())
					flags |= Inventory.UPDATE_SKILLS_FLAG;
			}

			item.addEquippedOptionData(this, optionData);

			if(removedOptionDatas != null)
				removedOptionDatas.remove(optionData.getId());
		}

		flags |= removeOptionDatas(actor, removedOptionDatas);
		return flags;
	}

	protected int removeOptionDatas(Playable actor, IntObjectMap<OptionDataTemplate> optionDataMap)
	{
		if(optionDataMap == null)
			return 0;

		if(!actor.isPlayer())
			return 0;

		Player player = actor.getPlayer();

		int flags = 0;
		for(OptionDataTemplate optionData : optionDataMap.valueCollection())
		{
			OptionDataTemplate template = player.removeOptionData(optionData.getId());
			if(template == null)
				continue;

			flags |= Inventory.UPDATE_STATS_FLAG;

			if(!template.getSkills().isEmpty())
				flags |= Inventory.UPDATE_SKILLS_FLAG;
		}
		return flags;
	}
}
