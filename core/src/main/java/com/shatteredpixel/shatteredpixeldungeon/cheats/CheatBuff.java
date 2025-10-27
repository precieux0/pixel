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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class CheatBuff extends Buff {
    
    public enum CheatType {
        GOD_MODE,
        FLIGHT,
        NO_CLIP,
        SPEED_BOOST,
        INFINITE_MANA,
        XRAY_VISION,
        TELEPORT,
        TIME_STOP
    }
    
    public CheatType type;
    public float power = 1.0f;
    public int duration = 0; // 0 = permanent
    
    {
        type = buffType.POSITIVE;
        announced = true;
    }
    
    public CheatBuff(CheatType type) {
        this.type = type;
        initializeBuff();
    }
    
    public CheatBuff(CheatType type, float power) {
        this.type = type;
        this.power = power;
        initializeBuff();
    }
    
    public CheatBuff(CheatType type, float power, int duration) {
        this.type = type;
        this.power = power;
        this.duration = duration;
        initializeBuff();
    }
    
    private void initializeBuff() {
        switch (type) {
            case GOD_MODE:
                // Immunité aux dégâts
                break;
            case FLIGHT:
                // Vol et ignore les pièges
                break;
            case NO_CLIP:
                // Passe à travers les murs
                break;
            case SPEED_BOOST:
                // Vitesse augmentée
                break;
            case INFINITE_MANA:
                // Mana illimité pour les sorts
                break;
            case XRAY_VISION:
                // Voir à travers les murs
                break;
            case TELEPORT:
                // Téléportation libre
                break;
            case TIME_STOP:
                // Temps arrêté
                break;
        }
    }
    
    @Override
    public boolean attachTo(Char target) {
        if (super.attachTo(target)) {
            applyEffects(true);
            return true;
        }
        return false;
    }
    
    @Override
    public void detach() {
        applyEffects(false);
        super.detach();
    }
    
    private void applyEffects(boolean attach) {
        if (target == null) return;
        
        int multiplier = attach ? 1 : -1;
        
        switch (type) {
            case GOD_MODE:
                if (attach) {
                    target.invulnerable += 9999;
                } else {
                    target.invulnerable -= 9999;
                }
                break;
                
            case FLIGHT:
                target.flying = attach;
                break;
                
            case NO_CLIP:
                // Géré dans les collisions du niveau
                break;
                
            case SPEED_BOOST:
                // Géré dans speedMultiplier()
                break;
                
            case INFINITE_MANA:
                if (target instanceof Hero) {
                    Hero hero = (Hero) target;
                    // Réinitialiser les cooldowns des sorts
                }
                break;
                
            case XRAY_VISION:
                // Géré dans la vision du héros
                break;
                
            case TELEPORT:
                // Capacité spéciale de téléportation
                break;
                
            case TIME_STOP:
                // Géré dans le temps du jeu
                break;
        }
    }
    
    @Override
    public boolean act() {
        if (duration > 0) {
            duration--;
            if (duration <= 0) {
                detach();
                return true;
            }
        }
        spend(TICK);
        return true;
    }
    
    @Override
    public int icon() {
        switch (type) {
            case GOD_MODE: return BuffIndicator.IMMUNITY;
            case FLIGHT: return BuffIndicator.LEVITATION;
            case NO_CLIP: return 31; // Icône fantôme
            case SPEED_BOOST: return BuffIndicator.HASTE;
            case INFINITE_MANA: return BuffIndicator.RECHARGING;
            case XRAY_VISION: return BuffIndicator.MIND_VISION;
            case TELEPORT: return 32; // Icône téléportation
            case TIME_STOP: return BuffIndicator.TIME;
            default: return BuffIndicator.NONE;
        }
    }
    
    @Override
    public void tintIcon(Image icon) {
        switch (type) {
            case GOD_MODE: icon.hardlight(0xFFFF00); break; // Jaune
            case FLIGHT: icon.hardlight(0x00FFFF); break;   // Cyan
            case NO_CLIP: icon.hardlight(0x888888); break;  // Gris
            case SPEED_BOOST: icon.hardlight(0xFF4500); break; // Orange
            case INFINITE_MANA: icon.hardlight(0x4169E1); break; // Bleu royal
            case XRAY_VISION: icon.hardlight(0x32CD32); break; // Vert lime
            case TELEPORT: icon.hardlight(0xBA55D3); break; // Violet moyen
            case TIME_STOP: icon.hardlight(0xFFFFFF); break; // Blanc
        }
    }
    
    @Override
    public float iconFadePercent() {
        if (duration == 0) return 0; // Permanent
        
        float timeLeft = duration;
        float maxDuration = getInitialDuration();
        return (maxDuration - timeLeft) / maxDuration;
    }
    
    private float getInitialDuration() {
        switch (type) {
            case SPEED_BOOST: return 300f;
            case XRAY_VISION: return 200f;
            case INFINITE_MANA: return 150f;
            default: return 100f;
        }
    }
    
    @Override
    public String toString() {
        switch (type) {
            case GOD_MODE: return Messages.get(this, "god_mode");
            case FLIGHT: return Messages.get(this, "flight");
            case NO_CLIP: return Messages.get(this, "noclip");
            case SPEED_BOOST: return Messages.get(this, "speed");
            case INFINITE_MANA: return Messages.get(this, "infinite_mana");
            case XRAY_VISION: return Messages.get(this, "xray");
            case TELEPORT: return Messages.get(this, "teleport");
            case TIME_STOP: return Messages.get(this, "time_stop");
            default: return Messages.get(this, "unknown");
        }
    }
    
    @Override
    public String desc() {
        String desc = Messages.get(this, "desc_" + type.name().toLowerCase());
        if (duration > 0) {
            desc += "\n\n" + Messages.get(this, "duration", dispTurns(duration));
        } else {
            desc += "\n\n" + Messages.get(this, "permanent");
        }
        return desc;
    }
    
    // Effets spéciaux selon le type de triche
    @Override
    public float speedMultiplier() {
        if (type == CheatType.SPEED_BOOST) {
            return 1.0f + power; // power = 2.0 pour 3x vitesse
        }
        return super.speedMultiplier();
    }
    
    @Override
    public int drRoll() {
        if (type == CheatType.GOD_MODE) {
            return 9999; // Énorme bonus de défense
        }
        return super.drRoll();
    }
    
    // Gestion des dégâts
    @Override
    public int onDamage(int damage, Object src) {
        if (type == CheatType.GOD_MODE) {
            return 0; // Aucun dégât
        }
        return super.onDamage(damage, src);
    }
    
    // Vision spéciale
    public boolean hasXRayVision() {
        return type == CheatType.XRAY_VISION;
    }
    
    public boolean canNoClip() {
        return type == CheatType.NO_CLIP;
    }
    
    // Sauvegarde et restauration
    private static final String TYPE = "type";
    private static final String POWER = "power";
    private static final String DURATION = "duration";
    
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(TYPE, type);
        bundle.put(POWER, power);
        bundle.put(DURATION, duration);
    }
    
    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        type = bundle.getEnum(TYPE, CheatType.class);
        power = bundle.getFloat(POWER);
        duration = bundle.getInt(DURATION);
        initializeBuff();
    }
    
    // Méthodes statiques pour créer facilement les buffs
    public static CheatBuff godMode() {
        return new CheatBuff(CheatType.GOD_MODE, 1.0f, 0);
    }
    
    public static CheatBuff flight() {
        return new CheatBuff(CheatType.FLIGHT, 1.0f, 0);
    }
    
    public static CheatBuff noClip() {
        return new CheatBuff(CheatType.NO_CLIP, 1.0f, 0);
    }
    
    public static CheatBuff speedBoost(float multiplier) {
        return new CheatBuff(CheatType.SPEED_BOOST, multiplier - 1.0f, 300);
    }
    
    public static CheatBuff infiniteMana() {
        return new CheatBuff(CheatType.INFINITE_MANA, 1.0f, 150);
    }
    
    public static CheatBuff xRayVision() {
        return new CheatBuff(CheatType.XRAY_VISION, 1.0f, 200);
    }
    
    public static CheatBuff timeStop() {
        return new CheatBuff(CheatType.TIME_STOP, 1.0f, 50);
    }
}