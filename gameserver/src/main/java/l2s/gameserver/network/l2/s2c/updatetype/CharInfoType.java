package l2s.gameserver.network.l2.s2c.updatetype;

/**
 * @author nexvill
 */
public enum CharInfoType implements IUpdateTypeComponent
{
	PAPERDOLL(0x00, 50),
	VARIATION(0x01, 26),
	SHAPE_SHIFTING(0x02, 38),
	REALTIME_INFO(0x03, 20);

	/** Int mask. */
	private final int _mask;
	private final int _blockLength;

	private CharInfoType(int mask, int blockLength)
	{
		_mask = mask;
		_blockLength = blockLength;
	}

	/**
	 * Gets the int mask.
	 * @return the int mask
	 */
	@Override
	public final int getMask()
	{
		return _mask;
	}

	public int getBlockLength()
	{
		return _blockLength;
	}
}