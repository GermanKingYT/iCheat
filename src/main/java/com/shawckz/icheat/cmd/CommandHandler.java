/*
 * Copyright (c) 2015. Copyright (c) 2015 Jonah Seguin (Shawckz).  All rights reserved.  You may not modify, decompile, distribute or use any code/text contained in this document(plugin) without explicit signed permission from Jonah Seguin.
 */

package com.shawckz.icheat.cmd;

import com.shawckz.icheat.ISettings;
import com.shawckz.icheat.cmd.commands.CommandAlerts;
import com.shawckz.icheat.cmd.commands.CommandHelp;
import com.shawckz.icheat.cmd.commands.CommandStatus;
import com.shawckz.icheat.cmd.commands.CommandToggleCheck;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class CommandHandler implements CommandExecutor {

    @Getter private List<ICommand> commands;
    private JavaPlugin javaPlugin;

    public CommandHandler(JavaPlugin javaPlugin) {
        this.commands = new ArrayList<>();
        this.javaPlugin = javaPlugin;

        javaPlugin.getCommand("icheat").setExecutor(this);

        // Register sub-commands
        registerCommand(new CommandAlerts(), true);
        registerCommand(new CommandStatus(), false);
        registerCommand(new CommandHelp(), false);
        registerCommand(new CommandToggleCheck(), false);
    }

    private void registerCommand(ICommand cmd,boolean single){
        if(!commands.contains(cmd)) {
            commands.add(cmd);
            if(single){
                if (cmd.getClass().isAnnotationPresent(Command.class)) {
                    Command command = cmd.getClass().getAnnotation(Command.class);
                    javaPlugin.getCommand(command.name()).setExecutor(this);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String s, String[] args) {
        if(cmd.getName().equalsIgnoreCase("icheat")){
            if(args.length > 0){
                String q = args[0];
                for(ICommand c : commands){
                    if(c.getClass().isAnnotationPresent(Command.class)) {
                        Command cc = c.getClass().getAnnotation(Command.class);
                        if(cc.name().equalsIgnoreCase(q)){
                            String[] newArgs = new String[args.length - 1];
                            int x = 0;
                            for(int i = 1; i < args.length; i++){
                                newArgs[x] = args[i];
                                x++;
                            }
                            args = newArgs;
                            if (!sender.hasPermission(ISettings.PERMISSION_USE)) {
                                sender.sendMessage(cc.noPerm());
                                return true;
                            }
                            if (!sender.hasPermission(cc.permission()) && !cc.permission().equals("")) {
                                sender.sendMessage(cc.noPerm());
                                return true;
                            }
                            if (args.length < cc.minArgs()) {
                                sender.sendMessage(ChatColor.RED + "Usage: " + cc.usage());
                                return true;
                            }
                            if (cc.playerOnly() && !(sender instanceof Player)) {
                                sender.sendMessage(ChatColor.RED + "This is a player only command.");
                                return true;
                            }
                            c.onCommand(new CmdArgs(sender,args));
                            return true;
                        }
                    }
                }
            }
            else{
                for(ICommand c : commands){
                    if(c.getClass().isAnnotationPresent(Command.class)) {
                        Command cc = c.getClass().getAnnotation(Command.class);
                        if(cc.name().equalsIgnoreCase("help")){
                            c.onCommand(new CmdArgs(sender,args));
                            return true;
                        }
                    }
                }
            }
        }
        for(ICommand pCmd : commands){
            if(pCmd.getClass().isAnnotationPresent(Command.class)){
                Command command = pCmd.getClass().getAnnotation(Command.class);
                if(command.name().equalsIgnoreCase(cmd.getName())) {
                    if (!sender.hasPermission(ISettings.PERMISSION_USE)) {
                        sender.sendMessage(command.noPerm());
                        return true;
                    }
                    if (!sender.hasPermission(command.permission()) && !command.permission().equals("")) {
                        sender.sendMessage(command.noPerm());
                        return true;
                    }
                    if (args.length < command.minArgs()) {
                        sender.sendMessage(ChatColor.RED + "Usage: " + command.usage());
                        return true;
                    }
                    if (command.playerOnly() && !(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "This is a player only command.");
                        return true;
                    }

                    pCmd.onCommand(new CmdArgs(sender, args));
                    return true;
                }
            }
        }
        return true;
    }
}
