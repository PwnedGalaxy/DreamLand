package me.cmesh.DreamLand;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class DreamLand extends JavaPlugin
{
	public static final Logger log = Logger.getLogger("Minecraft");

	public DreamLandWorld nightmare = new DreamLandWorld(this);
	public DreamLandWorld dream = new DreamLandWorld(this);
	public DreamLandWorld base = new DreamLandWorld(this);
	
    public PermissionManager permissionsExPlugin = null;
	
	public DreamLandOptions options = new DreamLandOptions(this);
	
	private HashMap<String, DreamLandPlayer> Players= new HashMap<String, DreamLandPlayer>(); 
	
	private Scheduler scheduler = new Scheduler(this);
	
	public void onEnable()
	{
		reload();
		
		SetupPermissions();
		
		if(nightmare.Chance != 0)
		{
			nightmare.create();
		}
		dream.create();
		
		scheduler.Start();

		new DreamLandPlayerListener(this);
		new DreamLandEntityListener(this);
		new DreamLandWeatherListener(this);
	}
	
	public boolean PermissionExEnabled()
	{
		return permissionsExPlugin != null;
	}
	
	private void SetupPermissions()
	{
		
		Plugin q = getServer().getPluginManager().getPlugin("PermissionsEx");
        if (q != null && q.isEnabled()) 
        {
        	permissionsExPlugin = PermissionsEx.getPermissionManager();
        }
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
	{
		if(sender instanceof Player)
		{
			DreamLandPlayer player = player((Player)sender);
			if (commandLabel.equalsIgnoreCase("setdreamspawn"))
			{
				if(player.hasPermission("dreamland.setdreamspawn", false) && player.Dreaming())
				{
					Location location = player.getLocation();
					location.getWorld().setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ());
					return true;
				}
			}
			
			if (commandLabel.equalsIgnoreCase("wakeup")) 
			{
				if(player.Dreaming())
				{
					player.leaveDream();
					return true;
				}
			}

                        if (commandLabel.equalsIgnoreCase("dream"))
                        {
                                if (args.length <= 0) 
                                {
                                        if(!(sender instanceof Player))
                                        {
                                                sender.sendMessage(ChatColor.RED + "Computers don't dream. Give me a player.");
                                                return true;
                                        }

                                        if(sender.hasPermission("dream.self") && !player.Dreaming())
                                        {
                                                player.enterDream(player.self().getLocation(), false);
                                                return true;
                                        } else {
                                                sender.sendMessage(ChatColor.RED + "I'm sorry " + player.self().getDisplayName() + ", I'm afraid I can't let you do that.");
                                                return true;
                                        } 
                                } else {
                                        if(!sender.hasPermission("dream.other"))
                                        {
                                                sender.sendMessage(ChatColor.RED + "I'm sorry " + player.self().getDisplayName() + ", I'm afraid I can't let you do that.");
                                                return true;
                                        }

                                        Player target = (Bukkit.getServer().getPlayer(args[0]));
                                        if (target == null)
                                        {
                                                sender.sendMessage(ChatColor.RED + "Who da fuk is " + ChatColor.GREEN + args[0] + ChatColor.RED + "???");
                                                return true;
                                        }

                                        DreamLandPlayer t = player(target);
                                        if(!t.Dreaming())
                                        {
                                                t.enterDream(t.self().getLocation(), false);
                                                return true;
                                        }
                                }
                        }
		}
		return false;
	}
	
	public void onDisable()
	{
		for(DreamLandPlayer player : Players.values())
		{
			if(player.Dreaming())
			{
				player.leaveDream();
			}
		}
		scheduler.Stop();
	}
	
	public void reload()
	{
		reloadConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		dream.load("dream");
		nightmare.load("nightmare");
		setupBase();
		options.load();
	}
	
	private void setupBase()
	{
		FileConfiguration config = getConfig();
		base.PersistInventory = config.getBoolean("dreamland.worlds.default.persistInventory",true);
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

	public void removePlayer(DreamLandPlayer player) 
	{
		Players.remove(player.getName());		
	}
}
