package com.kyzeragon.kyzbotmod;

public class KyzBot 
{
	String text;
	String lowerText;
	String player;
	String originalSay;
	String originalWarn;
	String say;
	String warn;
	// TODO: add modes?

	public KyzBot (String message)
	{
		int playerBegin = message.indexOf("[&r&a");
		int playerEnd = message.indexOf("&r&8]", playerBegin);
		this.player = message.substring(playerBegin + 5, playerEnd);

		int textBegin = message.indexOf(" &r&f");
		int textEnd = message.length() - 2;
		this.text = message.substring(textBegin + 5, textEnd);
		this.lowerText = this.text.toLowerCase();

		this.originalSay = "/ch qm g " + this.player;
		this.say = this.originalSay;
		this.originalWarn = "/warn " + this.player + " ";
		this.warn = this.originalWarn;
	}

	public String checkMessage()
	{
		System.out.println(this.text);		
		this.checkCaps();
		this.checkSpam();
		this.checkLag();
		this.checkEnglish();
		this.checkBad();
		this.checkCount();

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
		if (numLetters >= 10)
			this.addWarning("don't type in caps");
		else if (numLetters >= 6)
			this.say += ", don't type in caps";
		return;
	}

	private void checkSpam()
	{
		String[] words = this.lowerText.split(" ");
		for (int i = 0; i < words.length; i++)
		{
			if (words[i].length() > 17 && !words[i].contains("..") && !words[i].toLowerCase().contains("teamextrememc.com"))
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
				this.say += ", don't spam"; // TODO: repeated says
				return;
			}
		}
		//TODO: multi message spam "slow down on chat/don't spam"/
	}

	private void checkLag()
	{
		//TODO: more than single word?
		if (this.lowerText.equals("lag") || this.lowerText.equals("im lag"))
		{
			this.addWarning("don't complain about lag");
			return;
		}
		if (this.lowerText.contains("l") && this.lowerText.contains("a") && this.lowerText.contains("g"))
		{
			for (int i = 0; i < this.lowerText.length(); i++)
			{
				char letter = this.lowerText.charAt(i);
				if (Character.isLetter(letter) && "lagy".contains("" + letter))
					continue;
				else if (!Character.isLetter(letter))
					continue;
				else
					return;
			}
			this.addWarning("don't complain about lag");
		}
		return;
	}

	private void checkEnglish()
	{
		//TODO: add more keywords
		String[] nonEnglish = {"espada", "armadura", "vamos", "rk varm", "soy nuev", "alguien",
					"donde esta", "juegos", "yo tengo", "ayudame", "alguem", "rk olan", "ceza ",
					"yiycem", "e konu", "nerdes", "arkada", "eu sunt", "algien", "sunt romani"};
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
		// TODO: say ____ to youtube
	}
	

	///// called directly from LiteModKyzBot /////
	public String checkAdv()
	{
		// TODO: add more names
		String[] serverNames = {"mineplex", "wal-craft", "pika-network", "join my server", "pesocraft"};
		String[] serverIPs = {"play.islandcraft.", "wal-craft.eu", "play.pika-network.net", "play.pesocraft.net", ".mcpro.co"};
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
}
