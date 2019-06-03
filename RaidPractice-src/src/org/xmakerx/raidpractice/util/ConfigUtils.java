package org.xmakerx.raidpractice.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.configuration.file.YamlConfiguration;
import org.xmakerx.raidpractice.RaidPractice;

public class ConfigUtils {
	
	public static void copy(InputStream in, File file) {
		try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void save(final File file, final YamlConfiguration config) throws IOException {
		config.save(file);
	}
	
	public static void update(final RaidPractice instance, final String fileName, final YamlConfiguration config, final File configFile) {
		@SuppressWarnings("deprecation")
		final YamlConfiguration jarConfig = YamlConfiguration.loadConfiguration(instance.getResource(fileName));
		boolean upToDate = true;
		
		// Let's start comparing values.
		for(String valueName : jarConfig.getKeys(true)) {
			if(config.get(valueName) == null) {
				upToDate = false;
				config.set(valueName, jarConfig.get(valueName));
			}
		}
		
		// Now, let's see if we have extra values that don't exist in the jar version.
		for(String valueName : config.getKeys(true)) {
			if(jarConfig.get(valueName) == null) {
				upToDate = false;
				config.set(valueName, null);
			}
		}
		
		try {
			ConfigUtils.save(configFile, config);
		} catch (IOException e) {
			instance.getLogger().severe(String.format("Failed to save %s. Error: %s.", fileName, e.getMessage()));
		}
		
		if(upToDate) {
			instance.getLogger().info(String.format("%s is up to date.", fileName));
		}else {
			instance.getLogger().info(String.format("Brought %s up to date.", fileName));
		}
	}
}