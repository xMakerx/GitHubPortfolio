/**
 * SchematicManager v1.0 Utility
 * Based on TerrainManager by desht
 * Makes saving and loading schematics much easier.
 * Built upon WorldEdit 6.1 for Minecraft 1.8
 * @author xMakerx
 */

package org.xmakerx.raidpractice.util;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.util.io.file.FilenameException;

@SuppressWarnings("deprecation")
public class SchematicManager {
	
	private static String extension = "schematic";
	protected EditSession editSession;
	protected LocalSession localSession;
	
	public SchematicManager(final WorldEditPlugin worldEdit, final World world) {
		this.localSession = new LocalSession(worldEdit.getLocalConfiguration());
		this.editSession = new EditSession(new BukkitWorld(world), worldEdit.getLocalConfiguration().maxChangeLimit);
	}
	
	/**
	 * Updates the file extension for schematics.
	 * @param String extension
	 */
	
	public static void setExtension(String extension) {
		// Remove any dots if they were added.
		extension = extension.replaceAll("\\.", "");
		
		// Finally, set the new extension.
		SchematicManager.extension = extension;
	}
	
	public static String getExtension() {
		return SchematicManager.extension;
	}
	
	/**
	 * Saves a schematic at the path you specify.
	 * @param File saveFile
	 * @param Location l1
	 * @param Location l2
	 * @throws DataException
	 * @throws IOException
	 * @throws NullPointerException if you specified a null clipboard.
	 */
	
	public void saveSchematic(final File saveFile, final Location l1, final Location l2) throws DataException, IOException, NullPointerException {
		final Vector min = new Vector(
                Math.min(l1.getBlockX(), l2.getBlockX()),
                Math.min(l1.getBlockY(), l2.getBlockY()),
                Math.min(l1.getBlockZ(), l2.getBlockZ())
		);
		
		final Vector max = new Vector(
                Math.max(l1.getBlockX(), l2.getBlockX()),
                Math.max(l1.getBlockY(), l2.getBlockY()),
                Math.max(l1.getBlockZ(), l2.getBlockZ())
		);
		
		// Get the correct save location and save the schematic.
		try {
			final File schemFile = WorldEdit.getInstance().getSafeSaveFile(null, saveFile.getParentFile(), saveFile.getName(), 
					extension, new String[] {extension});
			
			editSession.enableQueue();
			final CuboidClipboard clipboard = new CuboidClipboard(max.subtract(min).add(new Vector(1, 1, 1)), min);
			clipboard.copy(editSession);
			SchematicFormat.MCEDIT.save(clipboard, schemFile);
			editSession.flushQueue();
			
		}catch(FilenameException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads a schematic if it exists.
	 * @param File schematicFile
	 * @throws DataException
	 * @throws IOException
	 * @return CuboidClipboard of the schematic or null if it doesn't exist.
	 */
	
	public CuboidClipboard loadSchematic(final File schematicFile) throws DataException, IOException {
		try {
			final File file = WorldEdit.getInstance().getSafeSaveFile(null, schematicFile.getParentFile(), schematicFile.getName(), 
					extension, new String[] {extension});
			return SchematicFormat.MCEDIT.load(file);
		}catch(FilenameException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Pastes a schematic at its origin.
	 * @param CuboidClipboard schematic the actual schematic to paste.
	 * @param World world
	 * @param boolean wantAir if air should be pasted.
	 * @param boolean wantEntities if entities should be pasted.
	 * @throws NullPointerException, MaxChangedBlocksException 
	 */
	
	public void pasteSchematic(final CuboidClipboard schematic, final World world, boolean wantAir, boolean wantEntities) throws NullPointerException, MaxChangedBlocksException {
		// Handle a null schematic.
		if(schematic == null) {
			throw new NullPointerException("Cannot paste a schematic that doesn't exist!");
		}
		
		// Handle attempt to paste a schematic as the world.
		pasteSchematic(schematic, wantAir, wantEntities);
	}
	
	private void pasteSchematic(final CuboidClipboard schematic, boolean wantAir, boolean wantEntities) throws NullPointerException, MaxChangedBlocksException {
		if(localSession == null || editSession == null) throw new NullPointerException("Invalid local session or edit session.");
		
		// Let's finally paste the schematic.
		editSession.enableQueue();
		schematic.paste(editSession, schematic.getOrigin(), !wantAir, wantEntities);
		editSession.flushQueue();
		WorldEdit.getInstance().flushBlockBag(null, editSession);
	}
}
