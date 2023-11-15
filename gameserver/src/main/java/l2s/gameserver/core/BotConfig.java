package l2s.gameserver.core;

import l2s.gameserver.model.Player;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

public abstract class BotConfig
{
	private final LinkedList<BotSkillStrategy> _attackStrategy = new LinkedList<BotSkillStrategy>();
	private int _findMobMaxDistance = 4500;
	private int _findMobMaxHeight = 500;
	private boolean _followMove = false;
	private boolean _followAttack = false;
	private boolean _autoAttack = true;
	private boolean _usePhysicalAttack = true;
	private boolean _pickUpItem = true;
	private boolean _pickUpFirst = false;
	private boolean _autoSweep = false;
	private boolean _absorbBody = false;
	private boolean _hpmpShift = false;
	private boolean _acceptRes = false;
	private int _mpProtected = 0;
	private int _hpProtected = 0;
	private boolean _summonAttack = false;
	private int _summonSkillId = 0;
	private Geometry _geometry = Geometry.CIRCLE;
	private int _startX;
	private int _startY;
	private int _startZ;
	private int _potionHpHeal = 0;
	private int _selfHpHeal = 0;
	private int _potionMpHeal = 0;
	private int _partyHpHeal = 0;
	private final Map<Integer, Integer> _partyMpHeal = new HashMap<Integer, Integer>(7);
	private int _hpPotionId = 0;
	private int _mpPotionId = 0;
	private int _healSkill1 = 0;
	private int _healSkill2 = 0;
	private int _petHpHeal = 0;
	private int _healSkill3 = 0;
	private boolean _antidote;
	private boolean _bondage;
	private boolean _partyAntidote;
	private boolean _partyBondage;
	private boolean _partyParalysis;
	private boolean _followRest = false;
	private boolean _idleRest = false;
	private boolean _useRes = false;
	private final Set<Integer> _petBuffs = new HashSet<Integer>();
	private final List<Integer> _autoItemBuffs = new ArrayList<Integer>();
	private final LinkedList<BotResType> _resType = new LinkedList<BotResType>(Arrays.asList(BotResType.values()));
	private final Polygon _polygon = new Polygon();
	private int _keepMp = 35;
	private BotPetOwnerIdleAction _bpoidleAction = BotPetOwnerIdleAction.\u539f\u5730\u4e0d\u52a8;
	private transient boolean _abort = true;
	private transient int _targetConfirmTimeConsuming = 0;
	private transient String _abortReason = "";
	private final transient Set<Integer> _phantomItem = new HashSet<Integer>();
	private transient boolean _existInDb = false;
	private transient int _currentTargetObjectId;
	private transient int _lastSpoilTargeId;
	private transient int _tryTimes;
	private transient int _deathTime;
	private transient Map<Integer, Long> _blockTarget = new HashMap<Integer, Long>();

	// 放花攻击
	private boolean _autoSpoiledAttack = false;
	// 跟随距离
	private int followInstance = 200;


	public int getThpMin() {
		return thpMin;
	}

	public void setThpMin(int thpMin) {
		this.thpMin = thpMin;
	}

	public int getThpMax() {
		return thpMax;
	}

	public void setThpMax(int thpMax) {
		this.thpMax = thpMax;
	}

	public int getMhpMin() {
		return mhpMin;
	}

	public void setMhpMin(int mhpMin) {
		this.mhpMin = mhpMin;
	}

	public int getMhpMax() {
		return mhpMax;
	}

	public void setMhpMax(int mhpMax) {
		this.mhpMax = mhpMax;
	}

	public int getMmpMin() {
		return mmpMin;
	}

	public void setMmpMin(int mmpMin) {
		this.mmpMin = mmpMin;
	}

	public int getMmpMax() {
		return mmpMax;
	}

	public void setMmpMax(int mmpMax) {
		this.mmpMax = mmpMax;
	}

	public String getUseStrategy() {
		return useStrategy;
	}

	public void setUseStrategy(String useStrategy) {
		this.useStrategy = useStrategy;
	}


	private String useStrategy = "只用一次";
	public static String autoDetection = "自动检测";
	// 技能策略百分比
	private int thpMin =0;
	private int thpMax =100;
	private int mhpMin =0;
	private int mhpMax =0;
	private int mmpMin =0;
	private int mmpMax =0;

	//每日最大购买数
	public final static int maxBuyTimes =7;

	// 是否对目标使用过复活券 在 目标使用后设置为 false
	private Map<Integer,Boolean> isUsedReviveOwner_target = new HashMap<Integer, Boolean>();

	public Map<Integer, Boolean> getIsUsedReviveOwner_target() {
		return isUsedReviveOwner_target;
	}

	public int getFollowInstance() {
		return followInstance;
	}

	public void setFollowInstance(int followInstance) {
		this.followInstance = followInstance;
	}

	public int getFindMobMaxDistance()
	{
		return _findMobMaxDistance;
	}

	public void setFindMobMaxDistance(int findMobMaxDistance)
	{
		_findMobMaxDistance = findMobMaxDistance;
	}

	public int getFindMobMaxHeight()
	{
		return _findMobMaxHeight;
	}

	public void setFindMobMaxHeight(int findMobMaxHeight)
	{
		_findMobMaxHeight = findMobMaxHeight;
	}

	public LinkedList<BotSkillStrategy> getAttackStrategy()
	{
		return _attackStrategy;
	}

	public boolean isAbort()
	{
		return _abort;
	}

	public void setAbort(boolean abort, String reason)
	{
		_abort = abort;
		_abortReason = reason;
	}
	public boolean is_autoSpoiledAttack() {
		return _autoSpoiledAttack;
	}

	public void set_autoSpoiledAttack(boolean _autoSpoiledAttack) {
		this._autoSpoiledAttack = _autoSpoiledAttack;
	}
	public String getAbortReason()
	{
		return _abortReason;
	}

	public boolean isUsePhysicalAttack()
	{
		return _usePhysicalAttack;
	}

	public void setUsePhysicalAttack(boolean usePhysicalAttack)
	{
		_usePhysicalAttack = usePhysicalAttack;
	}

	public boolean isPickUpItem()
	{
		return _pickUpItem;
	}

	public void setPickUpItem(boolean pickUpItem)
	{
		_pickUpItem = pickUpItem;
	}

	public boolean isAutoAttack()
	{
		return _autoAttack;
	}

	public void setAutoAttack(boolean autoAttack)
	{
		_autoAttack = autoAttack;
	}

	public boolean isAutoSweep()
	{
		return _autoSweep;
	}

	public void setAutoSweep(boolean autoSweep)
	{
		_autoSweep = autoSweep;
	}

	public boolean isAbsorbBody()
	{
		return _absorbBody;
	}

	public void setAbsorbBody(boolean absorbBody)
	{
		_absorbBody = absorbBody;
	}

	public boolean isAcceptRes()
	{
		return _acceptRes;
	}

	public void setAcceptRes(boolean acceptRes)
	{
		_acceptRes = acceptRes;
	}

	public boolean isHpmpShift()
	{
		return _hpmpShift;
	}

	public void setHpmpShift(boolean hpmpShift)
	{
		_hpmpShift = hpmpShift;
	}

	public int getTargetConfirmTimeConsuming()
	{
		return _targetConfirmTimeConsuming;
	}

	public void setTargetConfirmTimeConsuming(int targetConfirmTimeConsuming)
	{
		_targetConfirmTimeConsuming = targetConfirmTimeConsuming;
	}

	public boolean isExistInDb()
	{
		return _existInDb;
	}

	public void setExistInDb(boolean existInDb)
	{
		_existInDb = existInDb;
	}

	public int getMpProtected()
	{
		return _mpProtected;
	}

	public void setMpProtected(int mpProtected)
	{
		if(_idleRest)
		{
			return;
		}
		_mpProtected = mpProtected;
	}

	public boolean isFollowMove()
	{
		return _followMove;
	}

	public void setFollowMove(boolean followMove)
	{
		_followMove = followMove;
	}

	public boolean isFollowAttack()
	{
		return _followAttack;
	}

	public void setFollowAttack(boolean followAttack)
	{
		_followAttack = followAttack;
	}

	public int getHpProtected()
	{
		return _hpProtected;
	}

	public void setHpProtected(int hpProtected)
	{
		if(_idleRest)
		{
			return;
		}
		_hpProtected = hpProtected;
	}

	public Geometry getGeometry()
	{
		return _geometry;
	}

	public void setGeometry(Geometry geometry)
	{
		_geometry = geometry;
	}

	public int getStartX()
	{
		return _startX;
	}

	public void setStartX(int startX)
	{
		_startX = startX;
	}

	public int getStartY()
	{
		return _startY;
	}

	public void setStartY(int startY)
	{
		_startY = startY;
	}

	public int getStartZ()
	{
		return _startZ;
	}

	public void setStartZ(int startZ)
	{
		_startZ = startZ;
	}

	public int getPotionHpHeal()
	{
		return _potionHpHeal;
	}

	public void setPotionHpHeal(int potionHpHeal)
	{
		_potionHpHeal = potionHpHeal;
	}

	public int getSelfHpHeal()
	{
		return _selfHpHeal;
	}

	public void setSelfHpHeal(int selfHpHeal)
	{
		_selfHpHeal = selfHpHeal;
	}

	public int getPartyHpHeal()
	{
		return _partyHpHeal;
	}

	public void setPartyHpHeal(int partyHpHeal)
	{
		_partyHpHeal = partyHpHeal;
	}

	public int getHpPotionId()
	{
		return _hpPotionId;
	}

	public void setHpPotionId(int potionId)
	{
		_hpPotionId = potionId;
	}

	public int getHealSkill1()
	{
		return _healSkill1;
	}

	public void setHealSkill1(int healSkill1)
	{
		_healSkill1 = healSkill1;
	}

	public int getHealSkill2()
	{
		return _healSkill2;
	}

	public void setHealSkill2(int healSkill2)
	{
		_healSkill2 = healSkill2;
	}

	public Set<Integer> getPhantomItem()
	{
		return _phantomItem;
	}

	public int getSummonSkillId()
	{
		return _summonSkillId;
	}

	public void setSummonSkillId(int summSkillId)
	{
		_summonSkillId = summSkillId;
	}

	public boolean isSummonAttack()
	{
		return _summonAttack;
	}

	public void setSummonAttack(boolean summonAttack)
	{
		_summonAttack = summonAttack;
	}

	public int getPetHpHeal()
	{
		return _petHpHeal;
	}

	public void setPetHpHeal(int petHpHeal)
	{
		_petHpHeal = petHpHeal;
	}

	public int getHealSkill3()
	{
		return _healSkill3;
	}

	public void setHealSkill3(int healSkill3)
	{
		_healSkill3 = healSkill3;
	}

	public int getCurrentTargetObjectId()
	{
		return _currentTargetObjectId;
	}

	public void setCurrentTargetObjectId(int currentTargetObjectId)
	{
		_currentTargetObjectId = currentTargetObjectId;
	}

	public int getTryTimes()
	{
		return _tryTimes;
	}

	public void setTryTimes(int tryTimes)
	{
		_tryTimes = tryTimes;
	}

	public void addBlockTargetId(int objectId)
	{
		_blockTarget.put(objectId, System.currentTimeMillis() + 60000L);
	}

	public Map<Integer, Long> getBlockTarget()
	{
		return _blockTarget;
	}

	public int getLastSpoilTargeId()
	{
		return _lastSpoilTargeId;
	}

	public void setLastSpoilTargeId(int lastSpoilTargeId)
	{
		_lastSpoilTargeId = lastSpoilTargeId;
	}

	public List<Integer> getAutoItemBuffs()
	{
		return _autoItemBuffs;
	}

	public int getDeathTime()
	{
		return _deathTime;
	}

	public void setDeathTime(int deathTime)
	{
		_deathTime = deathTime;
	}

	public boolean isFollowRest()
	{
		return _followRest;
	}

	public void setFollowRest(boolean followRest)
	{
		if(_idleRest)
		{
			return;
		}
		_followRest = followRest;
	}

	public Polygon getPolygon()
	{
		return _polygon;
	}

	public int getPotionMpHeal()
	{
		return _potionMpHeal;
	}

	public void setPotionMpHeal(int potionMpHeal)
	{
		_potionMpHeal = potionMpHeal;
	}

	public int getMpPotionId()
	{
		return _mpPotionId;
	}

	public void setMpPotionId(int mpPotionId)
	{
		_mpPotionId = mpPotionId;
	}

	public Map<Integer, Integer> getPartyMpHeal()
	{
		return _partyMpHeal;
	}

	public Set<Integer> getPetBuffs()
	{
		return _petBuffs;
	}

	public void parseMpHeal(String line)
	{
		if(line.isEmpty())
		{
			return;
		}
		String[] tmp = line.split(";");
		for(int i = 0; i < tmp.length; ++i)
		{
			String[] id_value = tmp[i].split(",");
			_partyMpHeal.put(Integer.parseInt(id_value[0]), Integer.parseInt(id_value[1]));
		}
	}

	public static String itemIds(BotConfig config)
	{
		StringJoiner j = new StringJoiner(",");
		config._autoItemBuffs.forEach(id -> {
			StringJoiner stringJoiner2 = j.add(String.valueOf(id));
		});
		return j.toString();
	}

	public static void parseItemIds(BotConfig config, String line)
	{
		if(line == null || line.isEmpty())
		{
			return;
		}
		Stream.of(line.split(",")).map(Integer::parseInt).forEach(config._autoItemBuffs::add);
	}

	public void releaseMemory(Player player)
	{
		if(_blockTarget.isEmpty())
		{
			return;
		}
		ArrayList<Integer> ids = new ArrayList<Integer>();
		long now = System.currentTimeMillis();
		_blockTarget.forEach((id, time) -> {
			if(time <= now)
			{
				ids.add(id);
			}
		});
		ids.forEach(_blockTarget::remove);
		ids.clear();
	}

	public String getPolygonString()
	{
		if(getPolygon().npoints == 0)
		{
			return "";
		}
		StringJoiner joiner = new StringJoiner(";");
		for(int i = 0; i < getPolygon().npoints; ++i)
		{
			joiner.add(String.valueOf(_polygon.xpoints[i]) + "," + _polygon.ypoints[i]);
		}
		return joiner.toString();
	}

	public void parsePolygon(String line)
	{
		if(line.isEmpty())
		{
			return;
		}
		String[] tmp = line.split(";");
		for(int i = 0; i < tmp.length; ++i)
		{
			String[] xy = tmp[i].split(",");
			_polygon.addPoint(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
		}
	}

	public String getPartyMpHealString()
	{
		if(_partyMpHeal.isEmpty())
		{
			return "";
		}
		StringJoiner joiner = new StringJoiner(";");
		_partyMpHeal.forEach((id, value) -> {
			StringJoiner stringJoiner2 = joiner.add(id + "," + value);
		});
		return joiner.toString();
	}

	public boolean isIdleRest()
	{
		return _idleRest;
	}

	public void setIdleRest(boolean idleRest)
	{
		_idleRest = idleRest;
		if(_idleRest)
		{
			_followRest = false;
			_hpProtected = 0;
			_mpProtected = 0;
		}
	}

	public boolean isAntidote()
	{
		return _antidote;
	}

	public void setAntidote(boolean antidote)
	{
		_antidote = antidote;
	}

	public boolean isBondage()
	{
		return _bondage;
	}

	public void setBondage(boolean bondage)
	{
		_bondage = bondage;
	}

	public boolean isPartyBondage()
	{
		return _partyBondage;
	}

	public void setPartyBondage(boolean partyBondage)
	{
		_partyBondage = partyBondage;
	}

	public boolean isPartyParalysis()
	{
		return _partyParalysis;
	}

	public void setPartyParalysis(boolean partyParalysis)
	{
		_partyParalysis = partyParalysis;
	}

	public boolean isPartyAntidote()
	{
		return _partyAntidote;
	}

	public void setPartyAntidote(boolean partyAntidote)
	{
		_partyAntidote = partyAntidote;
	}

	public boolean isPickUpFirst()
	{
		return _pickUpFirst;
	}

	public void setPickUpFirst(boolean pickUpFirst)
	{
		_pickUpFirst = pickUpFirst;
	}

	public LinkedList<BotResType> getResType()
	{
		return _resType;
	}

	public void setUseRes(boolean useRes)
	{
		_useRes = useRes;
	}

	public boolean isUseRes()
	{
		return _useRes;
	}

	public int getKeepMp()
	{
		return _keepMp;
	}

	public void setKeepMp(int keepMp)
	{
		_keepMp = keepMp;
	}

	public BotPetOwnerIdleAction getBpoidleAction()
	{
		return _bpoidleAction;
	}

	public void setBpoidleAction(BotPetOwnerIdleAction bpoidleAction)
	{
		_bpoidleAction = bpoidleAction;
	}
}