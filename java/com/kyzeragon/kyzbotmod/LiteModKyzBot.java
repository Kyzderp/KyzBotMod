package com.kyzeragon.kyzbotmod;

import java.io.File;

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
	ChatStyle style = new ChatStyle();
	ChatComponentText toggleMessage;

	public LiteModKyzBot()
	{
		this.kyzBotOn = false;
		this.displayOn = true;
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
		if (this.kyzBotOn && message.length() > 10 && message.substring(0, 10).equals("§r§8[§r§fG"))
		{
			if (!(message.contains("§r§8[§r§eMod") || message.contains("§r§8[§r§bAssist")
					|| message.contains("§r§8[§r§6Dev") || message.contains("§r§8[§r§6Admin")))
			{
				this.kyzBot = new KyzBot(message.replaceAll("§", "&"));
				EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
				String adv = this.kyzBot.checkAdv();
				if (!adv.equals(""))
					player.sendChatMessage(adv);
				else
				{
					String result = this.kyzBot.checkMessage();
					if (!result.equals(""))
						player.sendChatMessage(result + ".");
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
			if (argv.length == 1)
			{
				this.kyzBotOn = !this.kyzBotOn;

				if (kyzBotOn)
					this.toggleMessage = new ChatComponentText("KyzBot: ON");
				else
					this.toggleMessage = new ChatComponentText("KyzBot: OFF");
				style.setColor(EnumChatFormatting.AQUA);
				toggleMessage.setChatStyle(style);
				Minecraft.getMinecraft().thePlayer.addChatComponentMessage(toggleMessage);	
			}
			else if (argv[1].equals("display"))
			{
				this.displayOn = !this.displayOn;
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

}
