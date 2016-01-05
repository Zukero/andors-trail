package com.gpl.rpg.AndorsTrail.model;

import java.util.LinkedList;
import java.util.ListIterator;

public final class CombatLog {
	private final LinkedList<String> messages = new LinkedList<String>();
	private static final int MAX_COMBAT_LOG_LENGTH = 100;
	private static final String newCombatSession = "--";


	private static final String preMultiplier = " (x";
	private static final String postMultiplier = ")";
	private static final int digitNumber = 2; //Number of digits for counter
	private static final String digitFiller = " "; //has to be parsable into Integer

	private static final int preLength = preMultiplier.length();
	//private static final int postLength = postMultiplier.length();
	private static final int counterLength = (preMultiplier.length() + postMultiplier.length() + digitNumber);

	public CombatLog() { }

	public void append(String msg) {
		while (messages.size() >= MAX_COMBAT_LOG_LENGTH) messages.removeFirst();
		appendNoRepeat(msg);
	}

	public void appendNoRepeat(String msg){
		if(messages.isEmpty()) {
			messages.addLast(msg);
			return;
		}

		String latest = messages.getLast();
		//String temp = latest;

		//Simplest case ever.
		if(latest.length() == msg.length()){
			if(latest.equals(msg)){
				String newCounterString = "2";
				int digitDifference = digitNumber - 1;
				if(digitDifference >0){
					for(int i=0; i<digitDifference; i++)
						newCounterString = digitFiller +newCounterString;
				}
				latest += preMultiplier+ newCounterString + postMultiplier;
				messages.removeLast();
				messages.addLast(latest);
				return;
			}
		}

		//Most complex case ever.
		if(latest.length() - counterLength == msg.length()){
			String partOne = latest.substring(0, latest.length()- counterLength);
			//System.out.println(partOne);
			String partTwo = latest.substring(latest.length() - counterLength);
			if(partOne.equals(msg) && partTwo.length() == counterLength){
				String oldPrefix = partTwo.substring(0, preLength);
				String middleDigit = partTwo.substring(preLength, preLength + digitNumber);
				String oldPostfix = partTwo.substring(preLength + digitNumber);

				if(oldPostfix.equals(postMultiplier) && oldPrefix.equals(preMultiplier)){
					int oldCounter = Integer.parseInt(middleDigit.trim());
					int newCounter = oldCounter +1;
					String newCounterString = newCounter +"";

					int digitDifference = digitNumber - newCounterString.length();
					if(digitDifference >0){
						for(int i=0; i<digitDifference; i++)
							newCounterString = digitFiller +newCounterString;
					}
					else if(digitDifference<0){
						messages.addLast(msg);
						return;
					}
					messages.remove(messages.size() -1);
					messages.addLast(partOne + oldPrefix + newCounterString + oldPostfix);
					return;
				}
			}
		}
		messages.addLast(msg);
		return;
	}

	public void appendCombatEnded() {
		if (messages.isEmpty()) return;
		if (messages.getLast().equals(newCombatSession)) return;
		append(newCombatSession);
	}

	public String getLastMessages() {
		if (messages.isEmpty()) return "";
		StringBuilder sb = new StringBuilder(100);
		ListIterator<String> it = messages.listIterator(messages.size());
		sb.append(it.previous());
		int i = 1;
		while (it.hasPrevious() && i++ < 3) {
			String s = it.previous();
			if (s.equals(newCombatSession)) break;
			sb.insert(0, '\n').insert(0, s);
		}
		return sb.toString();
	}

	public String[] getAllMessages() {
		return messages.toArray(new String[messages.size()]);
	}
}
