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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerData {
    
    // Identifiants uniques
    public String playerId;
    public String playerName;
    public int connectionId;
    
    // Données du héros
    public Hero hero;
    public int level;
    public int experience;
    public int health;
    public int maxHealth;
    
    // Position et état
    public int position;
    public boolean isAlive;
    public boolean isReady;
    public boolean isConnected;
    
    // Équipement et inventaire
    public HashMap<String, Item> equipment;
    public ArrayList<Item> inventory;
    
    // Statistiques
    public int killCount;
    public int goldCollected;
    public int itemsFound;
    public long playTime;
    
    // Apparence
    public int spriteType;
    public int colorScheme;
    
    public PlayerData(String id, String name) {
        this.playerId = id;
        this.playerName = name;
        this.connectionId = generateConnectionId();
        
        // Initialisation des valeurs par défaut
        this.level = 1;
        this.experience = 0;
        this.health = 20;
        this.maxHealth = 20;
        this.position = -1;
        this.isAlive = true;
        this.isReady = false;
        this.isConnected = true;
        
        this.equipment = new HashMap<>();
        this.inventory = new ArrayList<>();
        
        this.killCount = 0;
        this.goldCollected = 0;
        this.itemsFound = 0;
        this.playTime = 0;
        
        this.spriteType = 0;
        this.colorScheme = 0xFFFFFF;
    }
    
    // Constructeur pour chargement depuis bundle
    public PlayerData(Bundle bundle) {
        restoreFromBundle(bundle);
    }
    
    private int generateConnectionId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }
    
    // Mettre à jour les données depuis un héros
    public void updateFromHero(Hero hero) {
        if (hero != null) {
            this.hero = hero;
            this.level = hero.lvl;
            this.experience = hero.exp;
            this.health = hero.HP;
            this.maxHealth = hero.HT;
            this.position = hero.pos;
            this.isAlive = hero.isAlive();
        }
    }
    
    // Synchroniser les données avec d'autres joueurs
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        
        bundle.put("playerId", playerId);
        bundle.put("playerName", playerName);
        bundle.put("connectionId", connectionId);
        
        bundle.put("level", level);
        bundle.put("experience", experience);
        bundle.put("health", health);
        bundle.put("maxHealth", maxHealth);
        bundle.put("position", position);
        bundle.put("isAlive", isAlive);
        bundle.put("isReady", isReady);
        bundle.put("isConnected", isConnected);
        
        bundle.put("killCount", killCount);
        bundle.put("goldCollected", goldCollected);
        bundle.put("itemsFound", itemsFound);
        bundle.put("playTime", playTime);
        
        bundle.put("spriteType", spriteType);
        bundle.put("colorScheme", colorScheme);
        
        // Sauvegarder l'équipement (simplifié)
        Bundle equipmentBundle = new Bundle();
        int equipIndex = 0;
        for (String slot : equipment.keySet()) {
            equipmentBundle.put(slot, equipment.get(slot));
        }
        bundle.put("equipment", equipmentBundle);
        
        return bundle;
    }
    
    // Restaurer depuis un bundle
    public void restoreFromBundle(Bundle bundle) {
        playerId = bundle.getString("playerId");
        playerName = bundle.getString("playerName");
        connectionId = bundle.getInt("connectionId");
        
        level = bundle.getInt("level");
        experience = bundle.getInt("experience");
        health = bundle.getInt("health");
        maxHealth = bundle.getInt("maxHealth");
        position = bundle.getInt("position");
        isAlive = bundle.getBoolean("isAlive");
        isReady = bundle.getBoolean("isReady");
        isConnected = bundle.getBoolean("isConnected");
        
        killCount = bundle.getInt("killCount");
        goldCollected = bundle.getInt("goldCollected");
        itemsFound = bundle.getInt("itemsFound");
        playTime = bundle.getLong("playTime");
        
        spriteType = bundle.getInt("spriteType");
        colorScheme = bundle.getInt("colorScheme");
        
        // Restaurer l'équipement (simplifié)
        Bundle equipmentBundle = bundle.getBundle("equipment");
        if (equipmentBundle != null) {
            equipment = new HashMap<>();
            // Note: En réalité, il faudrait restaurer chaque item individuellement
        }
    }
    
    // Méthodes utilitaires
    public boolean isHost() {
        return "host".equals(playerId);
    }
    
    public boolean canInteract() {
        return isConnected && isAlive && isReady;
    }
    
    public float getHealthPercent() {
        return maxHealth > 0 ? (float) health / maxHealth : 0f;
    }
    
    public void addKill() {
        killCount++;
    }
    
    public void addGold(int amount) {
        goldCollected += amount;
    }
    
    public void addItemFound() {
        itemsFound++;
    }
    
    public void updatePlayTime(long delta) {
        playTime += delta;
    }
    
    // Getters et Setters
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
    
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    
    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean alive) { isAlive = alive; }
    
    public boolean isReady() { return isReady; }
    public void setReady(boolean ready) { isReady = ready; }
    
    public boolean isConnected() { return isConnected; }
    public void setConnected(boolean connected) { isConnected = connected; }
    
    @Override
    public String toString() {
        return String.format("PlayerData{id='%s', name='%s', level=%d, health=%d/%d, pos=%d}", 
            playerId, playerName, level, health, maxHealth, position);
    }
    
    // Comparaison pour l'égalité basée sur l'ID
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PlayerData that = (PlayerData) obj;
        return playerId.equals(that.playerId);
    }
    
    @Override
    public int hashCode() {
        return playerId.hashCode();
    }
}