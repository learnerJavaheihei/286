package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.network.l2.s2c.updatetype.NpcInfoType;
import l2s.gameserver.network.l2.ServerPacketOpcodes;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.skills.AbnormalEffect;

public class NpcInfoPoly extends AbstractMaskPacket<NpcInfoType>
{
	// Flags
	private static final int IS_IN_COMBAT = 1 << 0;
	private static final int IS_ALIKE_DEAD = 1 << 1;
	private static final int IS_TARGETABLE = 1 << 2;
	private static final int IS_SHOW_NAME = 1 << 3;

	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x0C,
		(byte) 0x0C,
		(byte) 0x00,
		(byte) 0x00
	};

	private final int _npcId;
	private final boolean _isAttackable;
	private final int _rHand, _lHand;
	private final String _name, _title;

	private int _initSize = 0;
	private int _blockSize = 0;

	private int _statusMask = 0;

	private int _showSpawnAnimation;
	private int _npcObjId;
	private Location _loc;
	private int _pAtkSpd, _mAtkSpd;
	private double _atkSpdMul, _runSpdMul;
	private int _pvpFlag, _karma;
	private boolean _alive, _running, _flying, _inWater;
	private TeamType _team;
	private int _currentHP, _currentMP;
	private int _maxHP, _maxMP;
	private int _enchantEffect;
	private int _transformId;
	private AbnormalEffect[] _abnormalEffects;
	private int _clanId, _clanCrestId, _largeClanCrestId;
	private int _allyId, _allyCrestId;

	public NpcInfoPoly(Player player, Creature attacker)
	{
		NpcTemplate template = NpcHolder.getInstance().getTemplate(player.getPolyId());
		_npcObjId = player.getObjectId();
		_name = player.getVisibleName(attacker.getPlayer());
		_title = player.getVisibleTitle(attacker.getPlayer());
		_npcId = template.displayId != 0 ? template.displayId : template.getId();
		_isAttackable = player.isAutoAttackable(attacker);
		_loc = player.getLoc();
		_rHand = template.rhand;
		_lHand = template.lhand;
		_mAtkSpd = player.getMAtkSpd();
		_pAtkSpd = player.getPAtkSpd();
		_atkSpdMul = player.getAttackSpeedMultiplier();
		_runSpdMul = player.getMovementSpeedMultiplier();
		_pvpFlag = player.getPvpFlag();
		_karma = player.getKarma();
		_alive = !player.isAlikeDead();
		_running = player.isRunning();
		_flying = player.isFlying();
		_inWater = player.isInWater();
		_team = player.getTeam();
		_currentHP = (int) player.getCurrentHp();
		_currentMP = (int) player.getCurrentMp();
		_maxHP = player.getMaxHp();
		_maxMP = player.getMaxMp();
		_enchantEffect = player.getEnchantEffect();
		_transformId = 0;
		_abnormalEffects = player.getAbnormalEffectsArray();

		for(NpcInfoType component : NpcInfoType.VALUES)
			addComponentType(component);

		if(player.isInCombat())
			_statusMask |= IS_IN_COMBAT;

		if(player.isAlikeDead())
			_statusMask |= IS_ALIKE_DEAD;

		if(player.isTargetable(attacker))
			_statusMask |= IS_TARGETABLE;

		_statusMask |= IS_SHOW_NAME;
	}

	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}

	@Override
	protected void onNewMaskAdded(NpcInfoType component)
	{
		switch(component)
		{
			case ATTACKABLE:
			case UNKNOWN1:
			{
				_initSize += component.getBlockLength();
				break;
			}
			case TITLE:
			{
				_initSize += component.getBlockLength() + (_title.length() * 2);
				break;
			}
			case NAME:
			{
				_blockSize += component.getBlockLength() + (_name.length() * 2);
				break;
			}
			default:
			{
				_blockSize += component.getBlockLength();
				break;
			}
		}
	}

	@Override
	protected ServerPacketOpcodes getOpcodes()
	{
		return ServerPacketOpcodes.NpcInfoPacket;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_npcObjId);
		writeC(0x00); // // 0=teleported 1=default 2=summoned
		writeH(37); // mask_bits_37
		writeB(_masks);
		
		// Block 1
		writeC(_initSize);
		writeC(_isAttackable);
		writeD(0x00); // unknown
		writeS(_title);
		
		// Block 2
		writeH(_blockSize);
		writeD(_npcId + 1000000); // npctype id
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeCutF(_runSpdMul);
		writeCutF(_atkSpdMul);
		writeD(_rHand); // right hand weapon
		writeD(0);
		writeD(_lHand); // left hand weapon
		writeC(_alive);
		writeC(_running);
		writeC(_inWater ? 1 : _flying ? 2 : 0);
		writeC(_team.ordinal());
		writeD(_enchantEffect);
		writeD(_flying);
		writeD(0x00); // Player ObjectId with Decoy
		writeD(0x00); // Unknown
		writeD(0x00);
		writeD(_transformId);
		writeD(_currentHP);
		writeD(_currentMP);
		writeD(_maxHP);
		writeD(_maxMP);
		writeC(0x00);	// тип клона 1 == приманка, 2 = клон у ножа
		writeD(0x00);
		writeD(0x00);
		writeS(_name);
		writeD(-1); // NPCStringId for name
		writeD(-1); // NPCStringId for title
		writeC(_pvpFlag); // PVP flag
		writeD(_karma); // Karma
		writeD(_clanId);
		writeD(_clanCrestId);
		writeD(_largeClanCrestId);
		writeD(_allyId);
		writeD(_allyCrestId);
		writeC(_statusMask);
		writeH(_abnormalEffects.length);
		for(AbnormalEffect abnormal : _abnormalEffects)
			writeH(abnormal.getId());
	}
}