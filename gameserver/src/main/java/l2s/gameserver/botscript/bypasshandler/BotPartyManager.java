package l2s.gameserver.botscript.bypasshandler;

import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.botscript.BotControlPage;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

public class BotPartyManager
{
	@Bypass(value = "bot.add_p")
	public void addMember(Player player, NpcInstance npc, String[] param)
	{
		BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
		if(param.length > 0)
		{
			String charName = param[0];
			if (param.length>=2) {
				if(player.getName().equals(charName))
				{
					return;
				}
				if(!config.getPartyMemberHolder().containsKey(charName))
				{
					config.getPartyMemberHolder().put(charName, false);
				}
			}else if(charName.equals("addList") || charName.equals("addEdit")){
				player.sendMessage("附近没有找到玩家或者你填入的信息为空！");
			}
		}
		BotControlPage.party(player,param);
	}

	@Bypass(value = "bot.remove_p")
	public void removeMember(Player player, NpcInstance npc, String[] param)
	{
		BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
		String charName = param[0];
		if(config.getPartyMemberHolder().containsKey(charName))
		{
			config.getPartyMemberHolder().remove(charName);
		}
		BotControlPage.party(player, param);
	}

	@Bypass(value = "bot.p_run")
	public void pRun(Player player, NpcInstance npc, String[] param)
	{
		String charName = param[0];
		Player partner = this.checkCondition(player, charName, true);
		if(partner != null && BotEngine.getInstance().getBotConfig(partner).isAbort())
		{
			if(!partner.isActive())
			{
				partner.setActive();
			}
			BotEngine.getInstance().startBotTask(partner);
			player.sendMessage("\u5df2\u542f\u52a8\u76ee\u6807\u7684\u5185\u6302");
			/*\u5df2\u542f\u52a8\u76ee\u6807\u7684\u5185\u6302 已启动目标的内挂*/
		}
		BotControlPage.party(player, param);
	}

	@Bypass(value = "bot.p_abort")
	public void pStop(Player player, NpcInstance npc, String[] param)
	{
		String charName = param[0];
		Player partner = this.checkCondition(player, charName, true);
		if(partner != null && !BotEngine.getInstance().getBotConfig(partner).isAbort())
		{
			BotEngine.getInstance().getBotConfig(partner).setAbort(true, "\u961f\u957f\u63a7\u5236\u505c\u6b62");
			/*\u961f\u957f\u63a7\u5236\u505c\u6b62 队长控制停止*/
		}
		BotControlPage.party(player, param);
	}

	@Bypass(value = "bot.auto_accept")
	public void autoJoinr(Player player, NpcInstance npc, String[] param)
	{
		BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
		if(param.length > 0)
		{
			String charName = param[0];
			config.setLeaderName(charName);
		}
		BotControlPage.party(player, param);
	}

	@Bypass(value = "bot.auto_invite")
	public void pInvite(Player player, NpcInstance npc, String[] param)
	{
		BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
		String charName = param[0];
		Player partner = this.checkCondition(player, charName, false);
		if(partner != null)
		{
			config.getPartyMemberHolder().put(charName, true);
		}
		BotControlPage.party(player, param);
	}

	@Bypass(value = "bot.remove_invite")
	public void removeInvite(Player player, NpcInstance npc, String[] param)
	{
		BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
		String charName = param[0];
		config.getPartyMemberHolder().put(charName, false);
		BotControlPage.party(player, param);
	}

	private Player checkCondition(Player player, String charName, boolean checkParty)
	{
		Party party = player.getParty();
		if(party == null && checkParty)
		{
			player.sendMessage("\u5f53\u524d\u4e0d\u5728\u7ec4\u961f\u72b6\u6001\u4e0b");
			/*\u5f53\u524d\u4e0d\u5728\u7ec4\u961f\u72b6\u6001\u4e0b 当前不在组队状态下*/
			return null;
		}
		Player partner = null;
		if(checkParty)
		{
			partner = party.getPartyMembers().stream().filter(member -> charName.equals(member.getName())).findFirst().orElse(null);
			if(partner == null)
			{
				player.sendMessage("\u76ee\u6807\u4e0d\u5728\u961f\u4f0d\u4e2d");
				/*\u76ee\u6807\u4e0d\u5728\u961f\u4f0d\u4e2d 目标不在队伍中*/
				return null;
			}
		}
		else
		{
			partner = GameObjectsStorage.getPlayer(charName);
			if(partner == null)
			{
				player.sendMessage(String.valueOf(charName) + "\u4e0d\u5728\u7ebf");
				/*\u4e0d\u5728\u7ebf 不在线*/
				return null;
			}
		}
		if(!((BotConfigImp) BotEngine.getInstance().getBotConfig(partner)).getLeaderName().equals(player.getName()))
		{
			player.sendMessage("\u6ca1\u6709\u6743\u9650,\u8bf7\u5148\u8ba9\u5bf9\u65b9\u6388\u4e88\u6743\u9650");
			/*\u6ca1\u6709\u6743\u9650,\u8bf7\u5148\u8ba9\u5bf9\u65b9\u6388\u4e88\u6743\u9650 没有权限,请先让对方授予权限*/
			return null;
		}
		return partner;
	}
}