package game.gameprocedures;

import events.Event;
import events.SiteType;
import events.app.game.DockingShipError;
import events.app.game.NotEnoughLoadError;
import events.app.game.ShipAlreadyDockedError;
import events.app.game.ShipDockedEvent;
import events.app.game.SiteAlreadyDockedError;
import game.Game;
import game.board.Ship;
import game.board.Stone;
import game.board.StoneSite;
import java.lang.reflect.Method;
import java.util.ArrayList;
import requests.gamemoves.Move;
import requests.gamemoves.VoyageToStoneSiteMove;

public class VoyageToStoneSite implements Procedure {
  private VoyageToStoneSiteMove move;
  private Game game;
  private int playerId;

  VoyageToStoneSite(Game game, int playerId) {
    this.game = game;
    this.playerId = playerId;
  }

  public void put(Move move) {
    this.move = (VoyageToStoneSiteMove) move;
  }

  public Event exec() {
    Ship ship = game.getShipByID(move.getShipId());
    StoneSite site;
    try {
      site = (StoneSite) game.getSiteByType(move.getStoneSite());
    } catch (Exception e) {
      return new DockingShipError();
    }

    if (!ship.isDocked()) {
      if (ship.getLoadedStones() >= ship.getMinimumStones()) {
        if (site.dockShip(ship)) {
          ship.setDocked(true);
          ArrayList<Integer> siteStones = new ArrayList<>();

          for (Stone stone : site.getStones()) {
            if (stone != null) {
              siteStones.add(stone.getPlayer().getId());
            }
          }
          if (move.getStoneSite() == SiteType.PYRAMID) {
            game.updatePyramids();
          }
          return new ShipDockedEvent(move.getShipId(), move.getStoneSite(), siteStones);
        } else {
          return new SiteAlreadyDockedError(move.getStoneSite());
        }
      } else {
        return new NotEnoughLoadError(move.getShipId());
      }
    }else {
      return new ShipAlreadyDockedError(move.getShipId());
    }
  }
}
