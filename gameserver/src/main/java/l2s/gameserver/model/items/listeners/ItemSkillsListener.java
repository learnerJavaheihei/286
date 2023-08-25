package l2s.gameserver.model.items.listeners;

import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.agathion.AgathionData;
import l2s.gameserver.templates.agathion.AgathionEnchantData;
import l2s.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.support.Ensoul;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ItemSkillsListener extends AbstractSkillListener
{
	private static final ItemSkillsListener _instance = new ItemSkillsListener();
	
	private static final SkillEntry SWORD_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, 50392, 1);
	private static final SkillEntry BIG_SWORD_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, 50395, 1);
	private static final SkillEntry BLUNT_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, 50397, 1);
	private static final SkillEntry BIG_BLUNT_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, 50399, 1);
	private static final SkillEntry BOW_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, 50401, 1);
	private static final SkillEntry DAGGER_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, 50402, 1);
	private static final SkillEntry DUAL_FIST_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, 50403, 1);
	private static final SkillEntry POLE_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, 50405, 1);
	private static final SkillEntry A_16_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, 50421, 1);
	private static final SkillEntry A_20_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, 50422, 1);
	private static final SkillEntry A_25_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, 50423, 1);
	private static final SkillEntry S_16_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, 50424, 1);
	private static final SkillEntry S_20_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, 50425, 1);
	private static final SkillEntry S_25_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, 50426, 1);

	public static ItemSkillsListener getInstance()
	{
		return _instance;
	}

	public int onEquip(int slot, ItemInstance item, Playable actor, boolean refresh)
	{
		if(!actor.isPlayer())
			return 0;

		Player player = actor.getPlayer();

		ItemTemplate template = item.getTemplate();

		if(!refresh)
			player.removeTriggers(template);

		int flags = 0;

		List<SkillEntry> addedSkills = new ArrayList<SkillEntry>();

		// Для оружия при несоотвествии грейда скилы не выдаем
		if(template.getType2() != ItemTemplate.TYPE2_WEAPON || player.getWeaponsExpertisePenalty() == 0)
		{
			if(!refresh)
				player.addTriggers(template);

			if(template.getItemType() == EtcItemType.RUNE_SELECT)
			{
				for(SkillEntry itemSkillEntry : template.getAttachedSkills())
				{
					int skillsCount = 1;
					for(ItemInstance ii : player.getInventory().getItems())
					{
						if(ii == item)
							continue;

						ItemTemplate it = ii.getTemplate();
						if(it.getItemType() == EtcItemType.RUNE_SELECT)
						{
							for(SkillEntry se : it.getAttachedSkills())
							{
								if(se == itemSkillEntry)
								{
									skillsCount++;
									break;
								}
							}
						}
					}

					int skillLevel = Math.min(itemSkillEntry.getTemplate().getMaxLevel(), skillsCount);
					SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, itemSkillEntry.getId(), skillLevel);
					if(skillEntry != null)
						addedSkills.add(skillEntry);
				}
			}
			else
			{
				addedSkills.addAll(Arrays.asList(template.getAttachedSkills()));

				for(int e = item.getFixedEnchantLevel(player); e >= 0; e--)
				{
					List<SkillEntry> enchantSkills = template.getEnchantSkills(e);
					if(enchantSkills != null) {
						addedSkills.addAll(enchantSkills);
						break;
					}
				}

				AgathionData agathionData = item.getTemplate().getAgathionData();
				if(agathionData != null) {
					for(int e = item.getFixedEnchantLevel(player); e >= 0; e--)
					{
						AgathionEnchantData agathionItemEnchant = agathionData.getEnchant(e);
						if(agathionItemEnchant != null) {
							if(item.getEquipSlot() == Inventory.PAPERDOLL_AGATHION_MAIN)
								addedSkills.addAll(agathionItemEnchant.getMainSkills());
							addedSkills.addAll(agathionItemEnchant.getSubSkills());
							break;
						}
					}
				}

				addedSkills.addAll(item.getAppearanceStoneSkills());

				for(Ensoul ensoul : item.getNormalEnsouls())
					addedSkills.addAll(ensoul.getSkills());

				for(Ensoul ensoul : item.getSpecialEnsouls())
					addedSkills.addAll(ensoul.getSkills());
			}
		}
		
		addBlessingSkills(item, player);

		flags |= refreshSkills(actor, item, addedSkills);
		return flags;
	}

	@Override
	protected boolean canAddSkill(Playable actor, ItemInstance item, SkillEntry skillEntry)
	{
		if(item.getTemplate().getItemType() == EtcItemType.RUNE_SELECT)
			return true;

		if (skillEntry.getLevel() < actor.getSkillLevel(skillEntry.getId())) {
			for (ItemInstance tempItem : actor.getInventory().getItems()) {
				if (tempItem != item) {
					int tempSkillLevel = tempItem.getEquippedSkillLevel(skillEntry.getId());
					if (tempSkillLevel > skillEntry.getLevel())
						return false;
				}
			}
			return true;
		}
		return skillEntry.getLevel() > actor.getSkillLevel(skillEntry.getId());
	}

	@Override
	protected int onAddSkill(Playable actor, ItemInstance item, SkillEntry skillEntry)
	{
		Skill itemSkill = skillEntry.getTemplate();
		if(itemSkill.isActive())
		{
			if(!actor.isSkillDisabled(itemSkill))
			{
				long reuseDelay = Formulas.calcSkillReuseDelay(actor, itemSkill);
				reuseDelay = Math.min(reuseDelay, 30000);

				if(reuseDelay > 0)
					actor.disableSkill(itemSkill, reuseDelay);
			}
		}
		return 0;
	}

	@Override
	public int onEquip(int slot, ItemInstance item, Playable actor)
	{
		return onEquip(slot, item, actor, false);
	}

	@Override
	public int onUnequip(int slot, ItemInstance item, Playable actor)
	{
		if(!actor.isPlayer())
			return 0;

		removeBlessingSkills(actor.getPlayer());
		actor.removeTriggers(item.getTemplate());
		return super.onUnequip(slot, item, actor);
	}

	@Override
	public int onRefreshEquip(ItemInstance item, Playable actor)
	{
		return onEquip(item.getEquipSlot(), item, actor, true);
	}
	
	private void addBlessingSkills(ItemInstance item, Player player)
	{
		if (item.isWeapon() && item.isBlessed())
		{
			if ((item.getItemType() == WeaponType.SWORD) || (item.getItemType() == WeaponType.RAPIER))
				player.addSkill(SWORD_SKILL);
			else if ((item.getItemType() == WeaponType.BIGSWORD) || (item.getItemType() == WeaponType.ANCIENTSWORD))
				player.addSkill(BIG_SWORD_SKILL);
			else if (item.getItemType() == WeaponType.BIGBLUNT)
				player.addSkill(BIG_BLUNT_SKILL);
			else if (item.getItemType() == WeaponType.BLUNT)
				player.addSkill(BLUNT_SKILL);
			else if (item.getItemType() == WeaponType.BOW)
				player.addSkill(BOW_SKILL);
			else if (item.getItemType() == WeaponType.DAGGER)
				player.addSkill(DAGGER_SKILL);
			else if ((item.getItemType() == WeaponType.DUAL) || (item.getItemType() == WeaponType.DUALFIST))
				player.addSkill(DUAL_FIST_SKILL);
			else if (item.getItemType() == WeaponType.POLE)
				player.addSkill(POLE_SKILL);
			
			if (item.getGrade() == ItemGrade.A)
			{
				if (item.getEnchantLevel() >= 16)
					player.addSkill(A_16_SKILL);
				if (item.getEnchantLevel() >= 20)
					player.addSkill(A_20_SKILL);
				if (item.getEnchantLevel() >= 25)
					player.addSkill(A_25_SKILL);
			}
			if (item.getGrade() == ItemGrade.S)
			{
				if (item.getEnchantLevel() >= 16)
					player.addSkill(S_16_SKILL);
				if (item.getEnchantLevel() >= 20)
					player.addSkill(S_20_SKILL);
				if (item.getEnchantLevel() >= 25)
					player.addSkill(S_25_SKILL);
			}
		}
	}
	
	private void removeBlessingSkills(Player player)
	{
		player.removeSkill(SWORD_SKILL, false);
		player.removeSkill(BIG_SWORD_SKILL, false);
		player.removeSkill(BIG_BLUNT_SKILL, false);
		player.removeSkill(BLUNT_SKILL, false);
		player.removeSkill(BOW_SKILL, false);
		player.removeSkill(DAGGER_SKILL, false);
		player.removeSkill(DUAL_FIST_SKILL, false);
		player.removeSkill(POLE_SKILL, false);
		player.removeSkill(A_16_SKILL, false);
		player.removeSkill(A_20_SKILL, false);
		player.removeSkill(A_25_SKILL, false);
		player.removeSkill(S_16_SKILL, false);
		player.removeSkill(S_20_SKILL, false);
		player.removeSkill(S_25_SKILL, false);
	}
}