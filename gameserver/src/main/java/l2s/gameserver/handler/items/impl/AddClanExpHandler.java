package l2s.gameserver.handler.items.impl;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.PledgeReceiveWarList;
import l2s.gameserver.network.l2.s2c.pledge.ExPledgeEnemyInfoList;
import l2s.gameserver.network.l2.s2c.pledge.ExPledgeV3Info;
import l2s.gameserver.skills.AbnormalEffect;

public class AddClanExpHandler extends DefaultItemHandler{
    public static final int CLAN_EXPERIENCE = 50;
    public static final int CLAN_EXPERIENCE_ITEM_ID = 94481;
    
    public Player _player;
    @Override
    public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
    {
        if (!playable.isPlayer())
            return false;
        _player = playable.getPlayer();
        if(_player == null)
            return false;
        Clan clan = _player.getClan();
        if (clan == null) {
            _player.sendMessage("你没有血盟不能使用该道具!");
            return false;
        }
        if (clan.getLevel() >= 10) {
            _player.sendMessage("你的血盟等级已经达到上限,不能使用该道具!");
            return false;
        }
        if (!_player.getInventory().destroyItemByItemId(CLAN_EXPERIENCE_ITEM_ID,1)) {
            _player.sendMessage("道具不足或没有该道具,使用失败!");
            return false;
        }

        clan.setPoints(clan.getPoints() +CLAN_EXPERIENCE);
        _player.startAbnormalEffect(AbnormalEffect.U_AVE_SNOW_SLOW);
        ThreadPoolManager.getInstance().schedule(this::stop,1000L);
        _player.sendPacket(new ExPledgeV3Info(_player.getClan().getPoints(), _player.getClan().getRank(), _player.getClan().getAnnounce(), _player.getClan().isShowAnnounceOnEnter()));
        _player.sendPacket(new PledgeReceiveWarList(_player.getClan(), 0));
        _player.sendPacket(new ExPledgeEnemyInfoList(_player.getClan()));
        _player.sendMessage("成功为血盟「"+clan.getName()+"」添加"+CLAN_EXPERIENCE+"经验值");
        return true;
    }
    public void stop(){
        _player.stopAbnormalEffect(AbnormalEffect.U_AVE_SNOW_SLOW);
    }
}
