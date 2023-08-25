package l2s.gameserver.model.items.listeners;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.data.xml.holder.ArmorSetsHolder;
import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.ArmorSet;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.network.l2.components.SystemMsg;

public final class ArmorSetListener extends AbstractSkillListener
{
	private static final ArmorSetListener _instance = new ArmorSetListener();

	public static ArmorSetListener getInstance()
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

		List<ArmorSet> armorSets = ArmorSetsHolder.getInstance().getArmorSets(item.getItemId());
		if(armorSets == null || armorSets.isEmpty())
			return 0;

		Player player = actor.getPlayer();

		int armorSetEnchant = 0;

		int flags = 0;

		List<SkillEntry> addedSkills = new ArrayList<SkillEntry>();

		for(ArmorSet armorSet : armorSets)
		{
			// checks if equipped item is part of set
			if(armorSet.containItem(slot, item.getItemId()))
			{
				List<SkillEntry> skills = armorSet.getSkills(armorSet.getEquipedSetPartsCount(player));
				for(SkillEntry skillEntry : skills)
					addedSkills.add(skillEntry);

				if(armorSet.containAll(player))
				{
					if(armorSet.containShield(player)) // has shield from set
					{
						skills = armorSet.getShieldSkills();
						for(SkillEntry skillEntry : skills)
							addedSkills.add(skillEntry);
					}

					int enchantLevel = armorSet.getEnchantLevel(player);
					if(enchantLevel >= 6) // has all parts of set enchanted to 6 or more
					{
						skills = armorSet.getEnchant6skills();
						for(SkillEntry skillEntry : skills)
							addedSkills.add(skillEntry);
					}
					if(enchantLevel >= 7) // has all parts of set enchanted to 7 or more
					{
						skills = armorSet.getEnchant7skills();
						for(SkillEntry skillEntry : skills)
							addedSkills.add(skillEntry);
					}
					if(enchantLevel >= 8) // has all parts of set enchanted to 8 or more
					{
						skills = armorSet.getEnchant8skills();
						for(SkillEntry skillEntry : skills)
							addedSkills.add(skillEntry);
					}
					if(enchantLevel >= 9) // has all parts of set enchanted to 9 or more
					{
						skills = armorSet.getEnchant9skills();
						for(SkillEntry skillEntry : skills)
							addedSkills.add(skillEntry);
					}
					if(enchantLevel >= 10) // has all parts of set enchanted to 10 or more
					{
						skills = armorSet.getEnchant10skills();
						for(SkillEntry skillEntry : skills)
							addedSkills.add(skillEntry);
					}
					if(enchantLevel > armorSetEnchant)
						armorSetEnchant = enchantLevel;
				}
			}
			else if(armorSet.containShield(item.getItemId()) && armorSet.containAll(player))
			{
				List<SkillEntry> skills = armorSet.getShieldSkills();
				for(SkillEntry skillEntry : skills)
					addedSkills.add(skillEntry);
			}
		}

		player.setArmorSetEnchant(armorSetEnchant);
		flags |= refreshSkills(player, item, addedSkills);
		return flags;
	}

	@Override
	public int onUnequip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return 0;

		if(!actor.isPlayer())
			return 0;

		List<ArmorSet> armorSets = ArmorSetsHolder.getInstance().getArmorSets(item.getItemId());
		if(armorSets == null || armorSets.isEmpty())
			return 0;

		Player player = actor.getPlayer();

		int flags = super.onUnequip(slot, item, actor);

		int armorSetEnchant = 0;

		for(ArmorSet armorSet : armorSets)
		{
			boolean remove = false;
			List<SkillEntry> removeSkillId1 = new ArrayList<SkillEntry>(); // set skill
			List<SkillEntry> removeSkillId2 = new ArrayList<SkillEntry>(); // shield skill
			List<SkillEntry> removeSkillId3 = new ArrayList<SkillEntry>(); // enchant +6 skill
			List<SkillEntry> removeSkillId4 = new ArrayList<SkillEntry>(); // enchant +7 skill
			List<SkillEntry> removeSkillId5 = new ArrayList<SkillEntry>(); // enchant +8 skill
			List<SkillEntry> removeSkillId6 = new ArrayList<SkillEntry>(); // enchant +9 skill
			List<SkillEntry> removeSkillId7 = new ArrayList<SkillEntry>(); // enchant +10 skill

			if(armorSet.containItem(slot, item.getItemId())) // removed part of set
			{
				remove = true;
				removeSkillId1 = armorSet.getSkillsToRemove();
				removeSkillId2 = armorSet.getShieldSkills();
				removeSkillId3 = armorSet.getEnchant6skills();
				removeSkillId4 = armorSet.getEnchant7skills();
				removeSkillId5 = armorSet.getEnchant8skills();
				removeSkillId6 = armorSet.getEnchant9skills();
				removeSkillId7 = armorSet.getEnchant10skills();
			}
			else if(armorSet.containShield(item.getItemId())) // removed shield
			{
				remove = true;
				removeSkillId2 = armorSet.getShieldSkills();
			}

			if(remove)
			{
				for(SkillEntry skillEntry : removeSkillId1)
				{
					if(player.removeSkill(skillEntry, false) != null)
						flags |= Inventory.UPDATE_SKILLS_FLAG;
				}
				for(SkillEntry skillEntry : removeSkillId2)
				{
					if(player.removeSkill(skillEntry) != null)
						flags |= Inventory.UPDATE_SKILLS_FLAG;
				}
				for(SkillEntry skillEntry : removeSkillId3)
				{
					if(player.removeSkill(skillEntry) != null)
						flags |= Inventory.UPDATE_SKILLS_FLAG;
				}
				for(SkillEntry skillEntry : removeSkillId4)
				{
					if(player.removeSkill(skillEntry) != null)
						flags |= Inventory.UPDATE_SKILLS_FLAG;
				}
				for(SkillEntry skillEntry : removeSkillId5)
				{
					if(player.removeSkill(skillEntry) != null)
						flags |= Inventory.UPDATE_SKILLS_FLAG;
				}
				for(SkillEntry skillEntry : removeSkillId6)
				{
					if(player.removeSkill(skillEntry) != null)
						flags |= Inventory.UPDATE_SKILLS_FLAG;
				}
				for(SkillEntry skillEntry : removeSkillId7)
				{
					if(player.removeSkill(skillEntry) != null)
						flags |= Inventory.UPDATE_SKILLS_FLAG;
				}
			}

			int enchantLevel = armorSet.getEnchantLevel(player);
			if(enchantLevel > armorSetEnchant)
				armorSetEnchant = enchantLevel;

			List<SkillEntry> skills = armorSet.getSkills(armorSet.getEquipedSetPartsCount(player));
			for(SkillEntry skillEntry : skills)
			{
				if(player.addSkill(skillEntry, false) != skillEntry)
				{
					flags |= Inventory.UPDATE_SKILLS_FLAG;
				}
				item.addEquippedSkill(armorSet, skillEntry);
			}
		}
		player.setArmorSetEnchant(armorSetEnchant);
		return flags;
	}

	@Override
	public int onRefreshEquip(ItemInstance item, Playable actor)
	{
		return onEquip(item.getEquipSlot(), item, actor);
	}
}