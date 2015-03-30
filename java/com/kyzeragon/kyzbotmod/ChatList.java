package com.kyzeragon.kyzbotmod;

import java.util.LinkedList;

public class ChatList
{
	LinkedList<String> names = new LinkedList<String>();
	LinkedList<String> messages = new LinkedList<String>();
	
	public ChatList() {}
	//TODO: check countdown/up
	public void addLine(String name, String lowerMessage, int listSize)
	{
		if (names.size() >= listSize)
		{
			names.removeLast();
			messages.removeLast();
		}
		names.addFirst(name);
		messages.addFirst(lowerMessage);
	}
	
	public String checkSpam()
	{
		int count = 0;
		int lineCount = 0;
		int lastNum = Integer.MAX_VALUE;
		int numCount = Integer.MAX_VALUE;
		if (messages.get(0).matches("-?[0-9]+"))
		{
			lastNum = Integer.parseInt(messages.get(0));
			numCount = 0;
		}
		for (int i = 0; i < names.size(); i++)
		{
			if (names.get(0).equals(names.get(i)))
			{
				lineCount++;
				if (lineCount > 4) // only checks the last 4 messages from player
					break;
				String currMsg = messages.get(i);
				if (lastNum != Integer.MAX_VALUE && currMsg.matches("-?[0-9]+") && currMsg.length() < 5) // if it's number
				{
					int currNum = Integer.parseInt(currMsg);
					if (currNum > lastNum && numCount <= 0) //countdown
					{
						lastNum = currNum;
						numCount--;
						if (numCount < -1)
							return "/kick " + names.get(0) + " " + (Integer.parseInt(messages.get(0)) - 1);
					}
					else if (currNum < lastNum && numCount >= 0) //count up
					{
						lastNum = currNum;
						numCount++;
						if (numCount > 1)
							return "/kick " + names.get(0) + " " + (Integer.parseInt(messages.get(0)) + 1);
					}
				}
				if (messages.get(0).equals(currMsg))
					count++;
			}
			if (count > 2)
			{
				return "/kick " + names.get(0) + " Don't spam chat.";
			}
		}
		return "";
	}
}
