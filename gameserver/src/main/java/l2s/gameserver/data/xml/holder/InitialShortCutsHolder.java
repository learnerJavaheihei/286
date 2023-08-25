package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.actor.instances.player.Macro;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.model.base.ClassType;
import l2s.gameserver.model.base.Race;

import java.util.*;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 15.05.2019
 * Developed for L2-Scripts.com
 **/
public final class InitialShortCutsHolder extends AbstractHolder {
	private static final InitialShortCutsHolder INSTANCE = new InitialShortCutsHolder();

	public static InitialShortCutsHolder getInstance() {
		return INSTANCE;
	}

	private static int makeHashCode(Race race, ClassType type) {
		return (race.ordinal() * 100) + type.ordinal();
	}

	private final Map<Integer, List<ShortCut>> initialShortCuts = new HashMap<>();
	private final Map<Integer, Macro> initialMacroses = new HashMap<>();

	public void addInitialShortCut(Race race, ClassType classType, ShortCut shortCut) {
		if (race == null) {
			for (Race r : Race.VALUES) {
				if (classType == null) {
					for (ClassType ct : ClassType.VALUES)
						addInitialShortCut0(r, ct, shortCut);
				} else
					addInitialShortCut0(r, classType, shortCut);
			}
		} else
			addInitialShortCut0(race, classType, shortCut);
	}

	private void addInitialShortCut0(Race race, ClassType classType, ShortCut shortCut) {
		List<ShortCut> shortCuts = initialShortCuts.computeIfAbsent(makeHashCode(race, classType), (m) -> new ArrayList<>());
		shortCuts.add(shortCut);
	}

	public List<ShortCut> getInitialShortCuts(Race race, ClassType classType) {
		List<ShortCut> shortCuts = initialShortCuts.get(makeHashCode(race, classType));
		if (shortCuts == null)
			return Collections.emptyList();
		return shortCuts;
	}

	public void addInitialMacro(Macro macro) {
		initialMacroses.put(macro.getId(), macro);
	}

	public Collection<Macro> getInitialMacroses() {
		return initialMacroses.values();
	}

	@Override
	public void log() {
		Set<ShortCut> shortCuts = new HashSet<>();
		for(Collection<ShortCut> shortCutList : initialShortCuts.values()) {
			shortCuts.addAll(shortCutList);
		}
		info(String.format("loaded %d initial short cut(s) count.", shortCuts.size()));
		info(String.format("loaded %d initial macro(s) count.", initialMacroses.size()));
	}

	@Override
	public int size() {
		return initialShortCuts.size() + initialMacroses.size();
	}

	@Override
	public void clear() {
		initialShortCuts.clear();
		initialMacroses.clear();
	}
}
