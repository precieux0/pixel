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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.cheats.CheatCodeManager;
import com.shatteredpixel.shatteredpixeldungeon.multiplayer.MultiplayerManager;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.WelcomeScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PlatformSupport;

public class ShatteredPixelDungeon extends Game {

	//rankings from v1.2.3 and older use a different score formula, so this reference is kept
	public static final int v1_2_3 = 628;

	//savegames from versions older than v2.4.2 are no longer supported, and data from them is ignored
	public static final int v2_4_2 = 782;
	public static final int v2_5_4 = 802;

	public static final int v3_0_2 = 833;
	public static final int v3_1_1 = 850;
	public static final int v3_2_0 = 859;
	
	public ShatteredPixelDungeon( PlatformSupport platform ) {
		super( sceneClass == null ? WelcomeScene.class : sceneClass, platform );

		//pre-v2.5.3
		com.watabou.utils.Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfDetectMagic.class,
				"com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfDisarming" );

		//pre-v2.5.2
		com.watabou.utils.Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.items.bombs.FlashBangBomb.class,
				"com.shatteredpixel.shatteredpixeldungeon.items.bombs.ShockBomb" );
		com.watabou.utils.Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.items.bombs.SmokeBomb.class,
				"com.shatteredpixel.shatteredpixeldungeon.items.bombs.Flashbang" );

		//pre-v2.5.0
		com.watabou.utils.Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MobSpawner.class,
				"com.shatteredpixel.shatteredpixeldungeon.levels.Level$Respawner" );
		com.watabou.utils.Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AnkhInvulnerability" );
	}
	
	@Override
	public void create() {
		super.create();

		// Initialisation des systèmes personnalisés
		initializeCustomSystems();
		
		updateSystemUI();
		SPDAction.loadBindings();
		
		Music.INSTANCE.enable( SPDSettings.music() );
		Music.INSTANCE.volume( SPDSettings.musicVol()*SPDSettings.musicVol()/100f );
		Sample.INSTANCE.enable( SPDSettings.soundFx() );
		Sample.INSTANCE.volume( SPDSettings.SFXVol()*SPDSettings.SFXVol()/100f );

		Sample.INSTANCE.load( Assets.Sounds.all );
		
	}

	// NOUVELLE MÉTHODE : Initialisation des systèmes personnalisés
	private void initializeCustomSystems() {
		// Initialisation du système de triches
		CheatCodeManager.cheatsEnabled = false;
		GLog.i("Système de triches initialisé - Appuyez sur F1 pour activer");
		
		// Initialisation du système multijoueur
		MultiplayerManager.sessionActive = false;
		MultiplayerManager.currentMode = MultiplayerManager.GameMode.SOLO;
		GLog.i("Système multijoueur initialisé");
		
		// Chargement des configurations personnalisées
		loadCustomConfigurations();
	}
	
	// NOUVELLE MÉTHODE : Chargement des configurations personnalisées
	private void loadCustomConfigurations() {
		try {
			// Charger les paramètres des armes personnalisées
			CustomWeaponsConfig.load();
			
			// Charger les paramètres multijoueur
			MultiplayerConfig.load();
			
			GLog.i("Configurations personnalisées chargées avec succès");
		} catch (Exception e) {
			GLog.w("Erreur lors du chargement des configurations personnalisées");
		}
	}

	@Override
	public void update() {
		super.update();
		
		// Mise à jour des systèmes personnalisés
		updateCustomSystems();
	}
	
	// NOUVELLE MÉTHODE : Mise à jour des systèmes personnalisés
	private void updateCustomSystems() {
		// Mise à jour du système multijoueur si une session est active
		if (MultiplayerManager.sessionActive) {
			MultiplayerManager.updateLocalPlayer();
			
			// Synchronisation toutes les 60 frames (~1 seconde)
			if (Game.timeTotal % 60 == 0) {
				MultiplayerManager.syncAllPlayers();
			}
		}
		
		// Vérification des conditions de victoire multijoueur
		if (MultiplayerManager.sessionActive && Game.timeTotal % 120 == 0) {
			MultiplayerManager.checkWinConditions();
		}
	}

	// NOUVELLE MÉTHODE : Gestion avancée des entrées clavier
	@Override
	public boolean onKeyDown(com.badlogic.gdx.Input.Keys key) {
		int keyCode = key;
		
		// Activation/Désactivation des triches avec F1
		if (keyCode == com.badlogic.gdx.Input.Keys.F1) {
			CheatCodeManager.cheatsEnabled = !CheatCodeManager.cheatsEnabled;
			if (CheatCodeManager.cheatsEnabled) {
				GLog.p("§ TRICHES ACTIVÉES §");
				GLog.i("Codes disponibles: IDDQD, GOLD, HEAL, LVLUP, MAP, KILLALL");
				GLog.i("FLY, NOCLIP, SPEED, INFINITE, TELEPORT, XRAY, SUPER");
			} else {
				GLog.w("Triches désactivées");
				CheatCodeManager.resetCheats();
			}
			return true;
		}
		
		// Réinitialisation des triches avec F2
		if (keyCode == com.badlogic.gdx.Input.Keys.F2) {
			CheatCodeManager.resetCheats();
			GLog.w("Triches réinitialisées");
			return true;
		}
		
		// Menu multijoueur rapide avec F3
		if (keyCode == com.badlogic.gdx.Input.Keys.F3) {
			if (MultiplayerManager.sessionActive) {
				GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndSessionInfo());
			} else {
				GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndMultiplayer());
			}
			return true;
		}
		
		// Informations de débogage avec F4
		if (keyCode == com.badlogic.gdx.Input.Keys.F4) {
			showDebugInfo();
			return true;
		}
		
		// Traitement des codes de triche
		if (CheatCodeManager.cheatsEnabled) {
			// Convertir le code de touche en caractère
			char character = keyCodeToChar(keyCode);
			if (character != 0) {
				CheatCodeManager.processKeyInput(character);
				return true;
			}
		}
		
		return super.onKeyDown(key);
	}
	
	// NOUVELLE MÉTHODE : Conversion des codes de touches en caractères
	private char keyCodeToChar(int keyCode) {
		// Conversion des touches alphabétiques
		if (keyCode >= com.badlogic.gdx.Input.Keys.A && keyCode <= com.badlogic.gdx.Input.Keys.Z) {
			return (char) ('A' + (keyCode - com.badlogic.gdx.Input.Keys.A));
		}
		
		// Conversion des touches numériques
		if (keyCode >= com.badlogic.gdx.Input.Keys.NUM_0 && keyCode <= com.badlogic.gdx.Input.Keys.NUM_9) {
			return (char) ('0' + (keyCode - com.badlogic.gdx.Input.Keys.NUM_0));
		}
		
		// Conversion du pavé numérique
		if (keyCode >= com.badlogic.gdx.Input.Keys.NUMPAD_0 && keyCode <= com.badlogic.gdx.Input.Keys.NUMPAD_9) {
			return (char) ('0' + (keyCode - com.badlogic.gdx.Input.Keys.NUMPAD_0));
		}
		
		return 0;
	}
	
	// NOUVELLE MÉTHODE : Affichage des informations de débogage
	private void showDebugInfo() {
		GLog.newLine();
		GLog.p("=== INFORMATIONS DE DÉBOGAGE ===");
		GLog.i("Version: Shattered Pixel Dungeon Modifié");
		GLog.i("Multijoueur: " + (MultiplayerManager.sessionActive ? "ACTIF" : "INACTIF"));
		GLog.i("Triches: " + (CheatCodeManager.cheatsEnabled ? "ACTIVÉES" : "DÉSACTIVÉES"));
		
		if (MultiplayerManager.sessionActive) {
			GLog.i("Mode: " + MultiplayerManager.currentMode.getDisplayName());
			GLog.i("Joueurs: " + MultiplayerManager.getConnectedPlayerCount() + "/" + MultiplayerManager.maxPlayers);
			GLog.i("Code Session: " + MultiplayerManager.sessionCode);
		}
		
		GLog.i("Mémoire: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + "MB");
		GLog.p("================================");
	}

	@Override
	public void finish() {
		// Sauvegarde des systèmes personnalisés avant fermeture
		saveCustomSystems();
		
		if (!DeviceCompat.isiOS()) {
			super.finish();
		} else {
			//can't exit on iOS (Apple guidelines), so just go to title screen
			switchScene(TitleScene.class);
		}
	}
	
	// NOUVELLE MÉTHODE : Sauvegarde des systèmes personnalisés
	private void saveCustomSystems() {
		try {
			// Sauvegarder l'état multijoueur
			if (MultiplayerManager.sessionActive) {
				MultiplayerManager.updateLocalPlayer();
			}
			
			// Sauvegarder les configurations
			CustomWeaponsConfig.save();
			MultiplayerConfig.save();
			
			GLog.i("Systèmes personnalisés sauvegardés");
		} catch (Exception e) {
			GLog.w("Erreur lors de la sauvegarde des systèmes personnalisés");
		}
	}

	public static void switchNoFade(Class<? extends PixelScene> c){
		switchNoFade(c, null);
	}

	public static void switchNoFade(Class<? extends PixelScene> c, SceneChangeCallback callback) {
		PixelScene.noFade = true;
		switchScene( c, callback );
	}
	
	public static void seamlessResetScene(SceneChangeCallback callback) {
		if (scene() instanceof PixelScene){
			((PixelScene) scene()).saveWindows();
			switchNoFade((Class<? extends PixelScene>) sceneClass, callback );
		} else {
			resetScene();
		}
	}
	
	public static void seamlessResetScene(){
		seamlessResetScene(null);
	}
	
	@Override
	protected void switchScene() {
		super.switchScene();
		if (scene instanceof PixelScene){
			((PixelScene) scene).restoreWindows();
		}
	}
	
	@Override
	public void resize( int width, int height ) {
		if (width == 0 || height == 0){
			return;
		}

		if (scene instanceof PixelScene &&
				(height != Game.height || width != Game.width)) {
			PixelScene.noFade = true;
			((PixelScene) scene).saveWindows();
		}

		super.resize( width, height );

		updateDisplaySize();

	}
	
	@Override
	public void destroy(){
		// Nettoyage des systèmes personnalisés
		cleanupCustomSystems();
		
		super.destroy();
		GameScene.endActorThread();
	}
	
	// NOUVELLE MÉTHODE : Nettoyage des systèmes personnalisés
	private void cleanupCustomSystems() {
		try {
			// Quitter proprement les sessions multijoueur
			if (MultiplayerManager.sessionActive) {
				MultiplayerManager.leaveSession();
			}
			
			// Réinitialiser les triches
			CheatCodeManager.resetCheats();
			
			GLog.i("Systèmes personnalisés nettoyés");
		} catch (Exception e) {
			GLog.w("Erreur lors du nettoyage des systèmes personnalisés");
		}
	}
	
	public void updateDisplaySize(){
		platform.updateDisplaySize();
	}

	public static void updateSystemUI() {
		platform.updateSystemUI();
	}
	
	// CLASSES INTERNES POUR LA CONFIGURATION
	// (Ces classes peuvent être déplacées dans des fichiers séparés si nécessaire)
	
	private static class CustomWeaponsConfig {
		static void load() {
			// Charger la configuration des armes personnalisées
		}
		
		static void save() {
			// Sauvegarder la configuration des armes personnalisées
		}
	}
	
	private static class MultiplayerConfig {
		static void load() {
			// Charger la configuration multijoueur
		}
		
		static void save() {
			// Sauvegarder la configuration multijoueur
		}
	}
}