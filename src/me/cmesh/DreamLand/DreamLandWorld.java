package me.cmesh.DreamLand;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.WorldCreator;

import org.bukkit.World;

public class DreamLandWorld 
{
	public static DreamLand plugin;
	public String World = new String();
	public Boolean PersistInventory = false;
	public Boolean InitialInventoryClear = false;
	public Boolean Invincible = false;
	public Boolean Flaming = false;
	public Boolean Fly = false;
	public Integer Chance = 0;
	public Boolean Kit = false;
	public List<String> Mobs = new ArrayList<String>();
	public Integer MobChance = 0;
	public Boolean ReturnToBed = true;
	public Environment environment = Environment.NORMAL;
	public String Generator;
	
	public DreamLandWorld(DreamLand instance)
	{
		plugin = instance;
	}
	public World getWorld()
	{
		return plugin.getServer().getWorld(World);
	}
	
	public void load(String world)
	{
		FileConfiguration config = plugin.getConfig();
				
		String endpoint = "dreamland.worlds." + world + ".";
		
		World = config.getString(endpoint + "name",World);
		PersistInventory = config.getBoolean("dreamland.worlds.dream.persistInventory",PersistInventory);
		InitialInventoryClear = config.getBoolean(endpoint + "clearInitialInventory", InitialInventoryClear);
		Invincible = config.getBoolean(endpoint + "invincible", Invincible);
		Fly = config.getBoolean(endpoint + "fly", Fly);
		Flaming = config.getBoolean(endpoint + "flaming", Flaming);
		Kit = config.getBoolean(endpoint + "kit", Kit);
		Chance = config.getInt("dreamland.chance." + world,Chance);
		ReturnToBed = config.getBoolean(endpoint + "returnToBed", ReturnToBed);
		environment = Environment.valueOf(config.getString(endpoint + "environment", environment.toString()));
		Generator = config.getString(endpoint + "generator", Generator);
		MobChance = config.getInt(endpoint + "mobChance",0);
		
		try
		{
			for(String node : config.getStringList(endpoint +"mobs"))
			{
				Mobs.add(node);
			}
		}
		catch(NullPointerException e){}
	}
	
	public void create()
	{
        WorldCreator c = new WorldCreator(World);
        c.seed(plugin.getServer().getWorlds().get(0).getSeed());
        c.environment(environment);
		if(Generator != null && !Generator.isEmpty())
	        c.generator(Generator);
		plugin.getServer().createWorld(c);
	}
}
