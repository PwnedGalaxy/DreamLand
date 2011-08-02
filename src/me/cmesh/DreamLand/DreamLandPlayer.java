package me.cmesh.DreamLand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DreamLandPlayer 
{
	public Player get()	{return player;}

	private Location Bed = null;
	private Long Attempt = new Long(0);
	private Player player;
	
	private static DreamLand plugin;
	private static final Logger log = Logger.getLogger("Minecraft");
	
	public DreamLandPlayer(DreamLand instance)
	{
		plugin = instance;
	}

	public DreamLandPlayer Update(Player player)
	{
		this.player = player;
		return this;
	}
	
	
	public void enterDream(Location bed, Boolean nightmare)
	{
		setBed(bed);
		savePlayerHealth();
		
		DreamLandWorld setting = nightmare? plugin.nightmare : plugin.dream;
		
		Location loc = setting.getWorld().getSpawnLocation();
		
		plugin.loadChunk(loc);
		
		savePlayerInv(player.getWorld());
		loadPlayerInv(loc.getWorld());
		
		player.teleport(loc);
		
		if(!plugin.message.isEmpty())
		{
			player.sendMessage(plugin.message);
		}
		
		log.info(player.getName() + " is dreaming");
		return;
	}

	public void leaveDream()
	{
		player.setFireTicks(0);
		player.setFallDistance(0);
		
		Location loc = plugin.getSetting(player.getWorld()).ReturnToBed ? getBed() : getBed().getWorld().getSpawnLocation();
		
		plugin.loadChunk(loc);

		savePlayerInv(player.getWorld());
		loadPlayerInv(loc.getWorld());

		player.teleport(loc);
		
		loadPlayerHealth();
		
		log.info(player.getName() + " woke up");
	}
	
	
	public Boolean getWait()
	{
		Long time = plugin.getServer().getWorlds().get(0).getTime() - Attempt;
		if(time >= plugin.attemptWait)
		{
			Attempt = plugin.getServer().getWorlds().get(0).getTime();
			return true;
		}
   		else
   		{
   			player.sendMessage("Wait " + ((Long)((plugin.attemptWait - time)/30)).toString() + "s before trying again");
			return false;
   		}
	}

	//health
	private File healthFile()
	{
		File healthFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Health");
		if (!healthFolder.exists()) 
		{
			healthFolder.mkdir();
		}
		return new File(healthFolder + File.separator + player.getName());
	}

	private void savePlayerHealth()
	{
		BufferedWriter bw;
		try 
		{
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(healthFile())));
			bw.write(((Integer)player.getHealth()).toString());
			bw.close();
		}
		catch (FileNotFoundException e) {}
		catch (IOException e) {}
	}

	private void loadPlayerHealth()
	{
		File save = healthFile();
		if (save.exists()) 
		{
			try 
			{
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
				player.setHealth(Integer.parseInt(br.readLine()));
				br.close();
			}
			catch (IOException e) {}
			catch (java.lang.NumberFormatException e){}
			catch (java.lang.IllegalArgumentException e){}
		}
		return;
	}

	//Inventory store/switcher
	private File playerInv(World world)
	{
		File invFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Inventories");
		if (!invFolder.exists()) 
		{
			invFolder.mkdir();
		}
		return new File(invFolder + File.separator + player.getName() + "." + world.getName());
	}

	public void savePlayerInv(World world)
	{
		if(plugin.getSetting(world).PersistInventory)
		{
			BufferedWriter bw;
			try 
			{
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playerInv(world))));
				
				ItemStack [] inv =player.getInventory().getContents();
				for(int i = 0; i<inv.length; i++)
				{
					ItemStack item = inv[i];
					if(item != null)
					{
						String temp = i + " " + item.getTypeId() + " " + item.getAmount() + " "+ item.getDurability();
						bw.write(temp);
						bw.newLine();
					}
				}
				bw.close();
			}
			catch (FileNotFoundException e) {}
			catch (IOException e) {}
		}
	}

	public void stringToInv(List<String> inv)
	{
		for(String item : inv)
		{
			String [] split = item.split(" ", 4);
			int spot = Integer.parseInt(split[0]);
			int itemId = Integer.parseInt(split[1]);
			int ammount = Integer.parseInt(split[2]);
			short damage = (short)Integer.parseInt(split[3]);
			
			player.getInventory().setItem(spot, new ItemStack(itemId, ammount, damage));
		}
	}

	@SuppressWarnings("deprecation")
	public void loadPlayerInv(World world)
	{
		File save = playerInv(world);
		try 
		{
			if (!save.exists()) 
			{
				if(plugin.getSetting(world).InitialInventoryClear)
				{
					player.getInventory().clear();
					if(plugin.kit.size() != 0 && plugin.getSetting(world).Kit)
					{
						stringToInv(plugin.kit);
					}
					player.updateInventory();
				}
				return;
			}
			
			if(plugin.getSetting(world).PersistInventory)
			{
				player.getInventory().clear();
				
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
				List<String> inv = new ArrayList<String>();
				
				String inputLine = br.readLine();
				while (inputLine != null)
				{
					inv.add(inputLine);
					inputLine = br.readLine();
				}
				stringToInv(inv);
				
				player.updateInventory();
			}
			return;
		}
		catch (IOException e){}
		catch (java.lang.NumberFormatException e){}
		player.sendMessage("There was an issue loading your inventory");
	}
	
	//saves bed locations of players
	public Location getBed() 
	{
		if(Bed != null)
		{
			return Bed;
		}
		File save = bedFile(player);
		if (save.exists()) 
		{
			try 
			{
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
				
				String world = br.readLine();
				String inputLine = br.readLine();
				
				if (inputLine != null && world != null) 
				{
					String splits[] = inputLine.replace(',', '.').split(" ", 3);
					Location bed = new Location(plugin.getServer().getWorld(world), Double.parseDouble(splits[0]), Double.parseDouble(splits[1]), Double.parseDouble(splits[2]));
					//bed = checkSpawnLoc(bed);
					Bed = bed;
					return bed;
				}
			}
			catch (IOException e) {}
			catch (java.lang.NumberFormatException e){}
		}
		log.info("There was an issue loading a player's bed location");
		return plugin.getServer().getWorlds().get(0).getSpawnLocation();
	}
	
	private void setBed(Location location) 
	{
		BufferedWriter bw;
		try 
		{
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bedFile(player))));
			bw.write(player.getWorld().getName());
			bw.newLine();
			bw.write(String.format("%f %f %f", location.getX(), location.getY(), location.getZ()));
			bw.close();
			Bed = location;
			return;
		}
		catch (FileNotFoundException e){}
		catch (IOException e){}
		log.info("There was an issue saving "+player.getName()+"'s bed location");
	}
	
	private File bedFile(Player player)
	{
		File bedFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "BedLocations");
		if (!bedFolder.exists()) 
		{
			bedFolder.mkdir();
		}
		return new File(bedFolder + File.separator + player.getName());
	}
	
	public void setAttempt(Long time)
	{
		Attempt = time;
	}
	
	public Boolean Dreaming()
	{
		return player.getWorld().equals(plugin.dream.getWorld())
		|| (player.getWorld().equals(plugin.nightmare.getWorld()) && plugin.nightmare.Chance != 0);
	}
}
