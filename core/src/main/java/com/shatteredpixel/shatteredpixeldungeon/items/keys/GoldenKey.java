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

package com.shatteredpixel.shatteredpixeldungeon.items.keys;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class GoldenKey extends Key {
	
	{
		image = ItemSpriteSheet.GOLDEN_KEY;
		
		stackable = true;
		
		// NOUVEAU : Peut être utilisé depuis l'inventaire
		defaultAction = AC_USE;
	}
	
	// CHANGEMENT : Plus de durabilité
	public GoldenKey() {
		this( 0 );
	}
	
	public GoldenKey( int depth ) {
		super();
		this.depth = depth;
		
		// CHANGEMENT : Clés dorées réutilisables 3 fois
		uses = 3;
	}
	
	// NOUVEAU : Actions disponibles
	private static final String AC_USE = "USE";
	private static final String AC_BLESS = "BLESS";
	private static final String AC_FUSE = "FUSE";
	
	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_USE);
		if (quantity() >= 2) {
			actions.add(AC_BLESS);
		}
		if (quantity() >= 3) {
			actions.add(AC_FUSE);
		}
		return actions;
	}
	
	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		
		if (action.equals(AC_USE)) {
			useGoldenPower(hero);
			
		} else if (action.equals(AC_BLESS)) {
			blessKey(hero);
			
		} else if (action.equals(AC_FUSE)) {
			fuseKeys(hero);
		}
	}
	
	// NOUVELLE MÉTHODE : Utiliser le pouvoir de la clé dorée
	private void useGoldenPower(Hero hero) {
		if (uses <= 0) {
			GLog.w(Messages.get(this, "no_charges"));
			return;
		}
		
		// Consomme une charge
		uses--;
		
		// Effets bénéfiques
		hero.HP = Math.min(hero.HT, hero.HP + 10); // Soin fixe
		Buff.affect(hero, GoldenAura.class, 50f); // Aura dorée temporaire
		
		// Effet visuel
		hero.sprite.centerEmitter().burst(SunlightParticle.FACTORY, 12);
		Sample.INSTANCE.play(Assets.Sounds.EVOKE);
		
		GLog.p(Messages.get(this, "power_used"));
		
		updateQuickslot();
		
		if (uses <= 0) {
			GLog.w(Messages.get(this, "key_weakened"));
		}
	}
	
	// NOUVELLE MÉTHODE : Bénir une clé pour la renforcer
	private void blessKey(Hero hero) {
		// Consomme 2 clés
		quantity(quantity() - 1); // On en garde une pour la bénir
		
		// Renforce la clé restante
		uses += 2; // +2 charges
		Buff.affect(this, BlessedKey.class); // Marque comme bénie
		
		// Effet visuel
		hero.sprite.centerEmitter().burst(SunlightParticle.FACTORY, 16);
		Sample.INSTANCE.play(Assets.Sounds.LEVELUP);
		
		GLog.p(Messages.get(this, "key_blessed"));
		updateQuickslot();
	}
	
	// NOUVELLE MÉTHODE : Fusionner des clés pour créer une clé légendaire
	private void fuseKeys(Hero hero) {
		// Consomme 3 clés
		quantity(quantity() - 2); // On garde une clé pour la fusion
		
		// Transforme en clé légendaire
		this.image = ItemSpriteSheet.MASTER_KEY; // Nouveau sprite
		uses = 5; // 5 charges
		Buff.affect(this, LegendaryKey.class); // Marque comme légendaire
		
		// Effet visuel spectaculaire
		hero.sprite.centerEmitter().burst(SunlightParticle.FACTORY, 24);
		Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
		
		GLog.h(Messages.get(this, "key_legendary"));
		updateQuickslot();
	}
	
	// CHANGEMENT : La clé peut ouvrir plus de coffres
	@Override
	public boolean useKey() {
		// Les clés légendaires ne se consomment pas
		if (buff(LegendaryKey.class) != null) {
			return true;
		}
		
		// Les clés normales utilisent une charge
		if (uses > 0) {
			uses--;
			updateQuickslot();
			
			if (uses <= 0) {
				GLog.w(Messages.get(this, "key_weakened"));
				// La clé n'est pas détruite, mais doit être réparée
			}
			return true;
		}
		
		return false;
	}
	
	// NOUVEAU : Aura dorée temporaire
	public static class GoldenAura extends Buff {
		
		{
			type = buffType.POSITIVE;
			announced = true;
		}
		
		@Override
		public int icon() {
			return 23; // Icône de lumière
		}
		
		@Override
		public float iconFadePercent() {
			return Math.max(0, (50 - visualcooldown()) / 50f);
		}
		
		@Override
		public String toString() {
			return Messages.get(this, "name");
		}
		
		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns(visualcooldown()));
		}
		
		// Bonus de chance pendant l'aura
		@Override
		public boolean act() {
			// 10% de chance de trouver un objet bonus par tour
			if (Random.Int(10) == 0) {
				// Effet de chance
			}
			return super.act();
		}
	}
	
	// NOUVEAU : Marqueur pour clé bénie
	public static class BlessedKey extends Buff {
		// Juste un marqueur
	}
	
	// NOUVEAU : Marqueur pour clé légendaire
	public static class LegendaryKey extends Buff {
		// Juste un marqueur
	}
	
	// CHANGEMENT : Description améliorée
	@Override
	public String info() {
		String info = super.info();
		info += "\n\n" + Messages.get(this, "desc_enhanced");
		info += "\n\n" + Messages.get(this, "charges", uses);
		
		if (buff(BlessedKey.class) != null) {
			info += "\n\n" + Messages.get(this, "blessed_desc");
		}
		if (buff(LegendaryKey.class) != null) {
			info += "\n\n" + Messages.get(this, "legendary_desc");
		}
		
		if (uses > 0) {
			info += "\n\n" + Messages.get(this, "hint_use");
		}
		if (quantity() >= 2) {
			info += "\n\n" + Messages.get(this, "hint_bless");
		}
		if (quantity() >= 3) {
			info += "\n\n" + Messages.get(this, "hint_fuse");
		}
		
		return info;
	}
	
	// CHANGEMENT : Nom selon le type
	@Override
	public String name() {
		if (buff(LegendaryKey.class) != null) {
			return Messages.get(this, "name_legendary");
		} else if (buff(BlessedKey.class) != null) {
			return Messages.get(this, "name_blessed");
		} else {
			return super.name();
		}
	}
	
	// CHANGEMENT : Valeur augmentée
	@Override
	public int value() {
		int value = 100; // Valeur de base augmentée
		
		if (buff(BlessedKey.class) != null) {
			value *= 2;
		}
		if (buff(LegendaryKey.class) != null) {
			value *= 5;
		}
		
		return value * quantity();
	}
	
	// NOUVEAU : Sauvegarde des états spéciaux
	private static final String USES = "uses";
	private static final String BLESSED = "blessed";
	private static final String LEGENDARY = "legendary";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(USES, uses);
		bundle.put(BLESSED, buff(BlessedKey.class) != null);
		bundle.put(LEGENDARY, buff(LegendaryKey.class) != null);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		uses = bundle.getInt(USES);
		
		if (bundle.getBoolean(BLESSED)) {
			Buff.affect(this, BlessedKey.class);
		}
		if (bundle.getBoolean(LEGENDARY)) {
			Buff.affect(this, LegendaryKey.class);
		}
	}
	
	// CHANGEMENT : Toujours identifiée
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	// CHANGEMENT : Peut être améliorée maintenant
	@Override
	public boolean isUpgradable() {
		return true;
	}
}