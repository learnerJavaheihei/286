package npc.bypasses.global;

import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

/**
 * @author Bonux
**/
public class BlessingOfProtection extends ListenerHook implements OnInitScriptListener
{
	@Override
	public void onInit()
	{
		addHookGlobal(ListenerHookType.NPC_ASK);
	}

	@Override
	public void onNpcAsk(NpcInstance npc, int ask, long reply, int state, Player player)
	{
		if(ask == -20151209)
		{
			if(reply == 1)
			{
				if(player.getLevel() <= 70)
					npc.forceUseSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 5182, 1), player);
				else
					npc.showChatWindow(player, "teleporter/no_bless.htm", true);
			}
		}
	}
}