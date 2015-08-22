package com.kyzeragon.kyzbotmod;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.logging.log4j.core.helpers.Strings;

public class KyzBot 
{
	String channel;
	String text;
	String lowerText;
	String player;
	String originalSay;
	String originalWarn;
	String say;
	String warn;
	String lastSay;
	String config;
	LinkedList<String> configItems;

	public KyzBot (String message, ChatList chatList, String config, String lastSay, String channel)
	{
		///// PLAYER /////
		int playerBegin = message.indexOf("[&r&a");
		int playerEnd = message.indexOf("&r&8]", playerBegin);
		this.player = message.substring(playerBegin + 5, playerEnd);

		///// TEXT /////
		int textBegin = message.indexOf(" &r&f");
		if (channel.equals("help"))
			textBegin = message.indexOf(" &r&c");
		this.text = "";
		if (textBegin > 0)
		{
			int textEnd = message.length() - 2;
			this.text = message.substring(textBegin + 5, textEnd);
		}
		this.text = this.text.replaceAll("(&r)*(&f)*", "");
		this.lowerText = this.text.toLowerCase();

		///// WARN MESSAGES /////
		this.originalSay = "/ch qm g " + this.player;
		this.originalWarn = "/warn " + this.player + " ";
		this.say = this.originalSay;
		this.warn = this.originalWarn;

		///// MISC /////
		this.channel = channel;
		this.config = config;
		this.lastSay = lastSay;
		configItems = new LinkedList<String>();	
		String[] configStrings = {"adv", "insult", "multispam", "caps", "spam", "lag", "english", "bad", "count", "youtube"};
		for (int i = 0; i < configStrings.length; i++)
			configItems.add(configStrings[i]);
		
		
		if (this.getVal("multispam") == 1)
			chatList.addLine(this.player, this.lowerText, 8);
		else if (this.getVal("multispam") == 2)
			chatList.addLine(this.player, this.lowerText, 5);
	}

	public String checkMessage()
	{
		if (this.getVal("spam") > 0)
			this.checkSpam();
		if (this.getVal("lag") > 0)
			this.checkLag();
		if (this.getVal("english") > 0)
			this.checkEnglish();
		if (this.getVal("caps") > 0)
			this.checkCaps();
		if (this.getVal("bad") > 0)
			this.checkBad();
		if (this.getVal("youtube") > 0)
			this.checkYoutube();

		if (!this.warn.equals(this.originalWarn))
			return this.warn;
		if (!this.say.equals(this.originalSay))
			return this.say;
		return "";
	}

	private void checkCaps()
	{
		// TODO: check halves separately
		int numCaps = 0;
		int numLetters = 0;
		for (int i = 0; i < this.text.length(); i++)
		{
			if (Character.isUpperCase(this.text.charAt(i)))
				numCaps++;
			if (Character.isLetter(this.text.charAt(i)))
				numLetters++;
		}
		if (numCaps < numLetters * 0.7) // 70%+ caps will get punishment
			return;
		if (this.warn.matches(".*ingles.*") && numLetters >= 12)
			this.addWarning("no usa mayusculas");
		else if (numLetters >= 17)
			this.addWarning("don't type in caps");
		else if (this.getVal("caps") == 1 && numLetters > 14)
		{
			if (this.lastSay.equals(this.player))
				this.addWarning("don't type in caps");
			this.say += ", don't type in caps";
		}
		return;
	}

	private void checkSpam()
	{
		String[] strippedWords = this.lowerText.replaceAll("(§.?|diamondminecart)", "").replaceAll("[\\(\\)\\.,?!@#$%^&]", " ").split(" ");
		if (strippedWords.length >= 4)
		{
			int count = 0;
			String currentWord = strippedWords[0];
			for (int i = 0; i < strippedWords.length; i++)
			{
				if (strippedWords[i].equals(currentWord))
					count++;
				else
				{
					currentWord = strippedWords[i];
					count = 1;
				}
			}
			if (count >= 4)
			{
				this.addWarning("don't spam words");
				return;
			}
		}

		String[] words = this.lowerText.split("[,.<>\\-_/ ]");
		for (int i = 0; i < words.length; i++)
		{
			int offset = 0;
			if (this.getVal("spam") == 2)
				offset = 8; //thediamondminecart
			if (words[i].length() > 19 + offset && !words[i].toLowerCase().contains("teamextrememc.com"))
			{
				this.addWarning("don't spam");
				return;
			}
			else if (this.getVal("spam") == 1 && words[i].length() > 9)
			{
				char letter = words[i].charAt(1);
				for (int j = 2; j < words[i].length() - 1; j++)
				{
					if (words[i].charAt(j) != letter)
						return;
				}
				if (this.lastSay.equals(this.player)) // if the last person was told verbally already
					this.addWarning("don't spam"); // warn instead
				this.say += ", don't spam";
				return;
			}
		}
	}

	private void checkLag()
	{
		String lagStrip = ConfigFile.lag;
		if (lagStrip.equals(""))
			lagStrip = "(i'*m)|i|am|(server'*s*)|wtf|the|tengo|many|real|so|much|hard|have|dis|this|stupid|dumb|very|is| ";
		String toCheck = this.lowerText.replaceAll(lagStrip, "");
		if (toCheck.matches("([\\., ?!;']*l+[\\., ?!]*a+[\\., ?!]*g+y*(ing)*[\\., ?!;']*)+"))
			this.addWarning("don't complain about lag");
		return;
	}

	private void checkEnglish()
	{
		//TODO: add more keywords
		String spanish = ConfigFile.spanish;
		if (spanish.equals(""))
			spanish = ".*(estoy|espada|armadura|vamos|soy nuev|(algu?i?e?n)|donde est|juegos|tengo|ayudame|quien habl|como est|mierda).*";
		String something = ConfigFile.language;
		if (something.equals(""))
			something = ".*(alguem|beyler|olan var|esti roma|unde esti|magyar|hrvat|gelirim|kann ich|(t.?rk var ?m)|(t.?rk olan)|ceza |yiycem|e konu|nerdes|arkada|eu sunt|sunt roma|yazd.?m| ima li |srbije|burday.?m).*";
		if ((" " + this.lowerText + " ").matches(spanish))
		{
			if (this.channel.equals("global"))
				this.addWarning("solamente Ingles en global chat");
			else
				this.addWarning("English in " + this.channel + " chat");
		}
		else if ((" " + this.lowerText + " ").matches(something))
			this.addWarning("English in " + this.channel + " chat");
	}

	private void checkBad()
	{
		if (this.lowerText.matches(".*(nigga|nigger).*"))
			this.addWarning("not appropriate");
	}

	private void checkYoutube()
	{
		// TODO: add more
		String youtube = ConfigFile.youtube;
		if (youtube.equals(""))
			youtube = "say (hi|hello).*youtube.*";
		if (this.lowerText.matches(youtube))
			this.addWarning("do not encourage spam");
	}


	///// called directly from LiteModKyzBot /////
	public String checkAdv()
	{
		// TODO: add more names
		String serverNames = ConfigFile.serverNames;
		if (serverNames.equals(""))
			serverNames = "mineplex|(wal[ -]*craft)|(pika[ -]*network)|join my server|(peso[ -]*craft)|hypixel|tagcraft";
		String serverIPs = ConfigFile.serverIPs;
		if (serverIPs.equals(""))
			serverIPs = "play.islandcraft.|wal-craft.eu|play.pika-network.net|play.pesocraft.net|.mcpro.co|hypixel.net|play.tagcraftmc";

		if (this.lowerText.matches(".*(" + serverIPs + ").*"))
			return "/banip " + this.player + " advertising";
		if (this.lowerText.matches(".*(" + serverNames + ").*"))
			return "/ch qm g " + this.player + ", do not talk about other servers.";
		return "";
	}

	public String checkInsult()
	{
		// TODO: check for insulting server
		return "";
	}

	///// other stufffff /////
	private void addWarning(String message)
	{
		if (this.warn.equals(this.originalWarn))
			this.warn += message.substring(0, 1).toUpperCase() + message.substring(1);
		else
			this.warn += ", " + message;
	}

	public String getPlayer() { return this.player; }

	private int getVal(String item)
	{
		int index = this.configItems.indexOf(item);
		return Integer.parseInt(config.substring(index, index + 1));
	}
}
