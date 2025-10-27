package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;

public class LegendarySword extends Weapon {
    { image = 15; tier = 5; }
    @Override public int max(int lvl) { return 5*(tier+1) + lvl*(tier+1); }
}
