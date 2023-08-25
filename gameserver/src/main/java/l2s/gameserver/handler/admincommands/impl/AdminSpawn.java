package l2s.gameserver.handler.admincommands.impl;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.dao.SpawnsDAO;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.geometry.Territory;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.spawn.SpawnRange;
import l2s.gameserver.utils.NpcUtils;

public class AdminSpawn implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_show_spawns,
		admin_print_spawnzone,
		admin_spawn,
		admin_spawn_monster,
		admin_spawn_index,
		admin_spawn1,
		admin_spawn2,
		admin_setheading,
		admin_setai,
		admin_setaiparam,
		admin_dumpparams,
		admin_generate_loc,
		admin_dumpspawn
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanEditNPC)
			return false;
		StringTokenizer st;
		GameObject target;
		NpcInstance npcTarget;
		Spawner spawn;
		NpcInstance npc;

		switch(command)
		{
			case admin_show_spawns:
				activeChar.sendPacket(new HtmlMessage(5).setFile("admin/spawns.htm"));
				break;
			case admin_print_spawnzone:
			{
				target = activeChar.getTarget();
				if(target == null || !target.isNpc())
				{
					activeChar.sendMessage("Please select target NPC or mob.");
					return false;
				}

				npcTarget = (NpcInstance) target;
				SpawnRange spawnRange = npcTarget.getSpawnRange();
				if(spawnRange == null || !(spawnRange instanceof Territory))
				{
					activeChar.sendMessage("Selected NPC dont have spawn territory.");
					return false;
				}

				((Territory) spawnRange).printToWorld(activeChar);
				activeChar.sendMessage("NPC ID[" + npcTarget.getNpcId() + "] territory success printed to world!");
				return true;
			}
			case admin_spawn_index:
				try
				{
					String val = fullString.substring(18);
					activeChar.sendPacket(new HtmlMessage(5).setFile("admin/spawns/" + val + ".htm"));
				}
				catch(StringIndexOutOfBoundsException e)
				{}
				break;
			case admin_spawn1:
				st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					String id = st.nextToken();
					int mobCount = 1;
					if(st.hasMoreTokens())
						mobCount = Integer.parseInt(st.nextToken());
					spawnMonster(activeChar, id, 0, mobCount, false);
				}
				catch(Exception e)
				{
					// Case of wrong monster data
				}
				break;
			case admin_spawn2: // Игнорирует запрет на спавн рейдбоссов
				st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					String id = st.nextToken();
					int respawnTime = 30;
					int mobCount = 1;
					if(st.hasMoreTokens())
						mobCount = Integer.parseInt(st.nextToken());
					if(st.hasMoreTokens())
						respawnTime = Integer.parseInt(st.nextToken());
					spawnMonster(activeChar, id, respawnTime, mobCount, true);
				}
				catch(Exception e)
				{}
				break;
			case admin_spawn:
			case admin_spawn_monster:
				st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					String id = st.nextToken();
					int respawnTime = 30;
					int mobCount = 1;
					if(st.hasMoreTokens())
						mobCount = Integer.parseInt(st.nextToken());
					if(st.hasMoreTokens())
						respawnTime = Integer.parseInt(st.nextToken());
					spawnMonster(activeChar, id, respawnTime, mobCount, false);
				}
				catch(Exception e)
				{
					// Case of wrong monster data
				}
				break;
			case admin_setai:
				if(activeChar.getTarget() == null || !activeChar.getTarget().isNpc())
				{
					activeChar.sendMessage("Please select target NPC or mob.");
					return false;
				}

				st = new StringTokenizer(fullString, " ");
				st.nextToken();
				if(!st.hasMoreTokens())
				{
					activeChar.sendMessage("Please specify AI name.");
					return false;
				}
				String aiName = st.nextToken();
				npcTarget = (NpcInstance) activeChar.getTarget();

				Constructor<?> aiConstructor = null;
				try
				{
					if(!aiName.equalsIgnoreCase("npc"))
						aiConstructor = Class.forName("l2s.gameserver.ai." + aiName).getConstructors()[0];
				}
				catch(Exception e)
				{
					try
					{
						aiConstructor = Scripts.getInstance().getClasses().get("ai." + aiName).getConstructors()[0];
					}
					catch(Exception e1)
					{
						activeChar.sendMessage("This type AI not found.");
						return false;
					}
				}

				if(aiConstructor != null)
				{
					try
					{
						npcTarget.setAI((CharacterAI) aiConstructor.newInstance(new Object[] { npcTarget }));
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					npcTarget.getAI().startAITask();
				}
				break;
			case admin_setaiparam:
				if(activeChar.getTarget() == null || !activeChar.getTarget().isNpc())
				{
					activeChar.sendMessage("Please select target NPC or mob.");
					return false;
				}

				st = new StringTokenizer(fullString, " ");
				st.nextToken();

				if(!st.hasMoreTokens())
				{
					activeChar.sendMessage("Please specify AI parameter name.");
					activeChar.sendMessage("USAGE: //setaiparam <param> <value>");
					return false;
				}

				String paramName = st.nextToken();
				if(!st.hasMoreTokens())
				{
					activeChar.sendMessage("Please specify AI parameter value.");
					activeChar.sendMessage("USAGE: //setaiparam <param> <value>");
					return false;
				}
				String paramValue = st.nextToken();
				npcTarget = (NpcInstance) activeChar.getTarget();
				npcTarget.setParameter(paramName, paramValue);
				npcTarget.decayMe();
				npcTarget.spawnMe();
				activeChar.sendMessage("AI parameter " + paramName + " succesfully setted to " + paramValue);
				break;
			case admin_dumpparams:
				if(activeChar.getTarget() == null || !activeChar.getTarget().isNpc())
				{
					activeChar.sendMessage("Please select target NPC or mob.");
					return false;
				}
				npcTarget = (NpcInstance) activeChar.getTarget();
				MultiValueSet<String> set = npcTarget.getParameters();
				if(!set.isEmpty())
					System.out.println("Dump of Parameters:\r\n" + set.toString());
				else
					System.out.println("Parameters is empty.");
				break;
			case admin_setheading:
				GameObject obj = activeChar.getTarget();
				if(!obj.isNpc())
				{
					activeChar.sendMessage("Target is incorrect!");
					return false;
				}

				npc = (NpcInstance) obj;
				npc.setHeading(activeChar.getHeading());
				npc.decayMe();
				npc.spawnMe();
				activeChar.sendMessage("New heading : " + activeChar.getHeading());

				spawn = npc.getSpawn();
				if(spawn == null)
				{
					activeChar.sendMessage("Spawn for this npc == null!");
					return false;
				}
				break;
			case admin_generate_loc:
				if(wordList.length < 2)
				{
					activeChar.sendMessage("Incorrect argument count!");
					return false;
				}

				int id = Integer.parseInt(wordList[1]);
				int id2 = 0;
				if(wordList.length > 2)
					id2 = Integer.parseInt(wordList[2]);

				int min_x = Integer.MIN_VALUE;
				int min_y = Integer.MIN_VALUE;
				int min_z = Integer.MIN_VALUE;
				int max_x = Integer.MAX_VALUE;
				int max_y = Integer.MAX_VALUE;
				int max_z = Integer.MAX_VALUE;

				String name = "";

				for(NpcInstance _npc : World.getAroundNpc(activeChar))
					if(_npc.getNpcId() == id || _npc.getNpcId() == id2)
					{
						name = _npc.getName();
						min_x = Math.min(min_x, _npc.getX());
						min_y = Math.min(min_y, _npc.getY());
						min_z = Math.min(min_z, _npc.getZ());
						max_x = Math.max(max_x, _npc.getX());
						max_y = Math.max(max_y, _npc.getY());
						max_z = Math.max(max_z, _npc.getZ());
					}

				min_x -= 500;
				min_y -= 500;
				max_x += 500;
				max_y += 500;

				System.out.println("(0,'" + name + "'," + min_x + "," + min_y + "," + min_z + "," + max_z + ",0),");
				System.out.println("(0,'" + name + "'," + min_x + "," + max_y + "," + min_z + "," + max_z + ",0),");
				System.out.println("(0,'" + name + "'," + max_x + "," + max_y + "," + min_z + "," + max_z + ",0),");
				System.out.println("(0,'" + name + "'," + max_x + "," + min_y + "," + min_z + "," + max_z + ",0),");

				System.out.println("delete from spawnlist where npc_templateid in (" + id + ", " + id2 + ")" + //
				" and locx <= " + min_x + //
				" and locy <= " + min_y + //
				" and locz <= " + min_z + //
				" and locx >= " + max_x + //
				" and locy >= " + max_y + //
				" and locz >= " + max_z + //
				";");
				break;
			case admin_dumpspawn:
				st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					String id3 = st.nextToken();
					int respawnTime = 30;
					int mobCount = 1;
					spawnMonster(activeChar, id3, respawnTime, mobCount, false);
					try
					{
						new File("dumps").mkdir();
						File f = new File("dumps/spawndump.txt");
						if(!f.exists())
							f.createNewFile();
						FileWriter writer = new FileWriter(f, true);
						writer.write("<spawn count=\"1\" respawn=\"60\" respawn_random=\"0\" period_of_day=\"none\">\n\t" + "<point x=\"" + activeChar.getLoc().x + "\" y=\"" + activeChar.getLoc().y + "\" z=\"" + activeChar.getLoc().z + "\" h=\"" + activeChar.getLoc().h + "\" />\n\t" + "<npc id=\"" + Integer.parseInt(id3) + "\" /><!--" + NpcHolder.getInstance().getTemplate(Integer.parseInt(id3)).getName() + "-->\n" + "</spawn>\n");
						writer.close();
					}
					catch(Exception e)
					{

					}
				}
				catch(Exception e)
				{
					// Case of wrong monster data
				}
				break;
		}
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void spawnMonster(Player activeChar, String monsterId, int respawnTime, int mobCount, boolean ignoreSpawnedRB)
	{
		GameObject target = activeChar.getTarget();
		if(target == null)
			target = activeChar;

		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher regexp = pattern.matcher(monsterId);
		NpcTemplate template;
		if(regexp.matches())
		{
			// First parameter was an ID number
			int monsterTemplate = Integer.parseInt(monsterId);
			if(NpcHolder.getInstance().getTemplate(monsterTemplate) == null)
			{
				activeChar.sendMessage("Incorrect monster template.");
				return;
			}			
			template = NpcHolder.getInstance().getTemplate(monsterTemplate);
		}
		else
		{
			// First parameter wasn't just numbers so go by name not ID
			monsterId = monsterId.replace('_', ' ');
			template = NpcHolder.getInstance().getTemplateByName(monsterId);
		}

		if(template == null)
		{
			activeChar.sendMessage("Incorrect monster template.");
			return;
		}

		if(!ignoreSpawnedRB && RaidBossSpawnManager.getInstance().isDefined(template.getId()))
		{
			activeChar.sendMessage("Raid Boss " + template.name + " already spawned.");
			return;
		}

		int heading = activeChar.getHeading();
		Location loc = target.getLoc();
		try
		{
			NpcUtils.spawnSimple(template.getId(), loc.setH(heading), activeChar.getReflection(), mobCount, respawnTime, 0);
			activeChar.sendMessage("Created " + template.name + " on " + target.getObjectId() + ".");
		}
		catch(Exception e)
		{
			activeChar.sendMessage("Target is not ingame.");
			return;
		}

		SpawnsDAO.getInstance().insert(template.getId(), loc.x, loc.y, loc.z, heading, respawnTime, mobCount);
	}
}