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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

// AJOUTER CES IMPORTS :
import com.shatteredpixel.shatteredpixeldungeon.cheats.CheatCodeManager;
import com.shatteredpixel.shatteredpixeldungeon.multiplayer.MultiplayerManager;
import com.shatteredpixel.shatteredpixeldungeon.multiplayer.PlayerData;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMultiplayer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSessionInfo;

// ... (les autres imports restent les mêmes)

public class GameScene extends PixelScene {

	static GameScene scene;

	// ... (les autres déclarations existantes)

	// NOUVEAUX : Éléments d'interface multijoueur
	private Group multiplayerGroup;
	private RenderedTextBlock multiplayerStatus;
	private RedButton sessionInfoBtn;
	private ArrayList<MobSprite> multiplayerPlayerSprites = new ArrayList<>();

	// ... (le reste des déclarations existantes)

	{
		inGameScene = true;
	}
	
	@Override
	public void create() {
		
		if (Dungeon.hero == null || Dungeon.level == null){
			ShatteredPixelDungeon.switchNoFade(TitleScene.class);
			return;
		}

		Dungeon.level.playLevelMusic();

		SPDSettings.lastClass(Dungeon.hero.heroClass.ordinal());
		
		super.create();
		Camera.main.zoom( GameMath.gate(minZoom, defaultZoom + SPDSettings.zoom(), maxZoom));
		Camera.main.edgeScroll.set(1);

		switch (SPDSettings.cameraFollow()) {
			case 4: default:    Camera.main.setFollowDeadzone(0);      break;
			case 3:             Camera.main.setFollowDeadzone(0.2f);   break;
			case 2:             Camera.main.setFollowDeadzone(0.5f);   break;
			case 1:             Camera.main.setFollowDeadzone(0.9f);   break;
		}

		// ... (le code existant reste le même jusqu'à la création de l'UI)

		// NOUVEAU : Initialiser l'interface multijoueur
		createMultiplayerUI();

		// ... (le reste du code create() existant)

	}

	// NOUVELLE MÉTHODE : Création de l'interface multijoueur
	private void createMultiplayerUI() {
		multiplayerGroup = new Group();
		uiCamera.add(multiplayerGroup);
		
		// Indicateur de statut multijoueur
		multiplayerStatus = PixelScene.renderTextBlock("", 6);
		multiplayerStatus.setPos(5, 5);
		multiplayerGroup.add(multiplayerStatus);
		
		// Bouton d'info session
		sessionInfoBtn = new RedButton("Session") {
			@Override
			protected void onClick() {
				showSessionInfo();
			}
			
			@Override
			protected boolean onLongClick() {
				// Menu rapide multijoueur
				ShatteredPixelDungeon.scene().addToFront(new WndMultiplayer());
				return true;
			}
		};
		sessionInfoBtn.setSize(50, 16);
		sessionInfoBtn.setPos(width - 55, 5);
		multiplayerGroup.add(sessionInfoBtn);
		
		updateMultiplayerDisplay();
	}

	// NOUVELLE MÉTHODE : Mise à jour de l'affichage multijoueur
	public static void updateMultiplayerDisplay() {
		if (scene != null) {
			scene.updateMultiplayerUI();
		}
	}

	// NOUVELLE MÉTHODE : Mise à jour de l'UI multijoueur
	private void updateMultiplayerUI() {
		if (!MultiplayerManager.sessionActive) {
			multiplayerGroup.visible = false;
			return;
		}
		
		multiplayerGroup.visible = true;
		
		// Mettre à jour le statut
		String status = "Multijoueur: " + 
					   MultiplayerManager.currentMode.getDisplayName() + " | " +
					   MultiplayerManager.getConnectedPlayerCount() + "/" + 
					   MultiplayerManager.maxPlayers + " joueurs";
		
		multiplayerStatus.text(status);
		multiplayerStatus.hardlight(0x00FF00);
		
		// Afficher les autres joueurs sur la carte
		showOtherPlayersOnMap();
	}

	// NOUVELLE MÉTHODE : Affichage des autres joueurs sur la carte
	private void showOtherPlayersOnMap() {
		// Nettoyer les anciens sprites de joueurs
		for (MobSprite sprite : multiplayerPlayerSprites) {
			sprite.killAndErase();
		}
		multiplayerPlayerSprites.clear();
		
		// Afficher les autres joueurs
		for (PlayerData player : MultiplayerManager.getOtherPlayers()) {
			if (player.isConnected() && player.isAlive() && player.getPosition() >= 0) {
				MobSprite playerSprite = createPlayerSprite(player);
				if (playerSprite != null) {
					multiplayerGroup.add(playerSprite);
					multiplayerPlayerSprites.add(playerSprite);
				}
			}
		}
	}

	// NOUVELLE MÉTHODE : Création d'un sprite pour un autre joueur
	private MobSprite createPlayerSprite(PlayerData player) {
		MobSprite sprite = new MobSprite() {
			@Override
			public void update() {
				super.update();
				// Mettre à jour la position
				if (player.getPosition() >= 0 && player.getPosition() < Dungeon.level.length()) {
					PointF point = DungeonTilemap.tileToWorld(player.getPosition());
					point.x += (DungeonTilemap.SIZE - width()) / 2;
					point.y += (DungeonTilemap.SIZE - height()) / 2 - 4;
					point.x -= camera.scroll.x;
					point.y -= camera.scroll.y;
					setPos(point.x, point.y);
					
					// Mettre à jour la visibilité selon le FOV
					visible = Dungeon.level.heroFOV[player.getPosition()];
				}
			}
		};
		
		// Utiliser un sprite basique pour les autres joueurs
		// En production, on utiliserait le spriteType du joueur
		sprite.view(0, null); // Sprite de base
		sprite.idle();
		
		// Colorer le sprite selon le joueur
		sprite.tint(player.getColorScheme(), 0.8f);
		
		return sprite;
	}

	// NOUVELLE MÉTHODE : Affichage des infos de session
	private void showSessionInfo() {
		if (MultiplayerManager.sessionActive) {
			parent.add(new WndSessionInfo());
		}
	}

	// MODIFICATION : Gestion des entrées pour les triches et multijoueur
	@Override
	public boolean onKeyDown(com.badlogic.gdx.Input.Keys key) {
		int keyCode = key;
		
		// Traitement des triches
		if (CheatCodeManager.cheatsEnabled) {
			char character = keyCodeToChar(keyCode);
			if (character != 0) {
				CheatCodeManager.processKeyInput(character);
				return true;
			}
		}
		
		// Menu multijoueur rapide avec M
		if (keyCode == com.badlogic.gdx.Input.Keys.M) {
			GameScene.show(new WndMultiplayer());
			return true;
		}
		
		// Informations de session avec N
		if (keyCode == com.badlogic.gdx.Input.Keys.N && MultiplayerManager.sessionActive) {
			showSessionInfo();
			return true;
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

	// MODIFICATION : Sauvegarde des données multijoueur
	@Override
	public void destroy() {
		// Sauvegarder l'état multijoueur si une session est active
		if (MultiplayerManager.sessionActive) {
			MultiplayerManager.updateLocalPlayer();
		}
		
		// Nettoyer les sprites multijoueur
		for (MobSprite sprite : multiplayerPlayerSprites) {
			sprite.killAndErase();
		}
		multiplayerPlayerSprites.clear();
		
		super.destroy();
	}

	// MODIFICATION : Mise à jour avec gestion multijoueur
	@Override
	public synchronized void update() {
		lastOffset = null;

		if (updateItemDisplays){
			updateItemDisplays = false;
			QuickSlotButton.refresh();
			InventoryPane.refresh();
			if (ActionIndicator.action instanceof MeleeWeapon.Charger) {
				//Champion weapon swap uses items, needs refreshing whenever item displays are updated
				ActionIndicator.refresh();
			}
		}

		// NOUVEAU : Mise à jour de l'affichage multijoueur
		if (MultiplayerManager.sessionActive) {
			updateMultiplayerUI();
		}

		if (Dungeon.hero == null || scene == null) {
			return;
		}

		super.update();

		// ... (le reste du code update() existant)

	}

	// NOUVELLE MÉTHODE : Après chargement d'un niveau
	public static void afterLoad() {
		// Resynchroniser les joueurs après un changement de niveau
		if (MultiplayerManager.sessionActive) {
			MultiplayerManager.syncAllPlayers();
			updateMultiplayerDisplay();
		}
	}

	// MODIFICATION : Méthode pour gérer les actions multijoueur
	public static void handleMultiplayerAction(int cell, String actionType) {
		if (!MultiplayerManager.sessionActive) return;
		
		switch (actionType) {
			case "ATTACK":
				// Gérer les attaques PvP
				break;
			case "SHARE_ITEM":
				// Gérer le partage d'objets
				break;
			case "TELEPORT":
				// Téléportation vers un joueur
				break;
		}
	}

	// MODIFICATION : Affichage des messages multijoueur
	public static void showMultiplayerMessage(String message) {
		if (scene != null) {
			GLog.i("[Multijoueur] " + message);
		}
	}

	// ... (le reste des méthodes existantes reste inchangé)

	// MODIFICATION : Méthode examineCell pour inclure les joueurs multijoueur
	public static void examineCell( Integer cell ) {
		if (cell == null
				|| cell < 0
				|| cell > Dungeon.level.length()
				|| (!Dungeon.level.visited[cell] && !Dungeon.level.mapped[cell])) {
			return;
		}

		ArrayList<Object> objects = getObjectsAtCell(cell);

		// NOUVEAU : Vérifier les joueurs multijoueur sur cette cellule
		if (MultiplayerManager.sessionActive) {
			for (PlayerData player : MultiplayerManager.getOtherPlayers()) {
				if (player.getPosition() == cell && player.isAlive() && player.isConnected()) {
					objects.add(player);
				}
			}
		}

		if (objects.isEmpty()) {
			GameScene.show(new WndInfoCell(cell));
		} else if (objects.size() == 1){
			examineObject(objects.get(0));
		} else {
			String[] names = getObjectNames(objects).toArray(new String[0]);

			GameScene.show(new WndOptions(Icons.get(Icons.INFO),
					Messages.get(GameScene.class, "choose_examine"),
					Messages.get(GameScene.class, "multiple_examine"),
					names){
				@Override
				protected void onSelect(int index) {
					examineObject(objects.get(index));
				}
			});

		}
	}

	// MODIFICATION : Méthode getObjectNames pour inclure les joueurs multijoueur
	private static ArrayList<String> getObjectNames( ArrayList<Object> objects ){
		ArrayList<String> names = new ArrayList<>();
		for (Object obj : objects){
			if (obj instanceof Hero)        
				names.add(((Hero) obj).className().toUpperCase(Locale.ENGLISH));
			else if (obj instanceof Mob)    
				names.add(Messages.titleCase( ((Mob)obj).name() ));
			else if (obj instanceof Heap)   
				names.add(Messages.titleCase( ((Heap)obj).title() ));
			else if (obj instanceof Plant)  
				names.add(Messages.titleCase( ((Plant) obj).name() ));
			else if (obj instanceof Trap)   
				names.add(Messages.titleCase( ((Trap) obj).name() ));
			// NOUVEAU : Joueurs multijoueur
			else if (obj instanceof PlayerData)
				names.add(Messages.titleCase( ((PlayerData)obj).getPlayerName() ));
		}
		return names;
	}

	// MODIFICATION : Méthode examineObject pour gérer les joueurs multijoueur
	public static void examineObject(Object o){
		if (o == Dungeon.hero){
			GameScene.show( new WndHero() );
		} else if ( o instanceof Mob && ((Mob) o).isActive() ){
			GameScene.show(new WndInfoMob((Mob) o));
			if (o instanceof Snake && !Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_SURPRISE_ATKS)){
				GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_SURPRISE_ATKS);
			}
		} else if ( o instanceof Heap && !((Heap) o).isEmpty() ){
			GameScene.show(new WndInfoItem((Heap)o));
		} else if ( o instanceof Plant ){
			GameScene.show( new WndInfoPlant((Plant) o) );
			//plants can be harmful to trample, so let the player ID just by examine
			Bestiary.setSeen(o.getClass());
		} else if ( o instanceof Trap ){
			GameScene.show( new WndInfoTrap((Trap) o));
			//traps are often harmful to trigger, so let the player ID just by examine
			Bestiary.setSeen(o.getClass());
		} 
		// NOUVEAU : Examiner un joueur multijoueur
		else if ( o instanceof PlayerData ){
			PlayerData player = (PlayerData) o;
			String info = "Joueur: " + player.getPlayerName() + 
						 "\nNiveau: " + player.getLevel() +
						 "\nPV: " + player.getHealth() + "/" + player.getMaxHealth() +
						 "\nStatut: " + (player.isAlive() ? "Vivant" : "Mort");
			GameScene.show( new WndMessage( info ) );
		}
		else {
			GameScene.show( new WndMessage( Messages.get(GameScene.class, "dont_know") ) ) ;
		}
	}

	// ... (le reste du code existant)

}