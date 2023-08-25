package l2s.gameserver.templates.cubic;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.player.Cubic;

/**
 * @author Bonux
 */
public enum CubicTargetType {
	BY_SKILL {
		@Override
		public Creature getTarget(Cubic cubic, CubicSkillInfo skillInfo) {
			throw new UnsupportedOperationException(getClass().getName() + " not implemented BY_SKILL:getTarget(Cubic,Skill)");
		}
	},
	TARGET {
		@Override
		public Creature getTarget(Cubic cubic, CubicSkillInfo skillInfo) {
			Player player = cubic.getOwner();
			Skill skill = skillInfo.getSkill();
			if (skill.isDebuff() && !player.isInCombat())
				return null;

			GameObject object = player.getTarget();
			if (object != null && object.isCreature()) {
				Creature target = (Creature) object;
				if (target.isDead())
					return null;
				if (target.isDoor() && !skillInfo.isCanAttackDoor())
					return null;
				if (skill.isDebuff() && (!target.isInCombat() || !target.isAutoAttackable(cubic.getOwner())))
					return null;
				if (!cubic.canCastSkill(target, skill, false))
					return null;
				return target;
			}
			return null;
		}
	},
	HEAL {
		@Override
		public Creature getTarget(Cubic cubic, CubicSkillInfo skillInfo) {
			Player player = cubic.getOwner();
			Skill skill = skillInfo.getSkill();
			if (player.getParty() == null) {
				if (player.isDead())
					return null;
				if (player.isCurrentHpFull())
					return null;
				if (!cubic.canCastSkill(player, skill, false))
					return null;
				return player;
			} else {
				Creature target = null;
				double currentHp = Integer.MAX_VALUE;
				for (Player member : player.getParty()) {
					if (member == null)
						continue;
					if (member.isDead())
						continue;
					if (member.isCurrentHpFull())
						continue;
					if (member.getCurrentHp() >= currentHp)
						continue;
					if (!cubic.canCastSkill(member, skill, true))
						continue;
					currentHp = member.getCurrentHp();
					target = member;
				}
				return target;
			}
		}
	},
	MANA_HEAL {
		@Override
		public Creature getTarget(Cubic cubic, CubicSkillInfo skillInfo) {
			Player player = cubic.getOwner();
			Skill skill = skillInfo.getSkill();
			if (player.getParty() == null) {
				if (player.isDead())
					return null;
				if (player.isCurrentMpFull())
					return null;
				if (!cubic.canCastSkill(player, skill, false))
					return null;
				return player;
			} else {
				Creature target = null;
				double currentMp = Integer.MAX_VALUE;
				for (Player member : player.getParty().getPartyMembers()) {
					if (member == null)
						continue;
					if (member.isDead())
						continue;
					if (member.isCurrentMpFull())
						continue;
					if (member.getCurrentMp() >= currentMp)
						continue;
					if (!cubic.canCastSkill(member, skill, true))
						continue;
					currentMp = member.getCurrentMp();
					target = member;
				}
				return target;
			}
		}
	},
	MASTER {
		@Override
		public Creature getTarget(Cubic cubic, CubicSkillInfo skillInfo) {
			Player player = cubic.getOwner();
			Skill skill = skillInfo.getSkill();
			if (!cubic.canCastSkill(player, skill, false))
				return null;
			return player;
		}
	};

	public abstract Creature getTarget(Cubic cubic, CubicSkillInfo skillInfo);
}