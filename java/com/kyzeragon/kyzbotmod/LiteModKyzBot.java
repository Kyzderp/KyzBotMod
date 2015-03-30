package com.kyzeragon.kyzbotmod;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.mumfrey.liteloader.ChatFilter;
import com.mumfrey.liteloader.ChatListener;
import com.mumfrey.liteloader.OutboundChatListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;

@ExposableOptions(strategy = ConfigStrategy.Versioned, filename="kyzbotmod.json")
public class LiteModKyzBot implements ChatFilter, OutboundChatListener, Tickable
{

	private static final boolean useButton = true;
	// set to " -KyzBot" to append message, or "" for none
	private static final String appendKyzbot = " -KyzBot";

	private KyzBot kyzBot;
	private boolean kyzBotOn;
	private boolean displayOn;
	private String lastSay;
	private String kyzBotMode;
	private String kyzBotTest;
	private ChatList chatList;
	private String config = "1111111111";
	private LinkedList<String> configItems;
	//set to true to use kicks, otherwise will use warnings (and banip for adv) 
	private boolean useKick = true;
	private boolean sentCmd;

	private ChatStyle style;
	private ChatComponentText displayMessage;


	public LiteModKyzBot()
	{
		this.kyzBotOn = false;
		this.displayOn = true;
		this.lastSay = "";
		this.kyzBotMode = "[NORMAL]";
		this.kyzBotTest = "";
		this.chatList = new ChatList();
		this.style = new ChatStyle();
		this.style.setColor(EnumChatFormatting.AQUA);
		this.sentCmd = false;

		configItems = new LinkedList<String>();
		String[] configStrings = {"adv", "insult", "multispam", "caps", "spam", "lag", "english", "bad", "count", "youtube"};
		for (int i = 0; i < configStrings.length; i++)
			configItems.add(configStrings[i]);
	}

	@Override
	public String getName() {return "KyzBot";}

	@Override
	public String getVersion() {return "1.1.0";}

	@Override
	public void init(File configPath){}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {}

	@Override
	public boolean onChat(S02PacketChat chatPacket, IChatComponent chat, String message) {
		///// GET RID OF UNKNOWN COMMAND ERROR /////
		if (message.matches(".*nknown.*ommand.*") && this.sentCmd)
		{
			this.sentCmd = false;
			return false;
		}

		///// EXTERNAL CONTROL /////
		if (message.length() > 10 && message.matches("§r(Kyzer|Flox|EbelAngel|Smokezarn|Jarriey|Noodzz|TalkyAttorney|chewy0ne|FluffBunneh|Mckebab|PyroAries_|Bigbosszee) : /m Kyzeragon /kyzbot toggle§r"))
		{
			this.kyzBotOn = !this.kyzBotOn;
			String[] words = message.split(" ");
			String toReply = "/m " + words[0].substring(2) + " KyzBot: OFF";
			if (this.kyzBotOn)
				toReply = "/m " + words[0].substring(2) + " KyzBot: ON";
			Minecraft.getMinecraft().thePlayer.sendChatMessage(toReply);
			return true;
		}

		///// GLOBAL AND LOCAL CHECKS /////
		if (this.kyzBotOn && message.matches("§r§8\\[§r§f(G|L|H|T).*")
				&& !message.matches(".*§r§8\\[§r(§eMod|§bAssist|§6Dev|§6Admin).*"))
		{
			String channel = "global";
			char ch = message.charAt(9);
			if (ch == 'H')
				channel = "help";
			else if (ch == 'T')
				channel = "trade";
			
			this.kyzBot = new KyzBot(message.replaceAll("§", "&"), this.chatList, this.config, this.lastSay, channel);
			EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;

			String adv = "";
			if (this.getVal("adv") > 0)
				adv = this.kyzBot.checkAdv();
			if (!adv.equals("")) // Check if advertising
			{
				if (this.useKick)
					adv = adv.replaceAll("banip", "kick"); //+ ", ban incoming.";
				player.sendChatMessage(this.kyzBotTest + adv + this.appendKyzbot);
			}
			else
			{
				String result = this.kyzBot.checkMessage(); // Do most checks
				if (!result.equals(""))
				{
					if (this.useKick)
						result = result.replaceAll("warn", "kick");
					player.sendChatMessage(this.kyzBotTest + result + "." + this.appendKyzbot);
					if (result.matches(".*ch qm g.*(don't spam|in caps).*"))
						this.lastSay = this.kyzBot.getPlayer();
				}
				else if (this.getVal("multispam") > 0)// Check multiline similar message spam
				{
					result = this.chatList.checkSpam();
					if (!result.equals(""))
					{
						if (this.useKick)
							result = result.replaceAll("warn", "kick");
						player.sendChatMessage(this.kyzBotTest + result + this.appendKyzbot);
					}
				}
			}
		}
		return true;
	}

	@Override
	// see https://github.com/totemo/watson/blob/0.7.0-1.7.2_02/src/watson/LiteModWatson.java
	public void onSendChatMessage(C01PacketChatMessage packet, String message) {
		String[] argv = message.toLowerCase().split(" ");
		if (argv[0].equalsIgnoreCase("/kyzbot") || argv[0].equalsIgnoreCase("/kb"))
		{
			this.sentCmd = true;
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			if (argv.length == 1) // toggles kyzbot ON/OFF
			{
				this.logMessage("KyzBot [v" + this.getVersion() + "]");
			}
			else if (argv[1].equalsIgnoreCase("on"))
			{
				this.kyzBotOn = true;
				this.logMessage("KyzBot: ON");
			}
			else if (argv[1].equalsIgnoreCase("off"))
			{
				this.kyzBotOn = false;
				this.logMessage("KyzBot: OFF");
			}
			else if (argv[1].equals("display")) // toggles display on bottom right
			{
				this.displayOn = !this.displayOn;
			}
			else if (argv[1].equals("help")) // Prints usage
			{
				this.logMessage("KyzBot v0.9.0 Usage:");
				String[] toDisplay = {"display - toggle ON/OFF display", "help - this usage message", 
						"list - list of checks", "[check] - toggle specified check",
				"mode - normal, lenient, test, kick, warn"};
				for (int i = 0; i < toDisplay.length; i++)
				{
					this.logMessage("/kyzbot " + toDisplay[i]);
				}
				String configList = "Configurable checks: ";
				for (String word: this.configItems)
					configList += word + " ";
				this.logMessage(configList);
			}
			else if (argv[1].equals("list")) // List checks ON/OFF
			{
				for (String word: this.configItems)
				{
					if (this.getVal(word) == 1)
						this.logMessage(word + ": NORMAL");
					else if (this.getVal(word) == 2)
						this.logMessage(word + ": LENIENT");
					else
						this.logMessage(word + ": OFF");
				}
			}
			else if (argv[1].equals("mode"))
			{
				if (argv.length > 2)
				{
					if (argv[2].equalsIgnoreCase("normal"))
					{
						this.kyzBotMode = "[NORMAL]";
						this.config = "1111111111";
						this.logMessage("Using normal mode");
					}
					else if (argv[2].equalsIgnoreCase("lenient"))
					{
						this.kyzBotMode = "[LENIENT]";
						this.config = "2222222222";
						this.logMessage("Using lenient mode");
					}
					else if (argv[2].equalsIgnoreCase("test"))
					{
						if (this.kyzBotTest.equals(""))
							this.kyzBotTest = "/m Kyzer ";
						else
							this.kyzBotTest = "";
						this.logMessage("Toggled test mode");
					}
					else if (argv[2].equalsIgnoreCase("kick"))
					{
						this.useKick = true;
						this.logMessage("KyzBot: Using /kick");
					}
					else if (argv[2].equalsIgnoreCase("warn"))
					{
						this.useKick = false;
						this.logMessage("KyzBot: Using /warn");
					}
				}
			}
			else
			{
				String word = argv[1];
				if (this.configItems.contains(word))
				{
					if (argv.length > 2)
					{
						if (argv[2].equalsIgnoreCase("off"))
							argv[2] = "0";
						else if (argv[2].equalsIgnoreCase("normal"))
							argv[2] = "1";
						else if (argv[2].equalsIgnoreCase("lenient"))
							argv[2] = "2";
						if (argv[2].matches("0|1|2"))
						{
							int index = this.configItems.indexOf(word);
							config = config.substring(0, index) + argv[2] + config.substring(index + 1);
						}
					}
					if (this.getVal(word) == 1)
						this.logMessage(word + ": NORMAL");
					else if (this.getVal(word) == 2)
						this.logMessage(word + ": LENIENT");
					else
						this.logMessage(word + ": OFF");
				}
			}
		}
	}

	@Override
	public void onTick(Minecraft minecraft, float partialTicks, boolean inGame,
			boolean clock) {
		if (this.displayOn && inGame && Minecraft.isGuiEnabled() && minecraft.currentScreen == null)
		{
			FontRenderer fontRender = minecraft.fontRenderer;
			String on = "OFF";
			if (this.kyzBotOn)
				on = "ON";
			if (this.useKick)
				on += " [KICK]";
			else
				on += " [WARN]";
			String mode = "Mode: " + this.kyzBotMode;
			if (!this.kyzBotTest.equals(""))
				mode += " [TEST]";
			int height = Minecraft.getMinecraft().displayHeight;
			fontRender.drawStringWithShadow("KyzBot: " + on, 1, height/2 - 20, 0x1FE700);
			fontRender.drawStringWithShadow(mode, 1, height/2 - 10, 0x1FE700);
		}

	}

	private int getVal(String item)
	{
		int index = this.configItems.indexOf(item);
		return Integer.parseInt(config.substring(index, index + 1));
	}
	
	/**
	 * Logs the message to the user
	 * @param message The message to log
	 */
	public static void logMessage(String message)
	{// "§8[§2FMO§8] §a" + 
		ChatComponentText displayMessage = new ChatComponentText("§8[§2Kyzbot§8] §a" + message);
		displayMessage.setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}

	/**
	 * Logs the error message to the user
	 * @param message The error message to log
	 */
	public static void logError(String message)
	{
		ChatComponentText displayMessage = new ChatComponentText("§8[§4!§8] §c" + message + " §8[§4!§8]");
		displayMessage.setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.RED));
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}
}

