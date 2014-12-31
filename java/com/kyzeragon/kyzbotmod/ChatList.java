package com.kyzeragon.kyzbotmod;

import java.util.LinkedList;

public class ChatList
{
	LinkedList<String> names = new LinkedList<String>();
	LinkedList<String> messages = new LinkedList<String>();
	
	public ChatList() {}
	
	public void addLine(String name, String lowerMessage)
	{
		if (names.size() >= 8)
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
		for (int i = 0; i < names.size(); i++)
		{
			if (names.get(0).equals(names.get(i)))
			{
				lineCount++;
				if (lineCount > 4) // only checks the last 4 messages from player
					break;
				// TODO: check SIMILAR messages
				if (messages.get(0).equals(messages.get(i)))
					count++;
			}
			if (count > 2)
			{
				return "/warn " + names.get(0) + " Don't spam chat.";
			}
		}
		return "";
	}
}
