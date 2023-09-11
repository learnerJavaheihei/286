package l2s.gameserver.botscript;

import l2s.gameserver.botscript.actionhandler.*;
import l2s.gameserver.botscript.bypasshandler.*;
import l2s.gameserver.botscript.voicehandler.BotCallOut;
import l2s.gameserver.core.BotActionHandler;
import l2s.gameserver.core.IBotActionHandler;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;

import java.lang.reflect.Method;

public class BotScriptsLoader {
    public static Class<? extends IBotActionHandler>[] ACTION_CLAZ = new Class[]{BotInvitePartner.class, BotPickUpItem.class, BotHpMpShift.class, BotAbsorbBody.class, BotSpoilMob.class, BotSweepMob.class, BotRes.class, BotHeal.class, BotSupport.class, BotSummon.class, BotRest.class, BotFollowAttack.class, BotAttack.class, BotFollow.class};
    public static Class<?>[] BYPASS_CLAZ = new Class[]{BotSwitchBypass.class, BotConfigSet.class, BotPage.class, BotStrategyEdit.class, BotPathVisualize.class, BotBuffManager.class, BotPartyManager.class};

    public static void load() {
        for (Class<?> claz : BYPASS_CLAZ) {
            Method[] methods = claz.getDeclaredMethods();
            Object o = null;
            for (Method method : methods) {
                if (o == null) {
                    try {
                        o = claz.newInstance();
                    }
                    catch (IllegalAccessException | InstantiationException e) {
                        e.printStackTrace();
                    }
                }
                if (!method.isAnnotationPresent(Bypass.class)) continue;
                Bypass bypass = method.getAnnotation(Bypass.class);
                BypassHolder.getInstance().registerBypass(bypass.value(), o, method);
            }
        }
        for (int i = 0; i < ACTION_CLAZ.length; ++i) {
            try {
                BotActionHandler.getInstance().regHandler(i, ACTION_CLAZ[i].newInstance());
                continue;
            }
            catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        VoicedCommandHandler.getInstance().registerVoicedCommandHandler((IVoicedCommandHandler)new BotCallOut());
    }
}