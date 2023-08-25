package l2s.gameserver.model.actor.instances.player;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;

public class PvpbookInfo {
	private final Pvpbook pvpbook;
	private final int objectId;
	private final int deathTime;

	private String name;
	private int level;
	private int classId;
	private String clanName;
	private boolean revenged;

	protected PvpbookInfo(Pvpbook pvpbook, Player killer, int deathTime) {
		this.pvpbook = pvpbook;
		objectId = killer.getObjectId();
		this.deathTime = deathTime;
		name = killer.getVisibleName(pvpbook.getOwner());
		level = killer.getLevel();
		classId = killer.getClassId().getId();
		Clan clan = killer.getClan();
		clanName = clan != null ? clan.getName() : "";
	}

	protected PvpbookInfo(Pvpbook pvpbook, int objectId, int deathTime, String name, int level, int classId, String clanName) {
		this.pvpbook = pvpbook;
		this.objectId = objectId;
		this.deathTime = deathTime;
		this.name = name;
		this.level = level;
		this.classId = classId;
		this.clanName = clanName;
	}

	public int getObjectId() {
		return objectId;
	}

	public Player getPlayer() {
		return GameObjectsStorage.getPlayer(objectId);
	}

	public int getDeathTime() {
		return deathTime;
	}

	public String getName() {
		Player player = getPlayer();
		if (player != null)
			name = player.getVisibleName(pvpbook.getOwner());
		return name;
	}

	public int getLevel() {
		Player player = getPlayer();
		if (player != null)
			level = player.getLevel();
		return level;
	}

	public int getClassId() {
		Player player = getPlayer();
		if (player != null)
			classId = player.getClassId().getId();
		return classId;
	}

	public String getClanName() {
		Player player = getPlayer();
		if (player != null) {
			Clan clan = player.getClan();
			clanName = clan != null ? clan.getName() : "";
		}
		return clanName;
	}

	public boolean isOnline() {
		Player player = getPlayer();
		if (player != null)
			return player.isOnline();
		return false;
	}

	public boolean isExpired() {
		return Pvpbook.isExpired(deathTime);
	}

	public boolean isRevenged() {
		return revenged;
	}

	public void setRevenged(boolean revenged) {
		this.revenged = revenged;
	}
}
