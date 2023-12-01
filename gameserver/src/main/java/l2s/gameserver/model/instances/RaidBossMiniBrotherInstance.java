package l2s.gameserver.model.instances;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.HeroDiary;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.NpcUtils;
import l2s.commons.collections.MultiValueSet;

public class RaidBossMiniBrotherInstance extends MonsterInstance
{
    public RaidBossMiniBrotherInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
    {
        super(objectId, template, set);
    }

    @Override
    public void onSpawn()
    {
        super.onSpawn();
    }

    @Override
    public void onDeath(Creature killer)
    {
        super.onDeath(killer);
    }
}

