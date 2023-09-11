package l2s.gameserver.core;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

public class DropItemPacket
extends L2GameServerPacket {
    private final Location _loc;
    private final int _playerId;
    private final int item_obj_id;
    private final int item_id;
    private final int _stackable;
    private final long _count;
    private final int _enchantLevel;
    private final int _ensoulCount;

    public DropItemPacket(ItemInstance item, int playerId) {
        this._playerId = playerId;
        this.item_obj_id = item.getObjectId();
        this.item_id = item.getItemId();
        this._loc = item.getLoc();
        this._stackable = item.isStackable() ? 1 : 0;
        this._count = item.getCount();
        this._enchantLevel = item.getEnchantLevel();
        this._ensoulCount = item.getNormalEnsouls().length + item.getSpecialEnsouls().length;
    }

    public DropItemPacket(int objectId, int itemId, int x, int y, int z, int playerId) {
        this._playerId = playerId;
        this.item_obj_id = objectId;
        this.item_id = itemId;
        this._loc = new Location(x, y, z);
        this._stackable = 1;
        this._count = 1L;
        this._enchantLevel = 0;
        this._ensoulCount = 0;
    }

    protected final void writeImpl() {
        this.writeD(this._playerId);
        this.writeD(this.item_obj_id);
        this.writeD(this.item_id);
        this.writeD(this._loc.x);
        this.writeD(this._loc.y);
        this.writeD(this._loc.z);
        this.writeC(this._stackable);
        this.writeQ(this._count);
        this.writeC(1);
        this.writeC(this._enchantLevel);
        this.writeC(0);
        this.writeC(this._ensoulCount);
    }
}