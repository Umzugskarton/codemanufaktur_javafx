package GameEvents;

import SRVevents.Event;

import java.util.Date;

public class FillUpStorageEvent implements Event {
  private String event = "FillUpStorage";
  private int playerId;
  private int storage;

  public FillUpStorageEvent(){
  }

  public FillUpStorageEvent(int playerId, int storage) {
    this.playerId = playerId;
    this.storage = storage;
  }
  
  public int getPlayerId() {
    return playerId;
  }

  public int getStorage() {
    return storage;
  }

  public String getEvent() {
    return event;
  }

  public void setStorage(int storage) {
    this.storage = storage;
  }

}