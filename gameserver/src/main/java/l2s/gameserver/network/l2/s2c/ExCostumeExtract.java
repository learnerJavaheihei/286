package l2s.gameserver.network.l2.s2c;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExCostumeExtract extends L2GameServerPacket {
	public static ExCostumeExtract FAIL = new ExCostumeExtract(0, 0, 0, 0, 0, 0);

	private final int result;
	private final int extractItemId;
	private final long extractItemCount;
	private final int resultItemId;
	private final long resultItemCount;
	private final long totalCount;

	public ExCostumeExtract(int result, int extractItemId, long extractItemCount, int resultItemId, long resultItemCount, long totalCount) {
		this.result = result;
		this.extractItemId = extractItemId;
		this.extractItemCount = extractItemCount;
		this.resultItemId = resultItemId;
		this.resultItemCount = resultItemCount;
		this.totalCount = totalCount;
	}

	@Override
	protected void writeImpl() {
		writeC(result);   //Result
		writeD(extractItemId);   //ExtractItemID
		writeQ(extractItemCount);   //ExtractItemNum
		writeD(resultItemId);   //ResultItemClassID
		writeQ(resultItemCount);   //ResultItemNum
		writeQ(totalCount);   //TotalNum*/
	}
}