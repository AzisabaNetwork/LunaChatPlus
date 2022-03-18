/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
package com.github.ucchyocean.lc3.member;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatBukkit;
import com.github.ucchyocean.lc3.bridge.VaultChatBridge;
import com.github.ucchyocean.lc3.util.BlockLocation;

import net.md_5.bungee.api.chat.BaseComponent;

/**
 * ChannelMemberのBukkitPlayer実装
 * @author ucchy
 */
public class ChannelMemberPlayer extends ChannelMemberBukkit {

    private UUID id;

    /**
     * コンストラクタ
     * @param id プレイヤーID
     */
    public ChannelMemberPlayer(String id) {
        this.id = UUID.fromString(id);
    }

    /**
     * コンストラクタ
     * @param id UUID
     */
    public ChannelMemberPlayer(UUID id) {
        this.id = id;
    }

    /**
     * プレイヤー名からUUIDを取得してChannelMemberPlayerを作成して返す
     * @param name プレイヤー名
     * @return ChannelMemberPlayer
     */
    public static ChannelMemberPlayer getChannelMemberPlayerFromName(String name) {
        Player player = Bukkit.getPlayerExact(name);
        if ( player != null ) {
            return new ChannelMemberPlayer(player.getUniqueId());
        }
        @SuppressWarnings("deprecation")
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        if ( offline != null && offline.getUniqueId() != null ) {
            return new ChannelMemberPlayer(offline.getUniqueId());
        }
        return null;
    }

    /**
     * CommandSenderから、ChannelPlayerを作成して返す
     * @param sender
     * @return ChannelPlayer
     */
    public static ChannelMemberPlayer getChannelPlayer(CommandSender sender) {
        if ( sender instanceof Player ) {
            return new ChannelMemberPlayer(((Player)sender).getUniqueId());
        }
        return new ChannelMemberPlayer(sender.getName());
    }

    /**
     * オンラインかどうか
     * @return オンラインかどうか
     */
    @Override
    public boolean isOnline() {
        Player player = Bukkit.getPlayer(id);
        return (player != null);
    }

    /**
     * プレイヤー名を返す
     * @return プレイヤー名
     * @see com.github.ucchyocean.lc.channel.ChannelPlayer#getName()
     */
    @Override
    public String getName() {
        String cache = LunaChat.getUUIDCacheData().get(id.toString());
        if ( cache != null ) {
            return cache;
        }
        Player player = Bukkit.getPlayer(id);
        if ( player != null ) {
            return player.getName();
        }
        OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(id);
        if ( offlineplayer != null ) {
            String name = offlineplayer.getName();
            return name;
        }
        return id.toString();
    }

    /**
     * プレイヤー表示名を返す
     * @return プレイヤー表示名
     * @see com.github.ucchyocean.lc.channel.ChannelPlayer#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        Player player = getPlayer();
        if ( player != null ) {
            return player.getDisplayName();
        }
        return getName();
    }

    /**
     * プレフィックスを返す
     * @return プレフィックス
     * @see com.github.ucchyocean.lc.channel.ChannelPlayer#getPrefix()
     */
    @Override
    public String getPrefix() {
        VaultChatBridge vault = LunaChatBukkit.getInstance().getVaultChat();
        if ( vault == null ) {
            return "";
        }
        Player player = getPlayer();
        if ( player != null ) {
            return vault.getPlayerPrefix(player);
        }
        return "";
    }

    /**
     * サフィックスを返す
     * @return サフィックス
     * @see com.github.ucchyocean.lc.channel.ChannelPlayer#getSuffix()
     */
    @Override
    public String getSuffix() {
        VaultChatBridge vault = LunaChatBukkit.getInstance().getVaultChat();
        if ( vault == null ) {
            return "";
        }
        Player player = getPlayer();
        if ( player != null ) {
            return vault.getPlayerSuffix(player);
        }
        return "";
    }

    /**
     * メッセージを送る
     * @param message 送るメッセージ
     * @see com.github.ucchyocean.lc.channel.ChannelPlayer#sendMessage(java.lang.String)
     */
    @Override
    public void sendMessage(String message) {
        if ( message == null || message.isEmpty() ) return;
        Player player = getPlayer();
        if ( player != null ) {
            player.sendMessage(message);
        }
    }

    /**
     * メッセージを送る
     * @param message 送るメッセージ
     * @see com.github.ucchyocean.lc3.member.ChannelMember#sendMessage(net.md_5.bungee.api.chat.BaseComponent[])
     */
    public void sendMessage(BaseComponent[] message) {
        if ( message == null || message.length == 0 ) return;
        Player player = getPlayer();
        if ( player != null ) {
            player.spigot().sendMessage(message);
        }
    }

    /**
     * BukkitのPlayerを取得する
     * @return Player
     * @see com.github.ucchyocean.lc.channel.ChannelPlayer#getPlayer()
     */
    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(id);
    }

    /**
     * 発言者が今いるワールドを取得する
     * @return 発言者が今いるワールド
     * @see com.github.ucchyocean.lc3.member.ChannelMemberBukkit#getWorld()
     */
    @Override
    public World getWorld() {
        Player player = getPlayer();
        if ( player != null ) return player.getWorld();
        return null;
    }

    /**
     * 発言者が今いるワールドのワールド名を取得する
     * @return ワールド名
     * @see com.github.ucchyocean.lc.channel.ChannelPlayer#getWorldName()
     */
    @Override
    public String getWorldName() {
        World world = getWorld();
        if ( world == null ) return "";
        if ( LunaChatBukkit.getInstance().getMultiverseCore() != null ) {
            return LunaChatBukkit.getInstance().getMultiverseCore().getWorldAlias(world.getName());
        }
        return world.getName();
    }

    /**
     * 発言者が今いる位置を取得する
     * @return 発言者の位置
     * @see com.github.ucchyocean.lc.channel.ChannelPlayer#getLocation()
     */
    @Override
    public Location getLocation() {
        Player player = getPlayer();
        if ( player != null ) {
            return player.getLocation();
        }
        return null;
    }

    /**
     * 指定されたパーミッションノードの権限を持っているかどうかを取得する
     * @param node パーミッションノード
     * @return 権限を持っているかどうか
     * @see com.github.ucchyocean.lc.channel.ChannelPlayer#hasPermission(java.lang.String)
     */
    @Override
    public boolean hasPermission(String node) {
        Player player = getPlayer();
        if ( player == null ) {
            return false;
        } else {
            return player.hasPermission(node);
        }
    }

    /**
     * 指定されたパーミッションノードが定義されているかどうかを取得する
     * @param node パーミッションノード
     * @return 定義を持っているかどうか
     * @see com.github.ucchyocean.lc.channel.ChannelPlayer#isPermissionSet(java.lang.String)
     */
    @Override
    public boolean isPermissionSet(String node) {
        Player player = getPlayer();
        if ( player == null ) {
            return false;
        } else {
            return player.isPermissionSet(node);
        }
    }

    /**
     * 指定されたメッセージの内容を発言する
     * @param message メッセージ
     * @see com.github.ucchyocean.lc3.member.ChannelMember#chat(java.lang.String)
     */
    public void chat(String message) {
        Player player = getPlayer();
        if ( player != null ) {
            player.chat(message);
        }
    }

    /**
     * IDを返す
     * @return "$" + UUID を返す
     * @see com.github.ucchyocean.lc.channel.ChannelPlayer#getID()
     */
    @Override
    public String toString() {
        return "$" + id.toString();
    }

    public static ChannelMemberPlayer getChannelMember(String nameOrUuid) {
        if ( nameOrUuid.startsWith("$") ) {
            return new ChannelMemberPlayer(nameOrUuid.substring(1));
        } else {
            // LCP start - use cache if possible
            String uuid = LunaChat.getUUIDCacheData().getUUIDFromName(nameOrUuid);
            if (uuid != null) return new ChannelMemberPlayer(uuid);
            // LCP end - use cache if possible
            @SuppressWarnings("deprecation")
            OfflinePlayer op = Bukkit.getOfflinePlayer(nameOrUuid);
            if ( op == null ) return null;
            return new ChannelMemberPlayer(op.getUniqueId());
        }
    }

    // LCP start
    public static ChannelMemberPlayer getChannelMemberFast(String nameOrUuid) {
        if ( nameOrUuid.startsWith("$") ) {
            return new ChannelMemberPlayer(nameOrUuid.substring(1));
        } else {
            Player op = Bukkit.getPlayer(nameOrUuid);
            if ( op != null ) return new ChannelMemberPlayer(op.getUniqueId());
            String uuid = LunaChat.getUUIDCacheData().getUUIDFromName(nameOrUuid);
            if (uuid != null) return new ChannelMemberPlayer(uuid);
            return null;
        }
    }

    public static ChannelMemberPlayer getOnlineChannelMember(String nameOrUuid) {
        if ( nameOrUuid.startsWith("$") ) {
            return new ChannelMemberPlayer(nameOrUuid.substring(1));
        } else {
            Player op = Bukkit.getPlayer(nameOrUuid);
            if ( op == null ) return null;
            return new ChannelMemberPlayer(op.getUniqueId());
        }
    }
    // LCP end

    public ChannelMemberOther toChannelMemberOther() {
        ChannelMemberOther other = new ChannelMemberOther(getName(), getDisplayName(),
                getPrefix(), getSuffix(), getBlockLocation(), id.toString());
        other.setWorldName(getWorldName());
        return other;
    }

    private BlockLocation getBlockLocation() {
        Location loc = getLocation();
        if ( loc == null ) return null;
        return new BlockLocation(getWorldName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
