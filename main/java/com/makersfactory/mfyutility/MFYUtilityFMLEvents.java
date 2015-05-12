package com.makersfactory.mfyutility;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;

public class MFYUtilityFMLEvents {

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent e) {
		EntityPlayer player = e.player;
		if (!MFYUtility.blacklistExemptPlayers.contains(e.player.getCommandSenderName())) {
			if (!e.player.worldObj.isRemote && e.phase == e.phase.END) {
				for (int i = 0; i < player.inventory.mainInventory.length; i++) {
					ItemStack is = player.inventory.mainInventory[i];
					if (is != null) {
						String it = Item.itemRegistry.getNameForObject(is.getItem());
						it = removePrefix(it, e.player);
						for (String compare : MFYUtility.blacklistItems) {
							if (compare.startsWith(it)) {
								if (!compare.contains(":")) confiscate(i, e.player, it);
								else {
									int dmgValue = Integer.parseInt(compare.substring(compare.indexOf(":")+1, compare.length()));
									if (is.getItemDamage() == dmgValue) confiscate(i, e.player, compare);
								}
							}
						}
						for (String compare : MFYUtility.blacklistBlocks) {
							if (compare.startsWith(it)) {
								if (!compare.contains(":")) confiscate(i, e.player, it);
								else {
									int dmgValue = Integer.parseInt(compare.substring(compare.indexOf(":")+1, compare.length()));
									if (is.getItemDamage() == dmgValue) confiscate(i, e.player, compare);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static void confiscate(int slot, EntityPlayer player, String itemName) {
		player.inventory.setInventorySlotContents(slot, null);
		EntityPlayerMP playerMP = (EntityPlayerMP) player;
		playerMP.sendContainerToPlayer(playerMP.openContainer);
		ChatComponentText msg = new ChatComponentText(itemName + " is blacklisted!");
		msg.getChatStyle().setColor(EnumChatFormatting.RED);
		player.addChatComponentMessage(msg);
	}
	
	public static String removePrefix(String orig, ICommandSender p) {
		String result = orig.substring(orig.indexOf(":")+1);
		result = result.substring(result.indexOf(".")+1);
		return result;
	}
}
