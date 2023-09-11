package l2s.gameserver.core;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;

public enum Geometry
{
	SQUARE, 
	CIRCLE, 
	POLYGON;

	private static /* synthetic */ int[] $SWITCH_TABLE$core$Geometry;

	public static boolean calc(Player actor, GameObject target)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(actor);
		int dis = config.getFindMobMaxDistance();
		int maxZDiff = config.getFindMobMaxHeight();
		int startX = config.getStartX();
		int startY = config.getStartY();
		int startZ = config.getStartZ();
		int zDiff = Math.abs(target.getZ() - startZ);
		if(zDiff > maxZDiff)
		{
			return false;
		}
		switch(Geometry.$SWITCH_TABLE$core$Geometry()[config.getGeometry().ordinal()])
		{
			case 1:
			{
				int los = (int) Math.sqrt(dis * dis / 2);
				int x1 = startX - los;
				int x2 = startX + los;
				int y1 = startY - los;
				int y2 = startY + los;
				int x = target.getX();
				int y = target.getY();
				return x >= x1 && x <= x2 && y >= y1 && y <= y2;
			}
			case 2:
			{
				return target.getDistance(startX, startY) <= (double) dis;
			}
			case 3:
			{
				return config.getPolygon().contains(target.getX(), target.getY()) && Math.abs(target.getZ() - config.getStartZ()) <= config.getFindMobMaxHeight();
			}
		}
		return true;
	}

	public String cnName()
	{
		if(this == SQUARE)
		{
			return "\u77e9\u5f62";
		/*\u77e9\u5f62 矩形*/
		}
		if(this == POLYGON)
		{
			return "\u591a\u8fb9\u5f62";
		/*\u591a\u8fb9\u5f62 多边形*/
		}
		return "\u5706\u5f62";
		/*\u5706\u5f62 圆形*/
	}

	static /* synthetic */ int[] $SWITCH_TABLE$core$Geometry()
	{
		if($SWITCH_TABLE$core$Geometry != null)
		{
			//int[] arrn = null;
			return $SWITCH_TABLE$core$Geometry;
		}
		int[] arrn = new int[Geometry.values().length];
		try
		{
			arrn[Geometry.CIRCLE.ordinal()] = 2;
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		try
		{
			arrn[Geometry.POLYGON.ordinal()] = 3;
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		try
		{
			arrn[Geometry.SQUARE.ordinal()] = 1;
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		$SWITCH_TABLE$core$Geometry = arrn;
		return $SWITCH_TABLE$core$Geometry;
	}
}
