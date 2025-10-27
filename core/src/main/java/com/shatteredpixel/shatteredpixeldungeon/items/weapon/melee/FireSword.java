package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;

public class FireSword extends Weapon {
    { image = 16; tier = 4; }
    @Override public int max(int lvl) { return 4*(tier+1) + lvl*tier; }
}
