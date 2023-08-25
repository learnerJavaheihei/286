package l2s.gameserver.network.l2.c2s;

/**
 * @author Bonux
**/
public class ExSendClientINI extends L2GameClientPacket
{
	private int _iniType;	// 0 - Unknown, 1 - Option, 2 - ChatFilter, 3 - WindowsInfo
	private byte[] _content;

	@Override
	protected boolean readImpl()
	{
		_iniType = readC(); // Part number
		_content = new byte[readH()]; // Part size
		readB(_content); // Content
		return true;
	}

	@Override
	protected void runImpl()
	{
		//System.out.println("ExSendClientINI: _iniType=" + _iniType + ", _content=" + new String(_content));
	}
}