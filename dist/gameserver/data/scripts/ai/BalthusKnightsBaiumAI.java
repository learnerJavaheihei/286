package ai;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 09.02.2019
 * Developed for L2-Scripts.com
 **/
public class BalthusKnightsBaiumAI extends AbstractBaiumAI {

    private static final int RASH_NPC_ID = 31716;	// Лаш - Адъютант Поддержки
    private static final int ARCHANGEL = 29100;

    public BalthusKnightsBaiumAI(NpcInstance actor) {
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
    protected boolean checkIfInLairZone(Creature target) {
        Zone zone = target.getReflection().getZone("[baium_balthus_knights]");
        if(zone == null)
            return false;
        return zone.checkIfInZone(target);
    }

    @Override
    protected int getArchangelId() {
        return ARCHANGEL;
    }
}
