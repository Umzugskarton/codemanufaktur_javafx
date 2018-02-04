package game;

import data.user.User;
import game.board.SupplySled;
import game.board.cards.Card;
import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert einen Spieler. Enthält seine Punktanzahl, seine aktuellen Karten und seine Steine.
 */
public class Player {

  private int id;
  private int points = 0;
  private User user;
  private List<Card> cards = new ArrayList<>();
  private SupplySled supplySled = new SupplySled();

  /**
   * Erstellt einen neuen Spieler.
   *
   * @param user der eingeloggte User, dem der Spieler zugeordnet ist
   * @param id die ID des Spielers im Spiel; "Spielernummer"
   */
  public Player(User user, int id) {
    this.user = user;
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void addPoints(int points) {
    this.points += points;
  }

  public void addCard(Card card) {
    cards.add(card);
  }

  public boolean ownsCard(Card card) {
    return cards.contains(card);
  }

  public int getPoints() {
    return points;
  }

  public SupplySled getSupplySled() {
    return supplySled;
  }
}