package l2s.gameserver.model.items.listeners;

import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.player.Agathion;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.agathion.AgathionTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.SkillEntry;

public final class AccessoryListener implements OnEquipListener
{
	private static final AccessoryListener _instance = new AccessoryListener();

	public static AccessoryListener getInstance()
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

		if(slot == Inventory.PAPERDOLL_AGATHION_MAIN)
		{
			AgathionTemplate agathionTemplate = item.getTemplate().getAgathionTemplate();
			if(agathionTemplate == null)
				return 0;

			Agathion agathion = new Agathion(player, agathionTemplate, null);
			agathion.init();
		}
		return 0;
	}

	@Override
	public int onUnequip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return 0;

		if(!actor.isPlayer())
			return 0;

		Player player = actor.getPlayer();

		if(item.getBodyPart() == ItemTemplate.SLOT_L_BRACELET || item.getBodyPart() == ItemTemplate.SLOT_AGATHION)
		{
			int transformNpcId = player.getTransformId();
			for(SkillEntry skillEntry : item.getTemplate().getAttachedSkills())
			{
				Skill skill = skillEntry.getTemplate();
				if(skill.getNpcId() == transformNpcId && skill.hasEffect(EffectUseType.NORMAL, "Transformation"))
					player.setTransform(null);
			}

			if(slot == Inventory.PAPERDOLL_AGATHION_MAIN)
			{
				AgathionTemplate agathionTemplate = item.getTemplate().getAgathionTemplate();
				Agathion agathion = player.getAgathion();
				if(agathionTemplate == null || agathion == null || agathionTemplate.getId() != agathion.getId())
					return 0;

				agathion.delete();
			}
		}
		return 0;
	}

	@Override
	public int onRefreshEquip(ItemInstance item, Playable actor)
	{
		return 0;
	}
}