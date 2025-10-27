package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.multiplayer.MultiplayerManager;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class WndCreateSession extends Window {
    
    private static final int WIDTH = 140;
    private static final int GAP = 2;
    
    private OptionSlider modeSlider;
    private OptionSlider playersSlider;
    private RenderedTextBlock sessionCodeText;
    
    public WndCreateSession() {
        super();
        
        resize(WIDTH, 0);
        
        RenderedTextBlock title = PixelScene.renderTextBlock("Créer une Session", 9);
        title.hardlight(TITLE_COLOR);
        title.setPos((WIDTH - title.width()) / 2, GAP);
        add(title);
        
        float pos = title.bottom() + GAP*2;
        
        // Sélection du mode de jeu
        RenderedTextBlock modeLabel = PixelScene.renderTextBlock("Mode de jeu:", 6);
        modeLabel.setPos(0, pos);
        add(modeLabel);
        pos = modeLabel.bottom() + GAP;
        
        modeSlider = new OptionSlider(Messages.get(MultiplayerManager.GameMode.class, "coop"),
                                     Messages.get(MultiplayerManager.GameMode.class, "pvp"),
                                     1, 4) {
            @Override
            protected void onChange() {
                updateDisplay();
            }
        };
        modeSlider.setSelectedValue(1); // Coop par défaut
        modeSlider.setRect(0, pos, WIDTH, 20);
        add(modeSlider);
        pos = modeSlider.bottom() + GAP;
        
        // Sélection du nombre de joueurs
        RenderedTextBlock playersLabel = PixelScene.renderTextBlock("Joueurs max:", 6);
        playersLabel.setPos(0, pos);
        add(playersLabel);
        pos = playersLabel.bottom() + GAP;
        
        playersSlider = new OptionSlider("2", "8", 2, 8) {
            @Override
            protected void onChange() {
                updateDisplay();
            }
        };
        playersSlider.setSelectedValue(4); // 4 joueurs par défaut
        playersSlider.setRect(0, pos, WIDTH, 20);
        add(playersSlider);
        pos = playersSlider.bottom() + GAP*2;
        
        // Affichage du code de session (généré après création)
        sessionCodeText = PixelScene.renderTextBlock("Code: -----", 8);
        sessionCodeText.setPos((WIDTH - sessionCodeText.width()) / 2, pos);
        add(sessionCodeText);
        pos = sessionCodeText.bottom() + GAP*2;
        
        // Bouton Créer
        RedButton createBtn = new RedButton("Créer la Session") {
            @Override
            protected void onClick() {
                createSession();
            }
        };
        createBtn.setRect(0, pos, WIDTH, 20);
        add(createBtn);
        pos = createBtn.bottom() + GAP;
        
        // Bouton Annuler
        RedButton cancelBtn = new RedButton("Annuler") {
            @Override
            protected void onClick() {
                hide();
            }
        };
        cancelBtn.setRect(0, pos, WIDTH, 20);
        add(cancelBtn);
        
        resize(WIDTH, (int)cancelBtn.bottom() + GAP);
        updateDisplay();
    }
    
    private void updateDisplay() {
        MultiplayerManager.GameMode selectedMode = getSelectedMode();
        int maxPlayers = playersSlider.getSelectedValue();
        
        // Mettre à jour l'affichage du mode
        switch (selectedMode) {
            case COOP:
                modeSlider.setTitle("Coopératif");
                break;
            case PVP:
                modeSlider.setTitle("Combat PvP");
                break;
            case SURVIVAL:
                modeSlider.setTitle("Mode Survie");
                break;
        }
    }
    
    private MultiplayerManager.GameMode getSelectedMode() {
        switch (modeSlider.getSelectedValue()) {
            case 1: return MultiplayerManager.GameMode.COOP;
            case 2: return MultiplayerManager.GameMode.PVP;
            case 3: return MultiplayerManager.GameMode.SURVIVAL;
            default: return MultiplayerManager.GameMode.COOP;
        }
    }
    
    private void createSession() {
        MultiplayerManager.GameMode mode = getSelectedMode();
        int maxPlayers = playersSlider.getSelectedValue();
        String playerName = "Héros"; // Récupérer le nom du héros actuel
        
        boolean success = MultiplayerManager.createSession(mode, maxPlayers, playerName);
        
        if (success) {
            // Mettre à jour l'affichage du code
            sessionCodeText.text("Code: " + MultiplayerManager.sessionCode);
            sessionCodeText.setPos((WIDTH - sessionCodeText.width()) / 2, sessionCodeText.top());
            
            GLog.p("Session créée! Code: " + MultiplayerManager.sessionCode);
            
            // Cacher après un délai
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {}
                
                Game.runOnRenderThread(() -> {
                    hide();
                    GameScene.show(new WndSessionInfo());
                });
            }).start();
        }
    }
}