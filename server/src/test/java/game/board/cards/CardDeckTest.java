package game.board.cards;

import org.junit.Test;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardDeckTest {

    @Test
    public void createCardTest() {
        CardDeck cardDeck = new CardDeck();
        assertEquals(24, cardDeck.getDeck().size());
    }
}