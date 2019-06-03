package com.coginvasion.stridebases.schematic;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import org.jnbt.ByteArrayTag;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.ShortTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

import com.coginvasion.stridebases.StrideBases;

public class SchematicUtils {
	
	private static StrideBases instance;
	
	public SchematicUtils(final StrideBases main) {
		SchematicUtils.instance = main;
	}
	
	public static Schematic loadSchematic(final File file) {
		try {
			if(file != null && file.exists()) {
				final NBTInputStream nbtStream = new NBTInputStream(new FileInputStream(file));
				final CompoundTag compound = (CompoundTag) nbtStream.readTag();
				final Map<String, Tag> tags = compound.getValue();
				Short width = ((ShortTag) tags.get("Width")).getValue();
				Short height = ((ShortTag) tags.get("Height")).getValue();
				Short length = ((ShortTag) tags.get("Length")).getValue();
				
				String materials = ((StringTag) tags.get("Materials")).getValue();
				
				byte[] blocks = ((ByteArrayTag) tags.get("Blocks")).getValue();
				byte[] data = ((ByteArrayTag) tags.get("Data")).getValue();
				
				nbtStream.close();
				
				Schematic schematic = new Schematic(file.getName().replace(".schematic", ""), width, height, length, materials, blocks, data);
				
				return schematic;
			}
		}catch(Exception e) {
			SchematicUtils.instance.getLogger().severe(String.format("Failed to load schematic. Error: %s.", e.getMessage()));
		}
		
		return null;
	}
}
