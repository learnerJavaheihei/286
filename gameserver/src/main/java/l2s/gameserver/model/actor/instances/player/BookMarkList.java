package l2s.gameserver.model.actor.instances.player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.utils.ItemFunctions;

import l2s.gameserver.utils.MyUtilsFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookMarkList
{
	private static final int TELEPORT_FLAG_ID = 20033;
	private static final int[] TELEPORT_SCROLLS = { 13016 };

	public static final ZoneType[] FORBIDDEN_ZONES = new ZoneType[] {
			ZoneType.RESIDENCE,
			ZoneType.ssq_zone,
			ZoneType.battle_zone,
			ZoneType.SIEGE,
			ZoneType.no_restart,
			ZoneType.no_summon, };

	private static final Logger _log = LoggerFactory.getLogger(BookMarkList.class);

	private final Player owner;
	private List<BookMark> elementData;
	private int capacity;
	private boolean isSaveDB = true;

	public BookMarkList(Player owner, int acapacity)
	{
		this.owner = owner;
		elementData = new ArrayList<BookMark>(acapacity);
		capacity = acapacity;
	}

	public synchronized void setCapacity(int val)
	{
		capacity = val;
	}

	public int getCapacity()
	{
		return capacity;
	}

	public void clear()
	{
		elementData.clear();
	}

	public BookMark[] toArray()
	{
		return elementData.toArray(new BookMark[elementData.size()]);
	}

	public int incCapacity(int val)
	{
		capacity += val;
		owner.sendPacket(SystemMsg.THE_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_BEEN_INCREASED);
		return getCapacity();
	}

	public synchronized boolean add(BookMark e)
	{
		if(elementData.size() >= getCapacity())
			return false;
		boolean add = elementData.add(e);
		boolean b;
		if (isSaveDB) {
			b = insertBookMark(e);
			return b && add;
		}
		return add;
	}

	public BookMark get(int slot)
	{
		if(slot < 1 || slot > elementData.size())
			return null;
		return elementData.get(slot - 1);
	}

	public void remove(int slot)
	{
		if(slot < 1 || slot > elementData.size())
			return;
		elementData.remove(slot - 1);
		removeBookMarkDB(slot);
	}

	public boolean tryTeleport(int slot)
	{
		if(!checkFirstConditions(owner) || !checkTeleportConditions(owner))
			return false;

		if(slot < 1 || slot > elementData.size())
			return false;

		BookMark bookmark = elementData.get(slot - 1);
		if(!checkTeleportLocation(owner, bookmark.x, bookmark.y, bookmark.z))
		{
			owner.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_IN_THIS_AREA);
			return false;
		}

		owner.bookmarkLocation = new Location(bookmark.x, bookmark.y, bookmark.z);

		if (Config.enablePremiumAccountPeaceZone && !MyUtilsFunction.isPremiumAccountPeaceZone(owner,owner.bookmarkLocation) && owner.getLevel() > 10){
			String msg = "非会员不能传送到非安全区区域！";
			owner.sendMessage(msg);
			return false;
		}
		SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 2588, 1);
		if(!skillEntry.checkCondition(owner, owner, false, true, true))
		{
			owner.bookmarkLocation = null;
			return false;
		}

		for(int item_id : TELEPORT_SCROLLS)
		{
			if(ItemFunctions.deleteItem(owner, item_id, 1))
			{
				owner.getAI().Cast(skillEntry, owner, false, true);
				return true;
			}
		}

		owner.sendPacket(SystemMsg.YOU_CANNOT_BOOKMARK_THIS_LOCATION_BECAUSE_YOU_DO_NOT_HAVE_A_MY_TELEPORT_FLAG);
		return false;
	}

	public boolean add(String aname, String aacronym, int aiconId)
	{
		return add(aname, aacronym, aiconId, true);
	}

	public boolean add(String aname, String aacronym, int aiconId, boolean takeFlag)
	{
		return owner != null && add(owner.getLoc(), aname, aacronym, aiconId, takeFlag);
	}

	public boolean add(Location loc, String aname, String aacronym, int aiconId, boolean takeFlag)
	{
		if(!checkFirstConditions(owner) || !checkTeleportLocation(owner, loc))
			return false;

		if(elementData.size() >= getCapacity())
		{
			owner.sendPacket(SystemMsg.YOU_HAVE_NO_SPACE_TO_SAVE_THE_TELEPORT_LOCATION);
			return false;
		}

		if(takeFlag)
			if(!ItemFunctions.deleteItem(owner, TELEPORT_FLAG_ID, 1))
			{
				owner.sendPacket(SystemMsg.YOU_CANNOT_BOOKMARK_THIS_LOCATION_BECAUSE_YOU_DO_NOT_HAVE_A_MY_TELEPORT_FLAG);
				return false;
			}

		add(new BookMark(loc, aiconId, aname, aacronym));

		return true;
	}

	public void store()
	{
//		Connection con = null;
//		PreparedStatement statement = null;
//		try
//		{
//			con = DatabaseFactory.getInstance().getConnection();
//			statement = con.prepareStatement("DELETE FROM `character_bookmarks` WHERE char_Id=?");
//			statement.setInt(1, owner.getObjectId());
//			statement.execute();
//
//			DbUtils.close(statement);
//			statement = con.prepareStatement("INSERT INTO `character_bookmarks` VALUES(?,?,?,?,?,?,?,?);");
//			int slotId = 0;
//			for(BookMark bookmark : elementData)
//			{
//				statement.setInt(1, owner.getObjectId());
//				statement.setInt(2, ++slotId);
//				statement.setString(3, bookmark.getName());
//				statement.setString(4, bookmark.getAcronym());
//				statement.setInt(5, bookmark.getIcon());
//				statement.setInt(6, bookmark.x);
//				statement.setInt(7, bookmark.y);
//				statement.setInt(8, bookmark.z);
//				statement.execute();
//			}
//		}
//		catch(Exception e)
//		{
//			_log.error("", e);
//		}
//		finally
//		{
//			DbUtils.closeQuietly(con, statement);
//		}
	}
	private void removeBookMarkDB(int slot) {

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			con.setAutoCommit(false);
			statement = con.prepareStatement("DELETE FROM `character_bookmarks` WHERE char_Id=? AND idx =?;");
			statement.setInt(1,owner.getObjectId());
			statement.setInt(2,slot);
			statement.execute();
			// 还要更新 idx
			statement = con.prepareStatement("UPDATE `character_bookmarks` SET idx=? WHERE char_Id=? AND idx = ?;");
			while(slot <= elementData.size()){
				statement.setInt(1,slot);
				statement.setInt(2,owner.getObjectId());
				statement.setInt(3,slot+1);
				statement.addBatch();
				slot++;
			}
			statement.executeBatch();
			con.commit();

		}
		catch(Exception e)
		{
			_log.error("", e);
			try {
				con.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	private boolean insertBookMark(BookMark b) {
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `character_bookmarks` VALUES(?,?,?,?,?,?,?,?);");
			int slotId = 0;
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, elementData.size());
			statement.setString(3, b.getName());
			statement.setString(4, b.getAcronym());
			statement.setInt(5, b.getIcon());
			statement.setInt(6, b.x);
			statement.setInt(7, b.y);
			statement.setInt(8, b.z);
			boolean execute = statement.execute();
			if (execute) {
				return true;
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return false;
	}

	public synchronized void restore()
	{
		if(getCapacity() == 0)
		{
			elementData.clear();
			return;
		}

		Connection con = null;
		Statement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery("SELECT * FROM `character_bookmarks` WHERE `char_Id`=" + owner.getObjectId() + " ORDER BY `idx` LIMIT " + getCapacity());
			elementData.clear();
			isSaveDB = false;
			while(rs.next())
				add(new BookMark(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getInt("icon"), rs.getString("name"), rs.getString("acronym")));
			isSaveDB = true;
		}
		catch(final Exception e)
		{
			_log.error("Could not restore " + owner + " bookmarks!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	public static boolean checkFirstConditions(Player player)
	{
		if(player == null)
			return false;

		if(player.getActiveWeaponFlagAttachment() != null)
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
			return false;
		}
		if(player.isInOlympiadMode())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH);
			return false;
		}
		if(!player.getReflection().isMain())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_IN_AN_INSTANT_ZONE);
			return false;
		}
		if(player.isInDuel())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL);
			return false;
		}
		if(player.isInCombat() || player.getPvpFlag() != 0)
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE);
			return false;
		}
		if(player.isInSiegeZone() || player.isInZoneBattle())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_A_LARGESCALE_BATTLE_SUCH_AS_A_CASTLE_SIEGE_FORTRESS_SIEGE_OR_HIDEOUT_SIEGE);
			return false;
		}
		if(player.isFlying())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING);
			return false;
		}
		if(player.isInWater() || player.isInBoat())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_UNDERWATER);
			return false;
		}

		return true;
	}

	public static boolean checkTeleportConditions(Player player)
	{
		if(player == null)
			return false;

		if(player.isAlikeDead())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_DEAD);
			return false;
		}

		if (Config.enablePremiumAccountPeaceZone && !MyUtilsFunction.isPremiumAccountPeaceZone(player,player.bookmarkLocation) && player.getLevel() > 10){
			String msg = "非会员不能传送到非安全区区域！";
			player.sendMessage(msg);
			return false;
		}

		if(player.isInStoreMode() || player.isInTrade())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_A_PRIVATE_STORE);
			return false;
		}
		if(player.isInBoat() || player.isDecontrolled() || player.isStunned() || player.isSleeping())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_IN_A_PETRIFIED_OR_PARALYZED_STATE);
			return false;
		}

		return true;
	}

	public static boolean checkTeleportLocation(Player player, Location loc)
	{
		return checkTeleportLocation(player, loc.x, loc.y, loc.z);
	}

	public static boolean checkTeleportLocation(Player player, int x, int y, int z)
	{
		if(player == null)
			return false;

		for(ZoneType zoneType : FORBIDDEN_ZONES)
		{
			Zone zone = player.getZone(zoneType);
			if(zone != null)
			{
				player.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
				return false;
			}
		}

		return true;
	}
}