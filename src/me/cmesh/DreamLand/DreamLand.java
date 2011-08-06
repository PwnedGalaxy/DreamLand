package me.cmesh.DreamLand;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.Plugin;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;

public class DreamLand extends JavaPlugin
{
	public static final Logger log = Logger.getLogger("Minecraft");
	private final DreamLandPlayerListener playerListener = new DreamLandPlayerListener(this);
	private final DreamLandEntityListener entityListener = new DreamLandEntityListener(this);
	private final DreamLandWeatherListener weatherListener = new DreamLandWeatherListener(this);
	
	public DreamLandWorld nightmare = new DreamLandWorld(this);
	public DreamLandWorld dream = new DreamLandWorld(this);
	public DreamLandWorld base = new DreamLandWorld(this);
	
	public DreamLandOptions options = new DreamLandOptions(this);
	
	public PermissionHandler Permissions = null;
	
	private HashMap<String, DreamLandPlayer> Players= new HashMap<String, DreamLandPlayer>(); 
	
	public void onEnable()
	{ 
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Normal, this);
		
		pm.registerEvent(Event.Type.PLAYER_PORTAL, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_BED_ENTER, playerListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.High, this);
		
		pm.registerEvent(Event.Type.WEATHER_CHANGE, weatherListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.THUNDER_CHANGE, weatherListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.LIGHTNING_STRIKE, weatherListener, Event.Priority.High, this);
		
		Plugin permissions = getServer().getPluginManager().getPlugin("Permissions");
		
		if (Permissions == null)
		{
			Permissions = (permissions != null) ? ((Permissions)permissions).getHandler() : null;
		}
		
		reload();
		
		if(nightmare.Chance != 0)
		{
			getServer().createWorld(nightmare.World, Environment.NETHER, getServer().getWorlds().get(0).getSeed());
		}
		getServer().createWorld(dream.World,Environment.SKYLANDS,getServer().getWorlds().get(0).getSeed());
		
		log.info(getDescription().getName()+" version "+getDescription().getVersion()+" is enabled!");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
	{
		if (commandLabel.equalsIgnoreCase("wakeup") && sender instanceof Player) 
		{
			DreamLandPlayer player = player((Player)sender);
			if(player.Dreaming())
			{
				player.leaveDream();
				return true;
			}
		}
		return false;
	}
	
	public void onDisable()
	{
		log.info(getDescription().getName()+" version "+getDescription().getVersion()+" is disabled!");
	}
	
	public void reload()
	{
		setupDream();
		setupNightmare();
		setupBase();
		options.load();
	}
	
	private void setupDream()
	{
		dream.World = getServer().getWorlds().get(0).getName() + "_skylands";
		dream.PersistInventory = true;
		dream.InitialInventoryClear = true;
		dream.Invincible = true;
		dream.Fly = true;
		dream.Flaming = false;
		dream.Kit = true;
		dream.Chance = 100;
		dream.ReturnToBed = true;
		dream.load("dream");		
	}
	private void setupNightmare()
	{
		nightmare.World = getServer().getWorlds().get(0).getName() + "_nightmare";
		nightmare.PersistInventory = false;
		nightmare.InitialInventoryClear = true;
		nightmare.Invincible = false;
		nightmare.Fly = false;
		nightmare.Flaming = true;
		nightmare.Kit = false;
		nightmare.Chance = 50;
		nightmare.ReturnToBed = true;
		nightmare.load("nightmare");
	}
	private void setupBase()
	{
		getConfiguration().load();
		base.PersistInventory = getConfiguration().getBoolean("dreamland.worlds.default.persistInventory",true);
		getConfiguration().save();
	}
	
	public DreamLandWorld world(World world)
	{
		if(world.equals(dream.getWorld()))
		{
			return dream;
		}
		if(world.equals(nightmare.getWorld()))
		{
			return nightmare;
		}
		return base;
	}
	
	public DreamLandPlayer player(Player player)
	{
		String key = player.getName();
		if(Players.containsKey(key))
		{
			return Players.get(key).self(player);
		}
		Players.put(player.getName(), new DreamLandPlayer(this).self(player));
		return player(player);
	}
}
