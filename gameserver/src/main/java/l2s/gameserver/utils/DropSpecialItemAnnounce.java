package l2s.gameserver.utils;

import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DropSpecialItemAnnounce {
    public static List<Integer> dropSpecialItems= new ArrayList<>();
    public static String[] wordsSimple= new String[]{
            "珍稀装备出世，你的好运来了！",
            "击杀BOSS，收获无尽财富！",
            "装备爆棚，实力飙升！快来捡战利品！",
            "突破界限，提升实力，这些装备你值得拥有！",
            "闪耀的装备掉落，你的胜利奖赏！",
            "打败敌人，收获他们的装备，让你如虎添翼！",
            "高级装备入手，离最强玩家更近一步！",
            "击杀精英怪，获得梦寐以求的稀有装备！",
            "装备掉落，你的好运正在向你招手！",
            "战斗的奖赏已到，快来捡取你的战利品！",
            "你的运气爆表，新的装备已经出现！",
            "打败对手，解锁隐藏的装备！",
            "稀有装备掉落，你的实力将更上一层楼！",
            "你的战斗成果丰硕，快来领取奖励！",
            "高级装备随机掉落，你的好运就在眼前！",
            "打败强大的BOSS，获得它的珍稀装备！",
            "装备升级，你的战斗力将直线上升！",
            "新的传说装备已经出现，你准备好了吗？",
            "你的努力得到了回报，稀有装备已经入手！",
            "战斗的胜利已经属于你，新的装备正在召唤你！",
            "你的勇武终于得到回报，新的装备在等待你！",
            "稀有装备等待着你的挑战，你准备好了吗？",
            "你的实力得到了最好的证明，新的装备已经到手！",
            "战斗的胜利是你的，新的装备让你的实力暴涨！",
            "你的幸运数字是今天，新的装备已经出现！",
            "打败那些强大的敌人，新的宝藏和装备在等你！",
            "你的勇猛无人能敌，新的装备将助你更上一层楼！",
            "你的技巧和实力得到了认可，新的装备已经加入你的背包！",
            "你的战斗成果丰硕，新的装备让你的实力暴涨！"
    };
    private DropSpecialItemAnnounce(){
        for (int itemId : itemIds) {
            dropSpecialItems.add(itemId);
        }
    }
    private int[] itemIds = new int[]{
        57,1864
    };
    private static final DropSpecialItemAnnounce _instance = new DropSpecialItemAnnounce();

    public static DropSpecialItemAnnounce getInstance()
    {
        return _instance;
    }
    public static void announcements(String text) {
        Collection<Player> players = GameObjectsStorage.getPlayers(false, false);
        Announcements instance = Announcements.getInstance();
        Announcements.Announce announce = instance.new Announce(0, text);
        for (Player player : players) {
            announce.showAnnounce(player);
        }
    }
    public String getWord(){
        String[] words = wordsSimple;
        int roll = Rnd.get(words.length);
        return words[roll];
    }

}


