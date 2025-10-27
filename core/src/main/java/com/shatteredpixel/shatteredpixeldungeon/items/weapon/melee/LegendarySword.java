package com.shatteredpixel.shatteredpixeldungeon.items.weapons.melee;

import com.shatteredpixel.shatteredpixeldungeon.items.weapons.Weapon;

public class EpéeLégendaire extends Weapon {
    
    {
        image = 15; // Changer ce numéro selon les sprites disponibles
        tier = 5;   // Niveau de l'arme (1-5)
        
        // Statistiques de base
        DLY = 1f;   // Délai entre les attaques
    }
    
    @Override
    public int min(int lvl) {
        return tier +  // Dégâts minimum
                lvl;   // Bonus par niveau
    }
    
    @Override
    public int max(int lvl) {
        return 5 * tier +  // Dégâts maximum
                3 * lvl;   // Bonus par niveau
    }
    
    @Override
    public String desc() {
        return "Une épée légendaire forgée dans des métaux anciens. " +
               "Elle brille d'une lueur mystique et inflige des dégâts considérables.";
    }
}