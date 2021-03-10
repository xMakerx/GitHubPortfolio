package liberty.maverick.dragonscale.pickaxe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import liberty.maverick.dragonscale.DragonScale;
import liberty.maverick.dragonscale.DragonScaleSettings;
import liberty.maverick.dragonscale.event.DragonScaleLootboxPickupEvent;
import liberty.maverick.dragonscale.event.DragonScaleMineEvent;
import liberty.maverick.dragonscale.event.DragonScalePickaxeExpChangeEvent;
import liberty.maverick.dragonscale.event.DragonScalePickaxeLevelChangeEvent;
import liberty.maverick.dragonscale.lootbox.LootBoxFactory;
import liberty.maverick.dragonscale.util.DragonUtils;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class DragonScalePickaxe {
	
	// This is the holder of the pickaxe.
	protected final Player wielder;
	
	private LevelData levelData;
	private int level;
	private int exp;
	
	// This is a list of blocks mined this period.
	// A new period begins every time the player breaks a block.
	protected HashSet<Block> minedThisPeriod;
	
	public DragonScalePickaxe(Player player, int level, int exp) {
		this.wielder = player;
		this.levelData = new LevelData(level);
		this.levelData.assignAttributesToPickaxe(this);
		this.level = level;
		this.exp = exp;
		this.minedThisPeriod = new HashSet<Block>();
		this.update();
	}
	
	/**
	 * Should be called whenever the physical representation
	 * of this pickaxe is equipped by the wielder.
	 * 
	 * See {@link #getWielder()} to see who the wielder is.
	 */
	
	public void onEquip() {
		for(PickaxeAttribute attribute : levelData.getPickaxeAttributes()) {
			attribute.pickaxe = this;
			attribute.onEquip();
		}
	}
	
	/**
	 * Should be called whenever the physical representation
	 * of this pickaxe is no longer held by the wielder.
	 * 
	 * See {@link #getWielder()} to see who the wielder is.
	 */
	
	public void onDequip() {
		for(PickaxeAttribute attribute : levelData.getPickaxeAttributes()) {
			attribute.pickaxe = this;
			attribute.onDequip();
		}
	}
	
	/**
	 * This is called after a block is voluntary destroyed by this pickaxe.
	 * @param {@link Block}
	 */
	
	public void onMine(Block block) {
		this.minedThisPeriod = new HashSet<Block>();
		
		for(PickaxeAttribute attribute : levelData.getPickaxeAttributes()) {
			attribute.onMine(block);
		}
	}
	
	/**
	 * Processes a block for its rewards and drops.
	 * @param {@link Block} block - The block that should be processed.
	 * @return True/false whether or not the block dropped rewards for us.
	 */
	
	private boolean processBlock(final Block block) {
		final ItemStack physicalPickaxe = generatePickaxe(DragonScale.singleton.getSettings());
		
		if(DragonUtils.canBuildHere(wielder, block.getLocation())) {
			final ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
			drops.addAll(block.getDrops(physicalPickaxe));
			
			if(drops.size() == 0) {
				// We need to compensate for no drops when destroying an ore with an
				// insufficient pickaxe.
				final Collection<ItemStack> compensateDrops = block.getDrops(new ItemStack(Material.DIAMOND_PICKAXE));
				drops.addAll(compensateDrops);
			}
			
			int earnedExp = DragonUtils.getOreExp(block);
			
			// Let's give the wielder the drops from this block and "destroy" this block.
			DragonUtils.givePlayerItems(wielder, block.getLocation(), DragonUtils.itemstackCollectionToArray(drops));
			block.setType(Material.AIR, true);
			
			// Let's increment our exp by the amount of drops the wielder got.
			incrementExp(earnedExp * drops.size());
			
			// Let's roll the dice to see if we should award a loot box.
			possiblyGiveLootBox();
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * This is called after {@link #onMine(Block)} from a
	 * {@link DragonScaleMineEvent}.
	 * @param firstBlock - The block that the wielder physically mined.
	 */
	
	public void processRewards(Block firstBlock) {
		final Iterator<Block> iterator = minedThisPeriod.iterator();
		
		processBlock(firstBlock);
		
		while(iterator.hasNext()) {
			Block block = iterator.next();
			
			if(!processBlock(block)) {
				// If for some reason we no longer have build access to a block, let's remove it from
				// the HashSet.
				iterator.remove();
			}
		}
		
		update();
	}
	
	private void possiblyGiveLootBox() {
		// Let's process our chances for a loot box to drop!
		final LootBoxFactory lootBoxFactory = DragonScale.singleton.getLootBoxFactory();
		if(Math.random() < lootBoxFactory.getDropChance()) {
			final ItemStack lootBox = lootBoxFactory.generate();
			DragonUtils.givePlayerItems(wielder, lootBox);
			DragonScale.singleton.getSettings().getSoundByName("LOOTBOX_PICKUP").play(wielder.getLocation());
			
			// Let's call the lootbox pickup event
			final DragonScaleLootboxPickupEvent pickUpEvt = new DragonScaleLootboxPickupEvent(wielder, lootBox);
			DragonScale.singleton.getServer().getPluginManager().callEvent(pickUpEvt);
		}
	}
	
	/**
	 * Processes when the plugin is reloaded.
	 */
	
	public void processReload() {
		levelData = new LevelData(level);
		levelData.assignAttributesToPickaxe(this);
		update();
	}
		
	public void update() {
		final int maxLevel = DragonScale.singleton.getSettings().getMaxLevel();
		if(level < maxLevel && exp >= levelData.getLevelUpExp()) {
			setLevel(getLevel() + 1);
			setExp(0);
			
			// Let's update our level data.
			levelData.setLevel(level);
			levelData.assignAttributesToPickaxe(this);
		}
		
		dequipIfHeld();
		
		if(wielder != null) {
			// Let's update the physical pickaxe item.
			int heldItemSlot = wielder.getInventory().getHeldItemSlot();
			for(int i = 0; i < wielder.getInventory().getSize(); i++) {
				if(isPluginPickaxe(DragonScale.singleton, wielder.getInventory().getItem(i))) {
					wielder.getInventory().setItem(i, generatePickaxe(DragonScale.singleton.getSettings()));
					wielder.updateInventory();
					
					if(i == heldItemSlot) {
						this.onEquip();
					}
					
					break;
				}
			}
		}
	}
	
	/**
	 * Dequips the pickaxe if it's held.
	 */
	
	private void dequipIfHeld() {
		// Let's return if the wielder is null.
		if(wielder == null) return;

		int heldItemSlot = wielder.getInventory().getHeldItemSlot();
		
		if(DragonScalePickaxe.isPluginPickaxe(DragonScale.singleton, wielder.getInventory().getItem(heldItemSlot))) {
			this.onDequip();
		}
	}
	
	/**
	 * Generates a physical copy of this pickaxe based off our data.
	 * @param {@link DragonScaleSettings} A settings instance to help with fetching settings.
	 * @return Generated {@link ItemStack} based off the data contained in this class.
	 */
	
	@SuppressWarnings("deprecation")
    public ItemStack generatePickaxe(final DragonScaleSettings settings) {
		ItemStack pickaxe = new ItemStack(levelData.getPickaxeType(), 1);
		
		// Some NBT tag stuff to make the pickaxe unbreakable.
		final net.minecraft.server.v1_15_R1.ItemStack cis = CraftItemStack.asNMSCopy(pickaxe);
		final NBTTagCompound tag = new NBTTagCompound();
		tag.setInt("Unbreakable", 1);
		cis.setTag(tag);
		
		pickaxe = CraftItemStack.asBukkitCopy(cis);
		
		ItemMeta meta = pickaxe.getItemMeta();

		meta.setDisplayName(handleConfigString(settings.getPickaxeName()));
		meta.setLore(new ArrayList<String>());
		
		final List<String> lore = settings.getPickaxeLore();
		
		// Let's prepare the lore for the pickaxe.
		for(int i = 0; i < lore.size(); i++) {
			lore.set(i, handleConfigString(lore.get(i)));
		}
		
		if(levelData.getPickaxeAttributes().size() > 0) {
			// Let's add a space between the basic info and the attributes lore.
			lore.add(" ");
			
			// Let's add enchantments and show attributes.
			for(final PickaxeAttribute attribute : levelData.getPickaxeAttributes()) {
				// Let's add the vanilla enchantments to the pickaxe.
				if(attribute instanceof PickaxeEnchantment && ((PickaxeEnchantment) attribute).isVanillaEnchantment()) {
					final PickaxeEnchantment enchantment = (PickaxeEnchantment) attribute;
					meta.addEnchant(Enchantment.getByName(enchantment.getEnchantmentName()), enchantment.getLevel(), true);
				}
				
				// Let's list the cool effects.
				lore.add(ChatColor.translateAlternateColorCodes('&', attribute.getName()));
			}
		}
		
		// Let's update our metadata.
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.setLore(lore);
		pickaxe.setItemMeta(meta);
		
		return pickaxe;
	}
	
	/**
	 * Replaces variable spaces with what's expected and colors the string.
	 * @param The input string to update.
	 * @return A colored and corrected string based off the input string.
	 */
	
	private String handleConfigString(String configString) {
		final int maxLevel = DragonScale.singleton.getSettings().getMaxLevel();
		configString = configString.replaceAll("\\{LEVEL\\}", String.valueOf((level < maxLevel) ? level : "MAX"));
		configString = configString.replaceAll("\\{EXP\\}", String.valueOf((level < maxLevel) ? exp : 0));
		configString = configString.replaceAll("\\{NEEDEDEXP\\}", String.valueOf((level < maxLevel) ? levelData.getLevelUpExp() : 0));
		return ChatColor.translateAlternateColorCodes('&', configString);
	}
	
	public static boolean isPluginPickaxe(final DragonScale mainInstance, final ItemStack item) {
		//final String specialPickaxeName = (String) mainInstance.getSettings().getSetting("Pickaxe Name").getCurrent();
		//final String pickaxeName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', specialPickaxeName));
		//final String itemName = (item.hasItemMeta()) ? ChatColor.stripColor(item.getItemMeta().getDisplayName()) : item.getType().name();
		return (item != null && item.getType().name().contains("PICKAXE")); //&& pickaxeName.equalsIgnoreCase(itemName));
	}
	
	/**
	 * Fetches the {@link Player} who is holding this pickaxe.
	 * @return Player instance
	 */
	
	public Player getWielder() {
		return this.wielder;
	}
	
	/**
	 * Fetches the {@link LevelData} instance.
	 * @return LevelData instance
	 */
	
	public LevelData getLevelData() {
		return this.levelData;
	}
	
	/**
	 * Sets the level of this pickaxe.
	 * Calls the {@link DragonScalePickaxeLevelChangeEvent} if the newLevel doesn't equal the current level.
	 * @param newLevel - What to set the level to.
	 */
	
	public void setLevel(int newLevel) {
		final int prevLevel = level;
		this.level = newLevel;
		
		if(prevLevel != newLevel) {
			dequipIfHeld();
			levelData.setLevel(newLevel);
			levelData.assignAttributesToPickaxe(this);
			
			// Our level changed, let's call our event!
			final DragonScalePickaxeLevelChangeEvent levelEvt = new DragonScalePickaxeLevelChangeEvent(this, prevLevel, newLevel);
			DragonScale.singleton.getServer().getPluginManager().callEvent(levelEvt);
		}

	}
	
	/**
	 * Fetches the current level of this pickaxe.
	 * @return int
	 */
	
	public int getLevel() {
		return this.level;
	}
	
	/**
	 * Increments the pickaxe's experience by the specified amount.
	 * See {@link #setExp(int)} for more information.
	 * @param increment - The amount to increment the experience by.
	 */
	
	public void incrementExp(int increment) {
		this.setExp(getExp() + increment);
	}
	
	/**
	 * Sets the experience of this pickaxe.
	 * Calls the {@link DragonScalePickaxeExpChangeEvent} if the newExp doesn't equal the current experience.
	 * @param newExp - What to set the exp to.
	 */
	
	public void setExp(int newExp) {
		final int maxLevel = DragonScale.singleton.getSettings().getMaxLevel();
		final int prevExp = exp;
		this.exp = (level < maxLevel) ? newExp : 0;
		
		if(prevExp != newExp) {
			final DragonScalePickaxeExpChangeEvent expChangeEvt = new DragonScalePickaxeExpChangeEvent(this, prevExp, newExp);
			DragonScale.singleton.getServer().getPluginManager().callEvent(expChangeEvt);
		}
	}
	
	/**
	 * Fetches the current exp of this pickaxe.
	 * @return int
	 */
	
	public int getExp() {
		return this.exp;
	}
	
	/**
	 * Fetches a HashSet of {@link Block} that were affected by
	 * a mine event this period.
	 * @return HashSet<Block>
	 */
	
	public HashSet<Block> getMinedThisPeriod() {
		return this.minedThisPeriod;
	}
	
}
