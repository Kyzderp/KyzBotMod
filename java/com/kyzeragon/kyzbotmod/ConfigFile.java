package com.kyzeragon.kyzbotmod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import com.mumfrey.liteloader.util.log.LiteLoaderLogger;

import net.minecraft.client.Minecraft;

public class ConfigFile 
{
	private final File path = new File(Minecraft.getMinecraft().mcDataDir, "liteconfig" + File.separator + "config.1.7.2" + File.separator + "kyzbotconfig.txt");
	
	public static String spanish = "";
	public static String lag = "";
	public static String language = "";
	public static String youtube = "";
	public static String serverNames = "";
	public static String serverIPs = "";
	
	public ConfigFile()
	{
		if (!path.exists())
		{
			if (!this.writeFile())
				LiteLoaderLogger.warning("Cannot write to file!");
			else
				LiteLoaderLogger.info("Created new Kyzbot configuration file.");
		}
		if (!this.loadFile())
			LiteLoaderLogger.warning("Cannot read from file!");
		else
			LiteLoaderLogger.info("Kyzbot configuration loaded.");
	}
	
	public boolean writeFile()
	{
		PrintWriter writer;
		try {
			writer = new PrintWriter(path);
		} catch (FileNotFoundException e) {
			return false;
		}
		writer.println("spanish=.*(estoy|espada|armadura|vamos|soy nuev|(algu?i?e?n)|donde est|juegos|tengo|ayudame|quien habl|como est|mierda).*");
		writer.println("lag=(i'*m)|i|am|(server'*s*)|wtf|the|tengo|many|so|much|hard|have|dis|this|stupid|dumb|very|is| ");
		writer.println("language=.*(alguem|beyler|olan var|esti roma|unde esti|magyar|hrvat|gelirim|kann ich|(t.?rk var ?m)|(t.?rk olan)|ceza |yiycem|e konu|nerdes|arkada|eu sunt|sunt roma|yazd.?m| ima li |srbije|burday.?m).*");
		writer.println("youtube=say (hi|hello).*youtube.*");
		writer.println("servernames=mineplex|(wal[ -]*craft)|(pika[ -]*network)|join my server|(peso[ -]*craft)|hypixel|tagcraft");
		writer.println("serverips=play.islandcraft.|wal-craft.eu|play.pika-network.net|play.pesocraft.net|.mcpro.co|hypixel.net|play.tagcraftmc");
		writer.close();
		return true;
	}
	// TODO: /kb reload
	public boolean loadFile()
	{
		if (!path.exists())
			return false;
		Scanner scan;
		try {
			scan = new Scanner(path);
		} catch (FileNotFoundException e) {
			return false;
		}
		while (scan.hasNext())
		{
			String line = scan.nextLine();
			if (line.matches("spanish=.*"))
				spanish = line.replaceFirst("spanish=", "");
			else if (line.matches("lag=.*"))
				lag = line.replaceFirst("lag=", "");
			else if (line.matches("language=.*"))
				language = line.replaceFirst("language=", "");
			else if (line.matches("youtube=.*"))
				youtube = line.replaceFirst("youtube=", "");
			else if (line.matches("servernames=.*"))
				serverNames = line.replaceFirst("servernames=", "");
			else if (line.matches("serverips=.*"))
				serverIPs = line.replaceFirst("serverips=", "");
		}
		scan.close();
		return true;
	}
	
}
