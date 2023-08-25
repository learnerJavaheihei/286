package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.ManufactureItem;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.utils.SqlBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bonux
 */
public class CharacterPrivateStoreDAO {
	private static final CharacterPrivateStoreDAO INSTANCE = new CharacterPrivateStoreDAO();

	public static CharacterPrivateStoreDAO getInstance() {
		return INSTANCE;
	}

	private static final String SELECT_BUYS_QUERY = "SELECT item_id, item_count, owner_price, enchant_level FROM character_private_buys WHERE char_id=? ORDER BY `index`";
	private static final String SELECT_SELLS_QUERY = "SELECT item_object_id, item_count, owner_price FROM character_private_sells WHERE char_id=? AND package=? ORDER BY `index`";
	private static final String SELECT_MANUFACTURES_QUERY = "SELECT recipe_id, cost FROM character_private_manufactures WHERE char_id=? ORDER BY `index`";

	private static final String INSERT_BUYS_QUERY = "REPLACE INTO character_private_buys (char_id, item_id, item_count, owner_price, enchant_level, `index`) VALUES";
	private static final String INSERT_SELLS_QUERY = "REPLACE INTO character_private_sells (char_id, package, item_object_id, item_count, owner_price, `index`) VALUES";
	private static final String INSERT_MANUFACTURES_QUERY = "REPLACE INTO character_private_manufactures (char_id, recipe_id, cost, `index`) VALUES";

	private static final String DELETE_BUYS_QUERY = "DELETE FROM character_private_buys WHERE char_id=?";
	private static final String DELETE_SELLS_QUERY = "DELETE FROM character_private_sells WHERE char_id=? AND package=?";
	private static final String DELETE_MANUFACTURES_QUERY = "DELETE FROM character_private_manufactures WHERE char_id=?";

	private static final Logger LOGGER = LoggerFactory.getLogger(CharacterPrivateStoreDAO.class);

	public List<TradeItem> selectBuys(Player owner) {
		List<TradeItem> result = new ArrayList<>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_BUYS_QUERY);
			statement.setInt(1, owner.getObjectId());
			rset = statement.executeQuery();

			while (rset.next()) {
				int itemId = rset.getInt("item_id");
				long itemCount = rset.getLong("item_count");
				long ownerPrice = rset.getLong("owner_price");
				int enchantLevel = rset.getInt("enchant_level");

				TradeItem tradeItem = new TradeItem();
				tradeItem.setItemId(itemId);
				tradeItem.setCount(itemCount);
				tradeItem.setOwnersPrice(ownerPrice);
				tradeItem.setEnchantLevel(enchantLevel);

				result.add(tradeItem);
			}
		} catch (Exception e) {
			LOGGER.error("CharacterPrivateStoreDAO.selectBuys(Player): " + e, e);
		} finally {
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public Map<Integer, TradeItem> selectSells(Player owner, boolean packageType) {
		Map<Integer, TradeItem> result = new LinkedHashMap<>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SELLS_QUERY);
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, packageType ? 1 : 0);
			rset = statement.executeQuery();

			while (rset.next()) {
				int itemObjectId = rset.getInt("item_object_id");
				long itemCount = rset.getLong("item_count");
				long ownerPrice = rset.getLong("owner_price");

				ItemInstance itemToSell = owner.getInventory().getItemByObjectId(itemObjectId);
				if (itemCount < 1 || itemToSell == null)
					continue;

				if (itemCount > itemToSell.getCount())
					itemCount = itemToSell.getCount();

				TradeItem i = new TradeItem(itemToSell);
				i.setCount(itemCount);
				i.setOwnersPrice(ownerPrice);

				result.put(i.getObjectId(), i);
			}
		} catch (Exception e) {
			LOGGER.error("CharacterPrivateStoreDAO.selectSells(Player,boolean): " + e, e);
		} finally {
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public Map<Integer, ManufactureItem> selectManufactures(Player owner) {
		Map<Integer, ManufactureItem> result = new LinkedHashMap<>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_MANUFACTURES_QUERY);
			statement.setInt(1, owner.getObjectId());
			rset = statement.executeQuery();

			while (rset.next()) {
				int recipeId = rset.getInt("recipe_id");
				long cost = rset.getLong("cost");
				if (!owner.findRecipe(recipeId))
					continue;

				result.put(recipeId, new ManufactureItem(recipeId, cost));
			}
		} catch (Exception e) {
			LOGGER.error("CharacterPrivateStoreDAO.selectManufactures(Player): " + e, e);
		} finally {
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public boolean insertBuys(Player owner, List<TradeItem> buyList) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_BUYS_QUERY);
			statement.setInt(1, owner.getObjectId());
			statement.execute();

			if (buyList.isEmpty())
				return true;

			SqlBatch b = new SqlBatch(INSERT_BUYS_QUERY);
			int i = 1;
			for (TradeItem tradeItem : buyList) {
				StringBuilder sb = new StringBuilder("(");
				sb.append(owner.getObjectId()).append(",");
				sb.append(tradeItem.getItemId()).append(",");
				sb.append(tradeItem.getCount()).append(",");
				sb.append(tradeItem.getOwnersPrice()).append(",");
				sb.append(tradeItem.getEnchantLevel()).append(",");
				sb.append(i).append(")");
				b.write(sb.toString());
				i++;
			}

			if (!b.isEmpty())
				statement.executeUpdate(b.close());
		} catch (Exception e) {
			LOGGER.error("CharacterPrivateStoreDAO.insertBuys(Player,List): " + e, e);
			return false;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean insertSells(Player owner, Map<Integer, TradeItem> sellList, boolean packageType) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_SELLS_QUERY);
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, packageType ? 1 : 0);
			statement.execute();

			if (sellList.isEmpty())
				return true;

			SqlBatch b = new SqlBatch(INSERT_SELLS_QUERY);
			int i = 1;
			for (TradeItem tradeItem : sellList.values()) {
				StringBuilder sb = new StringBuilder("(");
				sb.append(owner.getObjectId()).append(",");
				sb.append(packageType ? 1 : 0).append(",");
				sb.append(tradeItem.getObjectId()).append(",");
				sb.append(tradeItem.getCount()).append(",");
				sb.append(tradeItem.getOwnersPrice()).append(",");
				sb.append(i).append(")");
				b.write(sb.toString());
				i++;
			}

			if (!b.isEmpty())
				statement.executeUpdate(b.close());
		} catch (Exception e) {
			LOGGER.error("CharacterPrivateStoreDAO.insertSells(Player,Map,boolean): " + e, e);
			return false;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean insertManufactures(Player owner, Map<Integer, ManufactureItem> manufactureList) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_MANUFACTURES_QUERY);
			statement.setInt(1, owner.getObjectId());
			statement.execute();

			if (manufactureList.isEmpty())
				return true;

			SqlBatch b = new SqlBatch(INSERT_MANUFACTURES_QUERY);
			int i = 1;
			for (ManufactureItem manufactureItem : manufactureList.values()) {
				StringBuilder sb = new StringBuilder("(");
				sb.append(owner.getObjectId()).append(",");
				sb.append(manufactureItem.getRecipeId()).append(",");
				sb.append(manufactureItem.getCost()).append(",");
				sb.append(i).append(")");
				b.write(sb.toString());
				i++;
			}

			if (!b.isEmpty())
				statement.executeUpdate(b.close());
		} catch (Exception e) {
			LOGGER.error("CharacterPrivateStoreDAO.insertManufactures(Player,Map): " + e, e);
			return false;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean deleteBuys(Player owner) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_BUYS_QUERY);
			statement.setInt(1, owner.getObjectId());
			statement.execute();
		} catch (Exception e) {
			LOGGER.error("CharacterPrivateStoreDAO.deleteBuys(Player): " + e, e);
			return false;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean deleteSells(Player owner, boolean packageType) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_SELLS_QUERY);
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, packageType ? 1 : 0);
			statement.execute();
		} catch (Exception e) {
			LOGGER.error("CharacterPrivateStoreDAO.deleteSells(Player,boolean): " + e, e);
			return false;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean deleteManufactures(Player owner) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_MANUFACTURES_QUERY);
			statement.setInt(1, owner.getObjectId());
			statement.execute();
		} catch (Exception e) {
			LOGGER.error("CharacterPrivateStoreDAO.deleteManufactures(Player): " + e, e);
			return false;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
}
