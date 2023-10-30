package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AutoEnableGameProgressDao {
    private static final Logger _log = LoggerFactory.getLogger(AutoEnableGameProgressDao.class);
    private static final AutoEnableGameProgressDao _instance = new AutoEnableGameProgressDao();

    public static AutoEnableGameProgressDao getInstance()
    {
        return _instance;
    }
    String REPLACE_SQL = "REPLACE INTO autoenable_game_progress(progress_limit, progress_schedule, current_percent, update_time, update_percent_onTime, current_progress_endTime) VALUES (?,?,?,?,?,?)";
    String UPDATE_SQL = "UPDATE autoenable_game_progress SET progress_limit=?, progress_schedule=?, current_percent=?, update_time=?, update_percent_onTime=?, current_progress_endTime=? WHERE id=?";

    public void update(Map<String, Object> map){
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(UPDATE_SQL);
            statement.setInt(1,(int)map.get("progress_limit"));
            statement.setString(2,(String) map.get("progress_schedule"));
            statement.setString(3,(String) map.get("current_percent"));
            statement.setTimestamp(4,(Timestamp) map.get("update_time"));
            statement.setString(5,(String) map.get("update_percent_onTime"));
            statement.setTimestamp(6,(Timestamp) map.get("current_progress_endTime"));
            statement.setInt(7,1);
            int i = statement.executeUpdate();
            if (i<=0) {
                statement = con.prepareStatement(REPLACE_SQL);
                statement.setInt(1,(int)map.get("progress_limit"));
                statement.setString(2,(String) map.get("progress_schedule"));
                statement.setString(3,(String) map.get("current_percent"));
                statement.setTimestamp(4,(Timestamp) map.get("update_time"));
                statement.setString(5,(String) map.get("update_percent_onTime"));
                statement.setTimestamp(6,(Timestamp) map.get("current_progress_endTime"));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            _log.error("AutoEnableGameProgressDao::update"+e);
        }finally {
            DbUtils.closeQuietly(con, statement);
        }
    }
    public Map<String, Object> select(){
        Connection con = null;
        PreparedStatement statement = null;
        Map<String, Object> map =null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT progress_limit, progress_schedule, current_percent, update_time, update_percent_onTime, current_progress_endTime FROM autoenable_game_progress");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                map = new HashMap<>();
                map.put("progress_limit",resultSet.getInt("progress_limit"));
                map.put("progress_schedule",resultSet.getString("progress_schedule"));
                map.put("current_percent",resultSet.getString("current_percent"));
                map.put("update_time",resultSet.getTimestamp("update_time"));
                map.put("update_percent_onTime",resultSet.getString("update_percent_onTime"));
                map.put("current_progress_endTime",resultSet.getTimestamp("current_progress_endTime"));
            }
        } catch (SQLException e) {
            _log.error("AutoEnableGameProgressDao::select"+e);
        }finally {
            DbUtils.closeQuietly(con, statement);
        }
        return map;
    }
}
