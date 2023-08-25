package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.data.xml.holder.InitialShortCutsHolder;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Macro;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.CharacterCreateFailPacket;
import l2s.gameserver.network.l2.s2c.CharacterCreateSuccessPacket;
import l2s.gameserver.network.l2.s2c.CharacterSelectionInfoPacket;
import l2s.gameserver.templates.item.StartItem;
import l2s.gameserver.templates.player.PlayerTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CharacterCreate extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterCreate.class);

	// cSdddddddddddd
	private String _name;
	private int _sex;
	private int _classId;
	private int _hairStyle;
	private int _hairColor;
	private int _face;

	@Override
	protected boolean readImpl()
	{
		_name = readS();
		readD(); // race
		_sex = readD();
		_classId = readD();
		readD(); // int
		readD(); // str
		readD(); // con
		readD(); // men
		readD(); // dex
		readD(); // wit
		_hairStyle = readD();
		_hairColor = readD();
		_face = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		ClassId cid = ClassId.valueOf(_classId);
		if(cid == null || !cid.isOfLevel(ClassLevel.NONE))
			return;

		if(!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE))
			return;

		if(CharacterDAO.getInstance().getObjectIdByName(_name) > 0)
			return;

		if(Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0 && CharacterDAO.getInstance().accountCharNumber(getClient().getLogin()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT)
			return;

		if(_face > 2 || _face < 0)
		{
			_log.warn("Character Creation Failure: Character face " + _face + " is invalid. Possible client hack. " + getClient());
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);
			return;
		}

		if(_hairStyle < 0 || (_sex == 0 && _hairStyle > 4) || (_sex != 0 && _hairStyle > 6))
		{
			_log.warn("Character Creation Failure: Character hair style " + _hairStyle + " is invalid. Possible client hack. " + getClient());
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);
			return;
		}

		if(_hairColor > 3 || _hairColor < 0)
		{
			_log.warn("Character Creation Failure: Character hair color " + _hairColor + " is invalid. Possible client hack. " + getClient());
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);
			return;
		}

		Player newChar = Player.create(_classId, _sex, getClient().getLogin(), _name, _hairStyle, _hairColor, _face);
		if(newChar == null)
		{
			_log.warn("Character Creation Failure: Player.create returned null. Possible client hack. " + getClient());
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);
			return;
		}

		if(!initNewChar(newChar))
		{
			_log.warn("Character Creation Failure: Could not init new char. Possible client hack. " + getClient());
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);
			return;
		}

		newChar.storeLastIpAndHWID(getClient().getIpAddr(), getClient().getHWID());
			
		sendPacket(CharacterCreateSuccessPacket.STATIC);

		getClient().setCharSelection(CharacterSelectionInfoPacket.loadCharacterSelectInfo(getClient().getLogin()));
	}

	public static boolean initNewChar(Player newChar)
	{
		if(!newChar.getSubClassList().restore())
			return false;

		PlayerTemplate template = newChar.getTemplate();
		newChar.setLoc(template.getStartLocation());

		if(Config.CHAR_TITLE)
			newChar.setTitle(Config.ADD_CHAR_TITLE);
		else
			newChar.setTitle("");

		newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());
		newChar.setCurrentCp(0); // retail

		for(StartItem i : template.getStartItems())
		{
			ItemInstance item = ItemFunctions.createItem(i.getId());
			if(i.getEnchantLevel() > 0)
				item.setEnchantLevel(i.getEnchantLevel());

			long count = i.getCount();
			if(item.isStackable())
			{
				item.setCount(count);
				newChar.getInventory().addItem(item);
			}
			else
			{
				for(long n = 0; n < count; n++)
				{
					item = ItemFunctions.createItem(i.getId());
					if(i.getEnchantLevel() > 0)
						item.setEnchantLevel(i.getEnchantLevel());
					newChar.getInventory().addItem(item);
				}
				if(item.isEquipable() && i.isEquiped())
					newChar.getInventory().equipItem(item);
			}
		}

		for(ListenerHook hook : ListenerHook.getGlobalListenerHooks(ListenerHookType.PLAYER_CREATE))
			hook.onPlayerCreate(newChar);

		newChar.rewardSkills(false, false, false, true);

		Map<Integer, Integer> initedMacroses = new HashMap<>();

		for(Macro macro : InitialShortCutsHolder.getInstance().getInitialMacroses()) {
			if(!macro.isEnabled())
				continue;

			if(newChar.getMacroses().getAllMacroses().length > 48)
			{
				_log.warn("Character Initial Macro Failure: Cannot register more than 48 macros.");
				break;
			}

			Macro newMacro = new Macro(0, macro.getIcon(), macro.getName(), macro.getDescr(), macro.getAcronym(), macro.getCommands());
			newChar.registerMacro(newMacro);
			initedMacroses.put(macro.getId(), newMacro.getId());
		}

		for(ShortCut shortCut : InitialShortCutsHolder.getInstance().getInitialShortCuts(newChar.getRace(), newChar.getClassId().getType())) {
			if(shortCut.getType() == ShortCut.ShortCutType.MACRO) {
				Integer initedMacroId = initedMacroses.get(shortCut.getId());
				if(initedMacroId != null)
					newChar.registerShortCut(new ShortCut(shortCut.getSlot(), shortCut.getPage(), shortCut.getType(), initedMacroId, 0, 1));
				continue;
			}
			newChar.registerShortCut(shortCut);
		}

		newChar.checkLevelUpReward(true);

		newChar.setOnlineStatus(false);

		newChar.store(false);
		newChar.getInventory().store();
		newChar.deleteMe();
		return true;
	}
}