package l2s.gameserver.model.actor.instances.player;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExAutoplayDoMacro;
import l2s.gameserver.network.l2.s2c.ExAutoplaySetting;
import l2s.gameserver.utils.ItemFunctions;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class AutoFarm
{
	private class DistanceComparator implements Comparator<GameObject>
	{
		@Override
		public int compare(GameObject o1, GameObject o2)
		{
			int dist1 = _owner.getDistance(o1);
			int dist2 = _owner.getDistance(o2);
			return Integer.compare(dist1, dist2);
		}
	}

	private final Player _owner;

	private final DistanceComparator _distanceComparator = new DistanceComparator();

	private int _unkParam1 = 12;
	private int _unkParam2 = 0;

	private boolean _farmActivate = false;
	private boolean _autoPickUpItems = false;
	private boolean _meleeAttackMode = false;
	private int _healPercent = 0;
	private boolean _politeFarm = false;

	private ScheduledFuture<?> _farmTask = null;

	public AutoFarm(Player owner)
	{
		_owner = owner;
	}

	public Player getOwner()
	{
		return _owner;
	}

	public int getUnkParam1()
	{
		return _unkParam1;
	}

	public void setUnkParam1(int unkParam1)
	{
		this._unkParam1 = unkParam1;
	}

	public boolean isFarmActivate()
	{
		return _farmActivate;
	}

	public void setFarmActivate(boolean farmActivate)
	{
		_farmActivate = farmActivate;
	}

	public boolean isAutoPickUpItems()
	{
		return _autoPickUpItems;
	}

	public void setAutoPickUpItems(boolean autoPickUpItems)
	{
		_autoPickUpItems = autoPickUpItems;
	}

	public int getUnkParam2()
	{
		return _unkParam2;
	}

	public void setUnkParam2(int unkParam2)
	{
		_unkParam2 = unkParam2;
	}

	public boolean isMeleeAttackMode()
	{
		return _meleeAttackMode;
	}

	public void setMeleeAttackMode(boolean meleeAttackMode)
	{
		_meleeAttackMode = meleeAttackMode;
	}

	public int getHealPercent() {
		return _healPercent;
	}

	public void setHealPercent(int healPercent) {
		_healPercent = healPercent;
	}

	public boolean isPoliteFarm() {
		return _politeFarm;
	}

	public void setPoliteFarm(boolean politeFarm) {
		_politeFarm = politeFarm;
	}

	public synchronized void doAutoFarm()
	{
		if(_owner.isTeleporting() || _owner.isDead()){
			_farmActivate=false;
			//_farmTask.cancel(false);
			_owner.sendPacket(new ExAutoplaySetting(_owner));

		}
		if(_owner.isMounted() || _owner.isTransformed()) {
			_farmActivate=false;
			_owner.sendPacket(new ExAutoplaySetting(_owner));
			return;
		}
		if(!isFarmActivate())
		{
			if(_farmTask != null)
			{
				_farmTask.cancel(false);
				_farmTask = null;
			}
			return;
		}

		if(_farmTask == null)
			_farmTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(() -> doAutoFarm(), 500L, 500L);

		if(_owner.getAI().getIntention() == CtrlIntention.AI_INTENTION_PICK_UP)
			return;

		GameObject target = _owner.getTarget();
		if(!checkTargetCondition(target))
		{
			_owner.setTarget(null);
			target = findAutoFarmTarget();
			if(target != null)
			{
				if(target.isItem())
				{
					_owner.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, target, null);
					return;
				}
				_owner.setTarget(target);
			}
		}

		if(target == null)
			return;
		
		if (!_owner.isMageClass() || !_owner.getAutoShortCuts().autoSkillsActive())
			_owner.sendPacket(new ExAutoplayDoMacro());
	}

	private GameObject findAutoFarmTarget()
	{
		if(isAutoPickUpItems())
		{
			List<ItemInstance> items = World.getAroundItems(_owner, 2000, 500);
			items.sort(_distanceComparator);
			for(ItemInstance item : items)
			{
				if(!item.isVisible())
					continue;
				if(!ItemFunctions.checkIfCanPickup(_owner, item))
					continue;
				if(item.getDropTimeOwner() <= System.currentTimeMillis()) // На оффе не подымает итемы, которые уже не блестят.
					continue;
				if(!GeoEngine.canMoveToCoord(_owner, item))
					continue;
				return item;
			}
		}

		List<NpcInstance> npcs = World.getAroundNpc(_owner, 2000, 500);
		npcs.sort(_distanceComparator);

		if(_owner.hasServitor() || _owner.isInParty())
		{
			for(NpcInstance npc : npcs)
			{
				if(checkNpcCondition(npc, true))
					return npc;
			}
		}

		for(NpcInstance npc : npcs)
		{
			if(checkNpcCondition(npc, false))
				return npc;
		}
		return null;
	}

	private boolean checkTargetCondition(GameObject target)
	{
		if(target == null)
			return false;
		if(!target.isNpc())
			return false;
		return checkNpcCondition((NpcInstance) target, false);
	}

	private boolean checkNpcCondition(NpcInstance npc, boolean help)
	{
		if(!npc.isMonster())
			return false;
		if(npc.isAlikeDead())
			return false;
		if(npc.isInvulnerable())
			return false;
		if(npc.isRaid())
			return false;
		NpcInstance leader = npc.getLeader();
		if(leader != null && leader.isRaid())
			return false;
		if(!help)
		{
			int attackRange = isMeleeAttackMode() ? 600 : 1200;
			if(!npc.isInRange(_owner, attackRange))
				return false;
		}
		if(npc.isInvisible(_owner))
			return false;
		if(!npc.isAutoAttackable(_owner))
			return false;
		if(npc.getReflectionId() != _owner.getReflectionId())
			return false;
		if(!GeoEngine.canSeeTarget(_owner, npc))
			return false;

		Creature[] npcTargets = new Creature[]{npc.getAI().getAttackTarget(), npc.getAI().getCastTarget()};
		if(npc.isInCombat())
		{
			for(Creature attackTarget : npcTargets)
			{
				if(attackTarget != null)
				{
					if(!help && attackTarget == _owner)
						return true;
					if(_owner.isMyServitor(attackTarget.getObjectId()))
						return true;
					Player attackTargetPlayer = attackTarget.getPlayer();
					if(attackTargetPlayer != null && _owner.isInSameParty(attackTargetPlayer))
						return true;
				}
			}
		}

		if(help)
			return false;

		if (isPoliteFarm() && npc.isInCombat()) { // Вежливая охота
			for (Creature attackTarget : npcTargets) {
				if (attackTarget != null && attackTarget.isPlayable())
					return false;
			}
		}
		return true;
	}
}
