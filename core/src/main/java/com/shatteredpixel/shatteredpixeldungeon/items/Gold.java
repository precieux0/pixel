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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Gold extends Item {

	{
		image = ItemSpriteSheet.GOLD;
		stackable = true;
	}
	
	public Gold() {
		this( 1 );
	}
	
	public Gold( int value ) {
		this.quantity = value;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		return new ArrayList<>();
	}
	
	@Override
	public boolean doPickUp(Hero hero, int pos) {

		Catalog.setSeen(getClass());
		Statistics.itemTypesDiscovered.add(getClass());

		// CHANGEMENT : Multiplicateur d'or x2
		int bonusGold = quantity; // 100% de bonus
		Dungeon.gold += quantity + bonusGold;
		Statistics.goldCollected += quantity + bonusGold;
		Badges.validateGoldCollected();

		GameScene.pickUp( this, pos );
		// CHANGEMENT : Affiche le total avec bonus
		hero.sprite.showStatusWithIcon( CharSprite.NEUTRAL, Integer.toString(quantity + bonusGold), FloatingText.GOLD );
		hero.spendAndNext( pickupDelay() );
		
		Sample.INSTANCE.play( Assets.Sounds.GOLD, 1, 1, Random.Float( 0.9f, 1.1f ) );
		updateQuickslot();
		
		return true;
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public Item random() {
		// CHANGEMENT : Quantités d'or augmentées de 150%
		// Base : 75-150 au lieu de 30-60 au niveau 1
		// Niveau 5 : 175-350 au lieu de 70-140
		// Niveau 10 : 275-550 au lieu de 110-220
		// Niveau 15 : 375-750 au lieu de 150-300
		// Niveau 20 : 475-950 au lieu de 190-380
		// Niveau 25 : 575-1150 au lieu de 230-460
		int baseMin = 75 + Dungeon.depth * 15;  // 75, 90, 105, 120, 135, 150, etc.
		int baseMax = 150 + Dungeon.depth * 30; // 150, 180, 210, 240, 270, 300, etc.
		
		// CHANGEMENT : 25% de chance de trouver un tas d'or double
		if (Random.Int(4) == 0) { // 25% de chance
			baseMin *= 2;
			baseMax *= 2;
		}
		
		quantity = Random.IntRange( baseMin, baseMax );
		return this;
	}

	// NOUVELLE MÉTHODE : Or légendaire rare
	public static Gold createLegendaryGold() {
		Gold gold = new Gold();
		// CHANGEMENT : 5% de chance de trouver un tas d'or légendaire
		if (Random.Int(20) == 0) { // 5% de chance
			gold.quantity = Random.IntRange(500, 1000) + (Dungeon.depth * 100);
		}
		return gold;
	}

	// CHANGEMENT : Méthode pour générer de l'or avec des quantités améliorées
	public static Gold improvedRandom() {
		// 80% de chance d'or normal amélioré, 20% de chance d'or légendaire
		if (Random.Int(5) == 0) { // 20% de chance
			return createLegendaryGold();
		} else {
			Gold gold = new Gold();
			gold.random(); // Utilise la méthode random modifiée
			return gold;
		}
	}

}