package l2s.gameserver.botscript;

import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.core.BotProperties;
import l2s.gameserver.core.IBotRuntimeChecker;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;

public class BotRuntimeChecker
implements IBotRuntimeChecker {
    @Override
    public boolean test(Player actor) {
        BotConfig config = BotEngine.getInstance().getBotConfig(actor);
        if (!actor.hasPremiumAccount() && BotProperties.ONLY_VIP) {
            config.setAbort(true, "\u4ec5\u9650VIP\u4f1a\u5458\u4f7f\u7528");
		/*\u4ec5\u9650VIP\u4f1a\u5458\u4f7f\u7528 仅限VIP会员使用*/
        }
        if (actor.getPvpFlag() != 0) {
            config.setAbort(true, "\u65e0\u6cd5\u5728\u7d2b\u540d\u72b6\u6001\u4e0b\u4f7f\u7528\u6302\u673a\u529f\u80fd");
		/*无法在紫名状态下使用挂机功能*/
		}
		if (actor.getKarma() != 0) {
            config.setAbort(true, "\u65e0\u6cd5\u5728\u7ea2\u540d\u72b6\u6001\u4e0b\u4f7f\u7528\u6302\u673a\u529f\u80fd");
		/*无法在红名状态下使用挂机功能*/
        }
        if (actor.isInSiegeZone() || actor.isInZone(Zone.ZoneType.battle_zone)) {
            config.setAbort(true, "\u65e0\u6cd5\u5728\u653b\u57ce\u3001PvP\u5730\u533a\u4f7f\u7528\u6302\u673a\u529f\u80fd");
		/*无法在攻城、PvP地区使用挂机功能*/
        }
        if (actor.isInOlympiadMode()) {
            config.setAbort(true, "\u65e0\u6cd5\u5728\u5965\u8d5b\u8fc7\u7a0b\u4e2d\u4f7f\u7528\u6302\u673a\u529f\u80fd");
		/*无法在奥赛过程中使用挂机功能*/
        }
		/*PVP活动无法启动--*/
    	if (actor.isInPvPEvent())
    	{
    		config.setAbort(true, "活动中无法开启狩獵..");
    	}
		/*--PVP活动无法启动*/
        if (actor.isDead()) {
            config.setDeathTime(config.getDeathTime() + 1);
            if (config.getDeathTime() >= 300) {
                config.setAbort(true, "\u4f60\u6302\u4e86");
				/*\u4f60\u6302\u4e86 你挂了*/
            }
            return config.isAbort();
        }
        if (config.getDeathTime() > 0) {
            config.setDeathTime(0);
        }
        if (GameObjectsStorage.getPlayer(actor.getObjectId()) == null) {
            config.setAbort(true, "NONE");
        }
        if (actor.isInOfflineMode()) {
            config.setAbort(true, "NONE");
        }
        return config.isAbort();
    }
}