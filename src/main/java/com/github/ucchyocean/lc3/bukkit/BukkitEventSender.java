/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
package com.github.ucchyocean.lc3.bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.github.ucchyocean.lc.channel.ChannelPlayer;
import com.github.ucchyocean.lc.channel.ChannelPlayerBlock;
import com.github.ucchyocean.lc.channel.ChannelPlayerConsole;
import com.github.ucchyocean.lc.channel.ChannelPlayerName;
import com.github.ucchyocean.lc.channel.ChannelPlayerUUID;
import com.github.ucchyocean.lc.event.LunaChatChannelChatEvent;
import com.github.ucchyocean.lc.event.LunaChatChannelCreateEvent;
import com.github.ucchyocean.lc.event.LunaChatChannelMemberChangedEvent;
import com.github.ucchyocean.lc.event.LunaChatChannelMessageEvent;
import com.github.ucchyocean.lc.event.LunaChatChannelOptionChangedEvent;
import com.github.ucchyocean.lc.event.LunaChatChannelRemoveEvent;
import com.github.ucchyocean.lc.event.LunaChatPostJapanizeEvent;
import com.github.ucchyocean.lc.event.LunaChatPreChatEvent;
import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelChatEvent;
import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelCreateEvent;
import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelMemberChangedEvent;
import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelMessageEvent;
import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelOptionChangedEvent;
import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelRemoveEvent;
import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitPostJapanizeEvent;
import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitPreChatEvent;
import com.github.ucchyocean.lc3.event.EventResult;
import com.github.ucchyocean.lc3.event.EventSenderInterface;
import com.github.ucchyocean.lc3.member.ChannelMember;
import com.github.ucchyocean.lc3.member.ChannelMemberBlock;
import com.github.ucchyocean.lc3.member.ChannelMemberBukkitConsole;
import com.github.ucchyocean.lc3.member.ChannelMemberBungee;
import com.github.ucchyocean.lc3.member.ChannelMemberPlayer;

/**
 * Bukkit??????????????????????????????
 * @author ucchy
 */
@SuppressWarnings("deprecation")
public class BukkitEventSender implements EventSenderInterface {

    /**
     * ??????????????????????????????????????????????????????
     * @param channelName ??????????????????
     * @param player ?????????
     * @param originalMessage ????????????
     * @param ngMaskedMessage ???????????????NG???????????????
     * @param messageFormat ??????????????????????????????????????????
     * @return ????????????????????????
     * @see com.github.ucchyocean.lc3.event.EventSenderInterface#sendLunaChatChannelChatEvent(java.lang.String, com.github.ucchyocean.lc3.member.ChannelMember, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public EventResult sendLunaChatChannelChatEvent(String channelName, ChannelMember member, String originalMessage,
            String ngMaskedMessage, String messageFormat) {

        LunaChatBukkitChannelChatEvent event =
                new LunaChatBukkitChannelChatEvent(
                        channelName, member, originalMessage, ngMaskedMessage, messageFormat);
        Bukkit.getPluginManager().callEvent(event);

        LunaChatChannelChatEvent legacy = new LunaChatChannelChatEvent(
                channelName, convertChannelMemberToChannelPlayer(member),
                originalMessage, event.getNgMaskedMessage(), event.getMessageFormat());
        legacy.setCancelled(event.isCancelled());
        Bukkit.getPluginManager().callEvent(legacy);

        EventResult result = new EventResult();
        result.setCancelled(legacy.isCancelled());
        result.setNgMaskedMessage(legacy.getNgMaskedMessage());
        result.setMessageFormat(legacy.getMessageFormat());
        return result;
    }

    /**
     * ?????????????????????????????????
     * @param channelName ??????????????????
     * @param member ???????????????
     * @return ????????????????????????
     * @see com.github.ucchyocean.lc3.event.EventSenderInterface#sendLunaChatChannelCreateEvent(java.lang.String, com.github.ucchyocean.lc3.member.ChannelMember)
     */
    @Override
    public EventResult sendLunaChatChannelCreateEvent(String channelName, ChannelMember member) {

        LunaChatBukkitChannelCreateEvent event =
                new LunaChatBukkitChannelCreateEvent(channelName, member);
        Bukkit.getPluginManager().callEvent(event);

        LunaChatChannelCreateEvent legacy = new LunaChatChannelCreateEvent(
                event.getChannelName(), convertChannelMemberToCommandSender(member));
        legacy.setCancelled(event.isCancelled());
        Bukkit.getPluginManager().callEvent(legacy);

        EventResult result = new EventResult();
        result.setCancelled(legacy.isCancelled());
        result.setChannelName(legacy.getChannelName());
        return result;
    }

    /**
     * ??????????????????????????????
     * @param channelName ??????????????????
     * @param before ????????????????????????
     * @param after ????????????????????????
     * @return ????????????????????????
     * @see com.github.ucchyocean.lc3.event.EventSenderInterface#sendLunaChatChannelMemberChangedEvent(java.lang.String, java.util.List, java.util.List)
     */
    @Override
    public EventResult sendLunaChatChannelMemberChangedEvent(String channelName, List<ChannelMember> before,
            List<ChannelMember> after) {

        LunaChatBukkitChannelMemberChangedEvent event =
                new LunaChatBukkitChannelMemberChangedEvent(channelName, before, after);
        Bukkit.getPluginManager().callEvent(event);

        LunaChatChannelMemberChangedEvent legacy =
                new LunaChatChannelMemberChangedEvent(channelName,
                        convertMemberListToPlayerList(before), convertMemberListToPlayerList(after));
        legacy.setCancelled(event.isCancelled());
        Bukkit.getPluginManager().callEvent(legacy);

        EventResult result = new EventResult();
        result.setCancelled(event.isCancelled());
        return result;
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @param channelName ??????????????????
     * @param member ?????????
     * @param message ???????????????NG????????????Japanize????????????????????????
     * @param recipients ?????????
     * @param displayName ?????????????????????
     * @param originalMessage ?????????????????????????????????
     * @return ????????????????????????
     * @see com.github.ucchyocean.lc3.event.EventSenderInterface#sendLunaChatChannelMessageEvent(java.lang.String, com.github.ucchyocean.lc3.member.ChannelMember, java.lang.String, java.util.ArrayList, java.lang.String, java.lang.String)
     */
    @Override
    public EventResult sendLunaChatChannelMessageEvent(String channelName, ChannelMember member, String message,
            List<ChannelMember> recipients, String displayName, String originalMessage) {

        LunaChatBukkitChannelMessageEvent event =
                new LunaChatBukkitChannelMessageEvent(
                        channelName, member, message, recipients, displayName, originalMessage);
        Bukkit.getPluginManager().callEvent(event);

        ArrayList<ChannelPlayer> recipientsTemp =
                new ArrayList<ChannelPlayer>(convertMemberListToPlayerList(event.getRecipients()));
        LunaChatChannelMessageEvent legacy =
                new LunaChatChannelMessageEvent(
                        channelName, convertChannelMemberToChannelPlayer(member),
                        event.getMessage(), recipientsTemp, displayName, originalMessage);
        Bukkit.getPluginManager().callEvent(legacy);

        EventResult result = new EventResult();
        result.setMessage(legacy.getMessage());
        result.setRecipients(convertPlayerListToMemberList(legacy.getRecipients()));
        return result;
     }

    /**
     * ?????????????????????????????????
     * @param channelName ??????????????????
     * @param member ?????????????????????????????????
     * @param options ???????????????????????????
     * @return ????????????????????????
     * @see com.github.ucchyocean.lc3.event.EventSenderInterface#sendLunaChatChannelOptionChangedEvent(java.lang.String, com.github.ucchyocean.lc3.member.ChannelMember, java.util.HashMap)
     */
    @Override
    public EventResult sendLunaChatChannelOptionChangedEvent(String channelName, ChannelMember member,
            Map<String, String> options) {

        LunaChatBukkitChannelOptionChangedEvent event =
                new LunaChatBukkitChannelOptionChangedEvent(channelName, member, options);
        Bukkit.getPluginManager().callEvent(event);

        LunaChatChannelOptionChangedEvent legacy =
                new LunaChatChannelOptionChangedEvent(
                        channelName, convertChannelMemberToCommandSender(member),
                        new HashMap<String, String>(options));
        legacy.setCancelled(event.isCancelled());
        Bukkit.getPluginManager().callEvent(legacy);

        EventResult result = new EventResult();
        result.setCancelled(legacy.isCancelled());
        result.setOptions(legacy.getOptions());
        return result;
    }

    /**
     * ?????????????????????????????????
     * @param channelName ??????????????????
     * @param member ????????????????????????
     * @return ????????????????????????
     * @see com.github.ucchyocean.lc3.event.EventSenderInterface#sendLunaChatChannelRemoveEvent(java.lang.String, com.github.ucchyocean.lc3.member.ChannelMember)
     */
    @Override
    public EventResult sendLunaChatChannelRemoveEvent(String channelName, ChannelMember member) {

        LunaChatBukkitChannelRemoveEvent event = new LunaChatBukkitChannelRemoveEvent(channelName, member);
        Bukkit.getPluginManager().callEvent(event);

        LunaChatChannelRemoveEvent legacy =
                new LunaChatChannelRemoveEvent(
                        event.getChannelName(), convertChannelMemberToCommandSender(member));
        legacy.setCancelled(event.isCancelled());
        Bukkit.getPluginManager().callEvent(legacy);

        EventResult result = new EventResult();
        result.setCancelled(legacy.isCancelled());
        result.setChannelName(legacy.getChannelName());
        return result;
    }

    /**
     * Japanize?????????????????????????????????????????????????????????
     * @param channelName ??????????????????
     * @param member ????????????????????????
     * @param original ?????????????????????
     * @param japanized ?????????????????????
     * @return ????????????????????????
     * @see com.github.ucchyocean.lc3.event.EventSenderInterface#sendLunaChatPostJapanizeEvent(java.lang.String, com.github.ucchyocean.lc3.member.ChannelMember, java.lang.String, java.lang.String)
     */
    @Override
    public EventResult sendLunaChatPostJapanizeEvent(String channelName, ChannelMember member, String original,
            String japanized) {

        LunaChatBukkitPostJapanizeEvent event =
                new LunaChatBukkitPostJapanizeEvent(channelName, member, original, japanized);
        Bukkit.getPluginManager().callEvent(event);

        LunaChatPostJapanizeEvent legacy =
                new LunaChatPostJapanizeEvent(
                        channelName, convertChannelMemberToChannelPlayer(member), original, event.getJapanized());
        legacy.setCancelled(event.isCancelled());
        Bukkit.getPluginManager().callEvent(legacy);

        EventResult result = new EventResult();
        result.setCancelled(legacy.isCancelled());
        result.setJapanized(legacy.getJapanized());
        return result;
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     * @param channelName ??????????????????
     * @param member ????????????????????????
     * @param message ????????????
     * @return ????????????????????????
     * @see com.github.ucchyocean.lc3.event.EventSenderInterface#sendLunaChatPreChatEvent(java.lang.String, com.github.ucchyocean.lc3.member.ChannelMember, java.lang.String)
     */
    @Override
    public EventResult sendLunaChatPreChatEvent(String channelName, ChannelMember member, String message) {

        LunaChatBukkitPreChatEvent event =
                new LunaChatBukkitPreChatEvent(channelName, member, message);
        Bukkit.getPluginManager().callEvent(event);


        LunaChatPreChatEvent legacy =
                new LunaChatPreChatEvent(channelName, convertChannelMemberToChannelPlayer(member), event.getMessage());
        legacy.setCancelled(event.isCancelled());
        Bukkit.getPluginManager().callEvent(legacy);

        EventResult result = new EventResult();
        result.setCancelled(legacy.isCancelled());
        result.setMessage(legacy.getMessage());
        return result;
    }

    /**
     * ChannelMember???ChannelPlayer???????????????
     * @param cm
     * @return
     */
    private ChannelPlayer convertChannelMemberToChannelPlayer(ChannelMember cm) {
        if ( cm == null ) return null;
        if ( cm instanceof ChannelMemberBungee ) return null; // Bungee???????????????????????????????????????
        if ( cm instanceof ChannelMemberPlayer ) {
            return ChannelPlayer.getChannelPlayer(cm.toString());
        } else if ( cm instanceof ChannelMemberBukkitConsole ) {
            return new ChannelPlayerConsole(Bukkit.getConsoleSender());
        } else if ( cm instanceof ChannelMemberBlock ) {
            ChannelMemberBlock cmb = (ChannelMemberBlock)cm;
            if ( cmb.getBlockCommandSender() != null ) {
                return new ChannelPlayerBlock(cmb.getBlockCommandSender());
            }
        }
        return null;
    }

    /**
     * ChannelPlayer???ChannelMember???????????????
     * @param cp
     * @return
     */
    private ChannelMember convertChannelPlayerToChannelMember(ChannelPlayer cp) {
        if ( cp == null ) return null;
        if ( cp instanceof ChannelPlayerName || cp instanceof ChannelPlayerUUID ) {
            return ChannelMember.getChannelMember(cp.toString());
        } else if ( cp instanceof ChannelPlayerConsole ) {
            return new ChannelMemberBukkitConsole(Bukkit.getConsoleSender());
        } else if ( cp instanceof ChannelPlayerBlock ) {
            ChannelPlayerBlock cpb = (ChannelPlayerBlock)cp;
            if ( cpb.getBlockCommandSender() != null ) {
                return new ChannelMemberBlock(cpb.getBlockCommandSender());
            }
        }
        return null;
    }

    /**
     * ChannelMember???CommandSender???????????????
     * @param cm
     * @return
     */
    private CommandSender convertChannelMemberToCommandSender(ChannelMember cm) {
        if ( cm == null ) return null;
        if ( cm instanceof ChannelMemberBungee ) return null; // Bungee???????????????????????????????????????
        if ( cm instanceof ChannelMemberPlayer ) {
            return ((ChannelMemberPlayer)cm).getPlayer();
        } else if ( cm instanceof ChannelMemberBukkitConsole ) {
            return Bukkit.getConsoleSender();
        } else if ( cm instanceof ChannelMemberBlock ) {
            return ((ChannelMemberBlock)cm).getBlockCommandSender();
        }
        return null;
    }

    /**
     * ChannelMember???????????????ChannelPlayer???????????????????????????
     * @param list
     * @return
     */
    private List<ChannelPlayer> convertMemberListToPlayerList(List<ChannelMember> list) {
        List<ChannelPlayer> result = new ArrayList<ChannelPlayer>();
        for ( ChannelMember member : list ) {
            ChannelPlayer player = convertChannelMemberToChannelPlayer(member);
            if ( player != null ) result.add(player);
        }
        return result;
    }

    /**
     * ChannelPlayer???????????????ChannelMember???????????????????????????
     * @param list
     * @return
     */
    private List<ChannelMember> convertPlayerListToMemberList(List<ChannelPlayer> list) {
        List<ChannelMember> result = new ArrayList<ChannelMember>();
        for ( ChannelPlayer player : list ) {
            ChannelMember member = convertChannelPlayerToChannelMember(player);
            if ( member != null ) result.add(member);
        }
        return result;
    }
}
