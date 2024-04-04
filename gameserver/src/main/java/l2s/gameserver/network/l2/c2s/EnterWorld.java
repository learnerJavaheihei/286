package l2s.gameserver.network.l2.c2s;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.botscript.BotHangUpTimeDao;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.dao.MailDAO;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.*;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.actor.CreatureSkillCast;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.gs2as.ChangeAllowedHwid;
import l2s.gameserver.network.authcomm.gs2as.ChangeAllowedIp;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.network.l2.s2c.olympiad.ExOlympiadInfo;
import l2s.gameserver.network.l2.s2c.randomcraft.ExCraftInfo;
import l2s.gameserver.network.l2.s2c.updatetype.NpcInfoType;
import l2s.gameserver.model.entity.ranking.player.PlayerRanking;
import l2s.gameserver.model.entity.ranking.player.PlayerRankingManager;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.SkillCastingType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.utils.CompensationSystem.NewServerCompensationEntry;
import l2s.gameserver.utils.CompensationSystem.NewServerCompensationServiceImpl;
import l2s.gameserver.utils.GameStats;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.MyUtilsFunction;
import l2s.gameserver.utils.TradeHelper;

import org.napile.primitive.pair.IntObjectPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnterWorld extends L2GameClientPacket
{
	private static final Object _lock = new Object();

	private static final Logger _log = LoggerFactory.getLogger(EnterWorld.class);

	@Override
	protected boolean readImpl()
	{
		//readS(); - клиент всегда отправляет строку "narcasse"
		return true;
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		Player activeChar = client.getActiveChar();

		if(activeChar == null)
		{
			client.closeNow(false);
			return;
		}
		
		GameStats.incrementPlayerEnterGame();

		onEnterWorld(activeChar);
	}

	public static void onEnterWorld(Player activeChar)
	{
		// 检查背包金币
		List<ItemInstance> itemInstances = activeChar.getInventory().getItemsByItemId(57);
		if(itemInstances!=null && itemInstances.size() > 1){
			// 排序-
			itemInstances.sort(new Comparator<ItemInstance>() {
				@Override
				public int compare(ItemInstance item1, ItemInstance item2) {
					// 从大到小排序
					return Long.compare(item2.getCount(), item1.getCount());
				}
			});
			ItemInstance maxCountItemInstance = itemInstances.get(0);
			long sum = 0;
			boolean deleteSuccess = true;
			for (ItemInstance itemInstance : itemInstances) {
				sum += itemInstance.getCount();
				// 删除其他 的 金币道具
				if (itemInstance.getObjectId() != maxCountItemInstance.getObjectId()) {
					if (!activeChar.getInventory().destroyItem(itemInstance)) {
						deleteSuccess = false;
						break;
					}
				}
			}
			// 如果有一个删除失败 不执行
			if (deleteSuccess) {
				maxCountItemInstance.setCount(sum);
				maxCountItemInstance.setJdbcState(JdbcEntityState.UPDATED);
				activeChar.getInventory().store();
			}
		}
		restoreWeaponVisuals(activeChar);
		//
		if (Config.ENABLE_BOTSCRIPT_RESTRICT_TIME){
			botScriptSellTime(activeChar);
		}

		checkCompensationBtn(activeChar);

		boolean first = activeChar.entering;

		activeChar.sendPacket(ExLightingCandleEvent.DISABLED);
		//TODO: activeChar.sendPacket(new ExChannlChatEnterWorld(activeChar));
		//TODO: activeChar.sendPacket(new ExChannlChatPlegeInfo(activeChar));
		activeChar.sendPacket(new ExEnterWorldPacket());
		if(Config.EX_USE_TO_DO_LIST)
			activeChar.sendPacket(new ExConnectedTimeAndGettableReward(activeChar));
		activeChar.sendPacket(new ExPeriodicHenna(activeChar));
		activeChar.sendPacket(new HennaInfoPacket(activeChar));

		List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		for(Castle c : castleList)
			activeChar.sendPacket(new ExCastleState(c));

		activeChar.sendSkillList();
		activeChar.sendPacket(new EtcStatusUpdatePacket(activeChar));

		activeChar.sendPacket(new UIPacket(activeChar));
		activeChar.sendPacket(new ExUserInfoInvenWeight(activeChar));
		activeChar.sendPacket(new ExUserInfoEquipSlot(activeChar));
		activeChar.sendPacket(new ExUserInfoCubic(activeChar));
		activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(activeChar));

		activeChar.sendPacket(SystemMsg.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);

		double mpCostDiff = activeChar.getMPCostDiff(Skill.SkillMagicType.PHYSIC);
		if(mpCostDiff != 0)
			activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.PHYSIC, mpCostDiff));

		mpCostDiff = activeChar.getMPCostDiff(Skill.SkillMagicType.MAGIC);
		if(mpCostDiff != 0)
			activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.MAGIC, mpCostDiff));

		mpCostDiff = activeChar.getMPCostDiff(Skill.SkillMagicType.MUSIC);
		if(mpCostDiff != 0)
			activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.MUSIC, mpCostDiff));

		activeChar.sendPacket(new QuestListPacket(activeChar));
		activeChar.initActiveAutoShots();
		activeChar.sendPacket(new ExGetBookMarkInfoPacket(activeChar));

		activeChar.sendItemList(false);
		activeChar.sendPacket(new ExAdenaInvenCount(activeChar));
		activeChar.sendPacket(new ExBloodyCoinCount(activeChar));
		activeChar.sendPacket(new ShortCutInitPacket(activeChar));
		activeChar.sendPacket(new ExBasicActionList(activeChar));
		
		activeChar.getMacroses().sendMacroses();

		Announcements.getInstance().showAnnouncements(activeChar);

		if(first)
		{
			activeChar.setOnlineStatus(true);
			if(activeChar.getPlayerAccess().GodMode && !Config.SHOW_GM_LOGIN && !Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			{
				activeChar.setGMInvisible(true);
				activeChar.startAbnormalEffect(AbnormalEffect.STEALTH);
			}

			activeChar.setNonAggroTime(Long.MAX_VALUE);
			activeChar.setNonPvpTime(System.currentTimeMillis() + Config.NONPVP_TIME_ONTELEPORT);

			if(activeChar.isInBuffStore())
			{
				activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
			}
			else if(activeChar.isInStoreMode())
			{
				if(!TradeHelper.validateStore(activeChar))
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.storePrivateStore();
				}
			}

			activeChar.setRunning();
			activeChar.standUp();
			activeChar.spawnMe();
			activeChar.startTimers();
		}

		activeChar.sendPacket(new ExBR_PremiumStatePacket(activeChar, activeChar.hasPremiumAccount()));

		activeChar.sendPacket(new ExSetCompassZoneCode(activeChar));
		//TODO: Исправить посылаемые данные.
		activeChar.sendPacket(new MagicAndSkillList(activeChar, 3503292, 730502));
		activeChar.sendPacket(new ExStorageMaxCountPacket(activeChar));
		activeChar.getAttendanceRewards().onEnterWorld();
		activeChar.sendPacket(new ExReceiveShowPostFriend(activeChar));

		if(Config.ALLOW_WORLD_CHAT)
			activeChar.sendPacket(new ExWorldChatCnt(activeChar));

		if(Config.EX_USE_PRIME_SHOP)
		{
			activeChar.sendPacket(new ExBR_NewIConCashBtnWnd(activeChar));
			activeChar.sendPacket(new ReciveVipInfo(activeChar));
		}

		activeChar.sendPacket(new ExElementalSpiritInfo(activeChar, 0));
		
		activeChar.sendPacket(new ExCraftInfo(activeChar));

		if(!Config.EX_COSTUME_DISABLE)
			activeChar.sendPacket(new ExCostumeShortcutList(activeChar));

		checkNewMail(activeChar);

		if(first) {
			activeChar.getListeners().onEnter();
		}

		activeChar.checkAndDeleteOlympiadItems();

		if(activeChar.getClan() != null)
		{
			activeChar.getClan().loginClanCond(activeChar, true);

			activeChar.sendPacket(activeChar.getClan().listAll());
			activeChar.sendPacket(new PledgeSkillListPacket(activeChar.getClan()));
		}
		else
			activeChar.sendPacket(new ExPledgeCount(0));

		// engage and notify Partner
		if(first && Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance().engage(activeChar);
			CoupleManager.getInstance().notifyPartner(activeChar);
		}

		if(first)
		{
			activeChar.getFriendList().notifyFriends(true);
			//activeChar.restoreDisableSkills(); Зачем дважды ресторить откат скиллов?
		}

		activeChar.checkHpMessages(activeChar.getMaxHp(), activeChar.getCurrentHp());
		activeChar.checkDayNightMessages();

		if(Config.SHOW_HTML_WELCOME)
		{
			if (MyUtilsFunction.checkCanBuy(activeChar, 0, 1)) {
				String html = HtmCache.getInstance().getHtml("welcome.htm", activeChar);
				HtmlMessage msg = new HtmlMessage(0);
				msg.setItemId(-1);
				msg.setHtml(html);
				activeChar.sendPacket(msg);
			}
		}

		if(Config.PETITIONING_ALLOWED)
			PetitionManager.getInstance().checkPetitionMessages(activeChar);

		if(!first)
		{
			CreatureSkillCast skillCast = activeChar.getSkillCast(SkillCastingType.NORMAL);
			if(skillCast.isCastingNow())
			{
				Creature castingTarget = skillCast.getTarget();
				SkillEntry castingSkillEntry = skillCast.getSkillEntry();
				long animationEndTime = skillCast.getAnimationEndTime();
				if(castingSkillEntry != null && !castingSkillEntry.getTemplate().isNotBroadcastable() && castingTarget != null && castingTarget.isCreature() && animationEndTime > 0)
					activeChar.sendPacket(new MagicSkillUse(activeChar, castingTarget, castingSkillEntry.getId(), castingSkillEntry.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0, SkillCastingType.NORMAL));
			}

			skillCast = activeChar.getSkillCast(SkillCastingType.NORMAL_SECOND);
			if(skillCast.isCastingNow())
			{
				Creature castingTarget = skillCast.getTarget();
				SkillEntry castingSkillEntry = skillCast.getSkillEntry();
				long animationEndTime = skillCast.getAnimationEndTime();
				if(castingSkillEntry != null && !castingSkillEntry.getTemplate().isNotBroadcastable() && castingTarget != null && castingTarget.isCreature() && animationEndTime > 0)
					activeChar.sendPacket(new MagicSkillUse(activeChar, castingTarget, castingSkillEntry.getId(), castingSkillEntry.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0, SkillCastingType.NORMAL_SECOND));
			}

			if(activeChar.isInBoat())
				activeChar.sendPacket(activeChar.getBoat().getOnPacket(activeChar, activeChar.getInBoatPosition()));

			if(activeChar.getMovement().isMoving() || activeChar.getMovement().isFollow())
				activeChar.sendPacket(activeChar.movePacket());

			if(activeChar.getMountNpcId() != 0)
				activeChar.sendPacket(new RidePacket(activeChar));

			if(activeChar.isFishing())
				activeChar.getFishing().stop();
		}

		activeChar.entering = false;

		if(activeChar.isSitting())
			activeChar.sendPacket(new ChangeWaitTypePacket(activeChar, ChangeWaitTypePacket.WT_SITTING));
		if(activeChar.isInStoreMode())
			activeChar.sendPacket(activeChar.getPrivateStoreMsgPacket(activeChar));

		activeChar.unsetVar("offline");
		activeChar.unsetVar("offlinebuff");
		activeChar.unsetVar("offlinebuff_price");
		activeChar.unsetVar("offlinebuff_skills");
		activeChar.unsetVar("offlinebuff_title");

		OfflineBufferManager.getInstance().getBuffStores().remove(activeChar.getObjectId());

		// на всякий случай
		activeChar.sendActionFailed();

		if(first && activeChar.isGM() && Config.SAVE_GM_EFFECTS && activeChar.getPlayerAccess().CanUseGMCommand)
		{
			//silence
			if(activeChar.getVarBoolean("gm_silence"))
			{
				activeChar.setMessageRefusal(true);
				activeChar.sendPacket(SystemMsg.MESSAGE_REFUSAL_MODE);
			}
			//invul
			if(activeChar.getVarBoolean("gm_invul"))
			{
				activeChar.getFlags().getInvulnerable().start();
				activeChar.getFlags().getDebuffImmunity().start();
				activeChar.startAbnormalEffect(AbnormalEffect.INVINCIBILITY);
				activeChar.sendMessage(activeChar.getName() + " is now immortal.");
			}
			//undying
			if(activeChar.getVarBoolean("gm_undying"))
			{
				activeChar.setGMUndying(true);
				activeChar.sendMessage("Undying state has been enabled.");
			}
			//gmspeed
			activeChar.setGmSpeed(activeChar.getVarInt("gm_gmspeed", 0));
		}

		PlayerMessageStack.getInstance().CheckMessages(activeChar);

		IntObjectPair<OnAnswerListener> entry = activeChar.getAskListener(false);
		if(entry != null && entry.getValue() instanceof ReviveAnswerListener)
			activeChar.sendPacket(new ConfirmDlgPacket(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0).addString("Other player").addString("some"));

		if(!first)
		{
			//Персонаж вылетел во время просмотра
			if(activeChar.isInObserverMode())
			{
				if(activeChar.getObserverMode() == Player.OBSERVER_LEAVING)
					activeChar.returnFromObserverMode();
				else
					activeChar.leaveObserverMode();
			}
			else if(activeChar.isVisible())
				World.showObjectsToPlayer(activeChar);

			final List<Servitor> servitors = activeChar.getServitors();

			for(Servitor servitor : servitors)
				activeChar.sendPacket(new MyPetSummonInfoPacket(servitor));

			if(activeChar.isInParty())
			{
				Party party = activeChar.getParty();
				Player leader = party.getPartyLeader();
				if(leader != null) // некрасиво, но иначе NPE.
				{
					//sends new member party window for all members
					//we do all actions before adding member to a list, this speeds things up a little
					activeChar.sendPacket(new PartySmallWindowAllPacket(party, leader, activeChar));

					RelationChangedPacket rcp = new RelationChangedPacket();
					for(Player member : party.getPartyMembers())
					{
						if(member != activeChar)
						{
							activeChar.sendPacket(new PartySpelledPacket(member, true));

							for(Servitor servitor : servitors)
								activeChar.sendPacket(new PartySpelledPacket(servitor, true));

							rcp.add(member, activeChar);
							for(Servitor servitor : member.getServitors())
								rcp.add(servitor, activeChar);

							for(Servitor servitor : servitors)
								servitor.broadcastCharInfoImpl(activeChar, NpcInfoType.VALUES);
						}
					}

					activeChar.sendPacket(rcp);

					// Если партия уже в СС, то вновь прибывшем посылаем пакет открытия окна СС
					if(party.isInCommandChannel())
						activeChar.sendPacket(ExOpenMPCCPacket.STATIC);
				}
			}

			activeChar.sendActiveAutoShots();

			for(Abnormal e : activeChar.getAbnormalList())
			{
				if(e.getSkill().isToggle() && !e.getSkill().isNotBroadcastable())
					activeChar.sendPacket(new MagicSkillLaunchedPacket(activeChar.getObjectId(), e.getSkill().getId(), e.getSkill().getLevel(), activeChar, SkillCastingType.NORMAL));
			}

			activeChar.broadcastCharInfo();
		}
		
		activeChar.sendPacket(new ExOlympiadInfo());
		
		
		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, 3);
		if ((castle != null) && (castle.getSiegeEvent().hasState(SiegeEvent.REGISTRATION_STATE) || castle.getSiegeEvent().hasState(SiegeEvent.PROGRESS_STATE)))
		{
			activeChar.sendPacket(new ExMercenaryCastlewarCastleSiegeHudInfo(castle.getSiegeEvent()));
		}

		if(activeChar.isDead())
			activeChar.sendPacket(new DiePacket(activeChar));
		
		activeChar.setKarma(activeChar.getKarma());

		activeChar.updateAbnormalIcons();
		activeChar.updateStatBonus();
		activeChar.updateStats();
		activeChar.updateUserBonus();

		if(Config.ALT_PCBANG_POINTS_ENABLED)
		{
			if(!Config.ALT_PCBANG_POINTS_ONLY_PREMIUM || activeChar.hasPremiumAccount())
				activeChar.sendPacket(new ExPCCafePointInfoPacket(activeChar, 0, 1, 2, 12));
		}
		
		activeChar.checkLevelUpReward(true);
		activeChar.sendClassChangeAlert();

		if(first)
		{
			activeChar.useTriggers(activeChar, TriggerType.ON_ENTER_WORLD, null, null, 0);

			for(ListenerHook hook : ListenerHook.getGlobalListenerHooks(ListenerHookType.PLAYER_ENTER_GAME))
				hook.onPlayerEnterGame(activeChar);

			if(Config.ALLOW_IP_LOCK && Config.AUTO_LOCK_IP_ON_LOGIN)
				AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedIp(activeChar.getAccountName(), activeChar.getIP()));

			if(Config.ALLOW_HWID_LOCK && Config.AUTO_LOCK_HWID_ON_LOGIN)
			{
				GameClient client = activeChar.getNetConnection();
				if(client != null)
					AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(activeChar.getAccountName(), client.getHWID()));
			}
		}
		
		PlayerRanking ranking = PlayerRankingManager.getInstance().getRanking(activeChar.getObjectId());
		if (ranking != null)
			ranking.checkRewards();

		activeChar.checkRankingRewards();
		activeChar.getInventory().checkItems();
	}

	private static void checkCompensationBtn(Player activeChar) {
		List<NewServerCompensationEntry> filterList = NewServerCompensationServiceImpl.filterList;
		if(NewServerCompensationServiceImpl.OnOffset && !filterList.isEmpty()){
			String accountName = activeChar.getAccountName();
			for (NewServerCompensationEntry newServerCompensationEntry : filterList) {
				// TODO 还需要附加 当前一期是否被领取过
				if (accountName.equals(newServerCompensationEntry.getAccount())) {
					activeChar.setTurnOnCompensationBtn(true);
					break;
				}
			}
		}
	}

	private static void botScriptSellTime(Player activeChar) {
		/** 读取内挂剩余时间 */
		List<Integer> integers = BotHangUpTimeDao.getInstance().selectHangUpTime(activeChar.getObjectId());
		if(BotEngine.leftTimeMap == null){
			BotEngine.leftTimeMap = new ConcurrentHashMap<>();
		}
		if (integers != null) {
			int leftTime =integers.get(1);
			BotEngine.leftTimeMap.put(String.valueOf(activeChar.getObjectId()),String.valueOf(leftTime));
		}
		String leftTime = BotEngine.leftTimeMap.get(String.valueOf(activeChar.getObjectId()));

		/** 新 或未使用内挂的老用户 */
		if (leftTime==null ) {
			/* 给缓存设置初始值 */
			BotEngine.leftTimeMap.put(String.valueOf(activeChar.getObjectId()),"36000");
			/* 数据库初始化挂机时间 */
			BotHangUpTimeDao.getInstance().insertScriptTime(activeChar.getObjectId(),Player.scriptTime,Player.scriptTime,0);
		}
		int time = BotHangUpTimeDao.getInstance().selectIsBuyByObjId(activeChar.getObjectId());
		Player._buyTimesByOBJ.putIfAbsent(activeChar.getObjectId(),time);
	}
	private static void restoreWeaponVisuals(Player activeChar) {
		Connection con = null;
		PreparedStatement statement = null;
		List<Integer> visuals = new ArrayList<>();
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT deletetime, visual_id FROM _character_weapon WHERE player_id = ? AND use_item = 1");
			statement.setInt(1, activeChar.getObjectId());
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()){
				int deletetime = resultSet.getInt("deletetime");
				if (deletetime <  (System.currentTimeMillis() / 1000))
					continue;
				int visual_id = resultSet.getInt("visual_id");
				visuals.add(visual_id);
			}
			activeChar.setWeaponVisuals(visuals);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	private static void checkNewMail(Player activeChar)
	{
		activeChar.sendPacket(new ExUnReadMailCount(activeChar));
		for(Mail mail : MailDAO.getInstance().getReceivedMailByOwnerId(activeChar.getObjectId()))
		{
			if(mail.isUnread())
			{
				activeChar.sendPacket(ExNoticePostArrived.STATIC_FALSE);
				break;
			}
		}
	}
}
