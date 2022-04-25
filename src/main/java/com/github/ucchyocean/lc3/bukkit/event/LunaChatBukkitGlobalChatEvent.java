package com.github.ucchyocean.lc3.bukkit.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LunaChatBukkitGlobalChatEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CommandSender sender;
    private final String originalMessage;
    private final String ngMaskedMessage;

    public LunaChatBukkitGlobalChatEvent(@NotNull CommandSender sender, @NotNull String originalMessage, @NotNull String ngMaskedMessage) {
        this.sender = sender;
        this.originalMessage = originalMessage;
        this.ngMaskedMessage = ngMaskedMessage;
    }

    @NotNull
    public CommandSender getSender() {
        return sender;
    }

    @NotNull
    public String getOriginalMessage() {
        return originalMessage;
    }

    @NotNull
    public String getNgMaskedMessage() {
        return ngMaskedMessage;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
