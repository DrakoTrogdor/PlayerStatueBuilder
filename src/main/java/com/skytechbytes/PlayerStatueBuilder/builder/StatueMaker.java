package com.skytechbytes.PlayerStatueBuilder.builder;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.skytechbytes.PlayerStatueBuilder.LegacyConverter;
import com.skytechbytes.PlayerStatueBuilder.Log;
import com.skytechbytes.PlayerStatueBuilder.PlayerStatueBuilder;
/**
 * 
 * @author SkyTechBytes
 * This is where the magic happens- all the mappings!
 *
 */
public class StatueMaker extends BukkitRunnable {
	
	public static HashMap<String,Long> cooldowns = new HashMap<>();
	
	private Schematic s;
	private World w;
	private Player p;
	private Location origin;
	private boolean quote = false;
	private BufferedImage bi;
	private String mode;
	private LinkedHashMap<String, Float> params;

	public StatueMaker(World w, Player p, String mode, BufferedImage bi, boolean quote, LinkedHashMap<String, Float> flags) {
		s = new Schematic();
		this.w = w;
		this.p = p;
		this.quote = quote;
		this.bi = bi;
		this.mode = mode;
		this.params = flags;
		origin = new Location(w,p.getLocation().getBlockX(),p.getLocation().getBlockY(),p.getLocation().getBlockZ());
	}
	private void initialize() {
		FaceBuilder.minor_orientation = 0;

		String d = getDirection(p.getLocation().getYaw());
		
		//Log.log(d);

		if (d.equals("North")) {
			FaceBuilder.minor_orientation = 1;
		} else if (d.equals("East")) {
			FaceBuilder.minor_orientation = 2;
		} else if (d.equals("West")) {
			FaceBuilder.minor_orientation = 3;
		} else if (d.equals("South")) {
			FaceBuilder.minor_orientation = 0;
		}
	}
	/**
	 * Converts a rotation to a cardinal direction name.
	 * From sk89qs's command book plugin
	 * @param rot
	 * @return
	 */
	private static String getDirection(double rot) {
		if (rot < 0) {
			rot += 360;
		}
		if (0 <= rot && rot < 45) {
			return "North";
		} else if (45 <= rot && rot < 135) {
			return "East";
		}else if (135 <= rot && rot < 225) {
			return "South";
		} else if (225 <= rot && rot < 315) {
			return "West";
		} else if (315 <= rot && rot <= 360.0) {
			return "North";
		} else {
			Log.log("The Player's direction is somehow negative or greater than 360. Might want to report this.");
			return "North";
		}
		
	}
	public boolean hasCooldown(Player p) {
		if (p.hasPermission("playerstatuebuilderx.noWait")) {
			return false;
		}
		if (cooldowns.get(p.getName()) == null) {
			return false;
		}
		//cooldown has passed
		if (cooldowns.get(p.getName()) < System.currentTimeMillis()) {
			return false;
		}
		//millis
		long timeLeft =  cooldowns.get(p.getName()) - System.currentTimeMillis();
		
		long min = timeLeft / 60000;
		long sec = (timeLeft % 60000)/1000;
		
		p.sendMessage(ChatColor.RED + "You've created a statue recently. Please wait at least " + min + " minutes and " + sec + " seconds.");
		return true;
	}
	/**
	 * Actually make the statue from the sketchpad
	 */
	private void finish() {

		if (quote) {
			s.query(p);
			return;
		}

		boolean canBuild = s.canBuild(w, p);

		if (canBuild == false) {
			p.sendMessage(ChatColor.RED + "Insufficient build permissions. Move to a place where you're allowed to build.");
			return;
		}
		
		if (hasCooldown(p) == true) {
			return;
		}
		
		Log.log("Trying to remove items...");
		boolean takeMaterials = s.removeItems(p);
		
		if (takeMaterials == false) {
			Log.log("Insufficient materials");
			return;
		}
		Log.log("Creating Structure...");
		s.createStatue(w,p,false);

		cooldowns.put(p.getName(), System.currentTimeMillis() + PlayerStatueBuilder.instance.getConfig().getInt("cooldown") * 60000);
		
		p.sendMessage(ChatColor.GREEN + "Statue Created!");
		/**
		 * Now ready to be undone!
		 */
		Schematic.history.add(s);
	}
	@Override
	public void run() {
		
		try {
			if (mode.equals("default")) {
				this.makeStatue(origin, bi);
			} else if (mode.equals("slim")) {
				this.makeSlimStatue(origin, bi);
			} else if (mode.equals("legacy")) {
				this.makeLegacyStatue(origin, bi);
			} else {
				this.makeStatue(origin, bi);
			}
		} catch (Exception e) {
			p.sendMessage(ChatColor.RED + "Error! " + e.getMessage());
		}
	}
	/**
	 * Warning: the original image is not changed in this process. A new image is returned.
	 * @param l
	 * @param ss
	 * @return
	 * @throws Exception 
	 */
	private BufferedImage preProcessing(Location l, BufferedImage ss) throws Exception {
		/*
		 * Add armor to the player if they request it (or just add parts)
		 */
		
		BufferedImage customizedImage = ImageUtil.deepCopy(ss);
		
		customizedImage = ImageUtil.applyFilters(customizedImage, params);
		
		for (String key : AssetManager.armor.keySet()) {
			if (params.keySet().contains(key)) {
				//Since BufferedImage is mutable, I should be able to modify the bufferedImage inside the method and it should reflect outside.
				
				customizedImage = ImageUtil.overlayImage(customizedImage, AssetManager.armor.get(key));
				
			}
		}
		return customizedImage;
		
	}
	private void right_leg(FaceBuilder fb, Reader r, Location l, BufferedImage ss) throws Exception {
		//Right Leg
				fb.buildFace(l, r.part(ss, 8, 16, 4, 4), 1,0,1,-3,false,true);
				fb.buildFace(l, r.part(ss, 0, 20, 4, 12), 2,0,0,-3);
				//fb.buildFace(l, r.part(ss, 8, 16, 4, 4), 1,0,12,-3);
				fb.buildFace(l, r.part(ss, 12, 20, 4, 12), 0,0,0,-3,true,false);
				fb.buildFace(l, r.part(ss, 4, 20, 4, 12), 0,0,0,0);
				//Right Pantleg
				fb.buildFace(l, r.part(ss, 8, 16+16, 4, 4), 1,0,0,-3,false,true);
				fb.buildFace(l, r.part(ss, 0, 20+16, 4, 12), 2,-1,0,-3);
				//fb.buildFace(l, r.part(ss, 8, 16, 4, 4), 1,0,12,-3);
				fb.buildFace(l, r.part(ss, 12, 20+16, 4, 12), 0,0,0,-4,true,false);
				fb.buildFace(l, r.part(ss, 4, 20+16, 4, 12), 0,0,0,1);
	}
	private void left_leg(FaceBuilder fb,Reader r,Location l, BufferedImage ss) throws Exception {
		//Left Leg
				fb.buildFace(l, r.part(ss, 8+16, 16+32, 4, 4), 1,0+4,1,-3,false,true);
				fb.buildFace(l, r.part(ss, 24, 52, 4, 12), 2,0+7,0,-3,true,false);
				//fb.buildFace(l, r.part(ss, 8+16, 16+32, 4, 4), 1,0+4,12,-3);
				fb.buildFace(l, r.part(ss, 12+16, 20+32, 4, 12), 0,0+4,0,-3,true,false);
				fb.buildFace(l, r.part(ss, 4+16, 20+32, 4, 12), 0,0+4,0,0);
				//Left Pantleg
				fb.buildFace(l, r.part(ss, 8+16-16, 16+32, 4, 4), 1,0+4,1-1,-3,false,true);
				fb.buildFace(l, r.part(ss, 24-16, 52, 4, 12), 2,0+8,0,-3,true,false);
				//fb.buildFace(l, r.part(ss, 8+16, 16+32, 4, 4), 1,0+4,12,-3);
				fb.buildFace(l, r.part(ss, 12+16-16, 20+32, 4, 12), 0,0+4,0,-4,true,false);
				fb.buildFace(l, r.part(ss, 4+16-16, 20+32, 4, 12), 0,0+4,0,1);
	}
	private void body(FaceBuilder fb,Reader r,Location l, BufferedImage ss) throws Exception {
		//Body
				fb.buildFace(l, r.part(ss, 20, 20, 8, 12), 0,0,12,0);
				fb.buildFace(l, r.part(ss, 32, 20, 8, 12), 0,0,12,-3,true,false);
				//Jacket
				fb.buildFace(l, r.part(ss, 20, 20+16, 8, 12), 0,0,12,1);
				fb.buildFace(l, r.part(ss, 32, 20+16, 8, 12), 0,0,12,-4,true,false);
	}
	private void right_arm(FaceBuilder fb,Reader r,Location l, BufferedImage ss) throws Exception {
		//Right Arm
				fb.buildFace(l, r.part(ss, 8+40, 16, 4, 4), 1,0-4,1+12,-3,false,true);
				fb.buildFace(l, r.part(ss, 0+40, 20, 4, 12), 2,0-4,0+12,-3);
				fb.buildFace(l, r.part(ss, 8+40-4, 16, 4, 4), 1,0-4,12+12,-3);
				fb.buildFace(l, r.part(ss, 12+40, 20, 4, 12), 0,0-4,0+12,-3,true,false);
				fb.buildFace(l, r.part(ss, 4+40, 20, 4, 12), 0,0-4,0+12,0);
				//Right Sleeve
				fb.buildFace(l, r.part(ss, 8+40, 16+16, 4, 4), 1,0-4,1+12-1,-3,false,true);
				fb.buildFace(l, r.part(ss, 0+40, 20+16, 4, 12), 2,0-4-1,0+12,-3);
				fb.buildFace(l, r.part(ss, 44, 16+16, 4, 4), 1,0-4,12+12+1,-3);
				fb.buildFace(l, r.part(ss, 12+40, 20+16, 4, 12), 0,0-4,0+12,-3-1,true,false);
				fb.buildFace(l, r.part(ss, 4+40, 20+16, 4, 12), 0,0-4,0+12,1);
	}
	private void left_arm(FaceBuilder fb,Reader r,Location l, BufferedImage ss) throws Exception {
		//Left Arm
				fb.buildFace(l, r.part(ss, 8+16+16, 16+32, 4, 4), 1,0+4+4,1+12,-3,false,true);
				fb.buildFace(l, r.part(ss, 24+16, 52, 4, 12), 2,0+7+4,0+12,-3,true,false);
				fb.buildFace(l, r.part(ss, 8+16+16-4, 16+32, 4, 4), 1,0+4+4,12+12,-3);
				fb.buildFace(l, r.part(ss, 12+16+16, 20+32, 4, 12), 0,0+4+4,0+12,-3,true,false);
				fb.buildFace(l, r.part(ss, 4+16+16, 20+32, 4, 12), 0,0+4+4,0+12,0);
				//Left Sleeve
				fb.buildFace(l, r.part(ss, 8+16+16+16, 16+32, 4, 4), 1,0+4+4,1+12-1,-3,false,true);
				fb.buildFace(l, r.part(ss, 24+16+16, 52, 4, 12), 2,0+7+4+1,0+12,-3,true,false);
				fb.buildFace(l, r.part(ss, 8+16+16-4+16, 16+32, 4, 4), 1,0+4+4,12+12+1,-3);
				fb.buildFace(l, r.part(ss, 12+16+16+16, 20+32, 4, 12), 0,0+4+4,0+12,-3-1,true,false);
				fb.buildFace(l, r.part(ss, 4+16+16+16, 20+32, 4, 12), 0,0+4+4,0+12,1);
	}
	private void head(FaceBuilder fb,Reader r,Location l, BufferedImage ss) throws Exception {
		//Head
				fb.buildFace(l, r.part(ss, 16, 0, 8, 8), 1,0,1+24,-5,false,true);
				fb.buildFace(l, r.part(ss, 0, 8, 8, 8), 2,0,0+24,-5);
				fb.buildFace(l, r.part(ss, 16, 8, 8, 8), 2,0+7,0+24,-5,true,false);
				fb.buildFace(l, r.part(ss, 8, 0, 8, 8), 1,0,1+24+7,-5);
				fb.buildFace(l, r.part(ss, 24, 8, 8, 8), 0,0,24,-5,true,false);
				fb.buildFace(l, r.part(ss, 8, 8, 8, 8), 0,0,24,2);
				//Hat
				fb.buildFace(l, r.part(ss, 0+32, 8, 8, 8), 2,0-1,0+24,-5);
				fb.buildFace(l, r.part(ss, 16+32, 8, 8, 8), 2,0+7+1,0+24,-5,true,false);
				fb.buildFace(l, r.part(ss, 8+32, 0, 8, 8), 1,0,1+24+7+1,-5);
				fb.buildFace(l, r.part(ss, 24+32, 8, 8, 8), 0,0,24,-5-1,true,false);
				fb.buildFace(l, r.part(ss, 8+32, 8, 8, 8), 0,0,24,2+1);
	}
	
	private void right_arm_slim(FaceBuilder fb,Reader r,Location l, BufferedImage ss) throws Exception {
		//Right Arm
				fb.buildFace(l, r.part(ss, 8+40-1, 16, 3, 4), 1,0-4+1,1+12,-3,false,true);
				fb.buildFace(l, r.part(ss, 0+40, 20, 4, 12), 2,0-4+1,0+12,-3);//Done
				fb.buildFace(l, r.part(ss, 8+40-4, 16, 3, 4), 1,0-4+1,12+12,-3);
				fb.buildFace(l, r.part(ss, 12+40-1, 20, 3, 12), 0,0-4+1,0+12,-3,true,false);
				fb.buildFace(l, r.part(ss, 4+40, 20, 3, 12), 0,0-4+1,0+12,0);
				//Right Sleeve
				fb.buildFace(l, r.part(ss, 8+40-1, 16+16, 3, 4), 1,0-4+1,1+12-1,-3,false,true);
				fb.buildFace(l, r.part(ss, 0+40, 20+16, 4, 12), 2,0-4+1-1,0+12,-3);//Done
				fb.buildFace(l, r.part(ss, 44, 16+16, 3, 4), 1,0-4+1,12+12+1,-3);//Unsure
				fb.buildFace(l, r.part(ss, 12+40-1, 20+16, 3, 12), 0,0-4+1,0+12,-3-1,true,false);
				fb.buildFace(l, r.part(ss, 4+40, 20+16, 3, 12), 0,0-4+1,0+12,1);
	}
	private void left_arm_slim(FaceBuilder fb,Reader r,Location l, BufferedImage ss) throws Exception {
		//Left Arm
				fb.buildFace(l, r.part(ss, 8+16+16-1, 16+32, 3, 4), 1,0+4+4,1+12,-3,false,true);
				fb.buildFace(l, r.part(ss, 24+16-1, 52, 4, 12), 2,0+7+4-1,0+12,-3,true,false);//Done
				fb.buildFace(l, r.part(ss, 8+16+16-4, 16+32, 3, 4), 1,0+4+4,12+12,-3);
				fb.buildFace(l, r.part(ss, 12+16+16-1, 20+32, 3, 12), 0,0+4+4,0+12,-3,true,false);
				fb.buildFace(l, r.part(ss, 4+16+16, 20+32, 3, 12), 0,0+4+4,0+12,0);
				//Left Sleeve
				fb.buildFace(l, r.part(ss, 8+16+16+16-1, 16+32, 3, 4), 1,0+4+4,1+12-1,-3,false,true);
				fb.buildFace(l, r.part(ss, 24+16+16-1, 52, 4, 12), 2,0+7+4+1-1,0+12,-3,true,false);//Done
				fb.buildFace(l, r.part(ss, 8+16+16-4+16, 16+32, 3, 4), 1,0+4+4,12+12+1,-3);
				fb.buildFace(l, r.part(ss, 12+16+16+16-1, 20+32, 3, 12), 0,0+4+4,0+12,-3-1,true,false);
				fb.buildFace(l, r.part(ss, 4+16+16+16, 20+32, 3, 12), 0,0+4+4,0+12,1);
	}
	private boolean isSlimSkin(BufferedImage xx) {
		if (xx.getWidth() < 64 || xx.getHeight() < 64) {
			return false;
		}
		//Checks the top left hand side of the right and left arms to see if they are transparent (if they are it's probably slim)
		if (new Color(xx.getRGB(54, 20), true).getAlpha() < 255) {
			return true;
		}
		if (new Color(xx.getRGB(46, 52), true).getAlpha() < 255) {
			return true;
		}
		return false;
		
	}
	/**
	 * Warning: the original "xx" image should not be changed (other than to convert to the new skin format). ONLY modify the ss image.
	 * @param l
	 * @param xx
	 * @throws Exception
	 */
	private void makeStatue(Location l, BufferedImage xx) throws Exception {
		initialize();

		if (xx.getHeight() < 64) {
			makeStatue(l,LegacyConverter.convertLegacy(xx,false));
			p.sendMessage(ChatColor.YELLOW + "You attempted to make a statue with a 64x64 (newer format) but the skin you specified is"
					+ " smaller than 64x64, so the plugin made a legacy skin statue instead.");
			return;
		}
		if (isSlimSkin(xx)) {
			makeSlimStatue(l, xx);
			p.sendMessage(ChatColor.YELLOW + "You attempted to make a classic statue (4 pixel arms) but the skin you specified"
					+ " appears to have 3 pixel arms, so the plugin made a slim skin statue instead.");
			return;
		}
		
		BufferedImage ss = preProcessing(l,xx);

		//0 is vertical |_
		//1 is flat |-
		//2 is sideways |_
		//Bottom, Right, Left, Top, Back, Front
		//Right Leg
		FaceBuilder fb = new FaceBuilder(s);
		Reader r = new Reader();
		
		//see if the player wants us to just build one part
		boolean builtSomething = false;
		Set<String> flags = params.keySet();
		if (flags.contains("right_leg")) {
			right_leg(fb,r,l,ss);
			builtSomething = true;
		}
		if (flags.contains("left_leg")) {
			left_leg(fb,r,l,ss);
			builtSomething = true;
		}
		if (flags.contains("body")) {
			body(fb,r,l,ss);
			builtSomething = true;
		}
		if (flags.contains("right_arm")) {
			right_arm(fb,r,l,ss);
			builtSomething = true;
		}
		if (flags.contains("left_arm")) {
			left_arm(fb,r,l,ss);
			builtSomething = true;
		}
		if (flags.contains("head")) {
			head(fb,r,l,ss);
			builtSomething = true;
		}
		
		//if the player does not specify what part to build, just build the whole thing
		if (builtSomething == false) {
			right_leg(fb,r,l,ss);
			left_leg(fb,r,l,ss);
			body(fb,r,l,ss);
			right_arm(fb,r,l,ss);
			left_arm(fb,r,l,ss);
			head(fb,r,l,ss);
		}
		
		/*
		//BufferedImage ss = r.read();
		fb.buildFace(l, r.part(ss, 8, 16, 4, 4), 1,0,1,-3,false,true);
		fb.buildFace(l, r.part(ss, 0, 20, 4, 12), 2,0,0,-3);
		//fb.buildFace(l, r.part(ss, 8, 16, 4, 4), 1,0,12,-3);
		fb.buildFace(l, r.part(ss, 12, 20, 4, 12), 0,0,0,-3,true,false);
		fb.buildFace(l, r.part(ss, 4, 20, 4, 12), 0,0,0,0);
		//Right Pantleg
		fb.buildFace(l, r.part(ss, 8, 16+16, 4, 4), 1,0,0,-3,false,true);
		fb.buildFace(l, r.part(ss, 0, 20+16, 4, 12), 2,-1,0,-3);
		//fb.buildFace(l, r.part(ss, 8, 16, 4, 4), 1,0,12,-3);
		fb.buildFace(l, r.part(ss, 12, 20+16, 4, 12), 0,0,0,-4,true,false);
		fb.buildFace(l, r.part(ss, 4, 20+16, 4, 12), 0,0,0,1);
		
		//Left Leg
		fb.buildFace(l, r.part(ss, 8+16, 16+32, 4, 4), 1,0+4,1,-3,false,true);
		fb.buildFace(l, r.part(ss, 24, 52, 4, 12), 2,0+7,0,-3,true,false);
		//fb.buildFace(l, r.part(ss, 8+16, 16+32, 4, 4), 1,0+4,12,-3);
		fb.buildFace(l, r.part(ss, 12+16, 20+32, 4, 12), 0,0+4,0,-3,true,false);
		fb.buildFace(l, r.part(ss, 4+16, 20+32, 4, 12), 0,0+4,0,0);
		//Left Pantleg
		fb.buildFace(l, r.part(ss, 8+16-16, 16+32, 4, 4), 1,0+4,1-1,-3,false,true);
		fb.buildFace(l, r.part(ss, 24-16, 52, 4, 12), 2,0+8,0,-3,true,false);
		//fb.buildFace(l, r.part(ss, 8+16, 16+32, 4, 4), 1,0+4,12,-3);
		fb.buildFace(l, r.part(ss, 12+16-16, 20+32, 4, 12), 0,0+4,0,-4,true,false);
		fb.buildFace(l, r.part(ss, 4+16-16, 20+32, 4, 12), 0,0+4,0,1);
		
		//Body
		fb.buildFace(l, r.part(ss, 20, 20, 8, 12), 0,0,12,0);
		fb.buildFace(l, r.part(ss, 32, 20, 8, 12), 0,0,12,-3,true,false);
		//Jacket
		fb.buildFace(l, r.part(ss, 20, 20+16, 8, 12), 0,0,12,1);
		fb.buildFace(l, r.part(ss, 32, 20+16, 8, 12), 0,0,12,-4,true,false);
		
		//Right Arm
		fb.buildFace(l, r.part(ss, 8+40, 16, 4, 4), 1,0-4,1+12,-3,false,true);
		fb.buildFace(l, r.part(ss, 0+40, 20, 4, 12), 2,0-4,0+12,-3);
		fb.buildFace(l, r.part(ss, 8+40-4, 16, 4, 4), 1,0-4,12+12,-3);
		fb.buildFace(l, r.part(ss, 12+40, 20, 4, 12), 0,0-4,0+12,-3,true,false);
		fb.buildFace(l, r.part(ss, 4+40, 20, 4, 12), 0,0-4,0+12,0);
		//Right Sleeve
		fb.buildFace(l, r.part(ss, 8+40, 16+16, 4, 4), 1,0-4,1+12-1,-3,false,true);
		fb.buildFace(l, r.part(ss, 0+40, 20+16, 4, 12), 2,0-4-1,0+12,-3);
		fb.buildFace(l, r.part(ss, 44, 16+16, 4, 4), 1,0-4,12+12+1,-3);
		fb.buildFace(l, r.part(ss, 12+40, 20+16, 4, 12), 0,0-4,0+12,-3-1,true,false);
		fb.buildFace(l, r.part(ss, 4+40, 20+16, 4, 12), 0,0-4,0+12,1);
		
		//Left Arm
		fb.buildFace(l, r.part(ss, 8+16+16, 16+32, 4, 4), 1,0+4+4,1+12,-3,false,true);
		fb.buildFace(l, r.part(ss, 24+16, 52, 4, 12), 2,0+7+4,0+12,-3,true,false);
		fb.buildFace(l, r.part(ss, 8+16+16-4, 16+32, 4, 4), 1,0+4+4,12+12,-3);
		fb.buildFace(l, r.part(ss, 12+16+16, 20+32, 4, 12), 0,0+4+4,0+12,-3,true,false);
		fb.buildFace(l, r.part(ss, 4+16+16, 20+32, 4, 12), 0,0+4+4,0+12,0);
		//Left Sleeve
		fb.buildFace(l, r.part(ss, 8+16+16+16, 16+32, 4, 4), 1,0+4+4,1+12-1,-3,false,true);
		fb.buildFace(l, r.part(ss, 24+16+16, 52, 4, 12), 2,0+7+4+1,0+12,-3,true,false);
		fb.buildFace(l, r.part(ss, 8+16+16-4+16, 16+32, 4, 4), 1,0+4+4,12+12+1,-3);
		fb.buildFace(l, r.part(ss, 12+16+16+16, 20+32, 4, 12), 0,0+4+4,0+12,-3-1,true,false);
		fb.buildFace(l, r.part(ss, 4+16+16+16, 20+32, 4, 12), 0,0+4+4,0+12,1);
		
		//Head
		fb.buildFace(l, r.part(ss, 16, 0, 8, 8), 1,0,1+24,-5,false,true);
		fb.buildFace(l, r.part(ss, 0, 8, 8, 8), 2,0,0+24,-5);
		fb.buildFace(l, r.part(ss, 16, 8, 8, 8), 2,0+7,0+24,-5,true,false);
		fb.buildFace(l, r.part(ss, 8, 0, 8, 8), 1,0,1+24+7,-5);
		fb.buildFace(l, r.part(ss, 24, 8, 8, 8), 0,0,24,-5,true,false);
		fb.buildFace(l, r.part(ss, 8, 8, 8, 8), 0,0,24,2);
		//Hat
		fb.buildFace(l, r.part(ss, 0+32, 8, 8, 8), 2,0-1,0+24,-5);
		fb.buildFace(l, r.part(ss, 16+32, 8, 8, 8), 2,0+7+1,0+24,-5,true,false);
		fb.buildFace(l, r.part(ss, 8+32, 0, 8, 8), 1,0,1+24+7+1,-5);
		fb.buildFace(l, r.part(ss, 24+32, 8, 8, 8), 0,0,24,-5-1,true,false);
		fb.buildFace(l, r.part(ss, 8+32, 8, 8, 8), 0,0,24,2+1);
		*/
		finish();
	}
	private void makeSlimStatue(Location l, BufferedImage xx) throws Exception {
		initialize();
		
		if (xx.getHeight() < 64) {
			makeSlimStatue(l,LegacyConverter.convertLegacy(xx,true));
			p.sendMessage(ChatColor.YELLOW + "You attempted to make a statue with a 64x64 (newer format) but the skin you specified is"
					+ " smaller than 64x64, so the plugin made a legacy skin statue instead.");
			return;
		}
		
		BufferedImage ss = preProcessing(l,xx);
		
		//0 is vertical |_
		//1 is flat |-
		//2 is sideways |_
		//Bottom, Right, Left, Top, Back, Front
		//Right Leg
		FaceBuilder fb = new FaceBuilder(s);
		Reader r = new Reader();
		
		//see if the player wants us to just build one part
		boolean builtSomething = false;
		Set<String> flags = params.keySet();
		if (flags.contains("right_leg")) {
			right_leg(fb,r,l,ss);
			builtSomething = true;
		}
		if (flags.contains("left_leg")) {
			left_leg(fb,r,l,ss);
			builtSomething = true;
		}
		if (flags.contains("body")) {
			body(fb,r,l,ss);
			builtSomething = true;
		}
		if (flags.contains("right_arm")) {
			right_arm_slim(fb,r,l,ss);
			builtSomething = true;
		}
		if (flags.contains("left_arm")) {
			left_arm_slim(fb,r,l,ss);
			builtSomething = true;
		}
		if (flags.contains("head")) {
			head(fb,r,l,ss);
			builtSomething = true;
		}
		
		//if the player does not specify what part to build, just build the whole thing
		if (builtSomething == false) {
			right_leg(fb,r,l,ss);
			left_leg(fb,r,l,ss);
			body(fb,r,l,ss);
			right_arm_slim(fb,r,l,ss);
			left_arm_slim(fb,r,l,ss);
			head(fb,r,l,ss);
		}
		/*
		//BufferedImage ss = r.read();
		fb.buildFace(l, r.part(ss, 8, 16, 4, 4), 1,0,1,-3,false,true);
		fb.buildFace(l, r.part(ss, 0, 20, 4, 12), 2,0,0,-3);
		//fb.buildFace(l, r.part(ss, 8, 16, 4, 4), 1,0,12,-3);
		fb.buildFace(l, r.part(ss, 12, 20, 4, 12), 0,0,0,-3,true,false);
		fb.buildFace(l, r.part(ss, 4, 20, 4, 12), 0,0,0,0);
		//Right Pantleg
		fb.buildFace(l, r.part(ss, 8, 16+16, 4, 4), 1,0,0,-3,false,true);
		fb.buildFace(l, r.part(ss, 0, 20+16, 4, 12), 2,-1,0,-3);
		//fb.buildFace(l, r.part(ss, 8, 16, 4, 4), 1,0,12,-3);
		fb.buildFace(l, r.part(ss, 12, 20+16, 4, 12), 0,0,0,-4,true,false);
		fb.buildFace(l, r.part(ss, 4, 20+16, 4, 12), 0,0,0,1);
		//Left Leg
		fb.buildFace(l, r.part(ss, 8+16, 16+32, 4, 4), 1,0+4,1,-3,false,true);
		fb.buildFace(l, r.part(ss, 24, 52, 4, 12), 2,0+7,0,-3,true,false);
		//fb.buildFace(l, r.part(ss, 8+16, 16+32, 4, 4), 1,0+4,12,-3);
		fb.buildFace(l, r.part(ss, 12+16, 20+32, 4, 12), 0,0+4,0,-3,true,false);
		fb.buildFace(l, r.part(ss, 4+16, 20+32, 4, 12), 0,0+4,0,0);

		//Left Pantleg
		fb.buildFace(l, r.part(ss, 8+16-16, 16+32, 4, 4), 1,0+4,1-1,-3,false,true);
		fb.buildFace(l, r.part(ss, 24-16, 52, 4, 12), 2,0+8,0,-3,true,false);
		//fb.buildFace(l, r.part(ss, 8+16, 16+32, 4, 4), 1,0+4,12,-3);
		fb.buildFace(l, r.part(ss, 12+16-16, 20+32, 4, 12), 0,0+4,0,-4,true,false);
		fb.buildFace(l, r.part(ss, 4+16-16, 20+32, 4, 12), 0,0+4,0,1);
		//Body
		fb.buildFace(l, r.part(ss, 20, 20, 8, 12), 0,0,12,0);
		fb.buildFace(l, r.part(ss, 32, 20, 8, 12), 0,0,12,-3,true,false);
		//Jacket
		fb.buildFace(l, r.part(ss, 20, 20+16, 8, 12), 0,0,12,1);
		fb.buildFace(l, r.part(ss, 32, 20+16, 8, 12), 0,0,12,-4,true,false);
		//Bottom, Right, Left, Top, Back, Front

		//Right Arm
		fb.buildFace(l, r.part(ss, 8+40-1, 16, 3, 4), 1,0-4+1,1+12,-3,false,true);
		fb.buildFace(l, r.part(ss, 0+40, 20, 4, 12), 2,0-4+1,0+12,-3);//Done
		fb.buildFace(l, r.part(ss, 8+40-4, 16, 3, 4), 1,0-4+1,12+12,-3);
		fb.buildFace(l, r.part(ss, 12+40-1, 20, 3, 12), 0,0-4+1,0+12,-3,true,false);
		fb.buildFace(l, r.part(ss, 4+40, 20, 3, 12), 0,0-4+1,0+12,0);
		//Right Sleeve
		fb.buildFace(l, r.part(ss, 8+40-1, 16+16, 3, 4), 1,0-4+1,1+12-1,-3,false,true);
		fb.buildFace(l, r.part(ss, 0+40, 20+16, 4, 12), 2,0-4+1-1,0+12,-3);//Done
		fb.buildFace(l, r.part(ss, 44, 16+16, 3, 4), 1,0-4+1,12+12+1,-3);//Unsure
		fb.buildFace(l, r.part(ss, 12+40-1, 20+16, 3, 12), 0,0-4+1,0+12,-3-1,true,false);
		fb.buildFace(l, r.part(ss, 4+40, 20+16, 3, 12), 0,0-4+1,0+12,1);
		
		//Left Arm
		fb.buildFace(l, r.part(ss, 8+16+16-1, 16+32, 3, 4), 1,0+4+4,1+12,-3,false,true);
		fb.buildFace(l, r.part(ss, 24+16-1, 52, 4, 12), 2,0+7+4-1,0+12,-3,true,false);//Done
		fb.buildFace(l, r.part(ss, 8+16+16-4, 16+32, 3, 4), 1,0+4+4,12+12,-3);
		fb.buildFace(l, r.part(ss, 12+16+16-1, 20+32, 3, 12), 0,0+4+4,0+12,-3,true,false);
		fb.buildFace(l, r.part(ss, 4+16+16, 20+32, 3, 12), 0,0+4+4,0+12,0);
		//Left Sleeve
		fb.buildFace(l, r.part(ss, 8+16+16+16-1, 16+32, 3, 4), 1,0+4+4,1+12-1,-3,false,true);
		fb.buildFace(l, r.part(ss, 24+16+16-1, 52, 4, 12), 2,0+7+4+1-1,0+12,-3,true,false);//Done
		fb.buildFace(l, r.part(ss, 8+16+16-4+16, 16+32, 3, 4), 1,0+4+4,12+12+1,-3);
		fb.buildFace(l, r.part(ss, 12+16+16+16-1, 20+32, 3, 12), 0,0+4+4,0+12,-3-1,true,false);
		fb.buildFace(l, r.part(ss, 4+16+16+16, 20+32, 3, 12), 0,0+4+4,0+12,1);

		//Head
		fb.buildFace(l, r.part(ss, 16, 0, 8, 8), 1,0,1+24,-5,false,true);
		fb.buildFace(l, r.part(ss, 0, 8, 8, 8), 2,0,0+24,-5);
		fb.buildFace(l, r.part(ss, 16, 8, 8, 8), 2,0+7,0+24,-5,true,false);
		fb.buildFace(l, r.part(ss, 8, 0, 8, 8), 1,0,1+24+7,-5);
		fb.buildFace(l, r.part(ss, 24, 8, 8, 8), 0,0,24,-5,true,false);
		fb.buildFace(l, r.part(ss, 8, 8, 8, 8), 0,0,24,2);
		//Hat
		fb.buildFace(l, r.part(ss, 0+32, 8, 8, 8), 2,0-1,0+24,-5);
		fb.buildFace(l, r.part(ss, 16+32, 8, 8, 8), 2,0+7+1,0+24,-5,true,false);
		fb.buildFace(l, r.part(ss, 8+32, 0, 8, 8), 1,0,1+24+7+1,-5);
		fb.buildFace(l, r.part(ss, 24+32, 8, 8, 8), 0,0,24,-5-1,true,false);
		fb.buildFace(l, r.part(ss, 8+32, 8, 8, 8), 0,0,24,2+1);
		*/
		finish();
	}
	
	private void makeLegacyStatue(Location l, BufferedImage ss) throws Exception {
		makeStatue(l,LegacyConverter.convertLegacy(ss,false));
		/*
		
		initialize();
		//0 is vertical |_
		//1 is flat |-
		//2 is sideways |_
		//Bottom, Right, Left, Top, Back, Front
		//Right Leg
		FaceBuilder fb = new FaceBuilder(s);
		Reader r = new Reader();
		//BufferedImage ss = r.read();
		fb.buildFace(l, r.part(ss, 8, 16, 4, 4), 1,0,1,-3,false,true);
		fb.buildFace(l, r.part(ss, 0, 20, 4, 12), 2,0,0,-3);
		//fb.buildFace(l, r.part(ss, 8, 16, 4, 4), 1,0,12,-3);
		fb.buildFace(l, r.part(ss, 12, 20, 4, 12), 0,0,0,-3,true,false);
		fb.buildFace(l, r.part(ss, 4, 20, 4, 12), 0,0,0,0);

		//Left Leg
		fb.buildFace(l, r.part(ss, 8, 16, 4, 4), 1,0+4,1,-3,true,true);
		fb.buildFace(l, r.part(ss, 0, 20, 4, 12), 2,0+7,0,-3,false,false);
		//fb.buildFace(l, r.part(ss, 8+16, 16+32, 4, 4), 1,0+4,12,-3);
		fb.buildFace(l, r.part(ss, 12, 20, 4, 12), 0,0+4,0,-3,false,false);
		fb.buildFace(l, r.part(ss, 4, 20, 4, 12), 0,0+4,0,0,true,false);

		//Body
		fb.buildFace(l, r.part(ss, 20, 20, 8, 12), 0,0,12,0);
		fb.buildFace(l, r.part(ss, 32, 20, 8, 12), 0,0,12,-3,true,false);

		//Right Arm
		fb.buildFace(l, r.part(ss, 8+40, 16, 4, 4), 1,0-4,1+12,-3,false,true);
		fb.buildFace(l, r.part(ss, 0+40, 20, 4, 12), 2,0-4,0+12,-3);
		fb.buildFace(l, r.part(ss, 8+40-4, 16, 4, 4), 1,0-4,12+12,-3);
		fb.buildFace(l, r.part(ss, 12+40, 20, 4, 12), 0,0-4,0+12,-3,true,false);
		fb.buildFace(l, r.part(ss, 4+40, 20, 4, 12), 0,0-4,0+12,0);

		//Left Arm
		fb.buildFace(l, r.part(ss, 8+40, 16, 4, 4), 1,0+4+4,1+12,-3,true,true);
		fb.buildFace(l, r.part(ss, 0+40, 20, 4, 12), 2,0+7+4,0+12,-3,false,false);
		fb.buildFace(l, r.part(ss, 8+40-4, 16, 4, 4), 1,0+4+4,12+12,-3,true,false);
		fb.buildFace(l, r.part(ss, 12+40, 20, 4, 12), 0,0+4+4,0+12,-3,false,false);
		fb.buildFace(l, r.part(ss, 4+40, 20, 4, 12), 0,0+4+4,0+12,0,true,false);

		//Head
		fb.buildFace(l, r.part(ss, 16, 0, 8, 8), 1,0,1+24,-5,false,true);
		fb.buildFace(l, r.part(ss, 0, 8, 8, 8), 2,0,0+24,-5);
		fb.buildFace(l, r.part(ss, 16, 8, 8, 8), 2,0+7,0+24,-5,true,false);
		fb.buildFace(l, r.part(ss, 8, 0, 8, 8), 1,0,1+24+7,-5);
		fb.buildFace(l, r.part(ss, 24, 8, 8, 8), 0,0,24,-5,true,false);
		fb.buildFace(l, r.part(ss, 8, 8, 8, 8), 0,0,24,2);
		//Hat
		fb.buildFace(l, r.part(ss, 0+32, 8, 8, 8), 2,0-1,0+24,-5);
		fb.buildFace(l, r.part(ss, 16+32, 8, 8, 8), 2,0+7+1,0+24,-5,true,false);
		fb.buildFace(l, r.part(ss, 8+32, 0, 8, 8), 1,0,1+24+7+1,-5);
		fb.buildFace(l, r.part(ss, 24+32, 8, 8, 8), 0,0,24,-5-1,true,false);
		fb.buildFace(l, r.part(ss, 8+32, 8, 8, 8), 0,0,24,2+1);

		finish();
		*/
	}
	
	
}
