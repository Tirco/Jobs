package com.gamingmesh.jobs.container;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.event.server.ServerCommandEvent;

import com.gamingmesh.jobs.Jobs;

public class QuestProgression {

    private Quest quest;

    private long validUntil;
    private boolean givenReward = false;

    private final Map<QuestObjective, Integer> done = new HashMap<>();

    public QuestProgression(Quest quest) {
	this.quest = quest;

	validUntil = quest.getValidUntil();
    }

    public Quest getQuest() {
	return (quest == null || quest.getJob() == null) ? null : quest.getJob().getQuest(quest.getConfigName());
    }

    public void setQuest(Quest quest) {
	this.quest = quest;
    }

    public int getTotalAmountNeeded() {
	int amountNeeded = 0;
	for (Map<String, QuestObjective> oneA : quest.getObjectives().values()) {
	    for (QuestObjective one : oneA.values()) {
		amountNeeded += one.getAmount();
	    }
	}
	return amountNeeded;
    }

    public int getTotalAmountDone() {
	int amountDone = 0;
	for (Integer one : done.values()) {
	    amountDone += one;
	}

	return amountDone;
    }

    public int getAmountDone(QuestObjective objective) {
	return done.getOrDefault(objective, 0);
    }

    public void setAmountDone(QuestObjective objective, int amountDone) {
	if (quest.hasObjective(objective)) {
	    done.put(objective, amountDone);
	}
    }

    public Long getValidUntil() {
	return validUntil;
    }

    public void setValidUntil(Long validUntil) {
	this.validUntil = validUntil;
    }

    public boolean isValid() {
	return validUntil == getValidUntil();
    }

    public boolean isEnded() {
	return validUntil < System.currentTimeMillis();
    }

    public boolean isCompleted() {
	for (Map<String, QuestObjective> oneA : quest.getObjectives().values()) {
	    for (QuestObjective one : oneA.values()) {
		Integer amountDone = done.get(one);
		if (amountDone == null || amountDone < one.getAmount())
		    return false;
	    }
	}
	return true;
    }

    public void processQuest(JobsPlayer jPlayer, ActionInfo action) {
	if (quest.isStopped() || !quest.hasAction(action.getType()))
	    return;

	Map<String, QuestObjective> byAction = quest.getObjectives().get(action.getType());
	if (byAction != null && !byAction.containsKey(action.getNameWithSub()) && !byAction.containsKey(action.getName()))
	    return;

	org.bukkit.entity.Player player = jPlayer.getPlayer();

	for (String area : quest.getRestrictedAreas()) {
	    for (Entry<String, RestrictedArea> a : Jobs.getRestrictedAreaManager().getRestrictedAres().entrySet()) {
		if (a.getKey().equalsIgnoreCase(area) && a.getValue().inRestrictedArea(player.getLocation())) {
		    return;
		}
	    }
	}

	Job questJob = quest.getJob();
	if (questJob != null) {
	    int maxQuest = jPlayer.getPlayerMaxQuest(questJob.getName());
	    if (maxQuest > 0 && jPlayer.getDoneQuests() >= maxQuest) {
		return;
	    }
	}

	if (!isCompleted()) {
	    QuestObjective objective = null;
	    if (byAction != null) {
		objective = byAction.get(action.getName());
		if (objective == null)
		    objective = byAction.get(action.getNameWithSub());
	    }

	    if (objective != null) {
		Integer old = done.getOrDefault(objective, 0);
		done.put(objective, old < objective.getAmount() ? old + 1 : objective.getAmount());
	    }
	}

	jPlayer.setSaved(false);

	if (!isCompleted() || !player.isOnline() || givenReward)
	    return;

	givenReward = true;

	jPlayer.addDoneQuest(questJob);

	for (String one : quest.getRewardCmds()) {
	    ServerCommandEvent ev = new ServerCommandEvent(Bukkit.getConsoleSender(), one.replace("[playerName]", player.getName()));
	    Bukkit.getPluginManager().callEvent(ev);
	    if (!ev.isCancelled()) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ev.getCommand().startsWith("/") ? ev.getCommand().substring(1) : ev.getCommand());
	    }
	}
    }

    public boolean isGivenReward() {
	return givenReward;
    }

    public void setGivenReward(boolean givenReward) {
	this.givenReward = givenReward;
    }
}
