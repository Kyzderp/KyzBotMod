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
	public String getVersion() {return "0.9.2";}

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
		if (this.kyzBotOn && message.matches("§r§8\\[§r§f[GL].*"))
		{
			if (!message.matches(".*§r§8\\[§r(§eMod|§bAssist|§6Dev|§6Admin).*"))
			{
				this.kyzBot = new KyzBot(message.replaceAll("§", "&"), this.chatList, this.config, this.lastSay);
				EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;

				String adv = "";
				if (this.getVal("adv") > 0)
					adv = this.kyzBot.checkAdv();
				if (!adv.equals("")) // Check if advertising
				{
					if (this.useKick)
						adv = adv.replaceAll("banip", "kick") + ", ban incoming.";
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
				this.kyzBotOn = !this.kyzBotOn;

				if (kyzBotOn)
					this.messageToUser("KyzBot: ON");
				else
					this.messageToUser("KyzBot: OFF");
			}
			else if (argv[1].equals("display")) // toggles display on bottom right
			{
				this.displayOn = !this.displayOn;
			}
			else if (argv[1].equals("help")) // Prints usage
			{
				this.messageToUser("KyzBot v0.9.0 Usage:");
				String[] toDisplay = {"display - toggle ON/OFF display", "help - this usage message", 
						"list - list of checks", "[check] - toggle specified check",
						"mode - normal, lenient, test, kick, warn"};
				for (int i = 0; i < toDisplay.length; i++)
				{
					this.messageToUser("/kyzbot " + toDisplay[i]);
				}
				String configList = "Configurable checks: ";
				for (String word: this.configItems)
					configList += word + " ";
				this.messageToUser(configList);
			}
			else if (argv[1].equals("list")) // List checks ON/OFF
			{
				for (String word: this.configItems)
				{
					if (this.getVal(word) == 1)
						this.messageToUser(word + ": NORMAL");
					else if (this.getVal(word) == 2)
						this.messageToUser(word + ": LENIENT");
					else
						this.messageToUser(word + ": OFF");
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
					}
					else if (argv[2].equalsIgnoreCase("lenient"))
					{
						this.kyzBotMode = "[LENIENT]";
						this.config = "2222222222";
					}
					else if (argv[2].equalsIgnoreCase("test"))
					{
						if (this.kyzBotTest.equals(""))
							this.kyzBotTest = "/m Kyzer ";
						else
							this.kyzBotTest = "";
					}
					else if (argv[2].equalsIgnoreCase("kick"))
					{
						this.useKick = true;
						this.messageToUser("KyzBot: Using /kick");
					}
					else if (argv[2].equalsIgnoreCase("warn"))
					{
						this.useKick = false;
						this.messageToUser("KyzBot: Using /warn");
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
						this.messageToUser(word + ": NORMAL");
					else if (this.getVal(word) == 2)
						this.messageToUser(word + ": LENIENT");
					else
						this.messageToUser(word + ": OFF");
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

	private void messageToUser(String message)
	{
		this.displayMessage = new ChatComponentText(message);
		this.displayMessage.setChatStyle(style);
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}

	private int getVal(String item)
	{
		int index = this.configItems.indexOf(item);
		return Integer.parseInt(config.substring(index, index + 1));
	}
}
