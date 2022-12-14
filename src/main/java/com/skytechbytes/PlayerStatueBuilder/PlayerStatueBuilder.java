package com.skytechbytes.PlayerStatueBuilder;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import com.skytechbytes.PlayerStatueBuilder.builder.AssetManager;
import com.skytechbytes.PlayerStatueBuilder.support.VaultWrapper;
import com.skytechbytes.PlayerStatueBuilder.support.WorldGuardWrapper;
/**
 * 
 * @author SkyTechBytes
 *
 */
public class PlayerStatueBuilder extends JavaPlugin {
	public static PlayerStatueBuilder instance;
	public static WorldGuardWrapper wgw = null;
	public static VaultWrapper vw = null;
	public static StatueConfiguration sc;
	public PlayerStatueBuilder() {
		// TODO Auto-generated constructor stub
	}
	 // Fired when plugin is first enabled
    @Override
    public void onEnable() {
    	instance = this;
    	
    	sc = new StatueConfiguration();
    	sc.createConfig();
    	
    	try {
    		wgw = new WorldGuardWrapper();
    	} catch (Throwable fit) {
    		Log.log("Worldguard not detected. You MUST have WorldGuard if you want PlayerStatueBuilderX to respect claim/region protections.");
    		wgw = null;
    	}
    	
    	try {
    		vw = new VaultWrapper();
    	} catch (Throwable rock) {
    		vw = null;
    	}
    	
    	AssetManager.initialize();
    	
    	this.getCommand("statue").setExecutor(new CommandStatue());
    	this.getCommand("statue").setTabCompleter(new StatueTabComplete());
    	
    	this.getCommand("undostatue").setExecutor(new CommandUndostatue());
    	
    	//Bstats begins here
    	int pluginId = 7093;
    	Metrics m = new Metrics(this,pluginId);
    	int version = Metrics.B_STATS_VERSION;
    	
    }
    // Fired when plugin is disabled
    @Override
    public void onDisable() {

    }
}
