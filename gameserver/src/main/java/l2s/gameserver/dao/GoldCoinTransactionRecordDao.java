package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class GoldCoinTransactionRecordDao {
    private static final Logger _log = LoggerFactory.getLogger(GoldCoinTransactionRecordDao.class);
    private static final GoldCoinTransactionRecordDao _instance = new GoldCoinTransactionRecordDao();
    public static GoldCoinTransactionRecordDao getInstance()
    {
        return _instance;
    }

    public int addRecord(Map<String,Object> map){
        Connection con = null;
        PreparedStatement statement = null;
        boolean execute =false;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("INSERT INTO _gold_coin_transaction_record(transaction_num,sell_player_id,sell_player_name,buy_player_id,buy_player_name,unit_price,member_gold,sell_gold,transaction_date,`state`) VALUES (?,?,?,?,?,?,?,?,?,?)");
            statement.setString(1,(String) map.get("transaction_num"));
            statement.setInt(2,(int) map.get("sell_player_id"));
            statement.setString(3,(String) map.get("sell_player_name"));
            statement.setInt(4,(int) map.get("buy_player_id"));
            statement.setString(5,(String) map.get("buy_player_name"));
            statement.setInt(6,(int) map.get("unit_price"));
            statement.setLong(7,(long) map.get("member_gold"));
            statement.setLong(8,(long) map.get("sell_gold"));
            statement.setTimestamp(9,(Timestamp) map.get("transaction_date"));
            statement.setInt(10,(int)map.get("state"));
            execute = statement.execute();
        }
        catch(final Exception e)
        {
            _log.error("ConsignmentSalesDao:addSale()", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
        return execute ? 1 : 0;
    }
}
