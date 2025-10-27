package com.shatteredpixel.shatteredpixeldungeon.items.weapons.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;

public class EpéeDeFeu extends Weapon {
    
    {
        image = 16; // Sprite différent
        tier = 4;
    }
    
    @Override
    public int proc(Char attacker, Char defender, int damage) {
        // Effet spécial : dégâts de feu supplémentaires
        defender.damage(Random.Int(tier * 2), this);
        
        // Effet visuel de feu
        defender.sprite.emitter().burst(FlameParticle.FACTORY, 5);
        
        return super.proc(attacker, defender, damage);
    }
    
    @Override
    public String desc() {
        return "Cette épée brûlante inflige des dégâts de feu supplémentaires " +
               "à chaque coup porté à l'ennemi.";
    }
}