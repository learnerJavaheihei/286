package l2s.gameserver.network.l2.s2c;

import l2s.commons.geometry.Point2D;
import l2s.commons.geometry.Shape;

/**
 * @author Bonux
**/
public class ExShowTerritory extends L2GameServerPacket
{
	private final Shape _shape;

	public ExShowTerritory(Shape shape)
	{
		_shape = shape;
	}

	@Override
	protected void writeImpl()
	{
		final Point2D[] points = _shape.getPoints();
		writeD(points.length);
		writeD(_shape.getZmin());
		writeD(_shape.getZmax());
		for(Point2D point : points)
		{
			writeD(point.getX());
			writeD(point.getY());
		}
	}
}