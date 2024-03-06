package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.instancemanager.RankManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.updatetype.CharInfoType;
import l2s.gameserver.skills.AbnormalEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author Bonux
 * thx nexvill & artkill
 */
public class ExCharInfo extends L2GameServerPacket {
	public static final int[] PAPERDOLL_ORDER = {
			Inventory.PAPERDOLL_PENDANT,
			Inventory.PAPERDOLL_HEAD,
			Inventory.PAPERDOLL_RHAND,
			Inventory.PAPERDOLL_LHAND,
			Inventory.PAPERDOLL_GLOVES,
			Inventory.PAPERDOLL_CHEST,
			Inventory.PAPERDOLL_LEGS,
			Inventory.PAPERDOLL_FEET,
			Inventory.PAPERDOLL_BACK,
			Inventory.PAPERDOLL_LRHAND,
			Inventory.PAPERDOLL_HAIR,
			Inventory.PAPERDOLL_DHAIR
	};

	public static boolean isVisiblePaperdoll(int paperdollId) {
		return !(paperdollId == Inventory.PAPERDOLL_REAR || paperdollId == Inventory.PAPERDOLL_LEAR || paperdollId == Inventory.PAPERDOLL_NECK || paperdollId == Inventory.PAPERDOLL_RFINGER || paperdollId == Inventory.PAPERDOLL_LFINGER || paperdollId == Inventory.PAPERDOLL_RBRACELET || paperdollId == Inventory.PAPERDOLL_LBRACELET || paperdollId == Inventory.PAPERDOLL_DECO1 || paperdollId == Inventory.PAPERDOLL_DECO2 || paperdollId == Inventory.PAPERDOLL_DECO3 || paperdollId == Inventory.PAPERDOLL_DECO4 || paperdollId == Inventory.PAPERDOLL_DECO5 || paperdollId == Inventory.PAPERDOLL_DECO6 || paperdollId == Inventory.PAPERDOLL_BELT || paperdollId == Inventory.PAPERDOLL_HEAD);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ExCharInfo.class);

	private boolean canWrite = false;

	private int[][] paperdolls;
	private int mAtkSpd, pAtkSpd;
	private int runSpd, walkSpd, swimRunSpd, swimWalkSpd, flRunSpd, flWalkSpd, flyRunSpd, flyWalkSpd;
	private Location fishLoc;
	private String name, title;
	private int x, y, z, heading, boatId;
	private int objId, race, sex, baseClass, pvpFlag, karma, recHave;
	private double speedMove, speedAtack, colRadius, colHeight;
	private int hairStyle, hairColor, face;
	private int clanId, clanCrestId, largeClanCrestId, allyId, allyCrestId, classId;
	private int sit, run, combat, dead, privateStore, enchant;
	private int hero, fishing, mountType;
	private int pledgeClass, pledgeType, clanRepScore, mountId;
	private int nameColor, titleColor, transformId, agathionId;
	private Cubic[] cubics;
	private boolean partyRoomLeader, flying;
	private int curHp, maxHp, curMp, maxMp, curCp;
	private TeamType teamType;
	private Set<AbnormalEffect> abnormalEffects;
	private boolean showHeadAccessories;
	private int armorSetEnchant;
	private boolean noble;
	private int ranking;

	public ExCharInfo(Creature cha, Player receiver) {
		if (cha == null) {
			LOGGER.error("CIPacket: cha is null!", new Exception());
			return;
		}

		if (receiver == null)
			return;

		if (cha.isInvisible(receiver))
			return;

		if (cha.isDeleted())
			return;

		objId = cha.getObjectId();
		if (objId == 0)
			return;

		if (receiver.getObjectId() == objId) {
			LOGGER.error("You cant send CIPacket about his character to active user!!!", new Exception());
			return;
		}

		Player player = cha.getPlayer();
		if (player == null)
			return;

		Location loc = null;
		if (player.isInBoat()) {
			loc = player.getInBoatPosition();
			boatId = player.getBoat().getBoatId();
		}

		if (loc == null)
			loc = cha.getLoc();

		x = loc.x;
		y = loc.y;
		z = loc.z;
		heading = loc.h;


		name = player.getVisibleName(receiver);
		nameColor = player.getVisibleNameColor(receiver);

		if (player.isConnected() || player.isInOfflineMode() || player.isFakePlayer()) {
			title = player.getVisibleTitle(receiver);
			titleColor = player.getVisibleTitleColor(receiver);
		} else {
			title = "";
			titleColor = 255;
		}

		if (player.isPledgeVisible(receiver)) {
			Clan clan = player.getClan();
			Alliance alliance = clan == null ? null : clan.getAlliance();
			//
			clanId = clan == null ? 0 : clan.getClanId();
			clanCrestId = clan == null ? 0 : clan.getCrestId();
			largeClanCrestId = clan == null ? 0 : clan.getCrestLargeId();
			//
			allyId = alliance == null ? 0 : alliance.getAllyId();
			allyCrestId = alliance == null ? 0 : alliance.getAllyCrestId();
		}

		if (player.isMounted()) {
			enchant = 0;
			mountId = player.getMountNpcId() + 1000000;
			mountType = player.getMountType().ordinal();
		} else {
			enchant = player.getEnchantEffect();
			mountId = 0;
			mountType = 0;
		}

		paperdolls = new int[PcInventory.PAPERDOLL_MAX][4];

		for(int PAPERDOLL_ID : PAPERDOLL_ORDER)
		{
			paperdolls[PAPERDOLL_ID][0] = player.getInventory().getPaperdollItemId(PAPERDOLL_ID);
			paperdolls[PAPERDOLL_ID][1] = player.getInventory().getPaperdollVariation1Id(PAPERDOLL_ID);
			paperdolls[PAPERDOLL_ID][2] = player.getInventory().getPaperdollVariation2Id(PAPERDOLL_ID);
			paperdolls[PAPERDOLL_ID][3] = player.getInventory().getPaperdollVisualId(PAPERDOLL_ID);
		}

		mAtkSpd = player.getMAtkSpd();
		pAtkSpd = player.getPAtkSpd();
		speedMove = player.getMovementSpeedMultiplier();
		runSpd = (int) (player.getRunSpeed() / speedMove);
		walkSpd = (int) (player.getWalkSpeed() / speedMove);

		flRunSpd = 0; // TODO
		flWalkSpd = 0; // TODO

		if (player.isFlying()) {
			flyRunSpd = runSpd;
			flyWalkSpd = walkSpd;
		} else {
			flyRunSpd = 0;
			flyWalkSpd = 0;
		}

		swimRunSpd = player.getSwimRunSpeed();
		swimWalkSpd = player.getSwimWalkSpeed();
		race = player.getRace().ordinal();
		sex = player.getSex().ordinal();
		baseClass = player.getBaseClassId();
		pvpFlag = player.getPvpFlag();
		karma = player.getKarma();

		speedAtack = player.getAttackSpeedMultiplier();
		colRadius = player.getCurrentCollisionRadius();
		colHeight = player.getCurrentCollisionHeight();
		hairStyle = player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR) > 0 ? sex : (player.getBeautyHairStyle() > 0 ? player.getBeautyHairStyle() : player.getHairStyle());
		hairColor = player.getBeautyHairColor() > 0 ? player.getBeautyHairColor() : player.getHairColor();
		face = player.getBeautyFace() > 0 ? player.getBeautyFace() : player.getFace();
		if (clanId > 0 && player.getClan() != null)
			clanRepScore = player.getClan().getReputationScore();
		else
			clanRepScore = 0;
		sit = player.isSitting() ? 0 : 1; // standing = 1 sitting = 0
		run = player.isRunning() ? 1 : 0; // running = 1 walking = 0
		combat = player.isInCombat() ? 1 : 0;
		dead = player.isAlikeDead() ? 1 : 0;
		privateStore = player.isInObserverMode() ? Player.STORE_OBSERVING_GAMES : player.getPrivateStoreType();
		cubics = player.getCubics().toArray(new Cubic[0]);
		abnormalEffects = player.getAbnormalEffects();
		recHave = player.isGM() ? 0 : player.getRecomHave();
		classId = player.getClassId().getId();
		teamType = player.getTeam();
		hero = player.isHero() || player.isGM() && Config.GM_HERO_AURA ? 2 : 0; // 0x01: Hero Aura
		if (hero == 0)
			hero = Hero.getInstance().isInactiveHero(objId) ? 1 : 0;
		noble = hero > 0;
		fishing = player.getFishing().isInProcess() ? 1 : 0;
		fishLoc = player.getFishing().getHookLocation();
		pledgeClass = player.getPledgeRank().ordinal();
		pledgeType = player.getPledgeType();
		transformId = player.getVisualTransformId();
		agathionId = player.getAgathionNpcId();
		partyRoomLeader = player.getMatchingRoom() != null && player.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && player.getMatchingRoom().getLeader() == player;
		flying = player.isInFlyingTransform();
		curHp = (int) player.getCurrentHp();//receiver.canReceiveStatusUpdate(player, StatusUpdatePacket.UpdateType.DEFAULT, StatusUpdatePacket.CUR_HP) ? (int) player.getCurrentHp() : 0;
		maxHp = player.getMaxHp();//receiver.canReceiveStatusUpdate(player, StatusUpdatePacket.UpdateType.DEFAULT, StatusUpdatePacket.MAX_HP) ? player.getMaxHp() : 0;
		curMp = (int) player.getCurrentMp();
		maxMp = player.getMaxMp();
		curCp = (int) player.getCurrentCp();
		showHeadAccessories = !player.hideHeadAccessories();
		armorSetEnchant = player.getArmorSetEnchant();
		int rank = 0;
		if (player.isTopRank())
			rank |= 1 << 0;
		if (player.isTopRaceRank())
			rank |= 1 << 1;

		ranking = rank;
		canWrite = true;
	}

	@Override
	protected boolean canWrite() {
		return canWrite;
	}

	@Override
	protected final void writeImpl() {

		writeH(339 + (2 + (title.length() * 2)) + (cubics.length * 2) + (abnormalEffects.size() * 2));

		writeD(objId);
		writeH(race);
		writeC(sex);
		writeD(baseClass);

		writeH(CharInfoType.PAPERDOLL.getBlockLength()); // Paperdoll block size (2 + (4 * 12))

		for (int paperdollId : PAPERDOLL_ORDER)
			writeD(paperdolls[paperdollId][0]);

		writeH(CharInfoType.VARIATION.getBlockLength()); // Augmentation block size (2 + (4 * 6))

		writeD(paperdolls[Inventory.PAPERDOLL_RHAND][1]);
		writeD(paperdolls[Inventory.PAPERDOLL_RHAND][2]);

		writeD(paperdolls[Inventory.PAPERDOLL_LHAND][1]);
		writeD(paperdolls[Inventory.PAPERDOLL_LHAND][2]);

		writeD(paperdolls[Inventory.PAPERDOLL_LRHAND][1]);
		writeD(paperdolls[Inventory.PAPERDOLL_LRHAND][2]);

		writeC(armorSetEnchant);    // Armor Enchant Abnormal

		writeH(CharInfoType.SHAPE_SHIFTING.getBlockLength()); // Shape shifting item block size (2 + (4 * 9))

		writeD(paperdolls[Inventory.PAPERDOLL_RHAND][3]);
		writeD(paperdolls[Inventory.PAPERDOLL_LHAND][3]);
		writeD(paperdolls[Inventory.PAPERDOLL_LRHAND][3]);
		writeD(paperdolls[Inventory.PAPERDOLL_GLOVES][3]);
		writeD(paperdolls[Inventory.PAPERDOLL_CHEST][3]);
		writeD(paperdolls[Inventory.PAPERDOLL_LEGS][3]);
		writeD(paperdolls[Inventory.PAPERDOLL_FEET][3]);
		writeD(paperdolls[Inventory.PAPERDOLL_HAIR][3]);
		writeD(paperdolls[Inventory.PAPERDOLL_DHAIR][3]);

		writeC(pvpFlag);
		writeD(karma);

		writeD(mAtkSpd);
		writeD(pAtkSpd);

		writeD(runSpd);
		writeD(walkSpd);
		writeD(swimRunSpd);
		writeD(swimWalkSpd);
		writeD(flRunSpd);
		writeD(flWalkSpd);
		writeD(flyRunSpd);
		writeD(flyWalkSpd);

		writeCutF(speedMove);
		writeCutF(speedAtack);
		writeCutF(colRadius);
		writeCutF(colHeight);

		writeD(hairStyle);
		writeD(hairColor);
		writeD(face);

		writeString(title);
		writeD(clanId);
		writeD(clanCrestId);
		writeD(allyId);
		writeD(allyCrestId);

		writeC(sit);
		writeC(run);

		writeC(combat);
		writeC(dead);

		writeC(mountType); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		writeC(privateStore);

		writeD(cubics.length);
		for (Cubic cubic : cubics)
			writeH(cubic == null ? 0 : cubic.getId());

		writeC(partyRoomLeader); // find party members
		writeC(flying ? 0x02 : 0x00);
		writeH(recHave);
		writeD(mountId);
		writeD(classId);
		writeD(0); // Foot Effect
		writeC(enchant);
		writeC(teamType.ordinal()); // team circle around feet 1 = Blue, 2 = red

		writeD(largeClanCrestId);

		writeC(noble); // Is Noble
		writeC(hero);

		writeC(fishing);
		writeD(fishLoc.x);
		writeD(fishLoc.y);
		writeD(fishLoc.z);

		writeD(nameColor);
		writeD(heading);

		writeC(pledgeClass);
		writeH(pledgeType);

		writeD(titleColor);

		writeC(0); // Cursed Weapon Level

		writeD(clanRepScore);

		writeD(transformId);
		writeD(agathionId);
		writeC(1); // nPvPRestrainStatus

		writeD(curCp);
		writeD(curHp);
		writeD(maxHp);
		writeD(curMp);
		writeD(maxMp);

		writeC(1); // cBRLectureMark

		writeD(abnormalEffects.size());
		for (AbnormalEffect abnormal : abnormalEffects)
			writeH(abnormal.getId());

		writeC(0);    // Chaos Festival Winner
		writeC(showHeadAccessories);
		writeC(0); //  Used Abilities Points
		writeD(0);    // nCursedWeaponClassId: Меняет имя на название итема (Item ID)
		writeD(-1); // nWaitActionId
		writeD(ranking);
		writeH(0); // hNotoriety
		writeD(classId); // again class id

		writeH(CharInfoType.REALTIME_INFO.getBlockLength() + (2 + (name.length() * 2)));// Realtime parameters block size (2 + (1 x 2) + (4 x 4) + (2 + name size))

		writeC(0); // cCreateOrUpdate
		writeC(0); // cShowSpawnEvent
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(boatId); // nVehicleID
		writeString(name);
	}
}