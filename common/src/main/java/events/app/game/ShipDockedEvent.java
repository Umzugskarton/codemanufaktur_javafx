package events.app.game;

import events.SiteType;
import java.util.ArrayList;

public class ShipDockedEvent extends GameEvent {

  private SiteType site;
  private int shipID;
  private ArrayList<Integer> newstones;

  public ShipDockedEvent(int shipID, SiteType site, ArrayList<Integer> newstones) {
    this.shipID = shipID;
    this.site = site;
    this.newstones = newstones;
  }

  public ArrayList<Integer> getNewStones() {
    return newstones;
  }

  public int getShipID() {
    return shipID;
  }

  public SiteType getSite() {
    return site;
  }
}
