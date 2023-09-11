package l2s.gameserver.botscript.bypasshandler;

import l2s.gameserver.botscript.BotControlPage;
import l2s.gameserver.core.*;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.IBroadcastPacket;

import java.awt.*;

public class BotPathVisualize
{
	@Bypass(value = "bot.showPath")
	public void show(Player player, NpcInstance npc, String[] param)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(player);
		for(int id : config.getPhantomItem())
		{
			player.sendPacket((IBroadcastPacket) new DeleteObjectPacket(id));
		}
		config.getPhantomItem().clear();
		Geometry geometry = config.getGeometry();
		if(geometry == Geometry.CIRCLE)
		{
			int count = (int) (6.283185307179586 * (double) config.getFindMobMaxDistance() / 75.0);
			double angle = 6.283185307179586 / (double) count;
			for(int i = 0; i < count; ++i)
			{
				int objectId = i + 1;
				int x = (int) (Math.cos(angle * (double) i) * (double) config.getFindMobMaxDistance());
				int y = (int) (Math.sin(angle * (double) i) * (double) config.getFindMobMaxDistance());
				player.sendPacket((IBroadcastPacket) new DropItemPacket(objectId, 57, config.getStartX() + x, config.getStartY() + y, config.getStartZ() + 150, player.getObjectId()));
				config.getPhantomItem().add(objectId);
			}
		}
		else if(geometry == Geometry.POLYGON)
		{
			Polygon _p = config.getPolygon();
			int[] _x = _p.xpoints;
			int[] _y = _p.ypoints;
			int objectId = 1;
			for(int i = 0; i < _p.npoints; ++i)
			{
				int nextIndex = i + 1;
				if(nextIndex == _x.length)
				{
					nextIndex = 0;
				}
				int vx = _x[nextIndex] - _x[i];
				int vy = _y[nextIndex] - _y[i];
				float lenght = (float) Math.sqrt(vx * vx + vy * vy);
				int o = 1;
				while((float) o <= (lenght /= 75.0f))
				{
					float k = (float) o / lenght;
					player.sendPacket((IBroadcastPacket) new DropItemPacket(++objectId, 57, (int) ((float) _x[i] + k * (float) vx), (int) ((float) _y[i] + k * (float) vy), player.getZ() + 150, player.getObjectId()));
					config.getPhantomItem().add(objectId);
					++o;
				}
			}
		}
		else
		{
			int dis = config.getFindMobMaxDistance();
			int startX = config.getStartX();
			int startY = config.getStartY();
			int los = (int) Math.sqrt(dis * dis / 2);
			int x1 = startX - los;
			int x2 = startX + los;
			int y1 = startY - los;
			int y2 = startY + los;
			int i = 0;
			for(int x = x1; x < x2; x += 75)
			{
				config.getPhantomItem().add(++i);
				player.sendPacket((IBroadcastPacket) new DropItemPacket(i, 57, x, y1, config.getStartZ() + 150, player.getObjectId()));
				config.getPhantomItem().add(++i);
				player.sendPacket((IBroadcastPacket) new DropItemPacket(i, 57, x, y2, config.getStartZ() + 150, player.getObjectId()));
			}
			for(int y = y1; y < y2; y += 75)
			{
				config.getPhantomItem().add(++i);
				player.sendPacket((IBroadcastPacket) new DropItemPacket(i, 57, x1, y, config.getStartZ() + 150, player.getObjectId()));
				config.getPhantomItem().add(++i);
				player.sendPacket((IBroadcastPacket) new DropItemPacket(i, 57, x2, y, config.getStartZ() + 150, player.getObjectId()));
			}
		}
	}
	@Bypass(value = "bot.autoAdjustRange")
	public void autoDistance(Player player, NpcInstance npc, String[] param){
		BotConfig botConfig = BotEngine.getInstance().getBotConfig(player);
		//如果是开 就 调为关
		if (param[0].equalsIgnoreCase("ON")) {
			botConfig.set_autoAdjustRange(true);
		}
		if (param[0].equalsIgnoreCase("OFF")) {
			botConfig.set_autoAdjustRange(false);
		}
		BotControlPage.pathPage(player);
	}
	@Bypass(value = "bot.autoSpoiledAttack")
	public void autoSpoiledAttack(Player player, NpcInstance npc, String[] param){
		BotConfig botConfig = BotEngine.getInstance().getBotConfig(player);
		botConfig.set_autoSpoiledAttack(!botConfig.is_autoSpoiledAttack());
		BotControlPage.mainPage(player);
	}
	@Bypass(value = "bot.setFollowInstance")
	public void setFollowInstance(Player player, NpcInstance npc, String[] param){
		BotConfig botConfig = BotEngine.getInstance().getBotConfig(player);
		if (param.length>0) {
			String instance = param[0];
			int i = Integer.parseInt(instance);
			botConfig.setFollowInstance(i);
			player.sendMessage("你当前设置跟随队长移动的距离为:"+i);
		}
		BotControlPage.party(player,new String[]{"list"});
	}
}