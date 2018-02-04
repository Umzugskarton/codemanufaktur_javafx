package events.app.game;


import java.util.ArrayList;

public class ChooseCardEvent extends GameEvent {
    private int playerId;
    private ArrayList<Integer> choosenCardsId;

    public ChooseCardEvent(int playerId, ArrayList<Integer> choosenCardsId) {
        this.playerId = playerId;
        this.choosenCardsId = choosenCardsId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public ArrayList<Integer> getChoosenCardsId() {
        return choosenCardsId;
    }
}
