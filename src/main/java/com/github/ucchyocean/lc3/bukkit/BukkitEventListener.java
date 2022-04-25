/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
package com.github.ucchyocean.lc3.bukkit;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitGlobalChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.LunaChatBukkit;
import com.github.ucchyocean.lc3.LunaChatConfig;
import com.github.ucchyocean.lc3.Messages;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.event.EventResult;
import com.github.ucchyocean.lc3.japanize.JapanizeType;
import com.github.ucchyocean.lc3.member.ChannelMember;
import com.github.ucchyocean.lc3.member.ChannelMemberBukkit;
import com.github.ucchyocean.lc3.member.ChannelMemberOther;
import com.github.ucchyocean.lc3.member.ChannelMemberPlayer;
import com.github.ucchyocean.lc3.messaging.BukkitChatMessage;
import com.github.ucchyocean.lc3.util.ClickableFormat;
import com.github.ucchyocean.lc3.util.Utility;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Bukkit関連のイベントを監視するリスナ
 * @author ucchy
 */
public class BukkitEventListener implements Listener {
    private org.bukkit.scheduler.BukkitTask saveTask = null;
    private static final int MAX_LIST_ITEMS = 8;

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
    public void onAsyncPlayerChatLowest(AsyncPlayerChatEvent event) {
        if ( matchesEventPriority(EventPriority.LOWEST) ) {
            processChatEvent(event);
        }
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
    public void onAsyncPlayerChatLow(AsyncPlayerChatEvent event) {
        if ( matchesEventPriority(EventPriority.LOW) ) {
            processChatEvent(event);
        }
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    public void onAsyncPlayerChatNormal(AsyncPlayerChatEvent event) {
        if ( matchesEventPriority(EventPriority.NORMAL) ) {
            processChatEvent(event);
        }
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onAsyncPlayerChatHigh(AsyncPlayerChatEvent event) {
        if ( matchesEventPriority(EventPriority.HIGH) ) {
            processChatEvent(event);
        }
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onAsyncPlayerChatHighest(AsyncPlayerChatEvent event) {
        if ( matchesEventPriority(EventPriority.HIGHEST) ) {
            processChatEvent(event);
        }
    }

    /**
     * プレイヤーのサーバー参加ごとに呼び出されるメソッド
     * @param event プレイヤー参加イベント
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        LunaChatConfig config = LunaChat.getConfig();
        Player player = event.getPlayer();

        // UUIDをキャッシュ
        LunaChat.getUUIDCacheData().put(player.getUniqueId().toString(), player.getName());
        if (saveTask != null) saveTask.cancel();
        saveTask = org.bukkit.Bukkit.getScheduler().runTaskLater(LunaChatBukkit.getInstance(), () -> {
            LunaChat.getUUIDCacheData().save();
            saveTask = null;
        }, 100);

        // 強制参加チャンネル設定を確認し、参加させる
        forceJoinToForceJoinChannels(player);

        // グローバルチャンネル設定がある場合
        if ( !config.getGlobalChannel().equals("") ) {
            tryJoinToGlobalChannel(player);
        }

        // チャンネルチャット情報を表示する
        if ( config.isShowListOnJoin() ) {
            for ( BaseComponent[] msg : getListForMotd(player) ) {
                ChannelMember.getChannelMember(player).sendMessage(msg);
            }
        }
    }

    /**
     * プレイヤーのサーバー退出ごとに呼び出されるメソッド
     * @param event プレイヤー退出イベント
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        String pname = player.getName();

        // お互いがオフラインになるPMチャンネルがある場合は
        // チャンネルをクリアする
        ArrayList<Channel> deleteList = new ArrayList<Channel>();

        for ( Channel channel : LunaChat.getAPI().getChannels() ) {
            String cname = channel.getName();
            if ( channel.isPersonalChat() && cname.contains(pname) ) {
                boolean isAllOffline = true;
                for ( ChannelMember cp : channel.getMembers() ) {
                    if ( cp.isOnline() ) {
                        isAllOffline = false;
                    }
                }
                if ( isAllOffline ) {
                    deleteList.add(channel);
                }
            }
        }

        for ( Channel channel : deleteList ) {
            LunaChat.getAPI().removeChannel(
                    channel.getName(), ChannelMember.getChannelMember(player));
        }
    }

    /**
     * プレイヤーのチャットごとに呼び出されるメソッド
     * @param event チャットイベント
     */
    private void processChatEvent(AsyncPlayerChatEvent event) {

        LunaChatConfig config = LunaChat.getConfig();
        LunaChatAPI api = LunaChat.getAPI();

        // Bungeeパススルーモードなら、メッセージを返送して終了する
        if ( config.isBungeePassThroughMode() ) {

            // メッセージの返送
            ChannelMember member = ChannelMember.getChannelMember(event.getPlayer());
            if ( member instanceof ChannelMemberPlayer ) {
                ChannelMemberOther other = ((ChannelMemberPlayer)member).toChannelMemberOther();
                BukkitChatMessage msg = new BukkitChatMessage(other, event.getMessage());
                LunaChatBukkit.getInstance().sendPluginMessage(msg.toByteArray());

                // 設定が有効ならコンソールに表示する
                if ( config.isDisplayNormalChatOnConsole() ) {
                    String logmsg = event.getFormat()
                            .replace("%1$s", member.getDisplayName())
                            .replace("%2$s", event.getMessage());
                    LunaChat.getPlugin().log(Level.INFO, logmsg);
                }
            }

            // イベントをキャンセルして終了する
            event.setCancelled(true);
            return;
        }

        // 頭にglobalMarkerが付いている場合は、グローバル発言にする
        if ( config.getGlobalMarker() != null &&
                !config.getGlobalMarker().equals("") &&
                event.getMessage().startsWith(config.getGlobalMarker()) &&
                event.getMessage().length() > config.getGlobalMarker().length() ) {

            int offset = config.getGlobalMarker().length();
            event.setMessage( event.getMessage().substring(offset) );
            chatGlobal(event);
            return;
        }

        // クイックチャンネルチャット機能が有効で、専用の記号が含まれるなら、
        // クイックチャンネルチャットとして処理する。
        if ( config.isEnableQuickChannelChat() ) {
            String separator = config.getQuickChannelChatSeparator();
            if ( event.getMessage().contains(separator) ) {
                String[] temp = event.getMessage().split(separator, 2);
                String name = temp[0];
                String value = "";
                if ( temp.length > 0 ) {
                    value = temp[1];
                }

                Channel channel = api.getChannel(name);
                if ( channel != null && !channel.isPersonalChat() ) {
                    ChannelMember player =
                            ChannelMember.getChannelMember(event.getPlayer());
                    if ( !channel.getMembers().contains(player) ) {
                        // 指定されたチャンネルに参加していないなら、エラーを表示して何も発言せずに終了する。
                        event.getPlayer().sendMessage(Messages.errmsgNomember());
                        event.setCancelled(true);
                        return;
                    }

                    // 指定されたチャンネルに発言して終了する。
                    chatToChannelWithEvent(player, channel, value);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        ChannelMember player =
                ChannelMember.getChannelMember(event.getPlayer());
        Channel channel = api.getDefaultChannel(player.getName());

        // デフォルトの発言先が無い場合
        if ( channel == null ) {
            if ( config.isNoJoinAsGlobal() ) {
                // グローバル発言にする
                chatGlobal(event);
                return;

            } else {
                // 発言をキャンセルして終了する
                event.setCancelled(true);
                return;
            }
        }

        chatToChannelWithEvent(player, channel, event.getMessage());

        // もとのイベントをキャンセル
        event.setCancelled(true);
    }

    /**
     * イベントをグローバルチャット発言として処理する
     * @param event 処理するイベント
     */
    private void chatGlobal(AsyncPlayerChatEvent event) {

        LunaChatConfig config = LunaChat.getConfig();
        LunaChatAPI api = LunaChat.getAPI();
        ChannelMember player =
                ChannelMember.getChannelMember(event.getPlayer());

        if ( !config.getGlobalChannel().equals("") ) {
            // グローバルチャンネル設定がある場合

            // グローバルチャンネルの取得、無ければ作成
            Channel global = api.getChannel(config.getGlobalChannel());
            if ( global == null ) {
                global = api.createChannel(config.getGlobalChannel(), player);
            }

            String message = event.getMessage();

            // デフォルト発言先が無いなら、グローバルチャンネルに設定する
            Channel dchannel = api.getDefaultChannel(player.getName());
            if ( dchannel == null ) {
                api.setDefaultChannel(player.getName(), global.getName());
            }

            // チャンネルチャット発言
            chatToChannelWithEvent(player, global, message);

            // もとのイベントをキャンセル
            event.setCancelled(true);

            return;

        } else {
            // グローバルチャンネル設定が無い場合

            String message = event.getMessage();
            // NGワード発言をマスク
            for ( Pattern pattern : config.getNgwordCompiled() ) {
                Matcher matcher = pattern.matcher(message);
                if ( matcher.find() ) {
                    message = matcher.replaceAll(
                            Utility.getAstariskString(matcher.group(0).length()));
                }
            }

            Bukkit.getPluginManager().callEvent(new LunaChatBukkitGlobalChatEvent(event.getPlayer(), event.getMessage(), message));

            // カラーコード置き換え
            // 置き換え設定になっていて、発言者がパーミッションを持っているなら、置き換えする
            if ( config.isEnableNormalChatColorCode() &&
                    event.getPlayer().hasPermission("lunachat.allowcc") ) {
                message = Utility.replaceColorCode(message);
            }

            // hideされているプレイヤーを、recipientから抜く
            for ( ChannelMember cp : api.getHidelist(player) ) {
                Player p = ((ChannelMemberBukkit)cp).getPlayer();
                if ( p != null ) {
                    event.getRecipients().remove(p);
                }
            }

            // 一時的にJapanizeスキップ設定かどうかを確認する
            boolean skipJapanize = false;
            String marker = config.getNoneJapanizeMarker();
            if ( !marker.equals("") && message.startsWith(marker) ) {
                skipJapanize = true;
                message = message.substring(marker.length());
            }

            // 2byteコードを含む、または、半角カタカナのみなら、Japanize変換は行わない
            String kanaTemp = Utility.stripColorCode(message);
            if ( !skipJapanize &&
                    ( kanaTemp.getBytes(StandardCharsets.UTF_8).length > kanaTemp.length() ||
                            kanaTemp.matches("[ \\uFF61-\\uFF9F]+") ) ) {
                skipJapanize = true;
            }

            // Japanize変換と、発言処理
            if ( !skipJapanize &&
                    LunaChat.getAPI().isPlayerJapanize(player.getName()) &&
                    config.getJapanizeType() != JapanizeType.NONE ) {

                int lineType = config.getJapanizeDisplayLine();

                if ( lineType == 1 ) {

                    String taskFormat = Utility.replaceColorCode(config.getJapanizeLine1Format());

                    String japanized = api.japanize(
                            kanaTemp, config.getJapanizeType());
                    if ( japanized != null ) {
                        String temp = taskFormat.replace("%msg", message);
                        message = temp.replace("%japanize", japanized);
                    }

                } else {

                    String taskFormat = Utility.replaceColorCode(config.getJapanizeLine2Format());

                    BukkitNormalChatJapanizeTask task = new BukkitNormalChatJapanizeTask(
                            message, config.getJapanizeType(), player, taskFormat, event);

                    // 発言処理を必ず先に実施させるため、遅延を入れてタスクを実行する。
                    int wait = config.getJapanizeWait();
                    task.runTaskLater(LunaChatBukkit.getInstance(), wait);
                }
            }

            if ( config.isEnableNormalChatClickable() ) {
                // クリック可能チャットで発言

                // チャットフォーマット装飾の適用
                ClickableFormat format;
                if ( config.isEnableNormalChatMessageFormat() ) {
                    String f = config.getNormalChatMessageFormat();
                    format = ClickableFormat.makeFormat(f, ChannelMember.getChannelMember(event.getPlayer()));
                } else {
                    String f = event.getFormat()
                            .replace("%1$s", "%displayName")
                            .replace("%2$s", "%msg");
                    format = ClickableFormat.makeFormat(f, ChannelMember.getChannelMember(event.getPlayer()));
                }

                // 発言内容の送信
                format.replace("%msg", message);
                BaseComponent[] comps = format.makeTextComponent();
                for ( Player recipient : event.getRecipients() ) {
                    ChannelMember cm = ChannelMember.getChannelMember(recipient);
                    if ( cm != null ) {
                        cm.sendMessage(comps);
                    }
                }

                // イベントのキャンセル
                event.setCancelled(true);

                // 設定が有効ならコンソールに表示する
                if ( config.isDisplayNormalChatOnConsole() ) {
                    LunaChat.getPlugin().log(Level.INFO, makeLegacyText(comps));
                }

            } else {
                // 通常チャットイベントで発言

                // チャットフォーマット装飾の適用
                if ( config.isEnableNormalChatMessageFormat() ) {
                    String f = config.getNormalChatMessageFormat();
                    f = ClickableFormat.replaceForNormalChatFormat(
                            f, ChannelMember.getChannelMember(event.getPlayer()));
                    event.setFormat(Utility.replaceColorCode(f));
                }

                // 発言内容の設定
                event.setMessage(message);
            }

            // ロギング
            logNormalChat(message, player.getName());

            return;
        }
    }

    /**
     * 既定のチャンネルへの参加を試みる。
     * @param player プレイヤー
     * @return 参加できたかどうか
     */
    private boolean tryJoinToGlobalChannel(Player player) {

        LunaChatConfig config = LunaChat.getConfig();
        LunaChatAPI api = LunaChat.getAPI();

        String gcName = config.getGlobalChannel();

        // チャンネルが存在しない場合は作成する
        Channel global = api.getChannel(gcName);
        if ( global == null ) {
            global = api.createChannel(gcName, ChannelMember.getChannelMember(player));
        }

        // デフォルト発言先が無いなら、グローバルチャンネルに設定する
        Channel dchannel = api.getDefaultChannel(player.getName());
        if ( dchannel == null ) {
            api.setDefaultChannel(player.getName(), gcName);
        }

        return true;
    }

    /**
     * 強制参加チャンネルへ参加させる
     * @param player プレイヤー
     */
    private void forceJoinToForceJoinChannels(Player player) {

        LunaChatConfig config = LunaChat.getConfig();
        LunaChatAPI api = LunaChat.getAPI();

        List<String> forceJoinChannels = config.getForceJoinChannels();

        for ( String cname : forceJoinChannels ) {

            // チャンネルが存在しない場合は作成する
            Channel channel = api.getChannel(cname);
            if ( channel == null ) {
                channel = api.createChannel(cname, ChannelMember.getChannelMember(player));
            }

            // チャンネルのメンバーでないなら、参加する
            ChannelMember cp = ChannelMember.getChannelMember(player);
            if ( !channel.getMembers().contains(cp) ) {
                channel.addMember(cp);
            }

            // デフォルト発言先が無いなら、デフォルトチャンネルに設定する
            Channel dchannel = api.getDefaultChannel(player.getName());
            if ( dchannel == null ) {
                api.setDefaultChannel(player.getName(), cname);
            }
        }
    }

    /**
     * プレイヤーのサーバー参加時用の参加チャンネルリストを返す
     * @param player プレイヤー
     * @return リスト
     */
    private ArrayList<BaseComponent[]> getListForMotd(Player player) {

        ChannelMember cp = ChannelMember.getChannelMember(player);
        LunaChatAPI api = LunaChat.getAPI();
        Channel dc = api.getDefaultChannel(cp.getName());
        String dchannel = "";
        if ( dc != null ) {
            dchannel = dc.getName().toLowerCase();
        }

        // チャンネル一覧を取得して、参加人数でソートする
        ArrayList<Channel> channels = new ArrayList<>(api.getChannels());
        Collections.sort(channels, new Comparator<Channel>() {
            public int compare(Channel c1, Channel c2) {
                if ( c1.getOnlineNum() == c2.getOnlineNum() ) return c1.getName().compareTo(c2.getName());
                return c2.getOnlineNum() - c1.getOnlineNum();
            }
        });

        int count = 0;
        ArrayList<BaseComponent[]> items = new ArrayList<>();
        items.add(TextComponent.fromLegacyText(Messages.motdFirstLine()));
        for ( Channel channel : channels ) {

            // BANされているチャンネルは表示しない
            if ( channel.getBanned().contains(cp) ) {
                continue;
            }

            // 個人チャットはリストに表示しない
            if ( channel.isPersonalChat() ) {
                continue;
            }

            // 参加していないチャンネルは、グローバルチャンネルを除き表示しない
            if ( !channel.getMembers().contains(cp) &&
                    !channel.isGlobalChannel() ) {
                continue;
            }

            String disp = ChatColor.WHITE + channel.getName();
            if ( channel.getName().equals(dchannel) ) {
                disp = ChatColor.RED + channel.getName();
            }
            String desc = channel.getDescription();
            int onlineNum = channel.getOnlineNum();
            int memberNum = channel.getTotalNum();
            items.add(Messages.listFormat(disp, onlineNum, memberNum, desc));
            count++;

            if ( count > MAX_LIST_ITEMS ) {
                break;
            }
        }
        items.add(TextComponent.fromLegacyText(Messages.listEndLine()));

        return items;
    }

    /**
     * チャンネルに発言処理を行う
     * @param player プレイヤー
     * @param channel チャンネル
     * @param message 発言内容
     * @return イベントでキャンセルされたかどうか
     */
    private boolean chatToChannelWithEvent(ChannelMember player, Channel channel, String message) {

        // LunaChatPreChatEvent イベントコール
        EventResult result = LunaChat.getEventSender().sendLunaChatPreChatEvent(
                channel.getName(), player, message);
        if ( result.isCancelled() ) {
            return true;
        }
        Channel alt = result.getChannel();
        if ( alt != null ) {
            channel = alt;
        }
        message = result.getMessage();

        // チャンネルチャット発言
        channel.chat(player, message);

        return false;
    }

    /**
     * 通常チャットの発言をログファイルへ記録する
     * @param message
     * @param player
     */
    private void logNormalChat(String message, String player) {
        LunaChat.getNormalChatLogger().log(message, player);
    }

    /**
     * 指定されたEventPriorityが、LunaChatConfigで指定されているEventPriorityと一致するかどうかを調べる
     * @param priority
     * @return 一致するかどうか
     */
    private boolean matchesEventPriority(EventPriority priority) {
        String c = LunaChat.getConfig().getPlayerChatEventListenerPriority().name();
        return c.equals(priority.name());
    }

    /**
     * BaseComponent配列を、カラーコードを含むStringに変換する
     * @param comps BaseComponentの配列
     * @return カラーコードを含むString
     */
    private static String makeLegacyText(BaseComponent[] comps) {
        StringBuilder builder = new StringBuilder();
        for ( BaseComponent comp : comps ) {
            builder.append(comp.toLegacyText());
        }
        return builder.toString();
    }
}
