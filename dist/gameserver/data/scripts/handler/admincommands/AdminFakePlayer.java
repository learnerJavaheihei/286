package handler.admincommands;

import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.data.xml.holder.FakePlayersHolder;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.*;
import l2s.gameserver.tables.FakePlayersTable;

public class AdminFakePlayer extends ScriptAdminCommand {
	enum Commands {
		admin_fp_spawn
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar) {
		Commands command = (Commands) comm;
		switch (command) {
			case admin_fp_spawn: {
				if (GameObjectsStorage.getFakePlayers().size() >= FakePlayersTable.getFakePlayersLimit()) {
					activeChar.sendMessage("You have already reached the limit of fake players.");
					return true;
				}

				try {
					String name = wordList[4];
					if (CharacterDAO.getInstance().getObjectIdByName(name) > 0) {
						activeChar.sendMessage("Fake player NOT spawned! This character name is already taken.");
						return true;
					}

					Race race = Race.VALUES[Integer.parseInt(wordList[1])];
					ClassType classType = ClassType.VALUES[Integer.parseInt(wordList[2])];
					Sex sex = Sex.VALUES[Integer.parseInt(wordList[3])];
					for (ClassId c : ClassId.VALUES) {
						if (c.isOfLevel(ClassLevel.NONE) && c.getRace() == race && c.getType() == classType) {
							if (FakePlayersHolder.getInstance().getAITemplate(c.getRace(), c.getType()) != null) {
								FakePlayersTable.spawnFakePlayer(name, c, sex);
								activeChar.sendMessage("Fake player success spawn! Fake player NAME[" + name + "] CLASS_ID[" + c + "] SEX[" + sex + "].");
								return true;
							}
						}
					}
					activeChar.sendMessage("Fake player NOT spawned! Not found fake player template for RACE[" + race + "] CLASS_TYPE[" + classType + "] SEX[" + sex + "].");
				} catch (Exception e) {
					activeChar.sendMessage("USAGE: //fp_spawn RACE[0-" + (Race.VALUES.length - 1) + "] CLASS_TYPE[0-1] SEX[0-1] NAME");
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum() {
		return Commands.values();
	}
}
