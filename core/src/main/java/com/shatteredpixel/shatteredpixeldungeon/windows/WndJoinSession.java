package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.multiplayer.MultiplayerManager;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.TextField;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class WndJoinSession extends Window {
    
    private static final int WIDTH = 120;
    private static final int GAP = 2;
    
    private TextField codeField;
    
    public WndJoinSession() {
        super();
        
        resize(WIDTH, 0);
        
        RenderedTextBlock title = PixelScene.renderTextBlock("Rejoindre une Session", 9);
        title.hardlight(TITLE_COLOR);
        title.setPos((WIDTH - title.width()) / 2, GAP);
        add(title);
        
        float pos = title.bottom() + GAP*2;
        
        // Instructions
        RenderedTextBlock instructions = PixelScene.renderTextBlock("Entrez le code de session:", 6);
        instructions.setPos(0, pos);
        add(instructions);
        pos = instructions.bottom() + GAP;
        
        // Champ de saisie du code
        codeField = new TextField() {
            @Override
            public void onSelect(boolean enterPressed) {
                if (enterPressed) {
                    joinSession();
                }
            }
        };
        codeField.setMaxLength(6);
        codeField.setText("");
        codeField.setRect(0, pos, WIDTH, 20);
        add(codeField);
        pos = codeField.bottom() + GAP*2;
        
        // Bouton Rejoindre
        RedButton joinBtn = new RedButton("Rejoindre") {
            @Override
            protected void onClick() {
                joinSession();
            }
        };
        joinBtn.setRect(0, pos, WIDTH, 20);
        add(joinBtn);
        pos = joinBtn.bottom() + GAP;
        
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
        
        // Focus automatique sur le champ de texte
        codeField.setFocus(true);
    }
    
    private void joinSession() {
        String code = codeField.text().trim().toUpperCase();
        String playerName = "Aventurier"; // Récupérer le nom du héros
        
        if (code.length() != 6) {
            GLog.w("Le code doit contenir 6 caractères");
            return;
        }
        
        if (!code.matches("[A-Z0-9]+")) {
            GLog.w("Code invalide. Utilisez seulement des lettres et chiffres");
            return;
        }
        
        boolean success = MultiplayerManager.joinSession(code, playerName);
        
        if (success) {
            GLog.p("Session rejointe!");
            hide();
            GameScene.show(new WndSessionInfo());
        } else {
            GLog.w("Impossible de rejoindre la session");
        }
    }
}