package l2s.gameserver.network.l2.c2s;

/**
 * format: ddS
 */
public class PetitionVote extends L2GameClientPacket
{
	private int _type, _unk1;
	private String _petitionText;

	@Override
	protected boolean readImpl()
	{
		_type = readD();
		_unk1 = readD(); // possible always zero
		_petitionText = readS(4096);
		// not done
		return true;
	}

	@Override
	protected void runImpl()
	{
		//
	}
}