package com.kyzeragon.kyzbotmod;

import java.io.File;
import java.util.HashMap;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.mumfrey.liteloader.ChatListener;
import com.mumfrey.liteloader.OutboundChatListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;

@ExposableOptions(strategy = ConfigStrategy.Versioned, filename="kyzbotmod.json")
public class LiteModKyzBot implements ChatListener, OutboundChatListener, Tickable
{
	KyzBot kyzBot;
	boolean kyzBotOn;
	boolean displayOn;
	String lastSay;
	ChatList chatList;
	HashMap<String, Boolean> config;

	ChatStyle style;
	ChatComponentText displayMessage;

	public LiteModKyzBot()
	{
		this.kyzBotOn = false;
		this.displayOn = true;
		this.lastSay = "";
		this.chatList = new ChatList();
		this.style = new ChatStyle();
		this.style.setColor(EnumChatFormatting.AQUA);

		///// config map /////
		config = new HashMap<String, Boolean>();
		String[] configItems = {"adv", "insult", "multispam", "caps", "spam", "lag", "english", "bad", "count", "youtube"};
		for (String item: configItems)
		{
			config.put(item, true);
		}
	}

	@Override
	public String getName() {return "KyzBot";}

	@Override
	public String getVersion() {return "0.9.0";}

	@Override
	public void init(File configPath){}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {}

	@Override
	public void onChat(IChatComponent chat, String message) {
		///// EXTERNAL CONTROL /////
		if (message.length() > 10 && message.matches("§r(Kyzer|Flox|EbelAngel|Smokezarn|Jarriey|Noodzz|TalkyAttorney|chewy0ne|FluffBunneh|Mckebab|PyroAries_|Bigbosszee) : /m Kyzeragon /kyzbot toggle§r"))
		{
			this.kyzBotOn = !this.kyzBotOn;
			String[] words = message.split(" ");
			String toReply = "/m " + words[0].substring(2) + " KyzBot: OFF";
			if (this.kyzBotOn)
				toReply = "/m " + words[0].substring(2) + " KyzBot: ON";
			Minecraft.getMinecraft().thePlayer.sendChatMessage(toReply);
		}
		
		///// GLOBAL AND LOCAL CHECKS /////
		if (this.kyzBotOn && message.length() > 10 && (message.substring(0, 10).equals("§r§8[§r§fG")
				|| message.substring(0, 10).equals("§r§8[§r§fL")))
		{
			if (!(message.contains("§r§8[§r§eMod") || message.contains("§r§8[§r§bAssist")
					|| message.contains("§r§8[§r§6Dev") || message.contains("§r§8[§r§6Admin")))
			{
				this.kyzBot = new KyzBot(message.replaceAll("§", "&"), this.chatList, this.config, this.lastSay);
				EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;

				String adv = "";
				if (config.get("adv"))
					adv = this.kyzBot.checkAdv();
				if (!adv.equals("")) // Check if advertising
					player.sendChatMessage(adv);
				else
				{
					String result = this.kyzBot.checkMessage(); // Do most checks
					if (!result.equals(""))
					{
						player.sendChatMessage(result + ".");
						if (result.matches(".*ch qm g.*(don't spam|in caps).*"))
							this.lastSay = this.kyzBot.getPlayer();
					}
					else // Check multiline similar message spam
					{
						result = this.chatList.checkSpam();
						if (!result.equals(""))
						{
							player.sendChatMessage(result);
						}
					}
				}
			}
		}
	}

	@Override
	public void onSendChatMessage(C01PacketChatMessage packet, String message) {
		String[] argv = message.toLowerCase().split(" ");
		if (argv[0].equals("/kyzbot"))
		{
			// TODO: find some way to cancel chat packet?	
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
						"list - list of checks", "[check] - toggle specified check"};
				for (int i = 0; i < toDisplay.length; i++)
				{
					this.messageToUser("/kyzbot " + toDisplay[i]);
				}
				String configList = "Configurable checks: ";
				for (String word: config.keySet())
					configList += word + " ";
				this.messageToUser(configList);
			}
			else if (argv[1].equals("list")) // List checks ON/OFF
			{
				for (String word: config.keySet())
				{
					if (config.get(word))
						this.messageToUser(word + ": ON");
					else
						this.messageToUser(word + ": OFF");
				}
			}
			else
			{
				if (this.config.containsKey(argv[1]))
				{
					this.config.put(argv[1], !this.config.get(argv[1]));
					this.messageToUser(argv[1] + " toggled.");
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
			int height = Minecraft.getMinecraft().displayHeight;
			fontRender.drawStringWithShadow("KyzBot: " + on, 1, height/2 - 10, 0x1FE700);
		}

	}

	private void messageToUser(String message)
	{
		this.displayMessage = new ChatComponentText(message);
		this.displayMessage.setChatStyle(style);
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}

}
