package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.RankManager;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.ranking.player.PlayerRankingCategory;
import l2s.gameserver.model.entity.ranking.player.PlayerRankingManager;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SysString;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.templates.item.ItemTemplate;

public class SayPacket2 extends NpcStringContainer
{
	// Flags
	private static final int IS_FRIEND = 1 << 0;
	private static final int IS_CLAN_MEMBER = 1 << 1;
	private static final int IS_MENTEE_OR_MENTOR = 1 << 2;
	private static final int IS_ALLIANCE_MEMBER = 1 << 3;
	private static final int IS_GM = 1 << 4;

	private ChatType _type;
	private SysString _sysString;
	private SystemMsg _systemMsg;

	private int _objectId;
	private String _charName;
	private int _mask;
	private int _charLevel = -1;
	private String _text;
	private int castleId = 0;
	private int _isLocSharing = 0;

	public SayPacket2(int objectId, ChatType type, SysString st, SystemMsg sm)
	{
		super(NpcString.NONE);
		_objectId = objectId;
		_type = type;
		_sysString = st;
		_systemMsg = sm;
	}
	public SayPacket2(int objectId, ChatType type, String charName, String text)
	{
		this(objectId, type, charName, NpcString.NONE, text);
	}
	public SayPacket2(int objectId, ChatType type, String charName, NpcString npcString, String... params)
	{
		super(npcString, params);
		_objectId = objectId;
		_type = type;
		_charName = charName;
		_text = params.length > 0 ? params[0] : null;
	}

	public SayPacket2(int objectId, ChatType type, int isLocSharing, String charName, String text)
	{
		this(objectId, type, isLocSharing, charName, NpcString.NONE, text);
	}

	public SayPacket2(int objectId, ChatType type, int isLocSharing, String charName, NpcString npcString, String... params)
	{
		super(npcString, params);
		_objectId = objectId;
		_type = type;
		_isLocSharing = isLocSharing;
		_charName = charName;
		_text = params.length > 0 ? params[0] : null;

		Clan clan = ClanTable.getInstance().getClanByCharId(objectId);
		if (clan != null) {
			castleId = clan.getCastle();
		}
	}

	public void setCharName(String name)
	{
		_charName = name;
	}

	public void setSenderInfo(Player sender, Player receiver)
	{
		_charLevel = sender.getLevel();

		if(receiver.getFriendList().contains(sender.getObjectId()))
			_mask |= IS_FRIEND;

		if(receiver.getClanId() > 0 && receiver.getClanId() == sender.getClanId())
			_mask |= IS_CLAN_MEMBER;

		if(receiver.getAllyId() > 0 && receiver.getAllyId() == sender.getAllyId())
			_mask |= IS_ALLIANCE_MEMBER;

		// Does not shows level
		if(sender.isGM())
			_mask |= IS_GM;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_type.ordinal());
		switch(_type)
		{
			case SYSTEM_MESSAGE:
				writeD(_sysString.getId());
				writeD(_systemMsg.getId());
				break;
			case TELL:
				writeS(_charName);
				writeElements();
				writeC(_mask);
				if((_mask & IS_GM) == 0)
					writeH(_charLevel);
				break;
			case CLAN:
			case ALLIANCE:
				writeS(_charName);
				writeElements();
				writeC(0x00);	// TODO[UNDERGROUND]: UNK
				break;
			default:
				writeS(_charName);
				writeElements();
				break;
		}

		int rank = PlayerRankingManager.getInstance().getCurrentRank(PlayerRankingCategory.ALL, _objectId);

		writeC(rank); // Char global rank
		writeC(castleId); // Castle ID
		Player player = GameObjectsStorage.getPlayer(_objectId);
		if (_isLocSharing == 1)
		{
			int previousRank = player.getPreviousPvpRank();
			if ((previousRank > 0) &&(previousRank < 4))
			{
				manageTeleport(player, true);
			}
			else
			{
				manageTeleport(player, false);
			}
		}

		if(_text != null)
		{
			if(player != null)
				player.getListeners().onChatMessageReceive(_type, _charName, _text);
		}
	}

	private void manageTeleport(Player player, boolean free)
	{
		if (player.getAntiFlood().canLocationShare() && player.getInventory().destroyItemByItemId(ItemTemplate.ITEM_ID_MONEY_L, Config.SHARE_POSITION_COST))
		{
			int tpId = ServerVariables.getInt("last_tp_id", 0) + 1;
			ServerVariables.set("last_tp_id", tpId);
			ServerVariables.set("tpId_" + tpId + "_name", player.getName());
			ServerVariables.set("tpId_" + tpId + "_x", player.getX());
			ServerVariables.set("tpId_" + tpId + "_y", player.getY());
			ServerVariables.set("tpId_" + tpId + "_z", player.getZ());
			System.out.println("name: " + player.getName() + " tpId: " + tpId + " x: " + player.getX() + " y: " + player.getY() + " z: " + player.getZ());
			writeC(1);
			writeH(tpId);

			if (!free)
			{
				player.sendPacket(SystemMessagePacket.removeItems(ItemTemplate.ITEM_ID_MONEY_L, Config.SHARE_POSITION_COST));
			}
		}
		else
		{
			int tpId = ServerVariables.getInt("last_tp_id", 0);
			writeC(1);
			writeH(tpId);
		}
	}
}