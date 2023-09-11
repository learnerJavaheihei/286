package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotResType;
import l2s.gameserver.core.BotThinkTask;
import l2s.gameserver.core.IBotActionHandler;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.ItemFunctions;

import java.util.List;

public class BotRes implements IBotActionHandler
{
	int[] blessScrollIds = new int[] { 737,3936 ,3959,29030, 29700, 49084, 49526,49084, 49542,  29518,29546,};;
	/*物品ID29700 微祝福的復活卷軸	物品ID49084 高級復活卷軸	物品ID49526 高級復活卷軸	物品ID49542 高級復活卷軸	物品ID3936 祝福的復活卷軸*/
	int scrollId = 737;
	/*物品ID737 復活卷軸*/
	int skillId = 1016;
	/*技能ID1016 返生術*/
	private static /* synthetic */ int[] $SWITCH_TABLE$core$BotResType;

	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		if(!config.isUseRes())
		{
			return false;
		}
		if(isSitting || this.isActionsDisabledExcludeAttack(actor))
		{
			return false;
		}
		Party party = actor.getParty();
		if(party == null)
		{
			List<Player> aroundPlayers = World.getAroundPlayers(actor, 500);
			if (aroundPlayers!=null && aroundPlayers.size()>0) {
				for (Player aroundPlayer : aroundPlayers) {
					if (aroundPlayer.getClanId() == actor.getClanId()) {
						if(!aroundPlayer.isDead())
							continue;
						useRevive(actor,config,aroundPlayer);
					}
				}
			}
			return false;
		}
		for(Player player : party)
		{
			if(!player.isDead())
				continue;
			if (actor.getDistance(player.getX(), player.getY(),player.getZ()) > 400) {
				continue;
			}
			if (config.getIsUsedReviveOwner_target().get(player.getObjectId())!=null && !config.getIsUsedReviveOwner_target().get(player.getObjectId())) {
				continue;
			}
			useRevive(actor,config,player);
		}
		return false;
	}
	public boolean useRevive(Player actor, BotConfig config,Player target){
		for(BotResType botResType : config.getResType())
		{
			switch($SWITCH_TABLE$core$BotResType()[botResType.ordinal()])
			{
				case 1:
				{
					for(int itemId : blessScrollIds)
					//先使用祝福卷
					{
						if(!ItemFunctions.haveItem(actor, itemId, 1))
							continue;
						actor.setTarget(target);
						ItemInstance scroll = actor.getInventory().getItemByItemId(itemId);
						actor.useItem(scroll, false, false);
						return true;
					}
					break;
				}
				case 2:
					//"返生术
				{
					SkillEntry skillEntry = actor.getKnownSkill(this.skillId);
					if(skillEntry == null || actor.isSkillDisabled(skillEntry.getTemplate()) || !BotThinkTask.checkSkillMpCost(actor, skillEntry) || !skillEntry.checkCondition(actor, target, false, false, false))
						continue;
					actor.setTarget(target);
					actor.getAI().Cast(skillEntry, target, false, false);
					return true;
				}
				case 3:
					//"复活卷
				{
					if(!ItemFunctions.haveItem(actor, scrollId, 1))
						return false;
					actor.setTarget(target);
					ItemInstance scroll = actor.getInventory().getItemByItemId(scrollId);
					actor.useItem(scroll, false, false);
					return true;
				}
			}
		}
		return false;
	}


	static int[] $SWITCH_TABLE$core$BotResType()
	{
		if($SWITCH_TABLE$core$BotResType != null)
		{
			return $SWITCH_TABLE$core$BotResType;
		}
		int[] arrn = new int[BotResType.values().length];
		try
		{
			arrn[BotResType.BLESSED.ordinal()] = 1;//BLESSED("祝福的复活卷")
			arrn[BotResType.MAGIC.ordinal()] = 2;//MAGIC("返生术")
			arrn[BotResType.DEFAULT.ordinal()] = 3;//DEFAULT("复活卷")
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		$SWITCH_TABLE$core$BotResType = arrn;
		return $SWITCH_TABLE$core$BotResType;
	}
}