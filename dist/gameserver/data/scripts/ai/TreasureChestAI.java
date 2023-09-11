package ai;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;


import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.GuardInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.updatetype.NpcInfoType;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.PositionUtils;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.Guard;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.Creature;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author L2-scripts.com - (SanyaDC)
 */
public class TreasureChestAI extends Fighter {


	public TreasureChestAI(NpcInstance actor) {
		super(actor);

	}

	@Override
	public boolean canAttackCharacter(Creature target) {
		return true;
	}


	@Override
	protected void onEvtSpawn() {
		NpcInstance chest = getActor();

		for (NpcInstance target : chest.getAroundNpc(2000, 500)) {
			target.getAggroList().addDamageHate(chest, 1, 5000);
			target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, chest, 300);
			setAttackTarget(chest);
			changeIntention(CtrlIntention.AI_INTENTION_ATTACK, chest, null); 
			addTaskAttack(chest);
		}
	}
	@Override
	protected boolean thinkActive() {
		NpcInstance chest = getActor();

		for (NpcInstance target : chest.getAroundNpc(2000, 500)) {
			target.getAggroList().addDamageHate(chest, 1, 5000);
			target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, chest, 300);
			setAttackTarget(chest);
			changeIntention(CtrlIntention.AI_INTENTION_ATTACK, chest, null);
			addTaskAttack(chest);

		}
		return true;
	}

	@Override
	protected void onEvtDead(Creature killer) {
		NpcInstance actor = getActor();
		Calendar c = Calendar.getInstance();
		Integer dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		if(actor.getNpcId() == 21756) {
			if (dayOfWeek == 1) {
				if (Rnd.chance(30)) {
					actor.dropItem(killer.getPlayer(), 5535, 1);//древко кровавого лука
					if (Rnd.chance(20))
						actor.dropItem(killer.getPlayer(), 5444, 1);//кровавый лук
				}
			}
			if (dayOfWeek == 2) {
				if (Rnd.chance(30)) {
					actor.dropItem(killer.getPlayer(), 5536, 1);//навершие орхидеи
					actor.dropItem(killer.getPlayer(), 5543, 1);//часть посоха даспариона
					actor.dropItem(killer.getPlayer(), 8341, 1);//осколок глаза духа
					if (Rnd.chance(20)) {
						actor.dropItem(killer.getPlayer(), 5446, 1);//кров орхидея
						actor.dropItem(killer.getPlayer(), 5460, 1);//посох даспариона
						actor.dropItem(killer.getPlayer(), 8314, 1);//глаз духа
					}
				}
			}
			if (dayOfWeek == 3) {
				if (Rnd.chance(30)) {
					actor.dropItem(killer.getPlayer(), 5539, 1);//лезвие торнадо
					if (Rnd.chance(20))
						actor.dropItem(killer.getPlayer(), 5452, 1);//кровавый торнадо
				}
			}
			if (dayOfWeek == 4) {
				if (Rnd.chance(30)) {
					actor.dropItem(killer.getPlayer(), 5542, 1);//лезвие алебарды
					if (Rnd.chance(20))
						actor.dropItem(killer.getPlayer(), 5458, 1);//алебарда
				}
			}
			if (dayOfWeek == 5) {
				if (Rnd.chance(30)){
					actor.dropItem(killer.getPlayer(), 5532, 1);//навершие метеоритного дождя
					actor.dropItem(killer.getPlayer(), 8346, 1);//часть молота разрушителя
					if (Rnd.chance(20)) {
						actor.dropItem(killer.getPlayer(), 5438, 1);//метеоритный дождь
						actor.dropItem(killer.getPlayer(), 8487, 1);//молото разрушителя
					}
			}
			}
			if (dayOfWeek == 6) {
				if (Rnd.chance(30)) {
					actor.dropItem(killer.getPlayer(), 5536, 1);//навершие орхидеи
					if (Rnd.chance(20)) {
						actor.dropItem(killer.getPlayer(), 5446, 1);//кров орхидея
					}
				}
			}
			if (dayOfWeek == 7) {
				if (Rnd.chance(30)){
					actor.dropItem(killer.getPlayer(), 5548, 1);//лезвие талума
					actor.dropItem(killer.getPlayer(), 8331, 1);//лезвие мастера инферно
					if (Rnd.chance(20)) {
						actor.dropItem(killer.getPlayer(), 5470, 1);//клинок талума
						actor.dropItem(killer.getPlayer(), 8300, 1);//мастер инферно
					}
			}
			}
		}
		if(actor.getNpcId() == 21757) {
			if (dayOfWeek == 1) {
				if (Rnd.chance(40)){
					actor.dropItem(killer.getPlayer(), 5535, 1);//древко кровавого лука
				if (Rnd.chance(30))
					actor.dropItem(killer.getPlayer(), 5444, 1);//кровавый лук
			}
			}
			if (dayOfWeek == 2) {
				if (Rnd.chance(40)) {
					actor.dropItem(killer.getPlayer(), 5536, 1);//навершие орхидеи
					actor.dropItem(killer.getPlayer(), 5543, 1);//часть посоха даспариона
					actor.dropItem(killer.getPlayer(), 8341, 1);//осколок глаза духа
					if (Rnd.chance(30)) {
						actor.dropItem(killer.getPlayer(), 5446, 1);//кров орхидея
						actor.dropItem(killer.getPlayer(), 5460, 1);//посох даспариона
						actor.dropItem(killer.getPlayer(), 8314, 1);//глаз духа
					}
				}
			}
			if (dayOfWeek == 3) {
				if (Rnd.chance(40)){
					actor.dropItem(killer.getPlayer(), 5539, 1);//лезвие торнадо
				if (Rnd.chance(30))
					actor.dropItem(killer.getPlayer(), 5452, 1);//кровавый торнадо
			}
			}
			if (dayOfWeek == 4) {
				if (Rnd.chance(40)) {
					actor.dropItem(killer.getPlayer(), 5542, 1);//лезвие алебарды
					if (Rnd.chance(30))
						actor.dropItem(killer.getPlayer(), 5458, 1);//алебарда
				}
			}
			if (dayOfWeek == 5) {
				if (Rnd.chance(40)){
					actor.dropItem(killer.getPlayer(), 5532, 1);//навершие метеоритного дождя
					actor.dropItem(killer.getPlayer(), 8346, 1);//часть молота разрушителя
					if (Rnd.chance(30)) {
						actor.dropItem(killer.getPlayer(), 5438, 1);//метеоритный дождь
						actor.dropItem(killer.getPlayer(), 8487, 1);//молото разрушителя
					}
				}
			}
			if (dayOfWeek == 6) {
				if (Rnd.chance(40)) {
					actor.dropItem(killer.getPlayer(), 5536, 1);//навершие орхидеи
					if (Rnd.chance(30)) {
						actor.dropItem(killer.getPlayer(), 5446, 1);//кров орхидея
					}
				}
			}
				if (dayOfWeek == 7) {
					if (Rnd.chance(40)){
						actor.dropItem(killer.getPlayer(), 5548, 1);//лезвие талума
						actor.dropItem(killer.getPlayer(), 8331, 1);//лезвие мастера инферно
						if (Rnd.chance(30)) {
							actor.dropItem(killer.getPlayer(), 5470, 1);//клинок талума
							actor.dropItem(killer.getPlayer(), 8300, 1);//мастер инферно
						}
					}
				}
		}
		super.onEvtDead(killer);
	}
}
