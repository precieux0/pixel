package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.multiplayer.MultiplayerManager;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.ui.Component;

public class WndMultiplayer extends Window {
    
    private static final int WIDTH = 120;
    private static final int BTN_HEIGHT = 20;
    private static final int GAP = 2;
    
    public WndMultiplayer() {
        super();
        
        resize(WIDTH, 0); // La hauteur sera calculée automatiquement
        
        RenderedTextBlock title = PixelScene.renderTextBlock("Mode Multijoueur", 9);
        title.hardlight(TITLE_COLOR);
        title.setPos((WIDTH - title.width()) / 2, GAP);
        add(title);
        
        float pos = title.bottom() + GAP*2;
        
        // Bouton Créer Session
        RedButton createBtn = new RedButton("Créer une Session") {
            @Override
            protected void onClick() {
                hide();
                GameScene.show(new WndCreateSession());
            }
        };
        createBtn.setRect(0, pos, WIDTH, BTN_HEIGHT);
        add(createBtn);
        pos = createBtn.bottom() + GAP;
        
        // Bouton Rejoindre Session
        RedButton joinBtn = new RedButton("Rejoindre une Session") {
            @Override
            protected void onClick() {
                hide();
                GameScene.show(new WndJoinSession());
            }
        };
        joinBtn.setRect(0, pos, WIDTH, BTN_HEIGHT);
        add(joinBtn);
        pos = joinBtn.bottom() + GAP;
        
        // Si une session est active, afficher les informations
        if (MultiplayerManager.sessionActive) {
            RedButton sessionInfoBtn = new RedButton("Infos Session") {
                @Override
                protected void onClick() {
                    hide();
                    GameScene.show(new WndSessionInfo());
                }
            };
            sessionInfoBtn.setRect(0, pos, WIDTH, BTN_HEIGHT);
            add(sessionInfoBtn);
            pos = sessionInfoBtn.bottom() + GAP;
            
            RedButton leaveBtn = new RedButton("Quitter la Session") {
                @Override
                protected void onClick() {
                    hide();
                    MultiplayerManager.leaveSession();
                }
            };
            leaveBtn.setRect(0, pos, WIDTH, BTN_HEIGHT);
            add(leaveBtn);
            pos = leaveBtn.bottom() + GAP;
        }
        
        // Bouton Retour
        RedButton backBtn = new RedButton("Retour") {
            @Override
            protected void onClick() {
                hide();
            }
        };
        backBtn.setRect(0, pos, WIDTH, BTN_HEIGHT);
        add(backBtn);
        
        resize(WIDTH, (int)backBtn.bottom() + GAP);
    }
}