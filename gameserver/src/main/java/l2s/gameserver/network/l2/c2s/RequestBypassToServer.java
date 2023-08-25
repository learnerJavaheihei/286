package l2s.gameserver.network.l2.c2s;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.handler.admincommands.AdminCommandHandler;
import l2s.gameserver.handler.bbs.BbsHandlerHolder;
import l2s.gameserver.handler.bbs.IBbsHandler;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.instancemanager.OfflineBufferManager;
import l2s.gameserver.instancemanager.OlympiadHistoryManager;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.PvPEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExShowVariationCancelWindow;
import l2s.gameserver.network.l2.s2c.ExShowVariationMakeWindow;
import l2s.gameserver.network.l2.s2c.PackageToListPacket;
import l2s.gameserver.utils.BypassStorage.ValidBypass;
import l2s.gameserver.utils.BypassStorage.BypassType;
import l2s.gameserver.utils.MulticlassUtils;
import l2s.gameserver.utils.NpcUtils;
import l2s.gameserver.utils.WarehouseFunctions;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestBypassToServer extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestBypassToServer.class);

	private String _bypass = null;

	@Override
	protected boolean readImpl()
	{
		_bypass = readS();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _bypass.isEmpty())
			return;

		ValidBypass bp = activeChar.getBypassStorage().validate(_bypass);
		if(bp == null)
		{
			_log.debug("RequestBypassToServer: Unexpected bypass : " + _bypass + " client : " + getClient() + "!");
			return;
		}

		NpcInstance npc = activeChar.getLastNpc();

		try
		{
			if(bp.bypass.startsWith("admin_"))
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, bp.bypass);
			else if(bp.bypass.startsWith("multisell "))
			{
				int multisellId = Integer.parseInt(bp.bypass.substring(10).trim());
				MultiSellHolder.getInstance().SeparateAndSend(multisellId, activeChar, 0);
			}
			else if(bp.bypass.startsWith("Augment "))
			{
				if(Config.ALLOW_AUGMENTATION)
				{
					int cmdChoice = Integer.parseInt(bp.bypass.substring(8).trim());
					if(cmdChoice == 1)
						activeChar.sendPacket(SystemMsg.SELECT_THE_ITEM_TO_BE_AUGMENTED, ExShowVariationMakeWindow.STATIC);
					else if(cmdChoice == 2)
						activeChar.sendPacket(SystemMsg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, ExShowVariationCancelWindow.STATIC);
				}
			}
			else if(bp.bypass.startsWith("pcbang?"))
			{
				String command = bp.bypass.substring(7).trim();
				StringTokenizer st = new StringTokenizer(command, "_");

				String cmd = st.nextToken();
				if(cmd.equalsIgnoreCase("multisell"))
				{
					int multisellId = Integer.parseInt(st.nextToken());
					if(!Config.ALT_ALLOWED_MULTISELLS_IN_PCBANG.contains(multisellId))
					{
						_log.warn("Unknown multisell list use in PC-Bang shop! List ID: " + multisellId + ", player ID: " + activeChar.getObjectId() + ", player name: " + activeChar.getName());
						return;
					}
					MultiSellHolder.getInstance().SeparateAndSend(multisellId, activeChar, 0);
				}
			}
			else if(bp.bypass.startsWith("scripts_"))
			{
				_log.error("Trying to call script bypass: " + bp.bypass + " " + activeChar);
			}
			else if(bp.bypass.startsWith("htmbypass_"))
			{
				String command = bp.bypass.substring(10).trim();
				String word = command.split("\\s+")[0];
				String args = command.substring(word.length()).trim();

				Pair<Object, Method> b = BypassHolder.getInstance().getBypass(word);
				if(b != null)
				{
					try
					{
						b.getValue().invoke(b.getKey(), activeChar, npc, StringUtils.isEmpty(args) ? new String[0] : args.split("\\s+"));
					}
					catch(Exception e)
					{
						_log.error("Exception: " + e, e);
					}
				}
				else
					_log.warn("Cannot find html bypass: " + command);
			}
			else if(bp.bypass.startsWith("user_"))
			{
				String command = bp.bypass.substring(5).trim();
				String word = command.split("\\s+")[0];
				String args = command.substring(word.length()).trim();
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(word);

				if(vch != null)
					vch.useVoicedCommand(word, activeChar, args);
				else
					_log.warn("Unknow voiced command '" + word + "'");
			}
			else if(bp.bypass.startsWith("npc_"))
			{
				int endOfId = bp.bypass.indexOf('_', 5);
				String id;
				if(endOfId > 0)
					id = bp.bypass.substring(4, endOfId);
				else
					id = bp.bypass.substring(4);
				if(npc != null && npc.canBypassCheck(activeChar))
				{
					String command = bp.bypass.substring(endOfId + 1);
					npc.onBypassFeedback(activeChar, command);
				}
			}
			else if(bp.bypass.startsWith("npc?"))
			{
				if(npc != null && npc.canBypassCheck(activeChar))
				{
					String command = bp.bypass.substring(4).trim();
					npc.onBypassFeedback(activeChar, command);
				}
			}
			else if(bp.bypass.startsWith("item?"))
			{
			}
			else if(bp.bypass.startsWith("class_change?"))
			{
				String command = bp.bypass.substring(13).trim();
				if(command.startsWith("class_name="))
				{
					if(npc != null && npc.canBypassCheck(activeChar))
					{
						int classId = Integer.parseInt(command.substring(11).trim());
						npc.onChangeClassBypass(activeChar, classId);
					}
				}
			}
			else if(bp.bypass.startsWith("quest_accept?"))
			{
				String command = bp.bypass.substring(13).trim();
				if(command.startsWith("quest_id="))
				{
					if(npc != null && npc.canBypassCheck(activeChar))
					{
						int questId = Integer.parseInt(command.substring(9).trim());
						activeChar.processQuestEvent(questId, Quest.ACCEPT_QUEST_EVENT, npc);
					}
				}
			}
			else if(bp.bypass.startsWith("_olympiad?")) // _olympiad?command=move_op_field&field=1
			{
				// Переход в просмотр олимпа разрешен только от менеджера или с арены.
				final NpcInstance manager = NpcUtils.canPassPacket(activeChar, this, bp.bypass.split("&")[0]);
				if(manager != null)
					manager.onBypassFeedback(activeChar, bp.bypass);
			}
			else if(bp.bypass.equalsIgnoreCase("_heroes"))
			{
				// Просмотр героев олимпиады.
				final NpcInstance manager = NpcUtils.canPassPacket(activeChar, this, bp.bypass);
				if(manager != null)
					manager.onBypassFeedback(activeChar, bp.bypass);
			}
			else if(bp.bypass.startsWith("_diary"))
			{
				String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if(heroid > 0)
					Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
			}
			else if(bp.bypass.startsWith("_match"))
			{
				String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);

				OlympiadHistoryManager.getInstance().showHistory(activeChar, heroclass, heropage);
			}
			else if(bp.bypass.startsWith("manor_menu_select?")) // Navigate throught Manor windows
			{
				GameObject object = activeChar.getTarget();
				if(object != null && object.isNpc())
					((NpcInstance) object).onBypassFeedback(activeChar, bp.bypass);
			}
			else if(bp.bypass.startsWith("menu_select?"))
			{
				if(npc != null && npc.canBypassCheck(activeChar))
				{
					String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
					StringTokenizer st = new StringTokenizer(params, "&");
					int ask = Integer.parseInt(st.nextToken().split("=")[1].trim());
					long reply = st.hasMoreTokens() ? Long.parseLong(st.nextToken().split("=")[1].trim()) : 0L;
					int state = st.hasMoreTokens() ? Integer.parseInt(st.nextToken().split("=")[1].trim()) : 0;
					npc.onMenuSelect(activeChar, ask, reply, state);
				}
			}
			else if(bp.bypass.equals("talk_select"))
			{
				if(npc != null && npc.canBypassCheck(activeChar))
					npc.showQuestWindow(activeChar);
			}
			else if(bp.bypass.equals("teleport_request"))
			{
				if(npc != null && npc.canBypassCheck(activeChar))
					npc.onTeleportRequest(activeChar);
			}
			else if(bp.bypass.equals("learn_skill"))
			{
				if(npc != null && npc.canBypassCheck(activeChar))
					npc.onSkillLearnBypass(activeChar);
			}
			else if(bp.bypass.equals("deposit"))
			{
				if(npc != null && npc.canBypassCheck(activeChar))
					WarehouseFunctions.showDepositWindow(activeChar);
			}
			else if(bp.bypass.equals("withdraw"))
			{
				if(npc != null && npc.canBypassCheck(activeChar))
					WarehouseFunctions.showRetrieveWindow(activeChar);
			}
			else if(bp.bypass.equals("deposit_pledge"))
			{
				if(npc != null && npc.canBypassCheck(activeChar))
					WarehouseFunctions.showDepositWindowClan(activeChar);
			}
			else if(bp.bypass.equals("withdraw_pledge"))
			{
				if(npc != null && npc.canBypassCheck(activeChar))
					WarehouseFunctions.showWithdrawWindowClan(activeChar);
			}
			else if(bp.bypass.equals("package_deposit"))
			{
				if(npc != null && npc.canBypassCheck(activeChar))
					activeChar.sendPacket(new PackageToListPacket(activeChar));
			}
			else if(bp.bypass.equals("package_withdraw"))
			{
				if(npc != null && npc.canBypassCheck(activeChar))
					WarehouseFunctions.showFreightWindow(activeChar);
			}
			else if(bp.bypass.startsWith("Quest "))
			{
				_log.warn("Trying to call Quest bypass: " + bp.bypass + ", player: " + activeChar);
			}
			else if(bp.bypass.startsWith("buffstore?"))
			{
				OfflineBufferManager.getInstance().processBypass(activeChar, bp.bypass.substring(10).trim());
			}
			else if(bp.bypass.startsWith("pvpevent_")) // TODO: Нафиг данный хардкод в данном месте? о.0
			{
				String[] temp = bp.bypass.split(";");

				for(String bypass : temp)
				{
					if(bypass.startsWith("pvpevent"))
					{
						StringTokenizer st = new StringTokenizer(bypass, "_");
						st.nextToken();
						String cmd = st.nextToken();
						int val = Integer.parseInt(st.nextToken());

						if(cmd.equalsIgnoreCase("showReg"))
						{
							PvPEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, val);
							if(event != null && event.isRegActive())
							{
								event.showReg();
							}
						}
						else if(cmd.startsWith("reg"))
						{
							PvPEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, val);
							if(event != null && event.isRegActive())
								if(cmd.contains(":"))
									event.regCustom(activeChar, cmd);
								else
									event.reg(activeChar);
						}
					}
					else
					{
						IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler(bypass);
						if(handler != null)
						{
							handler.onBypassCommand(activeChar, bypass);
						}
					}
				}
			}
			else if(bp.bypass.startsWith("multiclass?"))
			{
				MulticlassUtils.onBypass(activeChar, bp.bypass.substring(11).trim());
			}
			else if(bp.type == BypassType.BBS)
			{
				if(!Config.BBS_ENABLED)
					activeChar.sendPacket(SystemMsg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
				else
				{
					IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler(bp.bypass);
					if(handler != null)
						handler.onBypassCommand(activeChar, bp.bypass);
				}
			}
		}
		catch(Exception e)
		{
			String st = "Error while handling bypass: " + bp.bypass;
			if(npc != null)
				st = st + " via NPC " + npc;

			_log.error(st, e);
		}
	}
}