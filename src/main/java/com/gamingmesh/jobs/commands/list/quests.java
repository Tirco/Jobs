package com.gamingmesh.jobs.commands.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.commands.Cmd;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import com.gamingmesh.jobs.container.QuestObjective;
import com.gamingmesh.jobs.container.QuestProgression;
import com.gamingmesh.jobs.stuff.TimeManage;
import com.gamingmesh.jobs.CMILib.RawMessage;

public class quests implements Cmd {

    @Override
    public boolean perform(Jobs plugin, final CommandSender sender, String[] args) {
	JobsPlayer jPlayer = null;
	boolean isPlayer = sender instanceof Player;

	if (args.length >= 1 && isPlayer && args[0].equalsIgnoreCase("next")) {
	    jPlayer = Jobs.getPlayerManager().getJobsPlayer((Player) sender);
	    jPlayer.resetQuests();
	} else {
	    if (args.length >= 1 && !args[0].equalsIgnoreCase("stop") && !args[0].equalsIgnoreCase("start")) {
		if (!Jobs.hasPermission(sender, "jobs.command.admin.quests", true))
		    return true;

		jPlayer = Jobs.getPlayerManager().getJobsPlayer(args[0]);
	    } else if (isPlayer)
		jPlayer = Jobs.getPlayerManager().getJobsPlayer((Player) sender);
	}

	if (jPlayer == null) {
	    if (args.length >= 1)
		sender.sendMessage(Jobs.getLanguage().getMessage("general.error.noinfo"));
	    else
		Jobs.getCommandManager().sendUsage(sender, "quests");
	    return true;
	}

	List<QuestProgression> questProgs = jPlayer.getQuestProgressions();

	if (questProgs.isEmpty()) {
	    sender.sendMessage(Jobs.getLanguage().getMessage("command.quests.error.noquests"));
	    return true;
	}

	if (args.length >= 1) {
	    Boolean stopped = null;
	    String cmd = args[args.length == 1 ? 0 : 1];

	    if (cmd.equalsIgnoreCase("stop") && Jobs.hasPermission(sender, "jobs.command.admin.quests.stop", false)) {
		stopped = true;
	    } else if (cmd.equalsIgnoreCase("start") && Jobs.hasPermission(sender, "jobs.command.admin.quests.start", false)) {
		stopped = false;
	    }

	    if (stopped != null) {
		for (QuestProgression q : questProgs) {
		    q.getQuest().setStopped(stopped);
		}

		sender.sendMessage(Jobs.getLanguage().getMessage("command.quests.status.changed", "%status%",
		stopped ? Jobs.getLanguage().getMessage("command.quests.status.stopped") :
		    Jobs.getLanguage().getMessage("command.quests.status.started")));
		return true;
	    }
	}

	sender.sendMessage(Jobs.getLanguage().getMessage("command.quests.toplineseparator", "[playerName]", jPlayer.getName(), "[questsDone]", jPlayer.getDoneQuests()));

	if (!isPlayer) {
	    return true;
	}

	for (JobProgression jobProg : jPlayer.progression) {
	    List<QuestProgression> list = jPlayer.getQuestProgressions(jobProg.getJob());

	    for (QuestProgression q : list) {
		String progressLine = Jobs.getCommandManager().jobProgressMessage(q.getTotalAmountNeeded(), q.getTotalAmountDone());

		if (q.isCompleted())
		    progressLine = Jobs.getLanguage().getMessage("command.quests.output.completed");

		RawMessage rm = new RawMessage();
		String msg = Jobs.getLanguage().getMessage("command.quests.output.questLine", "[progress]",
		    progressLine, "[questName]", q.getQuest().getQuestName(), "[done]", q.getTotalAmountDone(), "[required]", q.getTotalAmountNeeded());

		String hoverMsg = Jobs.getLanguage().getMessage("command.quests.output.hover");
		List<String> hoverList = new ArrayList<>();
		for (String current : hoverMsg.split("\n")) {
		    current = current.replace("[jobName]", jobProg.getJob().getName())
			.replace("[time]", TimeManage.to24hourShort(q.getValidUntil() - System.currentTimeMillis()));

		    if (current.contains("[desc]")) {
			hoverList.addAll(q.getQuest().getDescription());
		    } else {
			hoverList.add(current);
		    }
		}

		for (java.util.Map<String, QuestObjective> oneAction : q.getQuest().getObjectives().values()) {
		    for (Entry<String, QuestObjective> oneObjective : oneAction.entrySet()) {
			hoverList.add(Jobs.getLanguage().getMessage("command.info.output." + oneObjective.getValue().getAction().toString().toLowerCase() + ".info") + " " +
			    Jobs.getNameTranslatorManager().translate(oneObjective.getKey(), oneObjective.getValue().getAction(), oneObjective.getValue().getTargetId(), oneObjective.getValue()
				.getTargetMeta(), oneObjective.getValue().getTargetName())
			    + " " + q.getAmountDone(oneObjective.getValue()) + "/"
			    + oneObjective.getValue().getAmount());
		    }
		}

		String hover = "";
		for (String one : hoverList) {
		    if (!hover.isEmpty())
			hover += "\n";

		    hover += one;
		}

		if (list.size() < jobProg.getJob().getQuests().size() && Jobs.getGCManager().getDailyQuestsSkips() > jPlayer.getSkippedQuests() && !q.isCompleted()) {
		    if (Jobs.getGCManager().getDailyQuestsSkips() > 0) {
			hover += "\n" + Jobs.getLanguage().getMessage("command.quests.output.skip");
			hover += "\n" + Jobs.getLanguage().getMessage("command.quests.output.skips", "[skips]", (Jobs.getGCManager().getDailyQuestsSkips() - jPlayer.getSkippedQuests()));
		    }
		    rm.addText(msg).addHover(hover).addCommand("jobs skipquest " + jobProg.getJob().getName() + " " + q.getQuest().getConfigName() + " " + jPlayer.getName());
		} else
		    rm.addText(msg).addHover(hover);

		rm.show(sender);
	    }
	}

	sender.sendMessage(Jobs.getLanguage().getMessage("general.info.separator"));
	return true;
    }
}
