package au.com.mineauz.minigamesregions.actions;


import au.com.mineauz.minigames.MinigameMessageType;
import au.com.mineauz.minigames.blockRecorder.RecorderData;
import au.com.mineauz.minigames.menu.MenuUtility;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Map;

import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemNewLine;
import au.com.mineauz.minigames.menu.MenuItemPage;
import au.com.mineauz.minigames.menu.MenuItemString;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import org.bukkit.inventory.ItemStack;

/**
 * @author Turidus  https://github.com/Turidus/Minigames
 * 
 * This class provides the methods necessary to fill a gameboard with pairs of randomly placed blocks.
 * Its a region action and can as such only run inside a region.
 * 
 * The user can define two options in the menu.
 * A) the matchBlock, the block that is the placeholder in the game and which will be replaced by the random blocks
 * B) the blacklist, which removes blocks from the given blockPool to provide a free choice in gameboard design.
 * 	Removed blocks will not appear on the gameboard.
 * 	The blacklist follows this format: block:data,block2,block3,block4:data4 (no spaces)
 * 	If the block data is provided, only precise matches with that block will be removed.
 * 	If the block data is not provided, all blocks with that name will be removed. For example,
 * 	if the entry is WOOL, all WOOL blocks, no matter the color will be removed, but if the entry
 * 	is WOOL:1, only orange WOOL will be removed.
 *
 */
public class MemorySwapBlockAction extends AbstractAction {
	
	/**
	 * 
	 * @author Turidus
	 * This helper class plays the role of a struct like object and 
	 * represents an abstract block with a name, an data field and an boolean
	 * that indicates if this block should be precisely matched if compared 
	 * to other blocks, which defaults to true.
	 *
	 */
	static class PhantomBlock {
		
		public String blockName = "Stone";
		public boolean blockMatchData = true;
		
		public PhantomBlock(String name) {
			this.blockName = name;
			
		}
		
		public PhantomBlock(String name,boolean bool) {
			this.blockName = name;
			this.blockMatchData = bool;
		}
	}
	
	private StringFlag matchType = new StringFlag("WHITE_WOOL", "matchtype");
	private StringFlag blacklist = new StringFlag("", "blacklist");
	
	/*
	 * Building a blockPool to provide the blocks that could be used in the game.
	 */
	private static ArrayList<PhantomBlock> blockPool = new ArrayList<PhantomBlock>();
	
	/**
	 * Helper function to fill the blockPool with PhantomBlocks
	 * @param phantomBlock
	 */
	private static void addToBlockPool(PhantomBlock phantomBlock){
		blockPool.add(phantomBlock);
	}

	/*
	 * Filling the block pool with blocks than can be pulled and pushed by pistons manually
	 */
	static {
		/* TODO Maybe an automatic way of dealing with this. Problem: some curation is necessary
		 * to prevent blocks that are to visual similar to appear, for example quartz and white 
		 * concrete. Letting the user sort this out with the blacklist results in a very long
		 * blacklist string, which is annoying for the user.
		 */
		
		
		//Resource blocks
		addToBlockPool(new PhantomBlock("DIAMOND_BLOCK"));
		addToBlockPool(new PhantomBlock("IRON_BLOCK"));
		addToBlockPool(new PhantomBlock("EMERALD_BLOCK"));
		addToBlockPool(new PhantomBlock("GOLD_BLOCK"));
		addToBlockPool(new PhantomBlock("LAPIS_BLOCK"));
		
		//Concrete
		addToBlockPool(new PhantomBlock("WHITE_CONCRETE"));
		addToBlockPool(new PhantomBlock("ORANGE_CONCRETE"));
		addToBlockPool(new PhantomBlock("MAGENTA_CONCRETE"));
		addToBlockPool(new PhantomBlock("LIGHT_BLUE_CONCRETE"));
		addToBlockPool(new PhantomBlock("YELLOW_CONCRETE"));
		addToBlockPool(new PhantomBlock("LIME_CONCRETE"));
		addToBlockPool(new PhantomBlock("PINK_CONCRETE"));
		addToBlockPool(new PhantomBlock("GRAY_CONCRETE"));
		addToBlockPool(new PhantomBlock("LIGHT_GRAY_CONCRETE"));
		addToBlockPool(new PhantomBlock("CYAN_CONCRETE"));
		addToBlockPool(new PhantomBlock("PURPLE_CONCRETE"));
		addToBlockPool(new PhantomBlock("BLUE_CONCRETE"));
		addToBlockPool(new PhantomBlock("BROWN_CONCRETE"));
		addToBlockPool(new PhantomBlock("GREEN_CONCRETE"));
		addToBlockPool(new PhantomBlock("RED_CONCRETE"));
		addToBlockPool(new PhantomBlock("BLACK_CONCRETE"));
		
		//Ore blocks
		addToBlockPool(new PhantomBlock("COAL_ORE"));
		addToBlockPool(new PhantomBlock("DIAMOND_ORE"));
		addToBlockPool(new PhantomBlock("IRON_ORE"));
		addToBlockPool(new PhantomBlock("REDSTONE_ORE"));
		addToBlockPool(new PhantomBlock("EMERALD_ORE"));
		addToBlockPool(new PhantomBlock("GOLD_ORE"));
		addToBlockPool(new PhantomBlock("LAPIS_ORE"));
		addToBlockPool(new PhantomBlock("QUARTZ_ORE"));		
		
		//Wool blocks
		addToBlockPool(new PhantomBlock("WHITE_WOOL"));
		addToBlockPool(new PhantomBlock("ORANGE_WOOL"));
		addToBlockPool(new PhantomBlock("MAGENTA_WOOL"));
		addToBlockPool(new PhantomBlock("LIGHT_BLUE_WOOL"));
		addToBlockPool(new PhantomBlock("YELLOW_WOOL"));
		addToBlockPool(new PhantomBlock("LIME_WOOL"));
		addToBlockPool(new PhantomBlock("PINK_WOOL"));
		addToBlockPool(new PhantomBlock("GRAY_WOOL"));
		addToBlockPool(new PhantomBlock("LIGHT_GRAY_WOOL"));
		addToBlockPool(new PhantomBlock("CYAN_WOOL"));
		addToBlockPool(new PhantomBlock("PURPLE_WOOL"));
		addToBlockPool(new PhantomBlock("BLUE_WOOL"));
		addToBlockPool(new PhantomBlock("BROWN_WOOL"));
		addToBlockPool(new PhantomBlock("GREEN_WOOL"));
		addToBlockPool(new PhantomBlock("RED_WOOL"));
		addToBlockPool(new PhantomBlock("BLACK_WOOL"));
		
		//Logs
		addToBlockPool(new PhantomBlock("OAK_LOG"));
		addToBlockPool(new PhantomBlock("SPRUCE_LOG"));
		addToBlockPool(new PhantomBlock("BIRCH_LOG"));
		addToBlockPool(new PhantomBlock("JUNGLE_LOG"));
		addToBlockPool(new PhantomBlock("ACACIA_LOG"));
		addToBlockPool(new PhantomBlock("DARK_OAK_LOG"));
		addToBlockPool(new PhantomBlock("CRIMSON_STEM"));
		addToBlockPool(new PhantomBlock("WARPED_STEM"));
		
		//Planks
		addToBlockPool(new PhantomBlock("OAK_PLANKS"));
		addToBlockPool(new PhantomBlock("SPRUCE_PLANKS"));
		addToBlockPool(new PhantomBlock("BIRCH_PLANKS"));
		addToBlockPool(new PhantomBlock("JUNGLE_PLANKS"));
		addToBlockPool(new PhantomBlock("ACACIA_PLANKS"));
		addToBlockPool(new PhantomBlock("DARK_OAK_PLANKS"));
		addToBlockPool(new PhantomBlock("CRIMSON_PLANKS"));
		addToBlockPool(new PhantomBlock("WARPED_PLANKS"));
		
		//Misc
		addToBlockPool(new PhantomBlock("BRICK"));
		addToBlockPool(new PhantomBlock("PRISMARINE"));
		addToBlockPool(new PhantomBlock("SEA_LANTERN"));
		addToBlockPool(new PhantomBlock("SANDSTONE"));
		addToBlockPool(new PhantomBlock("STONE_BRICK"));
		addToBlockPool(new PhantomBlock("NETHER_BRICK"));
		addToBlockPool(new PhantomBlock("STONE"));
		addToBlockPool(new PhantomBlock("DIRT"));
		
		
		
	}
	
	/**
	 * Returns a array of PhantomBlock that will consists of all blocks of the block pool minus the ones on the blacklist.
	 * The blacklist string format is block1:data1,block2,block3,block4:data4. If the data field is missing, all blocks with the same
	 * name will be removed (for example all WOOL blocks if WOOL is the entry, but only WHITE WOLL if WOOL:0 is the entry.
	 * 
	 * @param player
	 * @return ArrayList<PhantomBlock>
	 */
	private ArrayList<PhantomBlock> cleanUpBlockPool(MinigamePlayer player) {
		
		
		ArrayList<String> blackListEntries = new ArrayList<String>();
		ArrayList<PhantomBlock> blocksToRemove = new ArrayList<PhantomBlock>();
		ArrayList<PhantomBlock> finalBlockList = new ArrayList<PhantomBlock>(); 
		String blackListString = blacklist.getFlag();
		
		if (blackListString.isEmpty()) {
			return blockPool;
		}
		
		//Parses the blacklist string into strings describing blocks.
		blackListString.trim();
		
		while (blackListString.contains(",")) {
			int commaIndex = blackListString.indexOf(",");
			blackListEntries.add(blackListString.substring(0,commaIndex));
			if (blackListString.length() > commaIndex + 1) {
				blackListString = blackListString.substring(commaIndex + 1);
			}else {
				blackListString = "";
			}
		}
		
		if (!blackListString.isEmpty()) {
			blackListEntries.add(blackListString);
		}
		
				
		//Gets the PhantomBlocks out of the provided blacklist string
		for (String blockString : blackListEntries) {
			
			if (!blockString.contains(":")) {
				blocksToRemove.add(new PhantomBlock(blockString,false)); //Generate blocks that will match all blocks with the same name, regardless of dataValue
			
			}else {
			
				try {
					int colonIndex = blockString.indexOf(":"); //Generates blocks that will be precise matches
					
					
					blocksToRemove.add(new PhantomBlock(blockString.substring(0,colonIndex)));
				}
				catch(NumberFormatException e){
					player.sendMessage("There was a wrong (non integer) data field in your blacklist", MinigameMessageType.ERROR);
				}
			}
			
		}
		
		//Cloning blockpool to prevent issues with the block pool
		for (PhantomBlock block : blockPool) {
			finalBlockList.add(new PhantomBlock(block.blockName,block.blockMatchData));
		}
		
		//Removing the blocks from the final block list
		for(PhantomBlock block : blocksToRemove) {
			
			boolean notFound = true;
			
			if(block.blockMatchData) { //Precisely removing the block
				
				for (ListIterator<PhantomBlock> poolIter = finalBlockList.listIterator(); poolIter.hasNext();) {
					
					PhantomBlock poolblock = poolIter.next();
					
					if (block.blockName.equalsIgnoreCase(poolblock.blockName)) {
						poolIter.remove();
						notFound = false;
					}
				}	
			} else {	//Removing all blocks with the same name, not matching the data field
			
				for (ListIterator<PhantomBlock> poolIter = finalBlockList.listIterator(); poolIter.hasNext();) {
					
					PhantomBlock poolblock = poolIter.next();
					
					if (block.blockName.equalsIgnoreCase(poolblock.blockName)) {
						poolIter.remove();
						notFound = false;
					}
				}
			}
			
			if(notFound) {
				player.sendMessage(block.blockName + " was not in the block pool", MinigameMessageType.ERROR);
			}
		}
		return finalBlockList;
	}
	
	

	@Override
	public String getName() {
		return "MEMORY_SWAP_BLOCK";
	}

	@Override
	public String getCategory() {
		return "Block Actions";
	}

	@Override
	public void describe(Map<String, Object> out) {
		out.put("From: ", matchType.getFlag());
		out.put("Block pool size", blockPool.size());
		out.put("Blacklist", blacklist.getFlag());
	}
	

	@Override
	public boolean useInRegions() {
		return true;
	}

	@Override
	public boolean useInNodes() {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	/**
	 *This will search for a certain type of block (user definable over the menu) and replaced with a random block
	 *from the block pool minus the blacklisted blocks (also user definable over the menu). 
	 *
	 *The block will always have a pair unless there is an odd number of blocks to replace. If there is an odd numbered
	 *amount of blocks there will be one unmatched block and player will be warned.
	 *If there are more blocks to replace than it is possible, the surplus blocks will be skipped and the player will
	 *be warned.
	 */
	public void executeRegionAction(MinigamePlayer player, Region region) {
		
		debug(player,region);
		ArrayList<PhantomBlock> localBlockPool = cleanUpBlockPool(player);
		

		
		ArrayList<Block> blocksToSwap = new ArrayList<Block>();
		ArrayList<PhantomBlock> targetBlocks = new ArrayList<PhantomBlock>();
		
		
		
		 //Collects all blocks to be swapped
		for (int y = region.getFirstPoint().getBlockY(); y <= region.getSecondPoint().getBlockY(); y++) {
			for (int x = region.getFirstPoint().getBlockX(); x <= region.getSecondPoint().getBlockX(); x++) {
				for (int z = region.getFirstPoint().getBlockZ(); z <= region.getSecondPoint().getBlockZ(); z++) {
					Block block = region.getFirstPoint().getWorld().getBlockAt(x, y, z);
					
					if (block.getType() == Material.getMaterial(matchType.getFlag())) {
						blocksToSwap.add(block);
					}
				}
			}
		}
		
		
		//Sanity checks that can be handled without throwing a exception but need a warning to player
		if (blocksToSwap.size() % 2 != 0) {
			player.sendMessage("This gameboard has an odd amount of playing fields, there will be unmatched blocks", MinigameMessageType.ERROR);
		}
		if (blocksToSwap.size() > 2 * localBlockPool.size()) {
			player.sendMessage("This gameboard has more fields then supported by the pool of available blocks (2 * " + localBlockPool.size() + "), there will be unswapt blocks", MinigameMessageType.ERROR);
		}
		
		//Fill the target list with blocks to swap to
		//TODO removing index and finding a better way to do this loop
		int index = 0;
		while (targetBlocks.size() < blocksToSwap.size()) {
			PhantomBlock tempBlock;
			
			if (index < localBlockPool.size()) {	
				tempBlock = localBlockPool.get(index);
			}else {
				tempBlock = new PhantomBlock("noBlock"); //If there are to few blocks in the block Pool, the list gets filled up with a "noBlock"
			}
			
			targetBlocks.add(tempBlock);
			targetBlocks.add(tempBlock);
			
			index++;
		}
		
		//Shuffle the target list to produce a random game field
		Collections.shuffle(targetBlocks);
		
		
		//Replacing the blocks
		for (int i = 0; i < blocksToSwap.size();i++) {
			
			if (targetBlocks.get(i).blockName == "noBlock") {//Missing blocks in the block pool will be caught here and lead to unswapped blocks
				continue;
			}
			
			Block fromBlock = blocksToSwap.get(i);
			PhantomBlock toBlock = targetBlocks.get(i);

			RecorderData data = player.getMinigame().getRecorderData();
			if(data != null){
				data.addBlock(fromBlock, null);
			}
			fromBlock.setType(Material.getMaterial(toBlock.blockName));
		}
		
		
	}

	@Override
	public void executeNodeAction(MinigamePlayer player,
			Node node) {
		debug(player,node);
	}
	

	@Override
	public void saveArguments(FileConfiguration config, String path) {
		matchType.saveValue(path, config);
		blacklist.saveValue(path, config);
		
	}

	@Override
	public void loadArguments(FileConfiguration config, String path) {
		matchType.loadValue(path, config);
		blacklist.loadValue(path, config);
		
	}

	@Override
	public boolean displayMenu(MinigamePlayer player, Menu previous) {
		 
		Menu m = new Menu(3, "Memory Swap Block", player);
		m.addItem(new MenuItemPage("Back", MenuUtility.getBackMaterial(), previous), m.getSize() - 9);
		final MinigamePlayer fply = player;
		
		//The menu entry for the from block, aka the block that will be replaced
		m.addItem(new MenuItemString("Match Block", Material.COBBLESTONE, new Callback<String>() {
			
			@Override
			public void setValue(String value) {
				if(Material.matchMaterial(value.toUpperCase()) != null)
					matchType.setFlag(value.toUpperCase());
				else
					fply.sendMessage("Invalid block type!", MinigameMessageType.ERROR);
			}
			
			@Override
			public String getValue() {
				return matchType.getFlag();
			}
		}) {
			@Override
			public ItemStack getItem() {
				ItemStack stack = super.getItem();
				Material m = Material.matchMaterial(matchType.getFlag());
				stack.setType(Objects.requireNonNullElse(m, Material.COBBLESTONE));
				return stack;
			}
		});
		
		//Menu entry for the blacklist entry, aka the blocks that will be removed from the block pool
		m.addItem(new MenuItemNewLine());
		m.addItem(new MenuItemString("Blacklist", MinigameUtils.stringToList("Format: WHITE_WOOL,OAK_LOG"), Material.BOOK, new Callback<String>() {
			
			
			@Override
			public void setValue(String value) {
				blacklist.setFlag(value);
			}
			
			@Override
			public String getValue() {
				return blacklist.getFlag();
			}
		}));
		
		m.displayMenu(player); 
		
		return false;
	}
	
}