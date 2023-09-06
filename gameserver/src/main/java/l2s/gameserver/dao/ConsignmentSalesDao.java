package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import org.napile.primitive.maps.IntIntMap;
import org.napile.primitive.maps.impl.HashIntIntMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ConsignmentSalesDao {
    private static final Logger _log = LoggerFactory.getLogger(ConsignmentSalesDao.class);
    private static final ConsignmentSalesDao _instance = new ConsignmentSalesDao();
    public static List<Map<String, Object>> maps = new CopyOnWriteArrayList<>();
    public static ConsignmentSalesDao getInstance()
    {
        return _instance;
    }

    public List<Map<String,Object>> selectAllList()
    {
        Connection con = null;
        PreparedStatement statement = null;
        CopyOnWriteArrayList<Map<String, Object>> list = null;
        ResultSet resultSet =null;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM _consignment_sales");
            resultSet = statement.executeQuery();
            list = new CopyOnWriteArrayList<>();
            while (resultSet.next()) {
                String order_number = resultSet.getString(1);
                int sell_player_id = resultSet.getInt(2);
                String sell_player_name = resultSet.getString(3);
                int unit_price = resultSet.getInt(4);
                long member_gold = resultSet.getLong(5);
                long sell_gold = resultSet.getLong(6);
                Timestamp onsell_date = resultSet.getTimestamp(7);
                HashMap<String, Object> map = new HashMap<>();
                map.put("order_number", order_number);
                map.put("sell_player_name", sell_player_name);
                map.put("sell_player_id",sell_player_id);
                map.put("unit_price", unit_price);
                map.put("member_gold",member_gold);
                map.put("sell_gold",sell_gold);
                map.put("onsell_date",onsell_date);
                list.add(map);
            }
            maps = list;
        }
        catch(final Exception e)
        {
            _log.error("ConsignmentSalesDao:selectAllList()", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
        return maps;
    }
    public List<Map<String,Object>> selectByPlayerId(int player_id)
    {
        Connection con = null;
        PreparedStatement statement = null;
        CopyOnWriteArrayList<Map<String, Object>> list = null;
        ResultSet resultSet =null;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM _consignment_sales WHERE sell_player_id=?");
            statement.setInt(1,player_id);
            resultSet = statement.executeQuery();
            list = new CopyOnWriteArrayList<>();
            while (resultSet.next()) {
                String order_number = resultSet.getString(1);
                int sell_player_id = resultSet.getInt(2);
                String sell_player_name = resultSet.getString(3);
                int unit_price = resultSet.getInt(4);
                long member_gold = resultSet.getLong(5);
                long sell_gold = resultSet.getLong(6);
                Timestamp onsell_date = resultSet.getTimestamp(7);
                HashMap<String, Object> map = new HashMap<>();
                map.put("order_number", order_number);
                map.put("sell_player_name", sell_player_name);
                map.put("sell_player_id",sell_player_id);
                map.put("unit_price", unit_price);
                map.put("member_gold",member_gold);
                map.put("sell_gold",sell_gold);
                map.put("onsell_date",onsell_date);
                list.add(map);
            }
        }
        catch(final Exception e)
        {
            _log.error("ConsignmentSalesDao:selectByPlayerId()", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
        return list;
    }
    public Map<String,Object> selectByOrderNum(String orderNumber)
    {
        Connection con = null;
        PreparedStatement statement = null;
        Map<String,Object> map = null;
        ResultSet resultSet =null;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM _consignment_sales WHERE order_number=?");
            statement.setString(1,orderNumber);
            resultSet = statement.executeQuery();
            map = new HashMap<String,Object>();
            if (resultSet.next()) {
                String order_number = resultSet.getString(1);
                int sell_player_id = resultSet.getInt(2);
                String sell_player_name = resultSet.getString(3);
                int unit_price = resultSet.getInt(4);
                long member_gold = resultSet.getLong(5);
                long sell_gold = resultSet.getLong(6);
                Timestamp onsell_date = resultSet.getTimestamp(7);
                map = new HashMap<>();
                map.put("order_number", order_number);
                map.put("sell_player_name", sell_player_name);
                map.put("sell_player_id",sell_player_id);
                map.put("unit_price", unit_price);
                map.put("member_gold",member_gold);
                map.put("sell_gold",sell_gold);
                map.put("onsell_date",onsell_date);

            }
        }
        catch(final Exception e)
        {
            _log.error("ConsignmentSalesDao:selectByPlayerId()", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
        return map;
    }

    public int deleteByOrderNumber(String order_number){
        Connection con = null;
        PreparedStatement statement = null;
        int execute =0;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM _consignment_sales WHERE order_number=?");
            statement.setString(1,order_number);
            execute = statement.executeUpdate();
            maps = maps.stream().filter(map -> !map.get("order_number").equals(order_number))
                    .collect(Collectors.toList());
        }
        catch(final Exception e)
        {
            _log.error("ConsignmentSalesDao:deleteByOrderNumber()", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
        return execute;
    }

    public int addSale(Map<String,Object> map){
        Connection con = null;
        PreparedStatement statement = null;
        int execute =0;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
            statement = con.prepareStatement("INSERT INTO _consignment_sales(order_number,sell_player_id,sell_player_name,unit_price,member_gold,sell_gold,onsell_date) VALUES (?,?,?,?,?,?,?)");
            statement.setString(1,(String) map.get("order_number"));
            statement.setInt(2,(int) map.get("sell_player_id"));
            statement.setString(3,(String) map.get("sell_player_name"));
            statement.setInt(4,(int) map.get("unit_price"));
            statement.setLong(5,(long) map.get("member_gold"));
            statement.setLong(6,(long) map.get("sell_gold"));
            statement.setTimestamp(7,(Timestamp) map.get("onsell_date"));
            execute = statement.executeUpdate();
            maps.add(map);
        }
        catch(final Exception e)
        {
            _log.error("ConsignmentSalesDao:addSale()", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
        return execute;
    }

}
