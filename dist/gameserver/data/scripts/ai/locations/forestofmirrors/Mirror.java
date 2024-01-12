package ai.locations.forestofmirrors;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author Bonux
 **/
public class Mirror extends Fighter {
    // Monster ID's
    private static final int MIRROR_NPC_ID = 20639; // Зекрало

    private static final int DESPAWN_TIME = 600000;

    private int _spawnStage = 0;
    private boolean canGenerateNewMonsters = true; // 新增一个标志位，默认为true

    public Mirror(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtDead(Creature killer) {
        super.onEvtDead(killer);

        // 判断是否可以生成新怪物（概率）
        if (canGenerateNewMonsters && Rnd.chance(80)) {
            final NpcInstance actor = getActor();
            ThreadPoolManager.getInstance().schedule(() -> {
                // 生成两个新怪物
                for (int i = 0; i < 2; i++) {
                    NpcInstance npc = NpcUtils.spawnSingle(MIRROR_NPC_ID, actor.getLoc(), DESPAWN_TIME);
                    if (npc.getAI() instanceof Mirror)
                        ((Mirror) npc.getAI()).setSpawnStage(_spawnStage + 1);
                    npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 200);
                    // 设置新生成的怪物标志位为false，防止再次生成新怪物
                    ((Mirror) npc.getAI()).setCanGenerateNewMonsters(false);
                }
            }, 1000L);
        }
    }

    // 新增一个方法，用于设置标志位
    public void setCanGenerateNewMonsters(boolean value) {
        canGenerateNewMonsters = value;
    }

    // 请将这行代码修改为 setSpawnStage(int value)
    public void setSpawnStage(int value) {
        _spawnStage = value;
    }
}
