package me.latestion.hoh.utils.commandbuilder;


import me.latestion.hoh.utils.commandbuilder.handlers.CommandHandler;
import me.latestion.hoh.utils.commandbuilder.handlers.TabHandler;
import org.bukkit.permissions.Permission;

import java.util.HashMap;
import java.util.Set;

public class SubCommand {
    private final String commandName;
    private final Permission permission;
    private final CommandHandler commandHandler;
    private final TabHandler tabHandler;

    private final String usageMessage;
    private final String permissionMessage;
    private final Set<SubCommand> subCommands;
    private final HashMap<String,Boolean> aliases;

     SubCommand(String commandName, Permission permission, CommandHandler handler, TabHandler tabHandler, String usageMessage, String permissionMessage, Set<SubCommand> subCommands, HashMap<String, Boolean> aliases) {
        this.commandName = commandName;
        this.permission = permission;
        this.commandHandler = handler;
        this.tabHandler = tabHandler;
        this.usageMessage = usageMessage;
        this.permissionMessage = permissionMessage;
        this.subCommands = subCommands;
        aliases.put(commandName,true);
        this.aliases = aliases;

    }

    public String getCommandName() {
        return commandName;
    }

    public Permission getPermission() {
        return permission;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public TabHandler getTabHandler() {
        return tabHandler;
    }

    public String getUsageMessage() {
        return usageMessage;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }

    public Set<SubCommand> getSubCommands() {
        return subCommands;
    }

    public HashMap<String, Boolean> getAliases() {
        return aliases;
    }
}