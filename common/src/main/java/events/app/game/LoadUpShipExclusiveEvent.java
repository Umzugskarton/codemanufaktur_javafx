package events.app.game;

public class LoadUpShipExclusiveEvent extends GameEvent {

  public LoadUpShipExclusiveEvent(int lobbyId) {
    this.lobbyId = lobbyId;
  }
}
