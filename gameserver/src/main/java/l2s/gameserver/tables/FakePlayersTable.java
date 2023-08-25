package l2s.gameserver.tables;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.data.xml.holder.FakePlayersHolder;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.network.l2.c2s.CharacterCreate;
import l2s.gameserver.network.l2.c2s.EnterWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FakePlayersTable
{
	private final static int CLIENTS_HASHCODE;

	private final static Map<String, Integer> CLIENTS = new HashMap<String, Integer>();
	static
	{
		CLIENTS.put(new String(new byte[]{ 0x31, 0x30, 0x39, 0x2E, 0x38, 0x36, 0x2E, 0x31, 0x30, 0x36, 0x2E, 0x31, 0x35, 0x33 }), -1); // Bonux server
		CLIENTS.put(new String(new byte[]{ 0x39, 0x34, 0x2E, 0x31, 0x39, 0x38, 0x2E, 0x31, 0x30, 0x39, 0x2E, 0x31, 0x36, 0x36 }), -1);	// Yuri test server
		CLIENTS.put(new String(new byte[]{ 0x35, 0x34, 0x2E, 0x33, 0x38, 0x2E, 0x31, 0x30, 0x33, 0x2E, 0x31, 0x36, 0x35 }), -1);	// 	s500
		CLIENTS.put(new String(new byte[]{ 0x35, 0x34, 0x2E, 0x33, 0x38, 0x2E, 0x31, 0x30, 0x33, 0x2E, 0x31, 0x36, 0x37 }), -1);	// 	s500
		//CLIENTS.put(new String(new byte[]{ 0x31, 0x30, 0x39, 0x2E, 0x38, 0x37, 0x2E, 0x32, 0x30, 0x31, 0x2E, 0x32, 0x35, 0x30 }), -1); // virusoflove
		CLIENTS.put(new String(new byte[]{ 0x39, 0x33, 0x2E, 0x37, 0x38, 0x2E, 0x32, 0x30, 0x38, 0x2E, 0x32, 0x30, 0x35 }), -1); //L2-scripts.com - (SanyaDC) (разработчик)
		CLIENTS.put(new String(new byte[]{ 0x31, 0x34, 0x35, 0x2E, 0x32, 0x33, 0x39, 0x2E, 0x31, 0x33, 0x30, 0x2E, 0x32, 0x33 }), -1);	// maximumofanger
		//CLIENTS.put(new String(new byte[]{ 0x35, 0x2E, 0x33, 0x39, 0x2E, 0x32, 0x32, 0x31, 0x2E, 0x36, 0x33 }), 100);	// 	scorpius
		CLIENTS_HASHCODE = CLIENTS.hashCode();
	}

	public static class Task implements Runnable
	{
		public void run()
		{
			try
			{
				if(_activeFakePlayers.size() < GameObjectsStorage.getPlayers(true, false).size() * Config.FAKE_PLAYERS_PERCENT / 100 && _activeFakePlayers.size() < _fakePlayerNames.size())
				{
					if(Rnd.chance(10))
					{
						String player = Rnd.get(_fakePlayerNames);
						if(player != null && !_activeFakePlayers.contains(player))
							_activeFakePlayers.add(player);
					}
				}
				else if(_activeFakePlayers.size() > 0)
					_activeFakePlayers.remove(Rnd.get(_activeFakePlayers.size()));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private static class SpawnFakePlayersTask implements Runnable {
		private int waitDelay = 0;

		@Override
		public void run() {
			waitDelay--;

			if(waitDelay > 0)
				return;

			int fakePlayersCount = Math.min(Config.FAKE_PLAYERS_COUNT, getFakePlayersLimit());
			if(GameObjectsStorage.getFakePlayers().size() >= fakePlayersCount) {
				return;
			}

			List<Integer> restoredPlayers = CharacterDAO.getInstance().getPlayersIdByAccount("#fake_account");
			restoredPlayers = restoredPlayers.stream().filter(id -> GameObjectsStorage.getPlayer(id) == null).collect(Collectors.toList());

			List<ClassId> classes = new ArrayList<>();
			List<String> names = new ArrayList<>(getFakePlayerNames());
			for(ClassId c : ClassId.VALUES) {
				if(c.isOfLevel(ClassLevel.NONE) && FakePlayersHolder.getInstance().getAITemplate(c.getRace(), c.getType()) != null)
					classes.add(c);
			}

			boolean canRestore = !restoredPlayers.isEmpty();
			boolean canSpawnNew = !classes.isEmpty() && !names.isEmpty();

			if (canRestore) {
				if (!canSpawnNew || Rnd.chance(70)) {
					Integer objectId = Rnd.get(restoredPlayers);
					if (objectId != null && restoredPlayers.remove(objectId)) {
						Player player = GameObjectsStorage.getPlayer(objectId);
						if(player == null) {
							player = Player.restore(objectId, true);
							if (player != null) {
								EnterWorld.onEnterWorld(player);
								waitDelay = Rnd.get(1, 3);
								return;
							}
						}
					}
				}
			}

			if (canSpawnNew) {
				String name = Rnd.get(names);
				while (CharacterDAO.getInstance().getObjectIdByName(name) > 0) {
					names.remove(name);
					if (names.isEmpty()) {
						return;
					}
					name = Rnd.get(names);
				}
				names.remove(name);

				ClassId classId = Rnd.get(classes);
				if (classId == null)
					return;

				Sex sex = Rnd.get(Sex.VALUES);
				if (sex == null)
					return;

				if (spawnFakePlayer(name, classId, sex)) {
					waitDelay = Rnd.get(3, 9);
				}
			}
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(FakePlayersTable.class);

	private static final List<String> _fakePlayerNames = new ArrayList<String>();
	private static final List<String> _activeFakePlayers = new ArrayList<String>();

	private static final FakePlayersTable _instance = new FakePlayersTable();

	private static ScheduledFuture<?> spawnFakePlayersTask = null;

	public static FakePlayersTable getInstance()
	{
		return _instance;
	}

	private FakePlayersTable() {
		parseData();
	}

	public void init() {
		if(!Config.ALLOW_FAKE_PLAYERS && Config.FAKE_PLAYERS_COUNT <= 0)
			return;

		if (Config.FAKE_PLAYERS_COUNT <= 0)
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new Task(), 180000, 1000);
		else {
			int fakePlayersCount = Math.min(Config.FAKE_PLAYERS_COUNT, getFakePlayersLimit());
			if (fakePlayersCount <= 0) {
				if (Config.ALLOW_FAKE_PLAYERS)
					ThreadPoolManager.getInstance().scheduleAtFixedRate(new Task(), 180000, 1000);
				return;
			}
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new SpawnFakePlayersTask(), TimeUnit.SECONDS.toMillis(Config.FAKE_PLAYERS_SPAWN_TASK_DELAY), TimeUnit.SECONDS.toMillis(Config.FAKE_PLAYERS_SPAWN_TASK_DELAY));
		}
	}

	private static void stopSpawnFakePlayersTask() {
		if(spawnFakePlayersTask != null) {
			spawnFakePlayersTask.cancel(false);
			spawnFakePlayersTask = null;
		}
	}

	public static boolean spawnFakePlayer(String name, ClassId classId, Sex sex) {
		Player player = Player.create(classId.getId(), sex.ordinal(), "#fake_account", name, Rnd.get(3), Rnd.get(3), Rnd.get(3));
		if(player == null)
			return false;

		CharacterCreate.initNewChar(player);

		player = Player.restore(player.getObjectId(), true);
		if(player == null)
			return false;

		EnterWorld.onEnterWorld(player);
		return true;
	}

	private static void parseData()
	{
		if(!Config.ALLOW_FAKE_PLAYERS && Config.FAKE_PLAYERS_COUNT <= 0)
			return;

		LineNumberReader lnr = null;
		try {
			File doorData = new File(Config.FAKE_PLAYERS_LIST);
			lnr = new LineNumberReader(new BufferedReader(new FileReader(doorData)));
			String line;
			while((line = lnr.readLine()) != null) {
				if(line.trim().length() == 0 || line.startsWith("#"))
					continue;
				_fakePlayerNames.add(line);
			}
			_log.info("FakePlayersTable: Loaded " + _fakePlayerNames.size() + " fake player names.");
		} catch(Exception e) {
			_log.warn("FakePlayersTable: Lists could not be initialized.");
			e.printStackTrace();
		} finally {
			try {
				if(lnr != null)
					lnr.close();
			} catch(Exception e1) {
				//
			}
		}
	}

	public static List<String> getFakePlayerNames()
	{
		return _fakePlayerNames;
	}

	public static int getActiveFakePlayersCount()
	{
		return _activeFakePlayers.size();
	}

	public static List<String> getActiveFakePlayers()
	{
		return _activeFakePlayers;
	}

	public static int getFakePlayersLimit() {
		if(!GameServer.DEVELOP)
		{
			Integer fakesLimit = CLIENTS.get(GameServer.getInstance().getLicenseHost());
			if(CLIENTS_HASHCODE == CLIENTS.hashCode() && fakesLimit != null && fakesLimit != 0)
				return fakesLimit == -1 ? 5000 : fakesLimit;
			return 10;
		}
		return 5000;
	}
}