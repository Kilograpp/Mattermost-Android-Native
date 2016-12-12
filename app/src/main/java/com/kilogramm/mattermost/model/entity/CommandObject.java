package com.kilogramm.mattermost.model.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evgeny on 12.12.2016.
 */

public class CommandObject  implements Comparable{

    private String command;
    private String description;
    private String hint;

    public CommandObject(String command, String description, String hint) {
        this.command = command;
        this.description = description;
        this.hint = hint;
    }

    public String getHint() {
        return hint;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public static List<CommandObject> getCommandList(){
        List<CommandObject> list = new ArrayList<>();
        list.add(new CommandObject("/msg","Send Direct Message to a user","@[username] 'message'"));
        list.add(new CommandObject("/offline","Set your status offline",""));
        list.add(new CommandObject("/shrug","Adds ¯\\_(ツ)_/¯ to your message","[message]"));
        list.add(new CommandObject("/echo","Echo back text from your account","'message' [delay in seconds]"));
        list.add(new CommandObject("/online","Set your status online",""));
        list.add(new CommandObject("/away","Set your status away",""));
        list.add(new CommandObject("/logout","Logout of Mattermost",""));
        return list;
    }

    @Override
    public int compareTo(Object o) {
        return this.command.compareTo(o.toString());
    }
}
