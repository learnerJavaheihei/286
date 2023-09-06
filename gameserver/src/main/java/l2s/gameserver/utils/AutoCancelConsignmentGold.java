package l2s.gameserver.utils;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.ConsignmentSalesDao;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExNoticePostArrived;
import l2s.gameserver.network.l2.s2c.ExUnReadMailCount;
import l2s.gameserver.skills.TimeStamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class AutoCancelConsignmentGold {
    private static final Logger _log = LoggerFactory.getLogger(AutoCancelConsignmentGold.class);
    private static final AutoCancelConsignmentGold _instance = new AutoCancelConsignmentGold();
    public static AutoCancelConsignmentGold getInstance()
    {
        return _instance;
    }
    public static long outTime = 72 * 60 * 60 * 1000L;
    public void checkConsignmentGold(){
        List<Map<String, Object>> maps = ConsignmentSalesDao.getInstance().selectAllList();
        if (maps!=null && maps.size()>0) {
            for (Map<String, Object> map : maps) {
                Timestamp onsell_date = (Timestamp) map.get("onsell_date");
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                long wouldDown = outTime + onsell_date.getTime()-8 *60*60*1000L;
                if (currentTime.getTime() > wouldDown) {
                    int isDown = ConsignmentSalesDao.getInstance().deleteByOrderNumber((String) map.get("order_number"));
                    if (isDown>0)
                        saveMail(map);
                }else {
                    ThreadPoolManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            int isDown = ConsignmentSalesDao.getInstance().deleteByOrderNumber((String) map.get("order_number"));
                            if (isDown>0)
                                saveMail(map);

                        }
                    },wouldDown-currentTime.getTime());
                }
            }
        }
    }

    private void saveMail(Map<String, Object> map) {
        String _topic = "寄售金币购买结果";
        String _body = "你寄售的金币「"+ map.get("sell_gold")+"」金币已超过3天未有人购买,系统自动下架退回！";
        Mail mail = new Mail();
        mail.setSenderId(1);
        mail.setSenderName("Admin");
        mail.setReceiverId((int) map.get("sell_player_id"));
        mail.setReceiverName((String) map.get("sell_player_name"));
        mail.setTopic(_topic);
        mail.setBody(_body);

        ItemInstance item = ItemFunctions.createItem(MyUtilsFunction._GOLD);
        item.setLocation(ItemInstance.ItemLocation.MAIL);
        item.setCount((long) map.get("sell_gold"));
        item.save();
        mail.addAttachment(item);

        mail.setType(Mail.SenderType.NEWS_INFORMER);
        mail.setUnread(true);
        mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
        mail.save();
        Player sell_player_id = World.getPlayer((int) map.get("sell_player_id"));
        if (sell_player_id!=null) {
            sell_player_id.sendPacket(ExNoticePostArrived.STATIC_TRUE);
            sell_player_id.sendPacket(new ExUnReadMailCount(sell_player_id));
            sell_player_id.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
            ThreadPoolManager.getInstance().execute(new MyUtilsFunction.WriteGoldConsignmentLog("downSuccessLog",map,sell_player_id,null));
        }
    }
}
