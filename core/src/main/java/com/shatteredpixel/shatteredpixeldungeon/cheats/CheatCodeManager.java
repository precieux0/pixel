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

package com.shatteredpixel.shatteredpixeldungeon.cheats;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.HashMap;

public class CheatCodeManager {
    
    public static boolean cheatsEnabled = false;
    private static HashMap<String, Runnable> cheatCodes = new HashMap<>();
    private static StringBuilder inputBuffer = new StringBuilder();
    private static long lastInputTime = 0;
    
    static {
        initializeCheatCodes();
    }
    
    private static void initializeCheatCodes() {
        // Codes de triche classiques
        cheatCodes.put("IDDQD", () -> { // Dieu immortel
            enableGodMode();
        });
        
        cheatCodes.put("IDKFA", () -> { // Armes et clés
            giveAllWeapons();
        });
        
        cheatCodes.put("GOLD", () -> { // Or infini
            giveInfiniteGold();
        });
        
        cheatCodes.put("HEAL", () -> { // Soin complet
            fullHeal();
        });
        
        cheatCodes.put("LVLUP", () -> { // Monter de niveau
            levelUp();
        });
        
        cheatCodes.put("MAP", () -> { // Révéler la carte
            revealMap();
        });
        
        cheatCodes.put("KILLALL", () -> { // Tuer tous les ennemis
            killAllEnemies();
        });
        
        cheatCodes.put("FLY", () -> { // Voler
            toggleFlying();
        });
        
        cheatCodes.put("NOCLIP", () -> { // Passer à travers les murs
            toggleNoClip();
        });
        
        cheatCodes.put("SPEED", () -> { // Vitesse augmentée
            increaseSpeed();
        });
        
        cheatCodes.put("GOD", () -> { // Mode Dieu alternatif
            enableGodMode();
        });
        
        cheatCodes.put("INFINITE", () -> { // Tout avoir
            giveEverything();
        });
        
        cheatCodes.put("TELEPORT", () -> { // Téléportation libre
            enableTeleport();
        });
        
        cheatCodes.put("XRAY", () -> { // Vision rayons X
            enableXRayVision();
        });
        
        cheatCodes.put("SUPER", () -> { // Super pouvoirs
            enableSuperPowers();
        });
    }
    
    public static void processKeyInput(char key) {
        // Ignorer les caractères non alphabétiques et non numériques
        if (!Character.isLetterOrDigit(key)) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Réinitialiser le buffer après 3 secondes d'inactivité
        if (currentTime - lastInputTime > 3000) {
            inputBuffer.setLength(0);
        }
        
        inputBuffer.append(Character.toUpperCase(key));
        lastInputTime = currentTime;
        
        // Vérifier les codes de triche
        String currentInput = inputBuffer.toString();
        for (String code : cheatCodes.keySet()) {
            if (currentInput.endsWith(code)) {
                activateCheat(code);
                inputBuffer.setLength(0); // Réinitialiser après activation
                break;
            }
        }
        
        // Limiter la taille du buffer
        if (inputBuffer.length() > 20) {
            inputBuffer.setLength(0);
        }
    }
    
    private static void activateCheat(String code) {
        if (!cheatsEnabled) {
            cheatsEnabled = true;
            GLog.w(Messages.get(CheatCodeManager.class, "cheats_enabled"));
        }
        
        GLog.p(Messages.get(CheatCodeManager.class, "cheat_activated", code));
        cheatCodes.get(code).run();
        
        // Effet sonore
        Sample.INSTANCE.play(Assets.Sounds.EVOKE);
    }
    
    // IMPLÉMENTATION DES TRICHES
    
    private static void enableGodMode() {
        Hero hero = Dungeon.hero;
        hero.HP = hero.HT = 9999;
        CheatBuff.godMode().attachTo(hero);
        GLog.h(Messages.get(CheatCodeManager.class, "god_mode"));
    }
    
    private static void giveAllWeapons() {
        giveLegendaryWeapons();
        
        // Donner aussi des potions de force
        for (int i = 0; i < 5; i++) {
            PotionOfStrength potion = new PotionOfStrength();
            if (!potion.doPickUp(Dungeon.hero)) {
                Dungeon.level.drop(potion, Dungeon.hero.pos);
            }
        }
        
        GLog.h(Messages.get(CheatCodeManager.class, "all_weapons"));
    }
    
    private static void giveInfiniteGold() {
        Gold gold = new Gold();
        gold.quantity(9999);
        if (gold.doPickUp(Dungeon.hero)) {
            GLog.h(Messages.get(CheatCodeManager.class, "infinite_gold"));
        } else {
            Dungeon.level.drop(gold, Dungeon.hero.pos);
        }
    }
    
    private static void fullHeal() {
        Hero hero = Dungeon.hero;
        hero.HP = hero.HT;
        new PotionOfHealing().apply(hero);
        GLog.h(Messages.get(CheatCodeManager.class, "full_heal"));
    }
    
    private static void levelUp() {
        Hero hero = Dungeon.hero;
        if (hero.lvl < 30) {
            hero.lvl++;
            hero.updateHT(true);
            new ScrollOfUpgrade().apply(hero);
            GLog.h(Messages.get(CheatCodeManager.class, "level_up", hero.lvl));
        } else {
            GLog.w(Messages.get(CheatCodeManager.class, "max_level"));
        }
    }
    
    private static void revealMap() {
        Level level = Dungeon.level;
        for (int i = 0; i < level.length(); i++) {
            level.visited[i] = true;
            level.mapped[i] = true;
            level.discoverable[i] = true;
        }
        Dungeon.observe();
        GLog.h(Messages.get(CheatCodeManager.class, "map_revealed"));
    }
    
    private static void killAllEnemies() {
        int killCount = 0;
        for (Char ch : Actor.chars().toArray(new Char[0])) {
            if (ch != null && ch != Dungeon.hero && ch.alignment == Char.Alignment.ENEMY && ch.isAlive()) {
                ch.die(Dungeon.hero);
                killCount++;
            }
        }
        GLog.h(Messages.get(CheatCodeManager.class, "all_enemies_killed", killCount));
    }
    
    private static void toggleFlying() {
        Hero hero = Dungeon.hero;
        CheatBuff existing = hero.buff(CheatBuff.class);
        
        if (existing != null && existing.type == CheatBuff.CheatType.FLIGHT) {
            existing.detach();
            GLog.h(Messages.get(CheatCodeManager.class, "flight_off"));
        } else {
            // Retirer les autres buffs de vol existants
            Buff flightBuff = hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation.class);
            if (flightBuff != null) flightBuff.detach();
            
            CheatBuff.flight().attachTo(hero);
            GLog.h(Messages.get(CheatCodeManager.class, "flight_on"));
        }
    }
    
    private static void toggleNoClip() {
        Hero hero = Dungeon.hero;
        CheatBuff existing = hero.buff(CheatBuff.class);
        
        if (existing != null && existing.type == CheatBuff.CheatType.NO_CLIP) {
            existing.detach();
            GLog.h(Messages.get(CheatCodeManager.class, "noclip_off"));
        } else {
            CheatBuff.noClip().attachTo(hero);
            GLog.h(Messages.get(CheatCodeManager.class, "noclip_on"));
        }
    }
    
    private static void increaseSpeed() {
        Hero hero = Dungeon.hero;
        CheatBuff.speedBoost(3.0f).attachTo(hero); // 3x plus rapide
        GLog.h(Messages.get(CheatCodeManager.class, "speed_boost"));
    }
    
    private static void giveEverything() {
        // Or
        giveInfiniteGold();
        
        // Armes
        giveAllWeapons();
        
        // Soin
        fullHeal();
        
        // Niveaux
        for (int i = 0; i < 10 && Dungeon.hero.lvl < 30; i++) {
            levelUp();
        }
        
        // Pouvoirs
        enableGodMode();
        CheatBuff.speedBoost(2.0f).attachTo(Dungeon.hero);
        
        GLog.h(Messages.get(CheatCodeManager.class, "everything_given"));
    }
    
    private static void enableTeleport() {
        CheatBuff teleportBuff = new CheatBuff(CheatBuff.CheatType.TELEPORT, 1.0f, 300);
        teleportBuff.attachTo(Dungeon.hero);
        GLog.h(Messages.get(CheatCodeManager.class, "teleport_enabled"));
    }
    
    private static void enableXRayVision() {
        CheatBuff.xRayVision().attachTo(Dungeon.hero);
        GLog.h(Messages.get(CheatCodeManager.class, "xray_enabled"));
    }
    
    private static void enableSuperPowers() {
        Hero hero = Dungeon.hero;
        
        // Mode Dieu
        enableGodMode();
        
        // Vol
        CheatBuff.flight().attachTo(hero);
        
        // Vitesse
        CheatBuff.speedBoost(2.5f).attachTo(hero);
        
        // Vision rayons X
        CheatBuff.xRayVision().attachTo(hero);
        
        // Mana infini
        CheatBuff.infiniteMana().attachTo(hero);
        
        GLog.h(Messages.get(CheatCodeManager.class, "super_powers"));
    }
    
    private static void giveLegendaryWeapons() {
        try {
            // Épée Légendaire
            if (legendarySword.doPickUp(Dungeon.hero)) {
                GLog.i("§ " + Messages.get(CheatCodeManager.class, "legendary_sword_obtained") + " §");
            } else {
                Dungeon.level.drop(legendarySword, Dungeon.hero.pos);
            }
            
            // Épée de Feu
            if (fireSword.doPickUp(Dungeon.hero)) {
                GLog.i("§ " + Messages.get(CheatCodeManager.class, "fire_sword_obtained") + " §");
            } else {
                Dungeon.level.drop(fireSword, Dungeon.hero.pos);
            }
            
        } catch (Exception e) {
            GLog.w(Messages.get(CheatCodeManager.class, "weapon_error"));
        }
    }
    
    // Méthode utilitaire pour réinitialiser
    public static void resetCheats() {
        Hero hero = Dungeon.hero;
        if (hero != null) {
            // Retirer tous les buffs de triche
            for (Buff buff : hero.buffs(CheatBuff.class).toArray(new Buff[0])) {
                buff.detach();
            }
        }
        cheatsEnabled = false;
        inputBuffer.setLength(0);
        GLog.w(Messages.get(CheatCodeManager.class, "cheats_reset"));
    }
    
    // Vérifier si un cheat spécifique est actif
    public static boolean isCheatActive(CheatBuff.CheatType type) {
        Hero hero = Dungeon.hero;
        if (hero == null) return false;
        
        CheatBuff buff = hero.buff(CheatBuff.class);
        return buff != null && buff.type == type;
    }
    
    // Obtenir la liste des codes disponibles
    public static String[] getAvailableCheats() {
        return cheatCodes.keySet().toArray(new String[0]);
    }
}