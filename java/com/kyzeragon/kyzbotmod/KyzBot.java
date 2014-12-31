package com.kyzeragon.kyzbotmod;

import java.util.HashMap;

import org.apache.logging.log4j.core.helpers.Strings;

public class KyzBot 
{
	String text;
	String lowerText;
	String player;
	String originalSay;
	String originalWarn;
	String say;
	String warn;
	String lastSay;
	HashMap<String, Boolean> config;
	// TODO: add modes?

	public KyzBot (String message, ChatList chatList, HashMap config, String lastSay)
	{
		//[&r&fl&r&8]&r&7=&r&8[&r&9•dynasty&r&8]&r&7=&r&8[&r&akyzer&r&8]
		//right now &r&fthisand.this&r&f should warn
		///// PLAYER /////
		int playerBegin = message.indexOf("[&r&a");
		int playerEnd = message.indexOf("&r&8]", playerBegin);
		this.player = message.substring(playerBegin + 5, playerEnd);

		///// TEXT /////
		int textBegin = message.indexOf(" &r&f");
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
		this.say = this.originalSay;
		this.originalWarn = "/warn " + this.player + " ";
		this.warn = this.originalWarn;

		///// MISC /////
		chatList.addLine(this.player, this.lowerText);
		this.config = config;
		this.lastSay = lastSay;
	}

	public String checkMessage()
	{
		System.out.println(this.text);	
		if (config.get("caps"))
			this.checkCaps();
		if (config.get("spam"))
			this.checkSpam();
		if (config.get("lag"))
			this.checkLag();
		if (config.get("english"))
			this.checkEnglish();
		if (config.get("bad"))
			this.checkBad();
		if (config.get("count"))
			this.checkCount();
		if (config.get("youtube"))
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
		String toCheck = this.text.replaceAll("BIGPAWS|DRAGONBITE|TIPOTE|QUIET_DEATH", "");
		int numCaps = 0;
		int numLetters = 0;
		for (int i = 0; i < toCheck.length(); i++)
		{
			if (Character.isUpperCase(toCheck.charAt(i)))
				numCaps++;
			if (Character.isLetter(toCheck.charAt(i)))
				numLetters++;
		}
		if (numCaps < numLetters * 0.7) // 70%+ caps will get punishment
			return;
		if (numLetters >= 10)
			this.addWarning("don't type in caps");
		else if (numLetters >= 6)
		{
			if (this.lastSay.equals(this.player))
				this.addWarning("don't type in caps");
			this.say += ", don't type in caps";
		}
		return;
	}

	private void checkSpam()
	{
		// catch multiple of same word spam, has to match with second word tho
		// TODO: possibly all words? more intensive...
		String[] strippedWords = this.lowerText.replaceAll("[\\.,?!@#$%^&]", "").split(" ");
		if (strippedWords.length >= 4)
		{
			int count = 0;
			for (int i = 0; i < strippedWords.length; i++)
			{
				if (strippedWords[i].equals(strippedWords[1]))
					count++;
			}
			if (count >= 4)
			{
				this.addWarning("don't spam");
				return;
			}
		}

		String[] words = this.lowerText.split(" ");
		for (int i = 0; i < words.length; i++)
		{
			if (words[i].length() > 17 && !words[i].toLowerCase().contains("teamextrememc.com"))
			{
				this.addWarning("don't spam");
				System.out.println(words[i]);
				return;
			}
			else if (words[i].length() > 7)
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
		String toCheck = this.lowerText.replaceAll("i|am|server|wtf|the|tengo|many|im|i'm|so|much|have|dis|this|stupid|dumb|very| ", "");
		if (toCheck.matches("l+[\\., ?!]*a+[\\., ?!]*g+y*(ing)*[\\., ?!]*"))
			this.addWarning("don't complain about lag");
		return;
	}

	private void checkEnglish()
	{
		//TODO: add more keywords, or maybe use regex?
		String[] nonEnglish = {"espada", "armadura", "vamos", "rk var", "soy nuev", "alguien",
				"donde esta", "juegos", "tengo", "ayudame", "alguem", "rk olan", "ceza ",
				"yiycem", "e konu", "nerdes", "arkada", "eu sunt", "algien", "sunt roma",
				"beyler", "olan var", "esti roma", "unde esti", "quien habl", "como esta", "magyar",
				"hrvata", "gelirim", "kann ich"};
		for (int i = 0; i < nonEnglish.length; i++)
		{
			if (this.lowerText.contains(nonEnglish[i]))
			{
				this.addWarning("English in global chat");
				return;
			}
		}
	}

	private void checkBad()
	{
		String[] inappropriate = {"faggot", "nigga", "nigger"};
		for (int i = 0; i < inappropriate.length; i++)
		{
			if (this.lowerText.contains(inappropriate[i]))
			{
				this.addWarning("not appropriate");
				return;
			}
		}
	}

	private void checkCount()
	{
		// TODO: counting up
		// TODO: countdown
	}

	private void checkYoutube()
	{
		if (this.lowerText.matches("say (hi|hello).*youtube.*"))
			this.addWarning("do not encourage spam");
		// TODO: say ____ to youtube
	}


	///// called directly from LiteModKyzBot /////
	public String checkAdv()
	{
		// TODO: add more names
		String[] serverNames = {"mineplex", "wal-craft", "pika-network", "join my server", "pesocraft", "hypixel"};
		String[] serverIPs = {"play.islandcraft.", "wal-craft.eu", "play.pika-network.net", "play.pesocraft.net", ".mcpro.co", "hypixel.net"};
		for (int i = 0; i < serverIPs.length; i++)
		{
			if (this.lowerText.contains(serverIPs[i]))
				return "/banip " + this.player + " advertising";
		}
		for (int i = 0; i < serverNames.length; i++)
		{
			if (this.lowerText.contains(serverNames[i]))
				return "/ch qm g " + this.player + ", do not talk about other servers.";
		}
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
}
