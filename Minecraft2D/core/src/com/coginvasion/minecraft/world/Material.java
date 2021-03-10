package com.coginvasion.minecraft.world;

import static com.coginvasion.minecraft.Globals.BLOCK_SCALE;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.coginvasion.minecraft.Globals;
import com.coginvasion.minecraft.item.ItemStack;
import com.coginvasion.minecraft.item.ItemType;
import com.coginvasion.minecraft.meta.BlockMeta;
import com.coginvasion.minecraft.meta.IMetadata;
import com.coginvasion.minecraft.meta.ItemMeta;

public enum Material {
	AIR("Air", -1, -1, new String[] {}, new String[] {}, new IMetadata[] {
			new BlockMeta().setFlattensPlants(false)
			.setDurability(-1.0f).setCanCollide(false)
	}),
	COBBLESTONE("Cobblestone", 0, 1, Globals.STONE_STEP_SOUND_PATHS, Globals.STONE_DIG_SOUND_PATHS,
	new IMetadata[] {
		new BlockMeta().setFlattensPlants(true)
		.setDurability(2.5f).setCanCollide(true)
		.setLightLoss(0.05f).addDrop("COBBLESTONE", 1)
	}),
	STONE("Stone", 1, 0, Globals.STONE_STEP_SOUND_PATHS, Globals.STONE_DIG_SOUND_PATHS, new IMetadata[] {
			new BlockMeta().setFlattensPlants(true)
			.setDurability(2.5f).setCanCollide(true)
			.setLightLoss(0.05f).addDrop(new ItemStack(Material.COBBLESTONE, 1))
	}),
	MOSSY_COBBLESTONE("Mossy Cobblestone", 4, 2, COBBLESTONE.getStepSoundPaths(), COBBLESTONE.getDigSoundPaths(),
	new IMetadata[] {
		new BlockMeta().setFlattensPlants(true)
		.setDurability(2.5f).setCanCollide(true)
		.setLightLoss(0.05f).addDrop("MOSSY_COBBLESTONE", 1)
	}),
	DIRT("Dirt", 2, 0, Globals.GRASS_STEP_SOUND_PATHS, Globals.GRASS_DIG_SOUND_PATHS, 
		new IMetadata[] { new BlockMeta().setLightLoss(0.05f).addDrop("DIRT", 1)}),
	GRAVEL("Gravel", 3, 1, Globals.GRAVEL_STEP_SOUND_PATHS, Globals.GRAVEL_DIG_SOUND_PATHS, new IMetadata[] {
		new BlockMeta().setFlattensPlants(true)
		.setDurability(1.5f).setCanCollide(true)
		.setLightLoss(0.05f).addDrop("GRAVEL", 1)
	}),
	GRASS("Grass", 3, 0, Globals.GRASS_STEP_SOUND_PATHS, Globals.GRASS_DIG_SOUND_PATHS, DIRT.getMetadata()),
	BEDROCK("Bedrock", 1, 1, STONE.getStepSoundPaths(), STONE.getDigSoundPaths(), new IMetadata[] {
	    new BlockMeta().setCanCollide(true).setDurability(999.0f).setCanCollide(true)
	    .setLightLoss(1.0f)
	}),
	OAK_PLANKS("Oak Planks", 4, 0, Globals.WOOD_STEP_SOUND_PATHS, Globals.WOOD_DIG_SOUND_PATHS,
	new IMetadata[] { new BlockMeta().setLightLoss(0.1f).addDrop("OAK_PLANKS", 1) }),
	OAK_LOG("Oak Log", 4, 1, OAK_PLANKS.getStepSoundPaths(), OAK_PLANKS.getDigSoundPaths(), OAK_PLANKS.getMetadata()),
	SAND("Sand", 2, 1, Globals.SAND_STEP_SOUND_PATHS, Globals.SAND_DIG_SOUND_PATHS, new IMetadata[] {
			new BlockMeta().setFlattensPlants(true)
			.setDurability(1.5f).setCanCollide(true)
			.setLightLoss(0.05f).addDrop("SAND", 1)
	}),
	CACTUS("Cactus", 6, 4, new String[] {}, Globals.CLOTH_DIG_SOUND_PATHS, new IMetadata[] {
		new BlockMeta().setCanCollide(true).setContactDamage(0.5d)
		.setDurability(2.5f).setLightLoss(0.01f)
		.addDrop("CACTUS", 1).setOpaque(false)
	}),
	WATER("Water", 14, 12, new String[] {}, new String[] {}, new IMetadata[] {
		new BlockMeta().setCanCollide(false).setDurability(-1.0f)
		.setFlattensPlants(true).setLightLoss(0.01f)
	}),
	LADDER("Ladder", 3, 5, Globals.LADDER_SOUND_PATHS, Globals.LADDER_SOUND_PATHS,
	new IMetadata[] {
		new BlockMeta().setCanCollide(false).setLightLoss(0.0f)
		.addDrop("LADDER", 1).setOpaque(false)
	}),
	TALL_GRASS("Tall Grass", 15, 3, new String[] {}, Globals.GRASS_DIG_SOUND_PATHS, new IMetadata[] {
		new BlockMeta().setCanCollide(false).setDurability(0.5f)
		.setFlattensPlants(false).setLightLoss(0.0f).addDrop("TALL_GRASS", 1).setOpaque(false)
	}),
	TORCH("Torch", 0, 5, new String[] {}, Globals.WOOD_DIG_SOUND_PATHS, new IMetadata[] {
		new BlockMeta().setCanCollide(false).setDurability(1.0f).setFlattensPlants(true)
		.setLightLoss(-0.5f).addDrop("TORCH", 1).setOpaque(false)
	}),
	TNT("TNT", 8, 0, new String[] {}, Globals.TNT_SOUND_PATHS, new IMetadata[] {
		new BlockMeta().setCanCollide(true).setDurability(0.5f).setFlattensPlants(true)
		.addDrop("TNT", 1)
	}),
	STONE_PICKAXE("Stone Pickaxe", 1, 6, new IMetadata[] { new ItemMeta(ItemType.PICKAXE, 3.5f)}),
	STONE_SHOVEL("Stone Shovel", 1, 5, new IMetadata[] { new ItemMeta(ItemType.SHOVEL, 3.5f)}),
	DIAMOND_PICKAXE("Diamond Pickaxe", 3, 6, new IMetadata[] { new ItemMeta(ItemType.PICKAXE, 12f)}),
	DIAMOND_SHOVEL("Diamond Shovel", 3, 5, new IMetadata[] { new ItemMeta(ItemType.SHOVEL, 12f)}),
	GOLD_PICKAXE("Gold Pickaxe", 4, 6, new IMetadata[] { new ItemMeta(ItemType.PICKAXE, 16f)}),
	GOLD_SHOVEL("Gold Shovel", 4, 5, new IMetadata[] { new ItemMeta(ItemType.SHOVEL, 16f)});
	
	private final String name;
	private TextureRegion texture;
	protected final int txtX, txtY;
	protected final String texturePath;
	protected String[] stepSoundsPath;
	protected String[] digSoundsPath;
	protected IMetadata[] metadata;
	
	private Material(String name, int textureX, int textureY, String[] ssPath, String[] digSPath, IMetadata[] metadata) {
		this.name = name;
		this.txtX = textureX;
		this.txtY = textureY;
		this.texturePath = "images/tileset.png";
		this.stepSoundsPath = ssPath;
		this.digSoundsPath = digSPath;
		this.metadata = metadata;
	}
	
	private Material(String name, int textureX, int textureY, IMetadata[] metadata) {
		this.name = name;
		this.txtX = textureX;
		this.txtY = textureY;
		this.texturePath = "images/items.png";
		this.stepSoundsPath = new String[] {};
		this.digSoundsPath = new String[] {};
		this.metadata = metadata;
	}
	
	private void setTexture(TextureRegion tex) {
		this.texture = tex;
	}
	
	public TextureRegion getTexture() {
		return this.texture;
	}
	
	public BlockMeta getBlockMeta() {
		for(IMetadata data : metadata) {
			if(data instanceof BlockMeta) {
				return (BlockMeta) data;
			}
		}
		
		return null;
	}
	
	public ItemMeta getItemMeta() {
		for(IMetadata data : metadata) {
			if(data instanceof ItemMeta) {
				return (ItemMeta) data;
			}else if(data instanceof BlockMeta) {
				final ItemMeta meta = new ItemMeta(ItemType.COLLECTIBLE);
				return meta;
			}
		}
		
		return null;
	}
	
	public static void load(AssetManager manager) {
		for(Material mat : Material.values()) {
			if(mat.txtX >= 0) {
				Texture texture = manager.get(mat.texturePath, Texture.class);
				mat.setTexture(new TextureRegion(texture, 
					mat.txtX * BLOCK_SCALE, 
					mat.txtY * BLOCK_SCALE, 
				BLOCK_SCALE, BLOCK_SCALE));
			}
			
			Sound[] stepSounds = new Sound[mat.stepSoundsPath.length];
			Sound[] digSounds = new Sound[mat.digSoundsPath.length];
			
			for(int i = 0; i < mat.stepSoundsPath.length; i++) {
				final String soundPath = mat.stepSoundsPath[i];
				Sound sound = manager.get(soundPath, Sound.class);
				stepSounds[i] = sound;
			}
			
			for(int i = 0; i < mat.digSoundsPath.length; i++) {
				final String soundPath = mat.digSoundsPath[i];
				Sound sound = manager.get(soundPath, Sound.class);
				digSounds[i] = sound;
			}
			
			if(mat.getBlockMeta() != null) {
				mat.getBlockMeta().setStepSounds(stepSounds);
				mat.getBlockMeta().setDigSounds(digSounds);
				mat.getBlockMeta().fixTemporaryDrops();
			}
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public IMetadata[] getMetadata() {
		return this.metadata;
	}
	
	public String[] getStepSoundPaths() {
		return this.stepSoundsPath;
	}
	
	public String[] getDigSoundPaths() {
		return this.digSoundsPath;
	}
	
}
