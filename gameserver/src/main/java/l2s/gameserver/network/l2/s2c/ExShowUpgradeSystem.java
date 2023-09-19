package l2s.gameserver.network.l2.s2c;

/**
 * @author VISTALL
 */
/* public class ExShowUpgradeSystem extends L2GameServerPacket {
	private final int unk;

	public ExShowUpgradeSystem(int unk) {
		this.unk = unk;
	}
	
	@Override
	protected void writeImpl()
	{
		writeH(unk);    // unk, maybe type
		writeH(100); // price percent
		writeD(0x00); // unk
	}
} */

	public class ExShowUpgradeSystem extends L2GameServerPacket
	{
        private final int _result;
    
        public ExShowUpgradeSystem(int result) 
        {
		this._result = result;
		}
		
		@Override
		protected final void writeImpl() 
		{
			writeH(_result);
			writeH(0x64);
		}
	}