package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

public class AutoEnableGameProgressRecordDao {
    private static final Logger _log = LoggerFactory.getLogger(AutoEnableGameProgressRecordDao.class);
    private static final AutoEnableGameProgressRecordDao _instance = new AutoEnableGameProgressRecordDao();

    public static AutoEnableGameProgressRecordDao getInstance()
    {
        return _instance;
    }

    public void insert(Map<String, Object> map){
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("insert into autoenablegameprogress_record(progressLimit,last_percent,current_percent,sliding_percent,update_time) VALUES (?,?,?,?,?)");
            statement.setInt(1,(int)map.get("progressLimit"));
            statement.setDouble(2,(double) map.get("last_percent"));
            statement.setDouble(3,(double) map.get("current_percent"));
            statement.setDouble(4,(double) map.get("sliding_percent"));
            statement.setTimestamp(5,(Timestamp) map.get("update_time"));
            statement.execute();
        } catch (SQLException e) {
            _log.error("AutoEnableGameProgressRecordDao::insert"+e);
        }finally {
            DbUtils.closeQuietly(con, statement);
        }
    }
}
