package rails.game;

import rails.common.GuiDef;
import rails.common.GuiHints;
import rails.common.LocalText;

/**
 * EndOfGameRound is a dummy implementation of the Round class
 * It generates no additional actions.
 * It also sets guiHints (default: shows map, stock market and activates status) 
 *
 *  */

public final class EndOfGameRound extends Round {

    public EndOfGameRound(GameManager parent, String id) {
        super(parent, id);
        guiHints.setVisibilityHint(GuiDef.Panel.MAP, true);
        guiHints.setActivePanel(GuiDef.Panel.STATUS);
    }
    
    public static EndOfGameRound create(GameManager parent, String id) {
        return new EndOfGameRound(parent, id);
    }

    @Override
    public boolean setPossibleActions() {
        possibleActions.clear();
        return true;
    }
    
    public GuiHints getGuiHints() {
        return guiHints;
    }

    public void setGuiHints(GuiHints guiHints) {
        this.guiHints = guiHints; 
    }
    
    @Override
    public String getHelp() {
        return LocalText.getText("EndOfGameHelpText");
    }

    @Override
    public String toString() {
        return "EndOfGameRound ";
    }

    @Override
    public String getRoundName() {
        return toString();
    }
    
}
