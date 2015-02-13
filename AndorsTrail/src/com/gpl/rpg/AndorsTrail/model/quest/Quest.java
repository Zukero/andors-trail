package com.gpl.rpg.AndorsTrail.model.quest;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.util.L;

public final class Quest implements Comparable<Quest> {
	public final String questID;
	public final String name;
	public final QuestLogEntry[] stages; //Must be sorted in ascending stage order
	public final boolean showInLog;
	public final int sortOrder;
    public final boolean repeatable;

	public Quest(
			String questID
			, String name
			, QuestLogEntry[] stages
			, boolean showInLog
			, int sortOrder
            , boolean repeatable
	) {
		this.questID = questID;
		this.name = name;
		this.stages = stages;
		this.showInLog = showInLog;
		this.sortOrder = sortOrder;
        this.repeatable = repeatable;
	}

	public QuestLogEntry getQuestLogEntry(final int progress) {
		for (QuestLogEntry s : stages) {
			if (s.progress == progress) return s;
		}
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			L.log("WARNING: Cannot find stage " + progress + " in quest \"" + questID + "\".");
		}
		return null;
	}

	public boolean isCompleted(final Player player) {
		for (QuestLogEntry e : stages) {
			if (!e.finishesQuest) continue;
			if (player.hasExactQuestProgress(questID, e.progress)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int compareTo(Quest q) {
		return sortOrder - q.sortOrder;
	}
}
