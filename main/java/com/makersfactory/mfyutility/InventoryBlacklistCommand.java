package com.makersfactory.mfyutility;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Configuration;
import static net.minecraft.util.EnumChatFormatting.*;

public class InventoryBlacklistCommand implements ICommand {
	
	private List aliases;
	public InventoryBlacklistCommand() {
		this.aliases = new ArrayList();
		this.aliases.add("inv-blacklist");
		this.aliases.add("inventory-blacklist");
		this.aliases.add("invbl");
	}

	@Override
	public int compareTo(Object arg0) {
		return 0;
	}

	@Override
	public String getCommandName() {
		return "inv-blacklist";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return "Usage: /inv-blacklist <add/remove/exempt/unexempt>";
	}
	
	public String getCommandUsageAddRemove(ICommandSender sender) {
		return "Usage: /inv-blacklist <add/remove> <item_name>";
	}
	
	public String getCommandUsageExempt(ICommandSender sender) {
		return "Usage: /inv-blacklist exempt <player_name>";
	}
	
	public String getCommandUsageUnexempt(ICommandSender sender) {
		return "Usage: /inv-blacklist unexempt <player_name>";
	}

	@Override
	public List getCommandAliases() {
		return this.aliases;
	}
	
	public void processCommandItem(ICommandSender sender, String cmd, Item origItemTarget, int dmgValue) {
		
		String itemTarget = MFYUtilityFMLEvents.removePrefix(Item.itemRegistry.getNameForObject(origItemTarget), sender);
		String itemTargetFull = dmgValue < 0 ? itemTarget : itemTarget + ":" + dmgValue;
		
		// ADD TO BLACKLIST
		if (cmd.equalsIgnoreCase("add")) {
			// Check if it's already on the list.
			if (MFYUtility.blacklistItems.contains(itemTargetFull) || MFYUtility.blacklistBlocks.contains(itemTargetFull)) {
				sender.addChatMessage(simpleColorChat(RED, "That item/block is already blacklisted."));
			}
			else {
				ArrayList<String> newList = new ArrayList<String>();
				if (isBlock(itemTarget)) {
					for (String toCopy : MFYUtility.blacklistBlocks) {
						newList.add(toCopy);
					}
					newList.add(itemTargetFull);
					MFYUtility.blacklistBlocks = ImmutableSet.copyOf(newList);
					sender.addChatMessage(simpleColorChat(RED, itemTargetFull + " added to BLOCKS blacklist!"));
					MFYUtility.refreshConfig();
				}
				else {
					for (String toCopy : MFYUtility.blacklistItems) {
						newList.add(toCopy);
					}
					newList.add(itemTargetFull);
					MFYUtility.blacklistItems = ImmutableSet.copyOf(newList);
					sender.addChatMessage(simpleColorChat(RED, itemTargetFull + " added to ITEMS blacklist!"));
					MFYUtility.refreshConfig();
				}
			}
		}
		
		// REMOVE FROM BLACKLIST
		else if (cmd.equalsIgnoreCase("remove")) {
			int a = 0;
			// Check if it's not on the list.
			if (!MFYUtility.blacklistItems.contains(itemTargetFull) && !MFYUtility.blacklistBlocks.contains(itemTargetFull)) {
				sender.addChatMessage(simpleColorChat(RED, "That item/block is not currently blacklisted."));
			}
			else {
				// Re-write ban list.
				ArrayList<String> newList = new ArrayList<String>();
				if (isBlock(itemTarget)) {
					for (String toCopy : MFYUtility.blacklistBlocks) {
						if (!toCopy.equals(itemTargetFull)) newList.add(toCopy);
					}
					MFYUtility.blacklistBlocks = ImmutableSet.copyOf(newList);
					sender.addChatMessage(simpleColorChat(RED, itemTargetFull + " removed from BLOCKS blacklist!"));
					MFYUtility.refreshConfig();
				}
				else {
					for (String toCopy : MFYUtility.blacklistItems) {
						if (!toCopy.equals(itemTargetFull)) newList.add(toCopy);
					}
					MFYUtility.blacklistItems = ImmutableSet.copyOf(newList);
					sender.addChatMessage(simpleColorChat(RED, itemTargetFull + " removed from ITEMS blacklist!"));
					MFYUtility.refreshConfig();
				}
			}
		}
		else {
			sender.addChatMessage(simpleColorChat(RED, this.getCommandUsageAddRemove(sender)));
			return;
		}
	}

	/*
	public void processCommandBlock(ICommandSender sender, String cmd, Block blockTarget) {
		if (cmd.equalsIgnoreCase("add")) {
			if (MFYUtility.instance.bannedBlocks.contains(blockTarget)) {
				sender.addChatMessage(simpleColorChat(RED, "That item is already blacklisted."));
			}
			else {
				ArrayList<Block> newList = new ArrayList<Block>();
				for (Block toCopy : MFYUtility.instance.bannedBlocks) {
					newList.add(toCopy);
				}
				newList.add(blockTarget);
				MFYUtility.instance.bannedBlocks = ImmutableSet.copyOf(newList);
				sender.addChatMessage(simpleColorChat(RED, blockTarget.getUnlocalizedName() + " added to blacklist!"));
				MFYUtility.instance.refreshConfig();
			}
		}
		else if (cmd.equalsIgnoreCase("remove")) {
			if (!MFYUtility.instance.bannedBlocks.contains(blockTarget)) {
				sender.addChatMessage(simpleColorChat(RED, "That item is not currently blacklisted."));
			}
			else {
				ArrayList<Block> newList = new ArrayList<Block>();
				for (Block toCopy : MFYUtility.instance.bannedBlocks) {
					if (toCopy != blockTarget) newList.add(toCopy);
				}
				MFYUtility.instance.bannedBlocks = ImmutableSet.copyOf(newList);
				sender.addChatMessage(simpleColorChat(RED, blockTarget.getUnlocalizedName() + " removed from blacklist!"));
				MFYUtility.instance.refreshConfig();
			}
		}
		else {
			sender.addChatMessage(simpleColorChat(RED, this.getCommandUsage(sender)));
			return;
		}
	}
	*/

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.addChatMessage(simpleColorChat(RED, this.getCommandUsage(sender)));
			return;
		}
		String cmd = args[0];
		if (cmd.equalsIgnoreCase("list")) {
			cmdList(sender,args);
		}
		else if (cmd.equalsIgnoreCase("exempt")) {
			if (args.length < 2) {
				sender.addChatMessage(simpleColorChat(RED, this.getCommandUsageExempt(sender)));
				return;
			}
			cmdExempt(sender,args);
		}
		else if (cmd.equalsIgnoreCase("unexempt")) {
			if (args.length < 2) {
				sender.addChatMessage(simpleColorChat(RED, this.getCommandUsageUnexempt(sender)));
				return;
			}
			cmdUnexempt(sender,args);
		}
		else if (cmd.equalsIgnoreCase("add") || cmd.equalsIgnoreCase("remove")) {
			if (args.length < 2) {
				sender.addChatMessage(simpleColorChat(RED, this.getCommandUsageAddRemove(sender)));
				return;
			}
			cmdAddRemove(sender, args);
		}
		else {
			sender.addChatMessage(simpleColorChat(RED, this.getCommandUsage(sender)));
			return;
		}
	}
	
	public void cmdExempt(ICommandSender sender, String[] args) {
		String name = args[1];
		if (MFYUtility.blacklistExemptPlayers.contains(name)) {
			sender.addChatMessage(simpleColorChat(RED, "That player is already exempt."));
			return;
		}
		ArrayList<String> newList = new ArrayList<String>();
		for (String toCopy : MFYUtility.blacklistExemptPlayers) {
			newList.add(toCopy);
		}
		newList.add(name);
		MFYUtility.blacklistExemptPlayers = ImmutableSet.copyOf(newList);
		sender.addChatMessage(simpleColorChat(RED, name + " is now exempt from the blacklist!"));
		MFYUtility.refreshConfig();
	}

	public void cmdUnexempt(ICommandSender sender, String[] args) {
		String name = args[1];
		if (!MFYUtility.blacklistExemptPlayers.contains(name)) {
			sender.addChatMessage(simpleColorChat(RED, "That player is not exempt."));
			return;
		}
		ArrayList<String> newList = new ArrayList<String>();
		for (String toCopy : MFYUtility.blacklistExemptPlayers) {
			if (!toCopy.equalsIgnoreCase(name)) newList.add(toCopy);
		}
		MFYUtility.blacklistExemptPlayers = ImmutableSet.copyOf(newList);
		sender.addChatMessage(simpleColorChat(RED, name + " is no longer exempt from the blacklist!"));
		MFYUtility.refreshConfig();
	}

	public void cmdAddRemove(ICommandSender sender, String[] args) {
		String cmd = args[0];
		Item itemTarget;
		int a = args[1].indexOf(":");
		String itemTargetName = a == -1 ? args[1] : args[1].substring(0, a);
		try {
			itemTarget = Item.getItemById(Integer.parseInt(itemTargetName));
		} catch (NumberFormatException e) {
			itemTarget = (Item) Item.itemRegistry.getObject(itemTargetName);
		}
		if (itemTarget != null) {
			int dmgValue = -1;
			if (a != -1) {
				try {
					dmgValue = Integer.parseInt(args[1].substring(a+1));
				} catch (NumberFormatException e) {
					sender.addChatMessage(simpleColorChat(RED, args[1].substring(a+1) + " isn't a number!"));
					return;
				}
				if (dmgValue < 0) {
					sender.addChatMessage(simpleColorChat(RED, "Damage value can't be negative!"));
					return;
				}
			}
			processCommandItem(sender, cmd, itemTarget, dmgValue);
			return;
		}
		sender.addChatMessage(simpleColorChat(RED, "That item doesn't seem to exist."));
	}

	public void cmdList(ICommandSender sender, String[] args) {
		ChatComponentText blocksTitle = simpleColorChat(RED, "BLOCKS:");
		blocksTitle.getChatStyle().setBold(true);
		sender.addChatMessage(blocksTitle);
		String blocksList = "";
		for (String s : MFYUtility.blacklistBlocks) blocksList += s + ", ";
		if (blocksList.length() > 2) blocksList = blocksList.substring(0, blocksList.length()-2);
		else blocksList = "(no blacklisted blocks)";
		sender.addChatMessage(simpleColorChat(RED, blocksList));
		
		ChatComponentText itemsTitle = simpleColorChat(RED, "ITEMS:");
		itemsTitle.getChatStyle().setBold(true);
		sender.addChatMessage(itemsTitle);
		String itemsList = "";
		for (String s : MFYUtility.blacklistItems) itemsList += s + ", ";
		if (itemsList.length() > 2) itemsList = itemsList.substring(0, itemsList.length()-2);
		else itemsList = "(no blacklisted items)";
		sender.addChatMessage(simpleColorChat(RED, itemsList));
		
		ChatComponentText exemptTitle = simpleColorChat(RED, "EXEMPT PLAYERS:");
		exemptTitle.getChatStyle().setBold(true);
		sender.addChatMessage(exemptTitle);
		String exemptList = "";
		for (String s : MFYUtility.blacklistExemptPlayers) exemptList += s + ", ";
		if (exemptList.length() > 2) exemptList = exemptList.substring(0, exemptList.length()-2);
		else exemptList = "(no exempt players)";
		sender.addChatMessage(simpleColorChat(RED, exemptList));
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
		return true;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender p_71516_1_,
			String[] p_71516_2_) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		return false;
	}
	
	public static ChatComponentText simpleColorChat (EnumChatFormatting color, String msg) {
		ChatComponentText result = new ChatComponentText(msg);
		result.getChatStyle().setColor(color);
		return result;
	}
	
	public static boolean isBlock(String name) {
		int a = 0;
		Block test = (Block) Block.blockRegistry.getObject(name);
		return test != Blocks.air;
	}

}
