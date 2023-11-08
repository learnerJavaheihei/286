package l2s.gameserver.utils.CompensationSystem;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NewServerCompensationDao {
    private static NewServerCompensationDao _instance = new NewServerCompensationDao();
    public static NewServerCompensationDao getInstance(){
        return _instance;
    }
    private static Logger _log = LoggerFactory.getLogger(NewServerCompensationDao.class);
    public List<NewServerCompensationEntry> selectCompensation(){
        ArrayList<NewServerCompensationEntry> compensationEntries = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            statement = conn.prepareStatement("SELECT * FROM _newserver_compensation");
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                NewServerCompensationEntry entry = new NewServerCompensationEntry();
                entry.setAccount(resultSet.getString("account"));
                entry.setRoleName(resultSet.getString("role_name"));
                entry.setFirstGiveCoin(resultSet.getInt("first_give_coin"));
                entry.setFirstDate(resultSet.getLong("first_date"));
                entry.setSecondGiveCoin(resultSet.getInt("second_give_coin"));
                entry.setSecondDate(resultSet.getLong("second_date"));
                entry.setThirdGiveCoin(resultSet.getInt("third_give_coin"));
                entry.setThirdDate(resultSet.getLong("third_date"));
                entry.setFourthGiveCoin(resultSet.getInt("fourth_give_coin"));
                entry.setFourthDate(resultSet.getLong("fourth_date"));
                entry.setFifthGiveCoin(resultSet.getInt("fifth_give_coin"));
                entry.setFifthDate(resultSet.getLong("fifth_date"));
                entry.setTotalVipCoin(resultSet.getInt("total_vip_coin"));
                entry.setRemain_coin(resultSet.getInt("remain_coin"));
                compensationEntries.add(entry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            _log.error("run NewServerCompensationDao:selectCompensation error: " + e.getMessage());
        }
        finally {
            try {
                DbUtils.close(conn);
                DbUtils.close(statement,resultSet);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return compensationEntries;
    }
    public void updateNewServerCompensation(String accountName, String name, int count, String compensationUnit){
        List<NewServerCompensationEntry> filterList = NewServerCompensationServiceImpl.filterList;
        NewServerCompensationEntry CompensationEntry = null;
        for (NewServerCompensationEntry newServerCompensationEntry : filterList) {
            if (newServerCompensationEntry.getAccount().equals(accountName)) {
                CompensationEntry = newServerCompensationEntry;
            }
        }

        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            statement = conn.prepareStatement("UPDATE _newserver_compensation SET role_name=?,"+compensationUnit+"=?,remain_coin=? WHERE account=?");
            if (CompensationEntry==null) {
                statement.setString(1,"");
            }
            else {
                statement.setString(1,CompensationEntry.getRoleName()==null?"":CompensationEntry.getRoleName()+";"+name);
            }

            statement.setLong(2,System.currentTimeMillis());
            if (CompensationEntry!=null)
                statement.setLong(3,Math.max(CompensationEntry.getRemain_coin()-count,0));
            else
                statement.setLong(3,0);
            statement.setString(4,accountName);
            statement.executeUpdate();

        }catch (SQLException e) {
            e.printStackTrace();
            _log.error("run NewServerCompensationDao:updateNewServerCompensation error: " + e.getMessage());
        }
        finally {
            try {
                DbUtils.close(conn);
                DbUtils.close(statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
