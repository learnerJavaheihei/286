package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.NSPacket;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ChatUtils;
import l2s.gameserver.utils.ItemFunctions;
import manager.FourSepulchersManager;
import manager.FourSepulchersSpawn;

import java.util.concurrent.Future;

// TODO: Жертва должна бегать за открывателем сундука и просить о помощи.
public class SepulcherNpcInstance extends NpcInstance
{
	protected Future<?> _closeTask = null, _spawnMonsterTask = null;

	protected final static String HTML_FILE_PATH = "four_sepulchers/";

	public SepulcherNpcInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	protected void onDelete()
	{
		if(_closeTask != null)
		{
			_closeTask.cancel(false);
			_closeTask = null;
		}
		if(_spawnMonsterTask != null)
		{
			_spawnMonsterTask.cancel(false);
			_spawnMonsterTask = null;
		}
		super.onDelete();
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		if(isDead())
		{
			player.sendActionFailed();
			return;
		}

		if(getNpcId() >= 31468 && getNpcId() <= 31487) {
			doDie(player);
			if(_spawnMonsterTask != null)
				_spawnMonsterTask.cancel(false);
			_spawnMonsterTask = ThreadPoolManager.getInstance().schedule(new SpawnMonster(getNpcId()), 3500);
			return;
		} else if(getNpcId() >= 31455 && getNpcId() <= 31467) {
			Party party = player.getParty();
			if (!hasPartyAKey(player) && (party != null && party.isLeader(player) || player.isGM())) {
				ItemFunctions.addItem(player, FourSepulchersManager.CHAPEL_KEY, 1/*, "Give items on talk with npc SepulcherNpcInstance"*/);
				doDie(player);
			}
			return;
		}

		super.showChatWindow(player, val, firstTalk, replace);
	}

	@Override
	public String getHtmlDir(String filename, Player player) {
		return HTML_FILE_PATH;
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(command.equalsIgnoreCase("open_gate"))
		{
			ItemInstance hallsKey = player.getInventory().getItemByItemId(FourSepulchersManager.CHAPEL_KEY);
			if(hallsKey == null)
				showChatWindow(player, HTML_FILE_PATH + "Gatekeeper-no.htm", false);
			else if(FourSepulchersManager.isAttackTime())
			{
				switch(getNpcId())
				{
					case 31929:
					case 31934:
					case 31939:
					case 31944:
						if(!FourSepulchersSpawn.isShadowAlive(getNpcId()))
							FourSepulchersSpawn.spawnShadow(getNpcId());
				}

				// Moved here from switch-default
				openNextDoor();

				Party party = player.getParty();
				if(party != null) {
					for (Player mem : party.getPartyMembers()) {
						hallsKey = mem.getInventory().getItemByItemId(FourSepulchersManager.CHAPEL_KEY);
						if (hallsKey != null)
							ItemFunctions.deleteItem(mem, FourSepulchersManager.CHAPEL_KEY, hallsKey.getCount());
					}
				}
				else if (hallsKey != null)
					ItemFunctions.deleteItem(player, FourSepulchersManager.CHAPEL_KEY, hallsKey.getCount());
			}
		} else if (command.equalsIgnoreCase("exit")) {
			FourSepulchersManager.exitPlayer(player);
		}
		else
			super.onBypassFeedback(player, command);
	}

	private void openNextDoor()
	{
		int doorId = getParameter("door_id", 0);
		if(doorId > 0) {
			DoorInstance door = ReflectionManager.MAIN.getDoor(doorId);
			if(door != null) {
				door.openMe();

				if(_closeTask != null)
					_closeTask.cancel(false);
				_closeTask = ThreadPoolManager.getInstance().schedule(new CloseNextDoor(door), 10000);
			}
		}
	}

	private class CloseNextDoor implements Runnable
	{
		private final DoorInstance door;

		private int state = 0;

		public CloseNextDoor(DoorInstance door)
		{
			this.door = door;
		}

		@Override
		public void run()
		{
			if(state == 0)
			{
				door.closeMe();
				state++;
				_closeTask = ThreadPoolManager.getInstance().schedule(this, 10000);
			}
			else if(state == 1)
			{
				FourSepulchersSpawn.spawnMysteriousBox(getNpcId());
				_closeTask = null;
			}
		}
	}

	private class SpawnMonster implements Runnable
	{
		private final int _NpcId;

		public SpawnMonster(int npcId)
		{
			_NpcId = npcId;
		}

		@Override
		public void run()
		{
			FourSepulchersSpawn.spawnMonster(_NpcId);
		}
	}

	public void sayInShout(NpcString npcString, String... params)
	{
		if(npcString == null)
			return; //wrong usage

		ChatUtils.say(this, 15000, new NSPacket(this, ChatType.SHOUT, npcString, params));
	}

	private boolean hasPartyAKey(Player player)
	{
		Party party = player.getParty();
		if(party != null) {
			for (Player m : party.getPartyMembers())
				if (ItemFunctions.getItemCount(m, FourSepulchersManager.CHAPEL_KEY) > 0)
					return true;
		} else if(player.isGM()) {
			if (ItemFunctions.getItemCount(player, FourSepulchersManager.CHAPEL_KEY) > 0)
				return true;
		}
		return false;
	}
}