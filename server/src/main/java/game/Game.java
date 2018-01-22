package game;

import GameEvents.GameInfoEvent;
import GameEvents.TurnEvent;
import GameMoves.Move;
import GameMoves.ToolCardMove;
import SRVevents.Event;
import game.GameProcedures.Procedure;
import game.GameProcedures.ProcedureFactory;
import game.board.BurialChamber;
import game.board.Cards.Card;
import game.board.Cards.LocationCard;
import game.board.Cards.OrnamentCard;
import game.board.Cards.ToolCard;
import game.board.Market;
import game.board.Obelisks;
import game.board.Pyramids;
import game.board.Ship;
import game.board.StoneSite;
import game.board.Temple;
import java.util.Collections;
import lobby.Lobby;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socket.ClientListener;
import user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Game implements Runnable {
  private final Logger log = LoggerFactory.getLogger(getClass().getName());

  private int gameID;
  private Lobby lobby;
  private Ship[] ships;
  private ArrayList<Integer> storages;
  private Player[] order;
  private int currentPlayer;
  private int round;
  private ArrayList<Card> cardStack = new ArrayList<>();


  //StoneSites
  private ArrayList<StoneSite> sites;
  private Pyramids pyramids;
  private Market market;
  private Obelisks obelisks;
  private Temple temple;
  private BurialChamber burialChamber;
  // Reihenfolge wichtig , muss mit der dreingabe der Sites in den sites Array übereinstimmen!
  private String[] siteString = {"Pyramids", "Temple", "BurialChamber", "Obelisks"};

  private ClientListener clientListener;
  private Move nextMove = null;
  private List<Event> executedMoves;
  ProcedureFactory pf;
  private MoveExecutor executor;

  public Game(Lobby lobby, ClientListener clientListener) {
    this.lobby = lobby;
    this.gameID = this.lobby.getLobbyID();
    this.clientListener = clientListener;
    this.executedMoves = new ArrayList<>();
    lobby.show(false);

    this.ships = new Ship[lobby.getSize()];
    this.order = new Player[lobby.getSize()];
    this.storages = new ArrayList<>();
    setGame();
    executor = new MoveExecutor();
    this.market = new Market(lobby.getSize());
    this.pyramids = new Pyramids(lobby.getSize(), 1);
    this.obelisks = new Obelisks(lobby.getSize());
    this.temple = new Temple(lobby.getSize());
    this.burialChamber = new BurialChamber(lobby.getSize());
    sites = new ArrayList<>();
    //Reihenfolge beachten!
    sites.add(pyramids);
    sites.add(temple);
    sites.add(burialChamber);
    sites.add(obelisks);

    setCards();
    distributeCards();
  }

  public void resetCurrentShips() {
    for (int i = 0; i <= lobby.getSize() - 1; i++) {
      this.ships[i] = new Ship(i, ThreadLocalRandom.current().nextInt(1, 4));
    }
  }

  private void setGame() {
    int seq = ThreadLocalRandom.current().nextInt(0, this.lobby.getSize() - 1);
    for (int i = 0; i <= lobby.getSize() - 1; i++) {
      this.ships[i] = new Ship(i, ThreadLocalRandom.current().nextInt(1, 4));
      this.order[i] = new Player(lobby.getUsers()[seq], i);
      this.storages.add(i, i + 1);
      seq = (seq + 1) % lobby.getSize();
    }
  }

  public void sendAll(Event event) {
    for (Player player : this.order) {
      sendTo(player.getUser(), event);
    }
  }

  public void sendAll(GameInfoEvent event) {
    for (Player player : this.order) {
      event.setMyId(player.getId());
      sendTo(player.getUser(), event);
    }
  }


  public int[] getPointsSum() {
    int[] points = new int[order.length];
    for (StoneSite site : sites) {
      int[] sitepoints = site.getPoints();
      log.error(site.getClass().getName());
      for (int i = 0; i <= points.length - 1; i++) {
        points[i] += sitepoints[i];
      }
    }
    return points;
  }

  public void sendTo(User user, Event event) {
    this.clientListener.getServer().sendTo(event, user.getUsername());
  }

  private GameInfoEvent getGameInfo() {
    GameInfoEvent gameInfo = new GameInfoEvent();

    String[] users = new String[this.lobby.getSize()];
    for (int i = 0; i <= this.lobby.getSize() - 1; i++) {
      users[i] = this.order[i].getUser().getUsername();
    }

    for (Ship ship : ships) {
      gameInfo.setCurrentShips(getCargoAsIntArrayByShip(ship));
    }

    int[] dockedSites = new int[5];

    if (market.isDocked()) {
      dockedSites[0] = market.getDockedShip().getId();
    } else {
      dockedSites[0] = -1;
    }
    for (int i = 1; i < sites.size(); i++) {
      if (sites.get(i).isDocked()) {
        dockedSites[i] = sites.get(i).getDockedShip().getId();
      } else {
        dockedSites[i] = -1;
      }
    }

    //TODO siteString doesn't contain market anymore
    gameInfo.setSiteString(siteString);
    gameInfo.setSitesAllocation(dockedSites);
    gameInfo.setOrder(users);
    gameInfo.setTurnTime(executor.getTurnTime());
    gameInfo.setRound(this.round);
    gameInfo.setStorages(this.storages);

    return gameInfo;
  }

  public int[] getCargoAsIntArrayByShip(Ship ship) {
    int[] shipInt = new int[ship.getStones().length];
    for (int i = 0; i < ship.getStones().length; i++) {
      if (ship.getStones()[i] != null) {
        shipInt[i] = ship.getStones()[i].getPlayer().getId();
      } else {
        shipInt[i] = -1;
      }
    }
    return shipInt;
  }

  private void setCards() {
    for (int i = 0; i < siteString.length; i++) {
      cardStack.add(new OrnamentCard(siteString[i]));
      cardStack.add(new OrnamentCard(siteString[i]));
    }

    String[] redcards = {"Eingang", "Sarkophag", "Gepflasterter Pfad"};
    for (String name : redcards) {
      cardStack.add(new LocationCard(name));
      cardStack.add(new LocationCard(name));
    }

    String[] bluecards = {"lever", "sail", "hammer", "chisel"};
    for (String name : bluecards) {
      int count = 2;
      if (name.equals("chisel") || name.equals("sail")) {
        count++;
      }
      for (int i = 0; i < count; i++) {
        cardStack.add(new ToolCard(name));
      }
    }
  }

  private void distributeCards(){
    ArrayList<Card> cards = new ArrayList<>(cardStack);
    Collections.shuffle(cards);
    market.addCards(cards);
  }

  @Override
  public void run() {
    for (int i = 1; i <= 6; i++) {
      this.round = i;
      sendAll(getGameInfo());
      while (!allshipsDocked())
        for (int player = 0; player <= this.order.length - 1; player++) {
          currentPlayer = player;
          setActivePlayer(player);
          waitForMove(player);

          if (this.nextMove != null) {
              int tryed = 0;
              while (!executeMove() && tryed < 2) {
                waitForMove(player);
                tryed++;
            }
          } else
            log.error("Kein Spielzug gesetzt!");
          nextMove = null;
          //Informiert alle User über den/die ausgeführten Move/s
          for (Event e : executedMoves) {
            sendAll(e);
          }
          executedMoves.clear();

          if (allshipsDocked()) {
            break;
          }
        }
      resetCurrentShips();
    }
  }

  public BurialChamber getBurialChamber() {
    return burialChamber;
  }

  public Market getMarket() {
    return market;
  }

  public Obelisks getObelisks() {
    return obelisks;
  }

  public Pyramids getPyramids() {
    return pyramids;
  }

  public Temple getTemple() {
    return temple;
  }

  public Ship[] getShips() {
    return ships;
  }

  private void setActivePlayer(int player) {
    pf = new ProcedureFactory(player, this);
    for (Player p : this.order) {
      sendTo(p.getUser(), new TurnEvent(p == this.order[player], this.order[player].getUser().getUsername()));
    }
  }

  private boolean executeMove() {
    Procedure nextProcedure = pf.getProcedure(nextMove);
    executeProcedure(nextProcedure);
    return true;
  }

  private void executeProcedure(Procedure procedure){
    executedMoves.add(procedure.exec());
  }

  public boolean executeMove(Move move){
    Procedure nextProcedure = pf.getProcedure(move);
    executeProcedure(nextProcedure);
    return true;
  }

  private boolean allshipsDocked() {
    for (Ship ship : this.ships) {
      if (!ship.isDocked())
        return false;
    }

    return true;
  }

  private void waitForMove(int p) {
    log.info("[Lobby " + this.lobby.getLobbyID() + "] Warte auf Spielzug von Spieler " + (p + 1) + " (Name: " + this.order[p].getUser().getUsername() + ")");
    executor.waitForMove();
    nextMove = executor.getMove();
  }


  public MoveExecutor getExecutor(){return executor;}

  public void addStonesToStorage(int playerId) {
    if (storages.get(playerId) + 3 > 5)
      storages.set(playerId, 5);
    else
      storages.set(playerId, storages.get(playerId) + 3);
  }

  public int getStorage(int playerID) {
    return storages.get(playerID);
  }

  public boolean decrPlayerStorage(int playerID) {
    if ((storages.get(playerID) - 1) >= 0) {
      int storage = storages.get(playerID) - 1;
      storages.set(playerID, storage);
      return true;
    }
    return false;
  }

  public int getGameID() {
    return gameID;
  }

  public void updateRound(int round) {
    this.round = round;
  }

  public void updateCboats(Ship[] ships) {
    this.ships = ships;
  }

  public Ship getShipByID(int shipId) {
    return ships[shipId];
  }

  public Player[] getOrder() {
    return order;
  }
}