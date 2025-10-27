/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.multiplayer;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MultiplayerManager {
    
    public enum GameMode {
        SOLO("Solo"),
        COOP("Coopératif"),
        PVP("Joueur vs Joueur"),
        SURVIVAL("Survie");
        
        private final String displayName;
        
        GameMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Configuration de session
    public static GameMode currentMode = GameMode.SOLO;
    public static boolean isHost = false;
    public static String sessionCode = "";
    public static int maxPlayers = 4;
    public static boolean sessionActive = false;
    
    // Données des joueurs
    public static HashMap<String, PlayerData> connectedPlayers = new HashMap<>();
    public static String localPlayerId = "";
    
    // Statistiques de session
    public static long sessionStartTime = 0;
    public static int totalMonstersKilled = 0;
    public static int totalGoldCollected = 0;
    
    // Initialiser une session multijoueur
    public static boolean createSession(GameMode mode, int playerLimit, String hostName) {
        if (sessionActive) {
            GLog.w(Messages.get(MultiplayerManager.class, "session_already_active"));
            return false;
        }
        
        currentMode = mode;
        maxPlayers = Math.max(2, Math.min(playerLimit, 8)); // Limite 2-8 joueurs
        isHost = true;
        sessionActive = true;
        sessionCode = generateSessionCode();
        sessionStartTime = System.currentTimeMillis();
        
        // Créer le joueur local (hôte)
        localPlayerId = UUID.randomUUID().toString();
        PlayerData host = new PlayerData(localPlayerId, hostName);
        host.updateFromHero(Dungeon.hero);
        connectedPlayers.put(localPlayerId, host);
        
        GLog.p(Messages.get(MultiplayerManager.class, "session_created", sessionCode, mode.getDisplayName()));
        GLog.i(Messages.get(MultiplayerManager.class, "waiting_players", maxPlayers - 1));
        
        return true;
    }
    
    // Rejoindre une session
    public static boolean joinSession(String code, String playerName) {
        if (sessionActive) {
            GLog.w(Messages.get(MultiplayerManager.class, "already_in_session"));
            return false;
        }
        
        if (!validateSessionCode(code)) {
            GLog.w(Messages.get(MultiplayerManager.class, "invalid_session_code"));
            return false;
        }
        
        // Simulation de connexion à une session existante
        sessionCode = code;
        isHost = false;
        sessionActive = true;
        currentMode = GameMode.COOP; // Par défaut en coop
        
        // Créer le joueur local
        localPlayerId = UUID.randomUUID().toString();
        PlayerData player = new PlayerData(localPlayerId, playerName);
        player.updateFromHero(Dungeon.hero);
        connectedPlayers.put(localPlayerId, player);
        
        // Simuler d'autres joueurs (pour le test)
        simulateOtherPlayers();
        
        GLog.p(Messages.get(MultiplayerManager.class, "session_joined", sessionCode));
        GLog.i(Messages.get(MultiplayerManager.class, "players_connected", getConnectedPlayerCount()));
        
        return true;
    }
    
    // Quitter la session
    public static void leaveSession() {
        if (!sessionActive) return;
        
        GLog.i(Messages.get(MultiplayerManager.class, "session_left"));
        
        // Nettoyer les données
        connectedPlayers.clear();
        sessionActive = false;
        isHost = false;
        sessionCode = "";
        localPlayerId = "";
        
        // Revenir en mode solo
        currentMode = GameMode.SOLO;
    }
    
    // Simulation d'autres joueurs (pour test)
    private static void simulateOtherPlayers() {
        if (!isHost) {
            // Ajouter des joueurs simulés
            String[] names = {"Guerrier", "Mage", "Archer", "Voleur"};
            for (int i = 0; i < 2; i++) {
                String playerId = "simulated_" + i;
                PlayerData simulated = new PlayerData(playerId, names[i]);
                simulated.setLevel(1 + i);
                simulated.setHealth(20 + i * 5);
                simulated.setMaxHealth(20 + i * 5);
                simulated.setPosition(Dungeon.hero.pos + i + 1);
                simulated.setReady(true);
                connectedPlayers.put(playerId, simulated);
            }
        }
    }
    
    // Générer un code de session
    private static String generateSessionCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        return code.toString();
    }
    
    private static boolean validateSessionCode(String code) {
        return code != null && code.length() == 6 && code.matches("[A-Z0-9]+");
    }
    
    // Mettre à jour les données du joueur local
    public static void updateLocalPlayer() {
        if (!sessionActive || localPlayerId.isEmpty()) return;
        
        PlayerData localPlayer = connectedPlayers.get(localPlayerId);
        if (localPlayer != null && Dungeon.hero != null) {
            localPlayer.updateFromHero(Dungeon.hero);
        }
    }
    
    // Synchroniser avec tous les joueurs
    public static void syncAllPlayers() {
        if (!sessionActive || !isHost) return;
        
        updateLocalPlayer();
        
        for (PlayerData player : connectedPlayers.values()) {
            if (!player.getPlayerId().equals(localPlayerId)) {
                broadcastPlayerUpdate(player);
            }
        }
    }
    
    // Diffuser une mise à jour de joueur
    private static void broadcastPlayerUpdate(PlayerData player) {
        // En production, cela enverrait les données via réseau
        Bundle playerData = player.toBundle();
        // Simulation: les autres joueurs reçoivent la mise à jour
        receivePlayerUpdate(playerData);
    }
    
    // Recevoir une mise à jour de joueur
    public static void receivePlayerUpdate(Bundle playerData) {
        if (!sessionActive) return;
        
        PlayerData updatedPlayer = new PlayerData(playerData);
        connectedPlayers.put(updatedPlayer.getPlayerId(), updatedPlayer);
        
        // Mettre à jour l'affichage si nécessaire
        GameScene.updateMultiplayerDisplay();
    }
    
    // Gérer le partage d'objets entre joueurs
    public static boolean shareItem(Item item, String fromPlayerId, String toPlayerId) {
        if (!sessionActive || currentMode != GameMode.COOP) {
            return false;
        }
        
        PlayerData fromPlayer = connectedPlayers.get(fromPlayerId);
        PlayerData toPlayer = connectedPlayers.get(toPlayerId);
        
        if (fromPlayer == null || toPlayer == null) {
            return false;
        }
        
        if (fromPlayerId.equals(localPlayerId)) {
            // Le joueur local donne un objet
            if (item != null && item.quantity() > 0) {
                GLog.i(Messages.get(MultiplayerManager.class, "item_shared", 
                    fromPlayer.getPlayerName(), toPlayer.getPlayerName(), item.name()));
                return true;
            }
        }
        
        return false;
    }
    
    // Gérer les interactions PvP
    public static boolean canAttackPlayer(String attackerId, String targetId) {
        if (!sessionActive || currentMode != GameMode.PVP) {
            return false;
        }
        
        if (attackerId.equals(targetId)) {
            return false; // Pas d'auto-attaque
        }
        
        PlayerData attacker = connectedPlayers.get(attackerId);
        PlayerData target = connectedPlayers.get(targetId);
        
        return attacker != null && target != null && 
               attacker.isAlive() && target.isAlive() &&
               attacker.isConnected() && target.isConnected();
    }
    
    // Gérer la résurrection d'un joueur
    public static void resurrectPlayer(String playerId) {
        if (!sessionActive) return;
        
        PlayerData player = connectedPlayers.get(playerId);
        if (player != null && !player.isAlive()) {
            player.setAlive(true);
            player.setHealth(player.getMaxHealth() / 2); // Ressusciter avec 50% PV
            player.setPosition(findSafeRespawnPosition());
            
            GLog.i(Messages.get(MultiplayerManager.class, "player_resurrected", player.getPlayerName()));
        }
    }
    
    // Trouver une position de respawn sécurisée
    private static int findSafeRespawnPosition() {
        // Chercher une position vide près du joueur local
        for (int i = 0; i < 10; i++) {
            int pos = Dungeon.hero.pos + i;
            if (pos >= 0 && pos < Dungeon.level.length() && 
                Dungeon.level.passable[pos] && Actor.findChar(pos) == null) {
                return pos;
            }
        }
        return Dungeon.hero.pos;
    }
    
    // Vérifier les conditions de victoire
    public static void checkWinConditions() {
        if (!sessionActive) return;
        
        switch (currentMode) {
            case COOP:
                // Tous les joueurs doivent être vivants au boss
                break;
            case PVP:
                // Un seul joueur doit rester
                checkPVPWinner();
                break;
            case SURVIVAL:
                // Survivre le plus longtemps possible
                checkSurvivalTime();
                break;
        }
    }
    
    private static void checkPVPWinner() {
        ArrayList<PlayerData> alivePlayers = new ArrayList<>();
        for (PlayerData player : connectedPlayers.values()) {
            if (player.isAlive() && player.isConnected()) {
                alivePlayers.add(player);
            }
        }
        
        if (alivePlayers.size() == 1) {
            PlayerData winner = alivePlayers.get(0);
            GLog.h(Messages.get(MultiplayerManager.class, "pvp_winner", winner.getPlayerName()));
            endSession();
        }
    }
    
    private static void checkSurvivalTime() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = (currentTime - sessionStartTime) / 1000; // en secondes
        
        // Victoire après 10 minutes de survie
        if (elapsedTime >= 600) {
            GLog.h(Messages.get(MultiplayerManager.class, "survival_victory", elapsedTime / 60));
            endSession();
        }
    }
    
    // Terminer la session
    public static void endSession() {
        if (!sessionActive) return;
        
        long sessionDuration = (System.currentTimeMillis() - sessionStartTime) / 1000;
        
        GLog.p(Messages.get(MultiplayerManager.class, "session_ended", 
            sessionDuration / 60, sessionDuration % 60));
        
        // Afficher les statistiques finales
        showSessionStatistics();
        
        leaveSession();
    }
    
    // Afficher les statistiques de session
    private static void showSessionStatistics() {
        GLog.newLine();
        GLog.p("=== STATISTIQUES DE SESSION ===");
        GLog.i("Durée: " + ((System.currentTimeMillis() - sessionStartTime) / 60000) + " minutes");
        GLog.i("Monstres tués: " + totalMonstersKilled);
        GLog.i("Or collecté: " + totalGoldCollected);
        GLog.i("Joueurs connectés: " + getConnectedPlayerCount());
        
        for (PlayerData player : connectedPlayers.values()) {
            GLog.i(player.getPlayerName() + ": Niv." + player.getLevel() + 
                   ", " + player.getKillCount() + " kills, " + 
                   player.getGoldCollected() + " or");
        }
    }
    
    // Méthodes utilitaires
    public static int getConnectedPlayerCount() {
        int count = 0;
        for (PlayerData player : connectedPlayers.values()) {
            if (player.isConnected()) {
                count++;
            }
        }
        return count;
    }
    
    public static int getAlivePlayerCount() {
        int count = 0;
        for (PlayerData player : connectedPlayers.values()) {
            if (player.isAlive() && player.isConnected()) {
                count++;
            }
        }
        return count;
    }
    
    public static PlayerData getLocalPlayer() {
        return connectedPlayers.get(localPlayerId);
    }
    
    public static ArrayList<PlayerData> getOtherPlayers() {
        ArrayList<PlayerData> others = new ArrayList<>();
        for (PlayerData player : connectedPlayers.values()) {
            if (!player.getPlayerId().equals(localPlayerId)) {
                others.add(player);
            }
        }
        return others;
    }
    
    public static boolean isSessionFull() {
        return getConnectedPlayerCount() >= maxPlayers;
    }
    
    // Sauvegarde et restauration
    private static final String SESSION_ACTIVE = "session_active";
    private static final String CURRENT_MODE = "current_mode";
    private static final String IS_HOST = "is_host";
    private static final String SESSION_CODE = "session_code";
    private static final String MAX_PLAYERS = "max_players";
    private static final String LOCAL_PLAYER_ID = "local_player_id";
    private static final String CONNECTED_PLAYERS = "connected_players";
    
    public static void storeInBundle(Bundle bundle) {
        if (!sessionActive) return;
        
        bundle.put(SESSION_ACTIVE, sessionActive);
        bundle.put(CURRENT_MODE, currentMode);
        bundle.put(IS_HOST, isHost);
        bundle.put(SESSION_CODE, sessionCode);
        bundle.put(MAX_PLAYERS, maxPlayers);
        bundle.put(LOCAL_PLAYER_ID, localPlayerId);
        
        Bundle playersBundle = new Bundle();
        for (PlayerData player : connectedPlayers.values()) {
            playersBundle.put(player.getPlayerId(), player.toBundle());
        }
        bundle.put(CONNECTED_PLAYERS, playersBundle);
    }
    
    public static void restoreFromBundle(Bundle bundle) {
        if (!bundle.getBoolean(SESSION_ACTIVE, false)) {
            return;
        }
        
        sessionActive = true;
        currentMode = bundle.getEnum(CURRENT_MODE, GameMode.class);
        isHost = bundle.getBoolean(IS_HOST);
        sessionCode = bundle.getString(SESSION_CODE);
        maxPlayers = bundle.getInt(MAX_PLAYERS);
        localPlayerId = bundle.getString(LOCAL_PLAYER_ID);
        
        Bundle playersBundle = bundle.getBundle(CONNECTED_PLAYERS);
        if (playersBundle != null) {
            connectedPlayers.clear();
            for (String key : playersBundle.keys()) {
                PlayerData player = new PlayerData(playersBundle.getBundle(key));
                connectedPlayers.put(player.getPlayerId(), player);
            }
        }
        
        GLog.i(Messages.get(MultiplayerManager.class, "session_restored"));
    }
}