/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.cm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ログインできない人がログインしようとしたときに、通知を送るプラグイン
 * @author ucchy
 */
public class ConnectMessage extends JavaPlugin implements Listener {

    private static final String MESSAGE_CONFIG_FILE = "messages.yml";
    private static final String PERMISSION_NODE = "connectmessage.notify";

    private String messageBanned;
    private String messageWhitelist;
    private String messageFull;
    private String messageOther;

    /**
     * プラグインが有効化されたときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        // リスナーとして登録する。
        getServer().getPluginManager().registerEvents(this, this);

        // メッセージ設定を取得する。
        loadMessageConfig();
    }

    /**
     * プレイヤーがログインしようとしたときに呼び出されるメソッド
     * @param event
     */
    @EventHandler
    public void onPreLogin(PlayerLoginEvent event) {

        // 正常にログインした場合は、何もしない。
        if ( event.getResult() == Result.ALLOWED ) {
            return;
        }

        // キックされた場合は、内容に応じてメッセージを取得する。
        String message;
        if ( event.getResult() == Result.KICK_BANNED ) {
            message = messageBanned;
        } else if ( event.getResult() == Result.KICK_WHITELIST ) {
            message = messageWhitelist;
        } else if ( event.getResult() == Result.KICK_FULL ) {
            message = messageFull;
        } else {
            message = messageOther;
        }

        // 該当プレイヤー名を置き換える
        message = message.replace("%player", event.getPlayer().getName());

        // カラーコードを置き換える
        message = ChatColor.translateAlternateColorCodes('&', message);

        // 権限を持っている人たちにメッセージを送信する。
        getServer().broadcast(message, PERMISSION_NODE);
    }

    /**
     * メッセージ設定ファイルを読込する
     */
    private void loadMessageConfig() {

        // プラグインのフォルダを取得。まだ存在しなければ作成。
        File folder = this.getDataFolder();
        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        // messages.yml を取得。まだ存在しなければデフォルトを作成。
        File file = new File(folder, MESSAGE_CONFIG_FILE);
        if ( !file.exists() ) {
            copyFileFromJar(this.getFile(), file, MESSAGE_CONFIG_FILE);
        }

        // ファイルのロード
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // 設定値の取得
        messageBanned = config.getString("banned",
                "&7%player はBANされているためログインできません。");
        messageWhitelist = config.getString("whitelist",
                "&7%player はホワイトリストに含まれていないためログインできません。");
        messageFull = config.getString("full",
                "&7%player はサーバーが満員のためログインできません。");
        messageOther = config.getString("other",
                "&7%player は他のプラグインによってキックされたためログインできません。");
    }

    /**
     * jarファイルの中に格納されているテキストファイルを、jarファイルの外にコピーするメソッド<br/>
     * WindowsだとS-JISで、MacintoshやLinuxだとUTF-8で保存されます。
     * @param jarFile jarファイル
     * @param targetFile コピー先
     * @param sourceFilePath コピー元
     */
    private static void copyFileFromJar(
            File jarFile, File targetFile, String sourceFilePath) {

        JarFile jar = null;
        InputStream is = null;
        FileOutputStream fos = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;

        File parent = targetFile.getParentFile();
        if ( !parent.exists() ) {
            parent.mkdirs();
        }

        try {
            jar = new JarFile(jarFile);
            ZipEntry zipEntry = jar.getEntry(sourceFilePath);
            is = jar.getInputStream(zipEntry);

            fos = new FileOutputStream(targetFile);

            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            writer = new BufferedWriter(new OutputStreamWriter(fos));

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( jar != null ) {
                try {
                    jar.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( writer != null ) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( reader != null ) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( fos != null ) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( is != null ) {
                try {
                    is.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
        }
    }
}
