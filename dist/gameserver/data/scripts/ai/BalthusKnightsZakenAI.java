package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author L2-scripts.com - (SanyaDC) (
 **/
public class BalthusKnightsZakenAI extends Zaken {

    private static final int RASH_NPC_ID = 31716;	// Лаш - Адъютант Поддержки


    public BalthusKnightsZakenAI(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtDead(Creature killer) {
        NpcInstance actor = getActor();
        Reflection reflection = actor.getReflection();
        NpcUtils.spawnSingle(RASH_NPC_ID, actor.getLoc(), reflection);
        reflection.setReenterTime(System.currentTimeMillis(), false);
        reflection.startCollapseTimer(5, true);
        super.onEvtDead(killer);
    }

   @Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		super.onEvtAttacked(attacker, skill, damage);


	}

}
