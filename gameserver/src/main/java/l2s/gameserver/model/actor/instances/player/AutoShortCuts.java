package l2s.gameserver.model.actor.instances.player;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Skill.SkillType;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.player.ShortCut.ShortCutType;
import l2s.gameserver.model.base.ItemAutouseType;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.c2s.RequestActionUse.Action;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.ExActivateAutoShortcut;
import l2s.gameserver.network.l2.s2c.ExAutoplayDoMacro;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

public class AutoShortCuts
{
	private enum AutoUseType
	{
		HEAL,
		ITEM_BUFF,
		SKILL_BUFF,
		SKILL_ATTACK,
		ATTACK,
		PET_SKILL;
	}

	private final Player _owner;

	private int autoHealItem = -1;
	private final Set<Integer> _autoBuffItems = new CopyOnWriteArraySet<>();
	private final Set<Integer> _autoBuffSkills = new CopyOnWriteArraySet<>();
	private final Set<Integer> _autoPetSkills = new CopyOnWriteArraySet<>();
	private final Set<Integer> _autoAttackSkills = new CopyOnWriteArraySet<>();
	private boolean _autoAttack = false;
	private boolean _autoAttackPet = false;

	private ScheduledFuture<?> _autoHealTask = null;
	private ScheduledFuture<?> _autoBuffItemsTask = null;
	private ScheduledFuture<?> _autoBuffSkillsTask = null;
	private ScheduledFuture<?> _autoPetSkillsTask = null;
	private ScheduledFuture<?> _autoAttackSkillsTask = null;
	private ScheduledFuture<?> _autoAttackTask = null;
	private ScheduledFuture<?> _autoAttackPetTask = null;
	private Action action;

	public AutoShortCuts(Player owner)
	{
		_owner = owner;
	}
	
	public boolean autoSkillsActive()
	{
		return _autoAttackSkillsTask != null;
	}

	private void doAutoShortCut(AutoUseType autoUseType)
	{
		if (_owner.isInPeaceZone())
			return;

		if(autoUseType == AutoUseType.HEAL)
		{
			if(_owner.getCurrentHpPercents() >= _owner.getAutoFarm().getHealPercent())
				return;

			ItemInstance item = _owner.getInventory().getItemByObjectId(autoHealItem);
			if(item == null) {
				autoHealItem = -1;
				stopAutoHealTask();
				return;
			}
			if(item.getTemplate().getFirstSkill() == null)
			{
				activate(1, ShortCut.PAGE_AUTOPLAY, false, true);
				return;
			}
			_owner.useItem(item, false, false);
		}
		else if(autoUseType == AutoUseType.ITEM_BUFF)
		{
			loop: for(int itemObjectId : _autoBuffItems)
			{
				ItemInstance item = _owner.getInventory().getItemByObjectId(itemObjectId);
				if(item == null)
				{
					_autoBuffItems.remove(itemObjectId);
					continue;
				}

				SkillEntry skillEntry = item.getTemplate().getFirstSkill();
				Skill skill = skillEntry.getTemplate();
				Creature target = skill.getAimingTarget(_owner, _owner);
				if(target == null)
					target = _owner;

				for(Abnormal abnormal : target.getAbnormalList())
				{
					if(!abnormal.canReplaceAbnormal(skill, 1))
						continue loop;
				}
				_owner.useItem(item, false, false);
			}

			if(_autoBuffItems.isEmpty())
				stopAutoBuffItemsTask();
		}
		else if(autoUseType == AutoUseType.SKILL_BUFF)
		{
			loop: for(int skillId : _autoBuffSkills)
			{
				SkillEntry skillEntry = _owner.getKnownSkill(skillId);
				if(skillEntry == null) {
					_autoBuffSkills.remove(skillId);
					continue;
				}

				Skill skill = skillEntry.getTemplate();
				Creature target = skill.getAimingTarget(_owner, _owner);
				if(target == null)
					continue;

				for(Abnormal abnormal : target.getAbnormalList())
				{
					if(!abnormal.canReplaceAbnormal(skill, 1))
						continue loop;
				}
				_owner.getAI().Cast(skillEntry, target, false, false);
			}

			if(_autoBuffSkills.isEmpty())
				stopAutoBuffSkillsTask();
		}
		else if((autoUseType == AutoUseType.SKILL_ATTACK) && _owner.getPlayer().getAutoFarm().isFarmActivate())
		{
			loop: for(int skillId : _autoAttackSkills)
			{
				SkillEntry skillEntry = _owner.getKnownSkill(skillId);
				if(skillEntry == null)
				{
					_autoAttackSkills.remove(skillId);
					continue;
				}
				
				Skill skill = skillEntry.getTemplate();
				Creature target = null;
				if (_owner.getPlayer().getAutoFarm().isFarmActivate())
				{
					if (skill.getSkillType() == SkillType.HEAL)
					{
						target = skill.getAimingTarget(_owner, _owner);
						_owner.getAI().Cast(skillEntry, target, true, false);
					}
					else
					{
						target = skill.getAimingTarget(_owner, _owner.getTarget());
						if (skill.getMpConsume() <= _owner.getCurrentMp())
							_owner.getAI().Cast(skillEntry, target, true, false);
						else
							_owner.sendPacket(new ExAutoplayDoMacro());
					}
					if(target == null)
						continue;
				}
			}
		
			if (_autoAttackSkills.isEmpty())
				stopAutoAttackSkillsTask();
		}
		else if ((autoUseType == AutoUseType.PET_SKILL) && _owner.getPlayer().getAutoFarm().isFarmActivate())
		{
			loop: for(int skillId : _autoPetSkills)
			{
				SummonInstance summon = _owner.getSummon();
				if (summon == null || summon.isOutOfControl())
					return;
				
				if(!servitorUseSkill(_owner, summon, skillId, action.id))
					_owner.sendActionFailed();
			}
		
			if (_autoPetSkills.isEmpty())
				stopAutoPetSkillsTask();
		}
		else if ((autoUseType == AutoUseType.ATTACK) && _owner.getPlayer().getAutoFarm().isFarmActivate())
		{
			if (_autoAttack)
				_owner.sendPacket(new ExAutoplayDoMacro());
			if (_autoAttackPet && (_owner.getTarget() != null) && (_owner.getTarget().isCreature()))
			{
				for (Servitor summon : _owner.getServitors())
					summon.getAI().Attack(_owner.getTarget(), true, false);
				if (_owner.getPet() != null)
					_owner.getPet().getAI().Attack(_owner.getTarget(), true, false);
			}
			
			if (!_autoAttack)
				stopAutoAttackTask();
			if (!_autoAttackPet)
				stopAutoAttackPetTask();
		}
	}

	public boolean activate(int slotIndex, boolean active)
	{
		if(slotIndex == 65535)
		{
			boolean success = false;
			for(int s = 0; s < 12; s++)
			{
				if(activate(s, ShortCut.PAGE_AUTOCONSUME, active, false))
					success = true;
			}
			return success;
		}

		int slot = slotIndex % 12;
		int page = slotIndex / 12;
		return activate(slot, page, active, true);
	}

	public synchronized boolean activate(int slot, int page, boolean active, boolean checkPage)
	{
		if(activate0(slot, page, active, checkPage))
		{
			if(autoHealItem > 0)
			{
				if(_autoHealTask == null)
					_autoHealTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(() -> doAutoShortCut(AutoUseType.HEAL), 0, 250L);
			}
			else
			{
				stopAutoHealTask();
			}
			if(!_autoBuffItems.isEmpty())
			{
				if(_autoBuffItemsTask == null)
					_autoBuffItemsTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(() -> doAutoShortCut(AutoUseType.ITEM_BUFF), 0, 1000L);
			}
			else
			{
				stopAutoBuffItemsTask();
			}
			if(!_autoBuffSkills.isEmpty())
			{
				if(_autoBuffSkillsTask == null)
					_autoBuffSkillsTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(() -> doAutoShortCut(AutoUseType.SKILL_BUFF), 0, 1000L);
			}
			else
			{
				stopAutoBuffSkillsTask();
			}
			if(!_autoAttackSkills.isEmpty())
			{
				if(_autoAttackSkillsTask == null)
					_autoAttackSkillsTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(() -> doAutoShortCut(AutoUseType.SKILL_ATTACK), 0, 500L);
			}
			else
			{
				stopAutoAttackSkillsTask();
			}
			if (!_autoPetSkills.isEmpty())
			{
				if (_autoPetSkillsTask == null)
					_autoPetSkillsTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(() -> doAutoShortCut(AutoUseType.PET_SKILL), 0, 500L);
			}
			else
			{
				stopAutoPetSkillsTask();
			}
			if (_autoAttack)
			{
				if (_autoAttackTask == null)
					_autoAttackTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(() -> doAutoShortCut(AutoUseType.ATTACK), 0, 500L);
			}
			else
				stopAutoAttackTask();
			if (_autoAttackPet)
			{
				if (_autoAttackPetTask == null)
					_autoAttackPetTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(() -> doAutoShortCut(AutoUseType.ATTACK), 0, 500L);
			}
			else
				stopAutoAttackPetTask();
			
			return true;
		}
		return false;
	}

	private void stopAutoHealTask()
	{
		if(_autoHealTask != null)
		{
			_autoHealTask.cancel(false);
			_autoHealTask = null;
		}
	}

	private void stopAutoBuffItemsTask()
	{
		if(_autoBuffItemsTask != null)
		{
			_autoBuffItemsTask.cancel(false);
			_autoBuffItemsTask = null;
		}
	}

	private void stopAutoBuffSkillsTask()
	{
		if(_autoBuffSkillsTask != null)
		{
			_autoBuffSkillsTask.cancel(false);
			_autoBuffSkillsTask = null;
		}
	}
	
	private void stopAutoAttackSkillsTask()
	{
		if(_autoAttackSkillsTask != null)
		{
			_autoAttackSkillsTask.cancel(false);
			_autoAttackSkillsTask = null;
		}
	}
	
	private void stopAutoPetSkillsTask()
	{
		if(_autoPetSkillsTask != null)
		{
			_autoPetSkillsTask.cancel(false);
			_autoPetSkillsTask = null;
		}
	}
	
	private void stopAutoAttackTask()
	{
		if(_autoAttackTask != null)
		{
			_autoAttackTask.cancel(false);
			_autoAttackTask = null;
		}
	}
	
	private void stopAutoAttackPetTask()
	{
		if(_autoAttackPetTask != null)
		{
			_autoAttackPetTask.cancel(false);
			_autoAttackPetTask = null;
		}
	}

	private boolean activate0(int slot, int page, boolean active, boolean checkPage)
	{
		ShortCut shortCut = _owner.getShortCut(slot, page);
		if(shortCut == null)
			return false;

		if(!checkShortCut(shortCut.getSlot(), shortCut.getPage(), shortCut.getType(), shortCut.getId()))
			return false;

		if(page == ShortCut.PAGE_AUTOCONSUME)
		{
			if(active)
				_autoBuffItems.add(shortCut.getId());
			else
				_autoBuffItems.remove(shortCut.getId());
			if(checkPage)
			{
				for(int s = 0; s < 12; s++)
				{
					ShortCut sc = _owner.getShortCut(s, page);
					if(sc != null && sc.getType() == shortCut.getType() && sc.getId() == shortCut.getId())
						_owner.sendPacket(new ExActivateAutoShortcut(s, page, active));
				}
			}
			else
				_owner.sendPacket(new ExActivateAutoShortcut(slot, page, active));
			return true;
		}
		else if (page == ShortCut.PAGE_AUTOPLAY)
		{
			if(slot == 1)
			{
				autoHealItem = active ? shortCut.getId() : -1;
				_owner.sendPacket(new ExActivateAutoShortcut(slot, page, active));
				return true;
			}
		}
		else if(page >= ShortCut.PAGE_NORMAL_0 && page <= ShortCut.PAGE_FLY_TRANSFORM)
		{
			if (shortCut.getType() == ShortCutType.ITEM)
			{
				ItemInstance item = _owner.getInventory().getItemByObjectId(shortCut.getId());
				if (item == null)
				{
					return false;
				}
				SkillEntry skillEntry = item.getTemplate().getFirstSkill();
				if ((skillEntry != null) && item.getTemplate().getAutouseType() == ItemAutouseType.BUFF)
				{
					if(active)
						_autoBuffItems.add(shortCut.getId());
					else
						_autoBuffItems.remove(shortCut.getId());
				}
			}
			else if (shortCut.getType() != ShortCutType.ACTION)
			{
				SkillEntry skillEntry = _owner.getKnownSkill(shortCut.getId());
				if ((skillEntry.getTemplate().getAutoUseType() == Skill.SkillAutoUseType.BUFF) || (skillEntry.getTemplate().getAutoUseType() == Skill.SkillAutoUseType.APPEARANCE))
				{
					if(active)
						_autoBuffSkills.add(shortCut.getId());
					else
						_autoBuffSkills.remove(shortCut.getId());
				}
				else if (skillEntry.getTemplate().getAutoUseType() == Skill.SkillAutoUseType.ATTACK)
				{
					if(active)
						_autoAttackSkills.add(shortCut.getId());
					else
						_autoAttackSkills.remove(shortCut.getId());
				}
			}
			else
			{
				if (active)
				{
					if (shortCut.getId() == 2)
						_autoAttack = true;
					else if ((shortCut.getId() == 16) || (shortCut.getId() == 22))
						_autoAttackPet = true;
					else
					{
						action = Action.find(shortCut.getId());
						if (action.value > 0)
						{
							_autoPetSkills.add(action.value);
						}
					}
						
				}
				else
				{
					if (shortCut.getId() == 2)
					{
						_autoAttack = false;
						stopAutoAttackTask();
					}
					else if ((shortCut.getId() == 16) || (shortCut.getId() == 22))
					{
						_autoAttackPet = false;
						stopAutoAttackPetTask();
					}
					else
					{
						action = Action.find(shortCut.getId());
						if (action.value > 0)
						{
							_autoPetSkills.remove(action.value);
						}
					}
				}
			}
			if(checkPage)
			{
				for(int p = ShortCut.PAGE_NORMAL_0; p <= ShortCut.PAGE_FLY_TRANSFORM; p++)
				{
					for(int s = 0; s < 12; s++)
					{
						ShortCut sc = _owner.getShortCut(s, p);
						if(sc != null && sc.getType() == shortCut.getType() && sc.getId() == shortCut.getId())
							_owner.sendPacket(new ExActivateAutoShortcut(s, p, active));
					}
				}
			}
			else
				_owner.sendPacket(new ExActivateAutoShortcut(slot, page, active));
			return true;
		}
		return false;
	}

	public IBroadcastPacket canRegShortCut(int slot, int page, ShortCut.ShortCutType shortCutType, int id)
	{
		if(page >= ShortCut.PAGE_NORMAL_0 && page <= ShortCut.PAGE_FLY_TRANSFORM)
		{
			return null;
		}
		else if(!checkShortCut(slot, page, shortCutType, id))
		{
			if(page == ShortCut.PAGE_AUTOPLAY && slot == 0)
				return SystemMsg.MACRO_USE_ONLY;
			else
				return ActionFailPacket.STATIC;
		}
		return null;
	}

	private boolean checkShortCut(int slot, int page, ShortCut.ShortCutType shortCutType, int id)
	{
		if(shortCutType == ShortCut.ShortCutType.MACRO)
		{
			if(page == ShortCut.PAGE_AUTOPLAY && slot == 0)
				return true;
		}
		else if(shortCutType == ShortCut.ShortCutType.ITEM)
		{
			ItemAutouseType autouseType;
			if(page == ShortCut.PAGE_AUTOPLAY)
			{
				if (slot != 1)
					return false;
				autouseType = ItemAutouseType.HEAL;
			}
			else if(page >= ShortCut.PAGE_NORMAL_0 && page <= ShortCut.PAGE_FLY_TRANSFORM)
			{
				autouseType = ItemAutouseType.BUFF;
			}
			else
				return false;

			ItemInstance item = _owner.getInventory().getItemByObjectId(id);
			if(item == null || item.getTemplate().getAutouseType() != autouseType)
				return false;

			return true;
		}
		else if(shortCutType == ShortCut.ShortCutType.SKILL)
		{
			if(page >= ShortCut.PAGE_NORMAL_0 && page <= ShortCut.PAGE_FLY_TRANSFORM)
			{
				SkillEntry skillEntry = _owner.getKnownSkill(id);
				if(skillEntry != null && (skillEntry.getTemplate().getAutoUseType() == Skill.SkillAutoUseType.BUFF //
						|| skillEntry.getTemplate().getAutoUseType() == Skill.SkillAutoUseType.APPEARANCE//
						|| skillEntry.getTemplate().getAutoUseType() == Skill.SkillAutoUseType.ATTACK))
					return true;
			}
		}
		else if (shortCutType == ShortCut.ShortCutType.ACTION)
		{
			return true;
		}
		return false;
	}
	
	private boolean servitorUseSkill(Player player, Servitor servitor, int skillId, int actionId)
	{
		if(servitor == null)
			return false;

		int skillLevel = servitor.getActiveSkillLevel(skillId);
		if(skillLevel == 0)
			return false;

		Skill skill = SkillHolder.getInstance().getSkill(skillId, skillLevel);
		if(skill == null)
			return false;

		if(servitor.isDepressed())
		{
			player.sendPacket(SystemMsg.YOUR_PETSERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
			return false;
		}

		if(servitor.isNotControlled()) // TODO: [Bonux] Проверить, распостраняется ли данное правило на саммонов.
		{
			player.sendPacket(SystemMsg.YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
			return false;
		}

		if(skill.getId() != 6054)
		{
			int npcId = servitor.getNpcId();
			if(npcId == 16051 || npcId == 16052 || npcId == 16053 || npcId == 1601 || npcId == 1602 || npcId == 1603 || npcId == 1562 || npcId == 1563 || npcId == 1564 || npcId == 1565 || npcId == 1566 || npcId == 1567 || npcId == 1568 || npcId == 1569 || npcId == 1570 || npcId == 1571 || npcId == 1572 || npcId == 1573)
			{
				if(!servitor.getAbnormalList().contains(6054))
				{
					player.sendPacket(SystemMsg.A_PET_ON_AUXILIARY_MODE_CANNOT_USE_SKILLS);
					return false;
				}
			}
		}

		if(skill.isToggle())
		{
			if(servitor.getAbnormalList().contains(skill))
			{
				if(skill.isNecessaryToggle())
					servitor.getAbnormalList().stop(skill.getId());
				return true;
			}
		}
		
		if (skill.getTemplate().getAutoUseType() == Skill.SkillAutoUseType.BUFF)
			for(Abnormal abnormal : servitor.getAbnormalList())
			{
				if(!abnormal.canReplaceAbnormal(skill, 1))
					return false;
			}

		SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.SERVITOR, skill);
		if(skillEntry == null)
			return false;

		Creature aimingTarget = skill.getAimingTarget(servitor, player.getTarget());
		
		if (!player.getAutoFarm().isFarmActivate() && (skill.getTemplate().getAutoUseType() == Skill.SkillAutoUseType.ATTACK))
			return false;
		
		if(!skill.checkCondition(skillEntry, servitor, aimingTarget, false, false, true, false, false))
			return false;
		
		if (servitor.isCastingNow())
			return false;

		servitor.setUsedSkill(skill, actionId); // TODO: [Bonux] Переделать.
		servitor.getAI().Cast(skillEntry, aimingTarget, false, false);
		return true;
	}
}
