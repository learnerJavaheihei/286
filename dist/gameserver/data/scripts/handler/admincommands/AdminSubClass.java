package handler.admincommands;

import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.htm.HtmTemplates;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.SubClass;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.utils.Functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
**/
public class AdminSubClass extends ScriptAdminCommand
{
	private static final Logger _log = LoggerFactory.getLogger(AdminSubClass.class);

	public enum Commands
	{
		admin_subclass
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player player)
	{
		Commands c = (Commands) comm;
		switch(c)
		{
			case admin_subclass:
			{
				Player targetPlayer = player;
				GameObject target = player.getTarget();
				if(target != null && target.isPlayer())
					targetPlayer = target.getPlayer();

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("admin/subclasses.htm", player);

				String backBypass = "admin_char_manage";
				StringBuilder content = new StringBuilder();
				if(wordList.length > 1)
				{
					if(wordList[1].equalsIgnoreCase("addlist"))
					{
						try
						{
							backBypass = "admin_subclass addlist";
							content.append(generateAvailableSubClassList(player, targetPlayer, Race.VALUES[Integer.parseInt(wordList[2])], tpls, -1));
						}
						catch(Exception e)
						{
							backBypass = "admin_subclass";
							try
							{
								content.append(generateAvailableSubClassList(player, targetPlayer, null, tpls, -1));
							}
							catch(Exception e2)
							{
								Functions.sendDebugMessage(player, "USAGE: //subclass addlist [RACE ID]");
								return false;
							}
						}
					}
					else if(wordList[1].equalsIgnoreCase("changelist"))
					{
						if(wordList.length > 2)
						{
							try
							{
								backBypass = "admin_subclass changelist " + wordList[2];
								content.append(generateAvailableSubClassList(player, targetPlayer, Race.VALUES[Integer.parseInt(wordList[3])], tpls, Integer.parseInt(wordList[2])));
							}
							catch(Exception e)
							{
								backBypass = "admin_subclass changelist";
								try
								{
									content.append(generateAvailableSubClassList(player, targetPlayer, null, tpls, Integer.parseInt(wordList[2])));
								}
								catch(Exception e2)
								{
									Functions.sendDebugMessage(player, "USAGE: //subclass changelist [CHANGE CLASS ID] [RACE ID]");
									return false;
								}
							}
						}
						else
						{
							backBypass = "admin_subclass";
							StringBuilder subClasses = new StringBuilder();
							for(SubClass sub : targetPlayer.getSubClassList().values())
							{
								String button = tpls.get(6);
								button = button.replace("<?apply_button?>", sub.isActive() ? "" : tpls.get(7)); // Активный класс нельзя применить
								button = button.replace("<?change_button?>", sub.isBase() ? "" : tpls.get(8)); // Основной класс нельзя изменить
								button = button.replace("<?delete_button?>", (sub.isBase() || sub.isActive()) ? "" : tpls.get(9));	// Основной или активный класс нельзя удалить
								button = button.replace("<?class_name?>", ClassId.valueOf(sub.getClassId()).getName(player));
								button = button.replace("<?class_id?>", String.valueOf(sub.getClassId()));
								subClasses.append(button);
							}
							content.append(tpls.get(5).replace("<?buttons?>", subClasses.toString()));
						}
					}
					else if(wordList[1].equalsIgnoreCase("addclass"))
					{
						try
						{
							int id = Integer.parseInt(wordList[2]);
							if(!player.addSubClass(id, true, 0, 0))
							{
								Functions.sendDebugMessage(player, "Could not add a new '" + ClassId.valueOf(id).getName(player) + "' subclass from player '" + targetPlayer.getName() + "'!");
								return false;
							}
							Functions.sendDebugMessage(player, "Success added a new '" + ClassId.valueOf(id).getName(player) + "' subclass from player '" + targetPlayer.getName() + "'!");
							return true;
						}
						catch(Exception e)
						{
							Functions.sendDebugMessage(player, "USAGE: //subclass addclass [CLASS ID]");
							return false;
						}
					}
					else if(wordList[1].equalsIgnoreCase("applyclass"))
					{
						try
						{
							int id = Integer.parseInt(wordList[2]);
							if(!targetPlayer.setActiveSubClass(id, true, false))
							{
								Functions.sendDebugMessage(player, "Could not apply a '" + ClassId.valueOf(id).getName(player) + "' subclass from player '" + targetPlayer.getName() + "'!");
								return false;
							}
							Functions.sendDebugMessage(player, "Success applied a '" + ClassId.valueOf(id).getName(player) + "' subclass from player '" + targetPlayer.getName() + "'!");
							return true;
						}
						catch(Exception e)
						{
							Functions.sendDebugMessage(player, "USAGE: //subclass applyclass [CLASS ID]");
							return false;
						}
					}
					else if(wordList[1].equalsIgnoreCase("changeclass"))
					{
						try
						{
							int oldId = Integer.parseInt(wordList[2]);
							int newId = Integer.parseInt(wordList[3]);
							if(!player.modifySubClass(oldId, newId, true))
							{
								Functions.sendDebugMessage(player, "Could not change a '" + ClassId.valueOf(oldId).getName(player) + "' to '" + ClassId.valueOf(newId).getName(player) + "' subclass from player '" + targetPlayer.getName() + "'! The base class can not be changed.");
								return false;
							}
							Functions.sendDebugMessage(player, "Success changed a '" + ClassId.valueOf(oldId).getName(player) + "' to '" + ClassId.valueOf(newId).getName(player) + "' subclass from player '" + targetPlayer.getName() + "'!");
							return true;
						}
						catch(Exception e)
						{
							Functions.sendDebugMessage(player, "USAGE: //subclass changeclass [OLD CLASS ID] [NEW CLASS ID]");
							return false;
						}
					}
					else if(wordList[1].equalsIgnoreCase("deleteclass"))
					{
						try
						{
							int id = Integer.parseInt(wordList[2]);
							SubClass sub = targetPlayer.getSubClassList().getByClassId(id);
							if(sub == null)
								return false;

							if(sub.isBase() || sub.isActive())
							{
								Functions.sendDebugMessage(player, "You can not delete the active or main class from player '" + targetPlayer.getName() + "'!");
								return false;
							}

							if(!targetPlayer.modifySubClass(id, -1, false))
							{
								Functions.sendDebugMessage(player, "Could not delete a '" + ClassId.valueOf(id).getName(player) + "' subclass from player '" + targetPlayer.getName() + "'!");
								return false;
							}
							Functions.sendDebugMessage(player, "Success deleted a '" + ClassId.valueOf(id).getName(player) + "' subclass from player '" + targetPlayer.getName() + "'!");
							return true;
						}
						catch(Exception e)
						{
							Functions.sendDebugMessage(player, "USAGE: //subclass addclass [CLASS ID]");
							return false;
						}
					}
				}
				else
				{
					content.append(tpls.get(1));
				}

				String html = tpls.get(0);
				html = html.replace("<?back_bypass?>", backBypass);
				html = html.replace("<?target_name?>", targetPlayer.getName());
				html = html.replace("<?target_current_class?>", targetPlayer.getClassId().getName(player));
				html = html.replace("<?content?>", content.toString());
				Functions.show(html, player);
				break;
			}
		}
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private static String generateAvailableSubClassList(Player player, Player target, Race race, HtmTemplates tpls, int classToChange) throws Exception
	{
		StringBuilder subClasses = new StringBuilder();
		if(race == null)
		{
			for(Race r : Race.VALUES)
			{
				boolean haveClasses = false;
				loop: for(ClassId c : ClassId.VALUES)
				{
					if(classToChange >= 0 && c.equalsOrChildOf(ClassId.valueOf(classToChange)))
						continue;

					if(!c.isOfRace(r))
						continue;

					if(!c.isOfLevel(ClassLevel.SECOND))
						continue;

					for(SubClass subClass : target.getSubClassList().values())
					{
						if(ClassId.valueOf(subClass.getClassId()).equalsOrChildOf(c))
							continue loop;
					}

					haveClasses = true;
					break;
				}

				if(haveClasses)
				{
					String button = tpls.get(4);
					button = button.replace("<?button_name?>", r.getName(player));
					if(classToChange < 0)
						button = button.replace("<?bypass?>", "addlist " + r.ordinal());
					else
						button = button.replace("<?bypass?>", "changelist " + classToChange + " " + r.ordinal());
					subClasses.append(button);
				}
			}
		}
		else
		{
			loop: for(ClassId c : ClassId.VALUES)
			{
				if(classToChange >= 0 && c.equalsOrChildOf(ClassId.valueOf(classToChange)))
					continue;

				if(!c.isOfRace(race))
					continue;

				if(!c.isOfLevel(ClassLevel.SECOND))
					continue;

				for(SubClass subClass : target.getSubClassList().values())
				{
					if(ClassId.valueOf(subClass.getClassId()).equalsOrChildOf(c))
						continue loop;
				}

				String button = tpls.get(4);
				button = button.replace("<?button_name?>", c.getName(player));
				if(classToChange < 0)
					button = button.replace("<?bypass?>", "addclass " + c.getId());
				else
					button = button.replace("<?bypass?>", "changeclass " + classToChange + " " + c.getId());
				subClasses.append(button);
			}
		}

		String listTable;
		if(classToChange < 0)
			listTable = tpls.get(2);
		else
		{
			listTable = tpls.get(3);
			listTable = listTable.replace("<?change_class_name?>", ClassId.valueOf(classToChange).getName(player));
		}
		return listTable.replace("<?buttons?>", subClasses.toString());
	}
}
