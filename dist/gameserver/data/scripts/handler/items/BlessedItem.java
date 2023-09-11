package handler.items;

import l2s.gameserver.listener.actor.player.OnBlessingItemListener;
import l2s.gameserver.listener.actor.player.OnEnchantItemListener;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;

public class BlessedItem implements OnInitScriptListener
{
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
	
	private static class BlessingItemListener implements OnBlessingItemListener
	{
		@Override
		public void onBlessingItem(Player player, ItemInstance item, boolean success) 
		{
			if (success)
			{				
				addSkills(item, player);
			}
		}
	}
	
	private static class EnchantItemListener implements OnEnchantItemListener
	{

		@Override
		public void onEnchantItem(Player player, ItemInstance item, boolean success) 
		{
			if (success)
			{
				addSkills(item, player);
			}
		}
	}
	
	private static class PlayerEnterListener implements OnPlayerEnterListener
	{

		@Override
		public void onPlayerEnter(Player player) 
		{
			addSkills(player.getActiveWeaponInstance(), player);
		}
	}
	
	private static final OnBlessingItemListener BLESSING_ITEM_LISTENER = new BlessingItemListener();
	private static final OnEnchantItemListener ENCHANT_ITEM_LISTENER = new EnchantItemListener();
	private static final OnPlayerEnterListener PLAYER_ENTER_LISTENER = new PlayerEnterListener();

	@Override
	public void onInit() 
	{
		CharListenerList.addGlobal(BLESSING_ITEM_LISTENER);
		CharListenerList.addGlobal(ENCHANT_ITEM_LISTENER);
		CharListenerList.addGlobal(PLAYER_ENTER_LISTENER);
	}
	
	private static void addSkills(ItemInstance item, Player player)
	{
		if (item == null)
			return;
		if (item.isEquipped() && item.isWeapon() && item.isBlessed())
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
				player.addSkill(S_16_SKILL);
				if (item.getEnchantLevel() >= 20)
					player.addSkill(S_20_SKILL);
				if (item.getEnchantLevel() >= 25)
					player.addSkill(S_25_SKILL);
			}
		}
	}
}
