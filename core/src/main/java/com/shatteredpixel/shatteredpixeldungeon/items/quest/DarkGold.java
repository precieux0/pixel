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

package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class DarkGold extends Item {
	
	{
		image = ItemSpriteSheet.ORE;
		
		stackable = true;
		unique = true;
		
		// NOUVEAU : Peut être utilisé depuis l'inventaire
		defaultAction = AC_USE;
	}
	
	// CHANGEMENT : Quantité trouvée augmentée
	@Override
	public int quantity() {
		return 3 + Dungeon.Int(8); // 3-10 au lieu de 1
	}
	
	// NOUVEAU : Actions disponibles
	private static final String AC_USE = "USE";
	private static final String AC_CRUSH = "CRUSH";
	
	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_USE);
		if (quantity() >= 5) {
			actions.add(AC_CRUSH);
		}
		return actions;
	}
	
	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		
		if (action.equals(AC_USE)) {
			useDarkGold(hero);
			
		} else if (action.equals(AC_CRUSH)) {
			crushDarkGold(hero);
		}
	}
	
	// NOUVELLE MÉTHODE : Utiliser l'or noir pour un bonus temporaire
	private void useDarkGold(Hero hero) {
		if (quantity() < 3) {
			GLog.w(Messages.get(this, "need_more"));
			return;
		}
		
		// Consomme 3 ors noirs
		quantity(quantity() - 3);
		
		// Effets bénéfiques
		hero.HP = Math.min(hero.HT, hero.HP + (hero.HT / 3)); // Soin de 33%
		hero.sprite.emitter().burst(ElmoParticle.FACTORY, 12);
		Sample.INSTANCE.play(Assets.Sounds.BURNING);
		
		// Bonus temporaire de dégâts
		Buff.affect(hero, DarkGoldBoost.class, 100f); // 100 tours de bonus
		
		GLog.p(Messages.get(this, "used"));
		
		if (quantity() <= 0) {
			detach(hero.belongings.backpack);
		}
	}
	
	// NOUVELLE MÉTHODE : Broyer l'or noir pour de l'or normal
	private void crushDarkGold(Hero hero) {
		if (quantity() < 5) {
			GLog.w(Messages.get(this, "need_more_crush"));
			return;
		}
		
		// Conversion en or normal
		int goldAmount = quantity() * 25; // 25 pièces d'or par or noir
		Gold gold = new Gold(goldAmount);
		
		if (gold.doPickUp(hero)) {
			GLog.i(Messages.get(this, "crushed", goldAmount));
		} else {
			Dungeon.level.drop(gold, hero.pos).sprite.drop();
		}
		
		// Consomme tout l'or noir
		detach(hero.belongings.backpack);
	}
	
	// NOUVEAU : Buff de bonus de dégâts
	public static class DarkGoldBoost extends Buff {
		
		{
			type = buffType.POSITIVE;
			announced = true;
		}
		
		@Override
		public int icon() {
			return 17; // Icône temporaire
		}
		
		@Override
		public float iconFadePercent() {
			return Math.max(0, (100 - visualcooldown()) / 100f);
		}
		
		@Override
		public String toString() {
			return Messages.get(this, "name");
		}
		
		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns(visualcooldown()));
		}
	}
	
	// CHANGEMENT : Valeur de vente augmentée
	@Override
	public int value() {
		return 50 * quantity(); // 50 pièces par or noir au lieu de 25
	}
	
	// CHANGEMENT : Description améliorée
	@Override
	public String info() {
		String info = super.info();
		info += "\n\n" + Messages.get(this, "desc_enhanced");
		if (quantity() >= 3) {
			info += "\n\n" + Messages.get(this, "hint_use");
		}
		if (quantity() >= 5) {
			info += "\n\n" + Messages.get(this, "hint_crush");
		}
		return info;
	}
	
	// CHANGEMENT : Nom plus cool
	@Override
	public String name() {
		return Messages.get(this, "name_enhanced");
	}
	
	// NOUVEAU : Sauvegarde/restauration
	private static final String QUANTITY = "quantity";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(QUANTITY, quantity());
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		quantity(bundle.getInt(QUANTITY));
	}
	
	// CHANGEMENT : Toujours identifié
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
}