package l2s.gameserver.network.l2.c2s.pledge;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanWar;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.tables.ClanTable;

/**
 * Written by Eden, on 21.02.2021
 */
public class RequestExPledgeEnemyDelete extends L2GameClientPacket{
    private int clanId;
    @Override
    protected boolean readImpl() throws Exception {
        clanId = readD();
        return true;
    }

    @Override
    protected void runImpl() throws Exception {
        Player activeChar = getClient().getActiveChar();
        if(activeChar == null)
            return;

        Clan playerClan = activeChar.getClan();
        if(playerClan == null)
        {
            activeChar.sendActionFailed();
            return;
        }

        if(!((activeChar.getClanPrivileges() & Clan.CP_CL_CLAN_WAR) == Clan.CP_CL_CLAN_WAR))
        {
            activeChar.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            activeChar.sendActionFailed();
            return;
        }

        Clan clan = ClanTable.getInstance().getClan(clanId);
        if(clan == null)
        {
            activeChar.sendPacket(SystemMsg.THE_TARGET_FOR_DECLARATION_IS_WRONG);
            activeChar.sendActionFailed();
            return;
        }

        if(!playerClan.isAtWarWith(clan.getClanId()))
        {
            activeChar.sendActionFailed();
            return;
        }

        for(UnitMember mbr : playerClan)
        {
            if(mbr.isOnline() && mbr.getPlayer().isInCombat())
            {
                activeChar.sendPacket(SystemMsg.A_CEASEFIRE_DURING_A_CLAN_WAR_CAN_NOT_BE_CALLED_WHILE_MEMBERS_OF_YOUR_CLAN_ARE_ENGAGED_IN_BATTLE);
                activeChar.sendActionFailed();
                return;
            }
        }


        ClanWar war = playerClan.getWarWith(clan.getClanId());
        if(war != null)
            war.cancel(playerClan);
    }
}
