package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.multiplayer.MultiplayerManager;
import com.shatteredpixel.shatteredpixeldungeon.multiplayer.PlayerData;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

import java.util.ArrayList;

public class WndSessionInfo extends Window {
    
    private static final int WIDTH = 150;
    private static final int GAP = 2;
    private static final int PLAYER_HEIGHT = 25;
    
    public WndSessionInfo() {
        super();
        
        resize(WIDTH, 0);
        
        RenderedTextBlock title = PixelScene.renderTextBlock("Session Multijoueur", 9);
        title.hardlight(TITLE_COLOR);
        title.setPos((WIDTH - title.width()) / 2, GAP);
        add(title);
        
        float pos = title.bottom() + GAP*2;
        
        // Informations de la session
        RenderedTextBlock sessionInfo = PixelScene.renderTextBlock(
            "Code: " + MultiplayerManager.sessionCode + 
            " | Mode: " + MultiplayerManager.currentMode.getDisplayName() +
            " | Joueurs: " + MultiplayerManager.getConnectedPlayerCount() + "/" + MultiplayerManager.maxPlayers, 
            6
        );
        sessionInfo.setPos(0, pos);
        add(sessionInfo);
        pos = sessionInfo.bottom() + GAP*2;
        
        // Liste des joueurs
        RenderedTextBlock playersTitle = PixelScene.renderTextBlock("Joueurs connectés:", 7);
        playersTitle.hardlight(0xFFFF00);
        playersTitle.setPos(0, pos);
        add(playersTitle);
        pos = playersTitle.bottom() + GAP;
        
        // Affichage de chaque joueur
        ArrayList<PlayerData> players = new ArrayList<>(MultiplayerManager.connectedPlayers.values());
        for (PlayerData player : players) {
            addPlayerInfo(player, pos);
            pos += PLAYER_HEIGHT + GAP;
        }
        
        // Boutons d'action
        RedButton closeBtn = new RedButton("Fermer") {
            @Override
            protected void onClick() {
                hide();
            }
        };
        closeBtn.setRect(0, pos, WIDTH, 20);
        add(closeBtn);
        pos = closeBtn.bottom() + GAP;
        
        if (MultiplayerManager.isHost) {
            RedButton endBtn = new RedButton("Terminer la Session") {
                @Override
                protected void onClick() {
                    MultiplayerManager.endSession();
                    hide();
                }
            };
            endBtn.setRect(0, pos, WIDTH, 20);
            add(endBtn);
            pos = endBtn.bottom() + GAP;
        } else {
            RedButton leaveBtn = new RedButton("Quitter la Session") {
                @Override
                protected void onClick() {
                    MultiplayerManager.leaveSession();
                    hide();
                }
            };
            leaveBtn.setRect(0, pos, WIDTH, 20);
            add(leaveBtn);
            pos = leaveBtn.bottom() + GAP;
        }
        
        resize(WIDTH, (int)pos + GAP);
    }
    
    private void addPlayerInfo(PlayerData player, float pos) {
        // Fond pour le joueur
        ColorBlock bg = new ColorBlock(WIDTH, PLAYER_HEIGHT, 
            player.getPlayerId().equals(MultiplayerManager.localPlayerId) ? 0x445588 : 0x333333);
        bg.y = pos;
        add(bg);
        
        // Nom du joueur
        RenderedTextBlock name = PixelScene.renderTextBlock(player.getPlayerName(), 6);
        name.hardlight(player.isAlive() ? 0xFFFFFF : 0xFF6666);
        name.setPos(5, pos + 3);
        add(name);
        
        // Niveau et PV
        String stats = "Niv." + player.getLevel() + " | " + 
                      player.getHealth() + "/" + player.getMaxHealth() + " PV";
        RenderedTextBlock statsText = PixelScene.renderTextBlock(stats, 5);
        statsText.hardlight(0xCCCCCC);
        statsText.setPos(5, pos + 12);
        add(statsText);
        
        // Statut
        String status = player.isReady() ? "Prêt" : "En attente";
        if (!player.isAlive()) status = "Mort";
        if (!player.isConnected()) status = "Déconnecté";
        
        RenderedTextBlock statusText = PixelScene.renderTextBlock(status, 5);
        statusText.hardlight(getStatusColor(status));
        statusText.setPos(WIDTH - statusText.width() - 5, pos + 3);
        add(statusText);
        
        // Indicateur hôte
        if (player.isHost()) {
            RenderedTextBlock hostText = PixelScene.renderTextBlock("Hôte", 5);
            hostText.hardlight(0xFFFF00);
            hostText.setPos(WIDTH - hostText.width() - 5, pos + 12);
            add(hostText);
        }
    }
    
    private int getStatusColor(String status) {
        switch (status) {
            case "Prêt": return 0x00FF00;
            case "En attente": return 0xFFFF00;
            case "Mort": return 0xFF0000;
            case "Déconnecté": return 0x666666;
            default: return 0xFFFFFF;
        }
    }
}