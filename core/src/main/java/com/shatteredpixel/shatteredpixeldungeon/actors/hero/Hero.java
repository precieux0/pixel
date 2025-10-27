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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

// ... (les imports restent les mêmes)

public class Hero extends Char {

	{
		actPriority = HERO_PRIO;
		
		alignment = Alignment.ALLY;
	}
	
	public static final int MAX_LEVEL = 35; // CHANGEMENT : Niveau max augmenté à 35
	
	public static final int STARTING_STR = 12; // CHANGEMENT : Force de départ augmentée
	
	private static final float TIME_TO_REST		    = 0.8f; // CHANGEMENT : Repos plus rapide
	private static final float TIME_TO_SEARCH	    = 1.5f;  // CHANGEMENT : Recherche plus rapide
	private static final float HUNGER_FOR_SEARCH	= 4f;    // CHANGEMENT : Moins de faim pour recherche

	// ... (le reste des déclarations reste le même)

	public Hero() {
		super();

		// CHANGEMENTS : Statistiques de base améliorées
		HP = HT = 30;        // +10 PV de base
		STR = STARTING_STR;  // Force augmentée
		
		belongings = new Belongings( this );
		
		visibleEnemies = new ArrayList<>();
	}
	
	public void updateHT( boolean boostHP ){
		int curHT = HT;
		
		// CHANGEMENT : PV par niveau augmentés (8 au lieu de 5)
		HT = 30 + 8*(lvl-1) + HTBoost; // Base augmentée à 30, +8 par niveau
		float multiplier = RingOfMight.HTMultiplier(this);
		HT = Math.round(multiplier * HT);
		
		if (buff(ElixirOfMight.HTBoost.class) != null){
			HT += buff(ElixirOfMight.HTBoost.class).boost();
		}
		
		if (boostHP){
			HP += Math.max(HT - curHT, 0);
		}
		HP = Math.min(HP, HT);
	}

	public int STR() {
		int strBonus = 0;

		strBonus += RingOfMight.strengthBonus( this );
		
		AdrenalineSurge buff = buff(AdrenalineSurge.class);
		if (buff != null){
			strBonus += buff.boost();
		}

		if (hasTalent(Talent.STRONGMAN)){
			strBonus += (int)Math.floor(STR * (0.05f + 0.08f*pointsInTalent(Talent.STRONGMAN))); // CHANGEMENT : Bonus augmenté
		}

		return STR + strBonus;
	}

	// ... (les méthodes de sauvegarde/restauration restent les mêmes)

	@Override
	public int attackSkill( Char target ) {
		KindOfWeapon wep = belongings.attackingWeapon();
		
		float accuracy = 1.2f; // CHANGEMENT : Précision de base augmentée de 20%
		accuracy *= RingOfAccuracy.accuracyMultiplier( this );
		
		// ... (le reste de la méthode reste le même)
		
		if (!RingOfForce.fightingUnarmed(this)) {
			return Math.max(1, Math.round(attackSkill * accuracy * wep.accuracyFactor( this, target )));
		} else {
			return Math.max(1, Math.round(attackSkill * accuracy));
		}
	}
	
	@Override
	public int defenseSkill( Char enemy ) {

		if (buff(Combo.ParryTracker.class) != null){
			if (canAttack(enemy) && !isCharmedBy(enemy)){
				Buff.affect(this, Combo.RiposteTracker.class).enemy = enemy;
			}
			return INFINITE_EVASION;
		}

		if (buff(RoundShield.GuardTracker.class) != null){
			return INFINITE_EVASION;
		}
		
		float evasion = defenseSkill * 1.2f; // CHANGEMENT : Esquive de base augmentée de 20%
		
		evasion *= RingOfEvasion.evasionMultiplier( this );

		// ... (le reste de la méthode reste le même)
	}

	@Override
	public int damageRoll() {
		KindOfWeapon wep = belongings.attackingWeapon();
		int dmg;

		if (!RingOfForce.fightingUnarmed(this)) {
			dmg = wep.damageRoll( this );

			if (!(wep instanceof MissileWeapon)) dmg += RingOfForce.armedDamageBonus(this);
		} else {
			dmg = RingOfForce.damageRoll(this);
			if (RingOfForce.unarmedGetsWeaponAugment(this)){
				dmg = ((Weapon)belongings.attackingWeapon()).augment.damageFactor(dmg);
			}
		}

		// CHANGEMENT : Bonus de dégâts global de 15%
		dmg = (int)(dmg * 1.15f);

		PhysicalEmpower emp = buff(PhysicalEmpower.class);
		if (emp != null){
			dmg += emp.dmgBoost;
			emp.left--;
			if (emp.left <= 0) {
				emp.detach();
			}
			Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG, 0.75f, 1.2f);
		}

		if (heroClass != HeroClass.DUELIST
				&& hasTalent(Talent.WEAPON_RECHARGING)
				&& (buff(Recharging.class) != null || buff(ArtifactRecharge.class) != null)){
			dmg = Math.round(dmg * 1.05f + (.05f*pointsInTalent(Talent.WEAPON_RECHARGING))); // CHANGEMENT : Bonus augmenté
		}

		if (dmg < 0) dmg = 0;
		return dmg;
	}

	@Override
	public float speed() {

		float speed = super.speed() * 1.1f; // CHANGEMENT : Vitesse augmentée de 10%

		speed *= RingOfHaste.speedMultiplier(this);
		
		// ... (le reste de la méthode reste le même)
	}

	// ... (les autres méthodes restent les mêmes jusqu'à earnExp)

	public void earnExp( int exp, Class source ) {

		// CHANGEMENT : 25% d'XP en plus
		int bonusExp = (int)(exp * 0.25f);
		this.exp += exp + bonusExp;

		float percent = (exp + bonusExp)/(float)maxExp();

		// ... (le reste de la méthode reste le même)
		
		boolean levelUp = false;
		while (this.exp >= maxExp()) {
			this.exp -= maxExp();

			if (buff(Talent.WandPreservationCounter.class) != null
				&& pointsInTalent(Talent.WAND_PRESERVATION) == 2){
				buff(Talent.WandPreservationCounter.class).detach();
			}

			if (lvl < MAX_LEVEL) {
				lvl++;
				levelUp = true;
				
				if (buff(ElixirOfMight.HTBoost.class) != null){
					buff(ElixirOfMight.HTBoost.class).onLevelUp();
				}
				
				updateHT( true );
				// CHANGEMENT : Compétences de combat qui augmentent plus vite
				attackSkill += 2; // +2 au lieu de +1
				defenseSkill += 2; // +2 au lieu de +1

			} else {
				Buff.prolong(this, Bless.class, Bless.DURATION);
				this.exp = 0;

				GLog.newLine();
				GLog.p( Messages.get(this, "level_cap"));
				Sample.INSTANCE.play( Assets.Sounds.LEVELUP );
			}
			
		}
		
		if (levelUp) {
			
			if (sprite != null) {
				GLog.newLine();
				GLog.p( Messages.get(this, "new_level") );
				sprite.showStatus( CharSprite.POSITIVE, Messages.get(Hero.class, "level_up") );
				Sample.INSTANCE.play( Assets.Sounds.LEVELUP );
				if (lvl < Talent.tierLevelThresholds[Talent.MAX_TALENT_TIERS+1]){
					GLog.newLine();
					GLog.p( Messages.get(this, "new_talent") );
					StatusPane.talentBlink = 10f;
					WndHero.lastIdx = 1;
				}
			}
			
			Item.updateQuickslot();
			
			Badges.validateLevelReached();
		}
	}
	
	public int maxExp() {
		return maxExp( lvl );
	}
	
	public static int maxExp( int lvl ){
		// CHANGEMENT : XP requis par niveau réduit de 20%
		return (int)((5 + lvl * 5) * 0.8f);
	}

	// ... (le reste des méthodes reste le même)

	// NOUVELLE MÉTHODE : Régénération passive
	@Override
	public boolean act() {
		
		// Régénération passive : 1% des PV max par tour
		if (HP < HT && lvl >= 3) {
			int heal = Math.max(1, HT / 100);
			HP = Math.min(HT, HP + heal);
		}
		
		// ... (le reste de la méthode act() reste le même)
	}

	// ... (le reste de la classe reste le même)
}