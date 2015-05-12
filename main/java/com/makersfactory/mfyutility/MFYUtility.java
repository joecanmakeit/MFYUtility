package com.makersfactory.mfyutility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.LoggerContextFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid="MFY-Utility", name="MFY-Utility", version="0.0.4", acceptableRemoteVersions="*")
public class MFYUtility {

	@Instance(value = "1")
	public static MFYUtility instance;
	//@SidedProxy(clientSide="mypackage.client.CommonProxy", serverSide="mypackage.CommonProxy")
	//public static CommonProxy proxy;
	
	public static ImmutableSet<String> blacklistItems;
	public static ImmutableSet<String> blacklistBlocks;
	public static ImmutableSet<String> blacklistExemptPlayers;
	
	public static Configuration config;
	public static String[] defaultItems = new String[] {
		"demoFakeItem1",
		"demoFakeItem2",
		"demoFakeItem3"
	};
	public static String[] defaultBlocks = new String[] {
		"demoFakeBlock1",
		"demoFakeBlock2"
	};
	public static String[] defaultExemptPlayers = new String[] {
		"demoFakePlayer1",
		"demoFakePlayer2",
		"demoFakePlayer3"
	};
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		
		// RANDOM CONFIG STUFF
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		String[] initItems = config.get("blacklist", "items", defaultItems).getStringList();
		String[] initBlocks = config.get("blacklist", "blocks", defaultBlocks).getStringList();
		String[] initPlayers = config.get("blacklist", "exemptPlayers", defaultExemptPlayers).getStringList();
		blacklistItems = ImmutableSet.copyOf(initItems);
		blacklistBlocks = ImmutableSet.copyOf(initBlocks);
		blacklistExemptPlayers = ImmutableSet.copyOf(initPlayers);
		config.save();
	}
	
	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new InventoryBlacklistCommand());
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {

		// INITIALIZE EVENT LISTENERS
		MinecraftForge.EVENT_BUS.register(new MFYUtilityForgeEvents());
		FMLCommonHandler.instance().bus().register(new MFYUtilityFMLEvents());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		
	}
	
	public static ArrayList<Item> stringsToItems(String[] orig) {
		ArrayList<Item> result = new ArrayList<Item>();
		for (String s : orig) {
			Item itemTarget;
			try {
				itemTarget = Item.getItemById(Integer.parseInt(s));
			} catch (NumberFormatException e) {
				itemTarget = (Item) Item.itemRegistry.getObject(s);
			}
			if (itemTarget != null) result.add(itemTarget);
		}
		return result;
	}
	
	public static ArrayList<Item> stringsToBlockItems(String[] orig) {
		ArrayList<Item> result = new ArrayList<Item>();
		for (String s : orig) {
			Block blockTarget;
			try {
				blockTarget = Block.getBlockById(Integer.parseInt(s));
			} catch (NumberFormatException e) {
				blockTarget = (Block) Block.blockRegistry.getObject(s);
			}
			if (blockTarget != null) result.add(Item.getItemFromBlock(blockTarget));
		}
		return result;
	}
	
	public static void refreshConfig() {
		String[] a = blacklistBlocks.toArray(new String[blacklistBlocks.size()]);
		String[] b = blacklistItems.toArray(new String[blacklistItems.size()]);
		String[] c = blacklistExemptPlayers.toArray(new String[blacklistExemptPlayers.size()]);
		config.get("blacklist", "blocks", new String[0]).setValues(a);
		config.get("blacklist", "items", new String[0]).setValues(b);
		config.get("blacklist", "exemptPlayers", new String[0]).setValues(c);
		config.save();
	}
}
