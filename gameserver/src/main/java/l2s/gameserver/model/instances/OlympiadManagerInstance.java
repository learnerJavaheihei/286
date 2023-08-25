package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.olympiad.CompType;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.c2s.RequestBypassToServer;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExReceiveOlympiadPacket;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.HtmlUtils;

/**
 * @author Bonux
**/
public class OlympiadManagerInstance extends NpcInstance
{
	public OlympiadManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);

		Olympiad.addOlympiadNpc(this);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == -50)
		{
			if(player.getClassLevel().ordinal() >= ClassLevel.SECOND.ordinal() && player.getLevel() >= Config.OLYMPIAD_MIN_LEVEL)
				showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator001.htm", false);
			else
				showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator002.htm", false);
		}
		else if(ask == -51)
		{
			if(!Olympiad.isRegistered(player, false))
			{
				if(!Olympiad.isRegistrationActive())
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010p.htm", false);
				else if(Olympiad.isClassedBattlesAllowed())
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010a.htm", false, "<?olympiad_round?>", Olympiad.getCurrentCycle(), "<?olympiad_week?>", Olympiad.getCompDay(), "<?olympiad_participant?>", Olympiad.getParticipantsCount());
				else
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010b.htm", false, "<?olympiad_round?>", Olympiad.getCurrentCycle(), "<?olympiad_week?>", Olympiad.getCompDay(), "<?olympiad_participant?>", Olympiad.getParticipantsCount());
			}
			else
				showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010n.htm", false);
		}
		else if(ask == -52)
		{
			switch((int) reply)
			{
				case 0:
				{
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator001.htm", false);
					break;
				}
				case 1:
				{
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010a.htm", false);
					break;
				}
				case 2:
				{
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010b.htm", false);
					break;
				}
				case 3:
				{
					int[] waitingCounts = Olympiad.getWaitingList();

					int classedWaitingCount = waitingCounts[0];
					int teamWaitingCount = 0;
					int nonClassedWaitingCount = waitingCounts[0];

					String WaitingCount = classedWaitingCount < 100 ? HtmlUtils.htmlNpcString(1000504, 100) : HtmlUtils.htmlNpcString(1000505, 100);
					String TeamWaitingCount = teamWaitingCount < 100 ? HtmlUtils.htmlNpcString(1000504, 100) : HtmlUtils.htmlNpcString(1000505, 100);
					String ClassFreeWaitingCount = nonClassedWaitingCount < 100 ? HtmlUtils.htmlNpcString(1000504, 100) : HtmlUtils.htmlNpcString(1000505, 100);

					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010f.htm", false, "<?WaitingCount?>", WaitingCount, "<?TeamWaitingCount?>", TeamWaitingCount, "<?ClassFreeWaitingCount?>", ClassFreeWaitingCount);
					break;
				}
				case 4:
				{
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010g.htm", false);
					break;
				}
				case 5:
				{
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010h.htm", false, "<?WaitingCount?>", Olympiad.getParticipantPoints(player.getObjectId()));
					break;
				}
				case 6:
				case 7:
				{
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010m.htm", false);
					break;
				}
			}
		}
		else if(ask == -53)
		{
			if(reply == 0)
				showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator001.htm", false);
			else if(reply == 1)
			{
				if(player.isBaseClassActive())
				{
					if(player.getClassLevel().ordinal() >= ClassLevel.SECOND.ordinal() && player.getLevel() >= Config.OLYMPIAD_MIN_LEVEL)
					{
						Olympiad.addParticipant(player);
	
						if(Olympiad.getParticipantPoints(player.getObjectId()) > 0)
						{
							if(!player.isQuestContinuationPossible(true))
								return;

							if(Olympiad.registerParticipant(player, CompType.NON_CLASSED))
								showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010d.htm", false); // TODO: Проверить на оффе, надо ли данное сообщение.
						}
						else
							showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010i.htm", false);
					}
					else
						showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010j.htm", false);
				}
				else
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010c.htm", false);
			}
		}
		else if(ask == -54)
		{
			if(reply == 0)
				showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator001.htm", false);
			else if(reply == 1)
			{
				if(player.isBaseClassActive())
				{
					if(player.getClassLevel().ordinal() >= ClassLevel.SECOND.ordinal() && player.getLevel() >= Config.OLYMPIAD_MIN_LEVEL)
					{
						Olympiad.addParticipant(player);

						if(Olympiad.getParticipantPoints(player.getObjectId()) > 0)
						{
							if(!player.isQuestContinuationPossible(true))
								return;

							if(Olympiad.registerParticipant(player, CompType.CLASSED))
								showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010e.htm", false); // TODO: Проверить на оффе, надо ли данное сообщение.
						}
						else
							showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010i.htm", false);
					}
					else
						showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010j.htm", false);
				}
				else
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010c.htm", false);
			}
		}
		else if(ask == -55)
		{
			showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator030.htm", false);
		}
		else if(ask == -56)
		{
			//
		}
		else if(ask == -57)
		{
			//
		}
		else if(ask == -58)
		{
			Olympiad.unregisterParticipant(player);
		}
		/*else if(ask == -59)
		{
			if(reply == 0)
			{
				myself->ShowPage(talker, "olympiad_field_list_npc.htm");
			}
			else if(reply == 1)
			{
				myself->FHTML_SetFileName(fhtml0, "olympiad_operator020.htm");
				for (i0 = 1; i0 <= 22; i0 = i0->++)
				{
					s0 = "";
					s1 = "";
					myself->FHTML_SetInt(fhtml0, "FI" + i0, i0);
					if(myself->GetStatusForOlympiadField(i0) == 0)
					{
						myself->FHTML_SetStr(fhtml0, "Status" + i0, "&$906;");
					}
					else if(myself->GetStatusForOlympiadField(i0) == 1)
					{
						s0 = "&$1718;" + "&nbsp;&nbsp;&nbsp;" + myself->GetPlayer1ForOlympiadField(i0) + "&nbsp; : &nbsp;" + myself->GetPlayer2ForOlympiadField(i0);
						myself->FHTML_SetStr(fhtml0, "Status" + i0, s0);
					}
					else
					{
						s0 = "&$1719;" + "&nbsp;&nbsp;&nbsp;" + myself->GetPlayer1ForOlympiadField(i0) + "&nbsp; : &nbsp;" + myself->GetPlayer2ForOlympiadField(i0);
						myself->FHTML_SetStr(fhtml0, "Status" + i0, s0);
					}
				}
				myself->ShowFHTML(talker, fhtml0);
			}
			else if(reply == 2)
			{
				myself->FHTML_SetFileName(fhtml0, "olympiad_operator021.htm");
				for (i0 = 23; i0 <= 44; i0 = i0->++)
				{
					s0 = "";
					s1 = "";
					myself->FHTML_SetInt(fhtml0, "FI" + i0, i0);
					if(myself->GetStatusForOlympiadField(i0) == 0)
					{
						myself->FHTML_SetStr(fhtml0, "Status" + i0, "&$906;");
					}
					else if(myself->GetStatusForOlympiadField(i0) == 1)
					{
						s0 = "&$1718;" + "&nbsp;&nbsp;&nbsp;" + myself->GetPlayer1ForOlympiadField(i0) + "&nbsp; : &nbsp;" + myself->GetPlayer2ForOlympiadField(i0);
						myself->FHTML_SetStr(fhtml0, "Status" + i0, s0);
					}
					else
					{
						s0 = "&$1719;" + "&nbsp;&nbsp;&nbsp;" + myself->GetPlayer1ForOlympiadField(i0) + "&nbsp; : &nbsp;" + myself->GetPlayer2ForOlympiadField(i0);
						myself->FHTML_SetStr(fhtml0, "Status" + i0, s0);
					}
				}
				myself->ShowFHTML(talker, fhtml0);
			}
			else if(reply == 3)
			{
				myself->FHTML_SetFileName(fhtml0, "olympiad_operator022.htm");
				for (i0 = 45; i0 <= 66; i0 = i0->++)
				{
					s0 = "";
					s1 = "";
					myself->FHTML_SetInt(fhtml0, "FI" + i0, i0);
					if(myself->GetStatusForOlympiadField(i0) == 0)
					{
						myself->FHTML_SetStr(fhtml0, "Status" + i0, "&$906;");
					}
					else if(myself->GetStatusForOlympiadField(i0) == 1)
					{
						s0 = "&$1718;" + "&nbsp;&nbsp;&nbsp;" + myself->GetPlayer1ForOlympiadField(i0) + "&nbsp; : &nbsp;" + myself->GetPlayer2ForOlympiadField(i0);
						myself->FHTML_SetStr(fhtml0, "Status" + i0, s0);
					}
					else
					{
						s0 = "&$1719;" + "&nbsp;&nbsp;&nbsp;" + myself->GetPlayer1ForOlympiadField(i0) + "&nbsp; : &nbsp;" + myself->GetPlayer2ForOlympiadField(i0);
						myself->FHTML_SetStr(fhtml0, "Status" + i0, s0);
					}
				}
				myself->ShowFHTML(talker, fhtml0);
			}
			else if(reply == 4)
			{
				myself->FHTML_SetFileName(fhtml0, "olympiad_operator023.htm");
				for (i0 = 67; i0 <= 88; i0 = i0->++)
				{
					s0 = "";
					s1 = "";
					myself->FHTML_SetInt(fhtml0, "FI" + i0, i0);
					if(myself->GetStatusForOlympiadField(i0) == 0)
					{
						myself->FHTML_SetStr(fhtml0, "Status" + i0, "&$906;");
					}
					else if(myself->GetStatusForOlympiadField(i0) == 1)
					{
						s0 = "&$1718;" + "&nbsp;&nbsp;&nbsp;" + myself->GetPlayer1ForOlympiadField(i0) + "&nbsp; : &nbsp;" + myself->GetPlayer2ForOlympiadField(i0);
						myself->FHTML_SetStr(fhtml0, "Status" + i0, s0);
					}
					else
					{
						s0 = "&$1719;" + "&nbsp;&nbsp;&nbsp;" + myself->GetPlayer1ForOlympiadField(i0) + "&nbsp; : &nbsp;" + myself->GetPlayer2ForOlympiadField(i0);
						myself->FHTML_SetStr(fhtml0, "Status" + i0, s0);
					}
				}
				myself->ShowFHTML(talker, fhtml0);
			}
		}*/
		else if(ask == -60)
		{
			if(reply == 0)
			{
				if(player.getClassLevel().ordinal() >= ClassLevel.SECOND.ordinal() && player.getLevel() >= Config.OLYMPIAD_MIN_LEVEL)
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator001.htm", false);
				else
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator002.htm", false);
			}
		}
		else if(ask == -61)
		{
			showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator020.htm", false);
		}
		else if(ask == -70)
		{
			if(reply == 0)
			{
				showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator001.htm", false);
			}
			else if(reply == 1)
			{
				int passes = Olympiad.getParticipantRewardCount(player, false);
				if(passes == 0)
				{
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator031a.htm", false);
				}
				else if(passes < 20)
				{
					if(player.isHero() || Hero.getInstance().isInactiveHero(player.getObjectId()))
					{
						showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator031b.htm", false);
					}
					else
					{
						showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator031a.htm", false);
					}
				}
				else
				{
					showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator031.htm", false);
				}
			}
			else if(reply == 2)
			{
				showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator010l.htm", false, "<?WaitingCount?>", Olympiad.getParticipantPointsPast(player.getObjectId()));
			}
			else if(reply == 603)
			{
				MultiSellHolder.getInstance().SeparateAndSend((int) reply, player, 0);
			}
		}
		else if(ask == -71)
		{
			if(reply == 0)
			{
				showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator030.htm", false);
			}
			else if(reply == 1)
			{
				if(!player.isQuestContinuationPossible(true))
					return;

				int passes = Olympiad.getParticipantRewardCount(player, true);
				if(passes > 0)
					ItemFunctions.addItem(player, Config.ALT_OLY_COMP_RITEM, passes);
			}
		}
		else if(ask == -80)
		{
			//
		}
		/*else if(ask == -110)
		{
			List<String> names = OlympiadDatabase.getClassLeaderBoard(reply);

			HtmlMessage html = new HtmlMessage(this, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_operator_rank_class.htm");
			for(int index = 1; index <= 15; index++)
			{
				if(names.size() < index)
				{
					html.replace("<?Rank" + index + "?>", "");
					html.replace("<?Name" + index + "?>", "");
				}
				else
				{
					html.replace("<?Rank" + index + "?>", String.valueOf(index));
					html.replace("<?Name" + index + "?>", names.get(index - 1));
				}
			}
			player.sendPacket(html);
		}*/
		else if(ask == -130)
		{
			if(!Config.ENABLE_OLYMPIAD_SPECTATING)
				return;

			Olympiad.addObserver((int) reply, player);
		}
		else
			super.onMenuSelect(player, ask, reply, state);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		// до всех проверок
		if(command.startsWith("_olympiad?")) // _olympiad?command=move_op_field&field=1
		{
			if(command.startsWith("_olympiad?command=op_field_list"))
			{
				if(!Olympiad.inCompPeriod() || Olympiad.isOlympiadEnd())
				{
					player.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
					return;
				}
				player.sendPacket(new ExReceiveOlympiadPacket.MatchList());
			}
			else if(command.startsWith("_olympiad?command=move_op_field"))
			{
				String[] ar = command.split("&");
				if(ar.length < 2)
					return;

				if(!Config.ENABLE_OLYMPIAD_SPECTATING)
					return;

				String[] command2 = ar[1].split("=");
				if(command2.length < 2)
					return;

				Olympiad.addObserver(Integer.parseInt(command2[1]) - 1, player);
			}
			return;
		}

		super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlDir(String filename, Player player)
	{
		return Olympiad.OLYMPIAD_HTML_PATH;
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg)
	{
		if(val == 0) // Grand Olympiad Manager
		{
			String fileName = Olympiad.OLYMPIAD_HTML_PATH;
			if(player.getClassLevel().ordinal() >= ClassLevel.SECOND.ordinal() && player.getLevel() >= Config.OLYMPIAD_MIN_LEVEL)
				fileName += "olympiad_operator001.htm";
			else
				fileName += "olympiad_operator002.htm";

			showChatWindow(player, fileName, firstTalk);
		}
		else
			super.showChatWindow(player, val, firstTalk, arg);
	}

	@Override
	public boolean canPassPacket(Player player, Class<? extends L2GameClientPacket> packet, Object... arg)
	{
		return packet == RequestBypassToServer.class && arg.length == 1 && (arg[0].equals("_olympiad?command=op_field_list") || arg[0].equals("_olympiad?command=move_op_field"));
	}
}