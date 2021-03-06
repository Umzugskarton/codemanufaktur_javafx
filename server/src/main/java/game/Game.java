package game;

import data.user.User;
import events.Event;
import events.SiteType;
import events.app.game.GameInfoEvent;
import events.app.game.TurnEvent;
import events.app.game.UpdatePointsEvent;
import events.app.game.WinEvent;
import game.board.BurialChamber;
import game.board.Market;
import game.board.Obelisks;
import game.board.Pyramids;
import game.board.Ship;
import game.board.Site;
import game.board.StoneSite;
import game.board.Temple;
import game.board.cards.Card;
import game.board.cards.CardDeck;
import game.board.cards.OrnamentCard;
import game.board.cards.StatueCard;
import game.gameprocedures.Procedure;
import game.gameprocedures.ProcedureFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import lobby.Lobby;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import requests.gamemoves.CardType;
import requests.gamemoves.Move;
import socket.ClientListener;

public class Game implements Runnable {

  private static final int NUMBER_OF_SHIPS = 4;
  private final Logger log = LoggerFactory.getLogger(getClass().getName());

  private int gameID;
  private Lobby lobby;
  private Ship[] ships = new Ship[NUMBER_OF_SHIPS];
  private Player[] players;
  private int currentPlayer;
  private int round;
  private CardDeck cardDeck = new CardDeck();
  private int size;

  //StoneSites
  private ArrayList<StoneSite> sites = new ArrayList<>();
  private Pyramids pyramids;
  private Market market;
  private Obelisks obelisks;
  private Temple temple;
  private BurialChamber burialChamber;
  // Reihenfolge wichtig , muss mit der dreingabe der Sites in den sites Array übereinstimmen!
  private SiteType[] siteTypesArray = {
      SiteType.MARKET, SiteType.PYRAMID, SiteType.TEMPLE,
      SiteType.BURIALCHAMBER, SiteType.OBELISKS
  };
  private ArrayList<SiteType> siteTypes = new ArrayList<>(Arrays.asList(siteTypesArray));

  private ClientListener clientListener;
  private Move nextMove = null;
  private ProcedureFactory pf;
  private MoveExecutor executor = new MoveExecutor();

  public Game(Lobby lobby, ClientListener clientListener) {
    this.lobby = lobby;
    this.gameID = this.lobby.getLobbyID();
    this.clientListener = clientListener;
    initialize();
  }

  private void initialize() {
    size = lobby.getSize();
    players = new Player[size];
    market = new Market(size, cardDeck.getDeck());
    pyramids = new Pyramids(size);
    obelisks = new Obelisks(size);
    temple = new Temple(size);
    burialChamber = new BurialChamber(size);
    sites.add(pyramids);
    sites.add(temple);
    sites.add(burialChamber);
    sites.add(obelisks);

    for (int i = 0; i < NUMBER_OF_SHIPS; i++) {
      ships[i] = new Ship(i);
    }
    List<User> lobbyUsers = new ArrayList<>(Arrays.asList(lobby.getUsers()));
    for (int i = 0; i < size; i++) {
      players[i] = new Player(lobbyUsers.get(i), i);
      players[i].addStones(i + 2);
    }
  }

  public int getSize() {
    return size;
  }

  private void resetCurrentShips() {
    ships = new Ship[NUMBER_OF_SHIPS]; // Bitte da lasssen sonst werden die Schiffe nicht richtig zurückgesetzt !
    for (int i = 0; i < NUMBER_OF_SHIPS; i++) {
      this.ships[i] = new Ship(i);
    }
  }

  public void sendAll(Event event) {
    for (Player player : this.players) {
      sendTo(player.getUser(), event);
    }
  }

  public void sendAll(GameInfoEvent event) {
    for (Player player : this.players) {
      event.setMyId(player.getId());
      sendTo(player.getUser(), event);
    }
  }

  public void sendAll(WinEvent event) {
    for (Player p : players) {
      event.setWinning(event.getWinner().equals(p.getUser().getUsername()));
      sendTo(p.getUser(), event);
    }
  }

  public int[] getPointsSum() {
    int[] points = new int[players.length];
    for (Player player : this.players) {
      points[player.getId()] = player.getPoints();
    }
    return points;
  }

  /**
   * Sendet die aktuelle Punktezahl an alle Spieler
   *
   */

  private void updatePoints() {
    sendAll(new UpdatePointsEvent(getPointsSum(), gameID));
  }

  /**
   * Berechnet die aktuellen Punkte aller Spieler für die Pyramide und führt updatePoints aus
   */

  public void updatePyramids() {
    int[] newPoints = pyramids.getPointsAndFinishTurn();
    for (int player = 0; player < this.players.length; player++) {
      this.players[player].addPoints(newPoints[player]);
    }
    updatePoints();
  }

  /**
   * Sendet ein Event an einen User
   *
   * @param user der User an den das Event geschickt werden soll
   * @param event das Event das an den User gesschickt werden soll
   */

  public void sendTo(User user, Event event) {
    this.clientListener.getServer().sendTo(event, user.getUsername());
  }

  /**
   *
   * Erstellt ein GameInfoEvent das an den Client gesendet wird, dies enthält alle wichtigen Informationen
   * zu dem aktuellen Zustand des Spieles
   *
   * @return GameInfoEvent, mit allen Informationen zum Stand des Spieles
   */

  private GameInfoEvent getGameInfo() {
    GameInfoEvent gameInfo = new GameInfoEvent(gameID);

    String[] users = new String[this.lobby.getSize()];
    for (int i = 0; i < this.lobby.getSize(); i++) {
      users[i] = this.players[i].getUser().getUsername();
    }

    for (Ship ship : ships) {
      gameInfo.setCurrentShips(ship.getCargoAsIntArrayByShip());
    }

    int numberOfSites = 5;
    int[] dockedSites = new int[numberOfSites];

    if (market.isDocked()) {
      dockedSites[0] = market.getDockedShip().getId();
    } else {
      dockedSites[0] = -1;
    }
    int j = 1;
    for (StoneSite site : sites) {
      if (site.isDocked()) {
        dockedSites[j] = site.getDockedShip().getId();
      } else {
        dockedSites[j] = -1;
      }
      j++;
    }
    ArrayList<CardType> cards = new ArrayList<>();
    market.getActiveCards().forEach(card -> cards.add(card.getType()));

    gameInfo.setCards(cards);
    gameInfo.setSiteTypes(siteTypes);
    gameInfo.setSitesAllocation(dockedSites);
    gameInfo.setOrder(users);
    gameInfo.setTurnTime(MoveExecutor.TURN_TIME);
    gameInfo.setRound(this.round);
    ArrayList<Integer> storages = new ArrayList<>();
    for (Player p : players) {
      storages.add(p.getStones());
    }
    gameInfo.setStorages(storages);
    return gameInfo;
  }

  /**
   * Die Spielschleife , die die Grundstruktur des Spiels darstellt, von hier aus werden alle Parameter für eine Runde initiert
   * und gestartet
   *
   */

  @Override
  public void run() {
    int numberOfRounds = 6;
    boolean endOfRound = false;
    for (int i = 1; i <= numberOfRounds; i++) {
      this.round = i;
      sites.forEach(Site::prepareRound);
      market.prepareRound();
      sendAll(getGameInfo());
      while (!allshipsDocked()) {
        int playerRound = 0;
        if(endOfRound){ //beginnt neue Runde mit dem Spieler, der als Naechster an der Reihe gewesen waere
          playerRound = (currentPlayer +1) % this.players.length;
          endOfRound = false;
        }
        for (int player = playerRound; player < this.players.length; player++) {
          currentPlayer = player; //Leichterer Zugriff auf aktuellen Player
          setActivePlayer(player);
          waitForMove(player);
          if (this.nextMove != null) {
            executeMove();
          } else {
            log.error("[ Game: {} ] Kein Spielzug gesetzt von Spieler {}!", gameID,
                players[currentPlayer].getId());
          }
          nextMove = null;
          if (allshipsDocked()) {
            break;
          }
          playerRound++;
        }
      }
      resetCurrentShips();
      addPointsEndOfRound();
      endOfRound = true;
    }
    addPointsEndOfGame();
    nominateWinner();
  }

  /**
   * Berechnet alle Punkte, die am Ende eines Spiels abgerechnet werden , und führt alle Punkte aus den StoneSites und
   * Karten zusammen und führt darauf hin updatePoints aus
   *
   */

  private void addPointsEndOfGame() {
    int[] burialChamberPoints = burialChamber.getPoints();
    int[] obelisksPoints = obelisks.getPoints();
    for (int player = 0; player < this.players.length; player++) {
      this.players[player].addPoints(getStatueCardPoints(player));
      this.players[player].addPoints(getOrnamentPoints(player));
      this.players[player].addPoints(burialChamberPoints[player]);
      this.players[player].addPoints(obelisksPoints[player]);
    }
    updatePoints();
  }

  /**
   * Berechnet die Punkte, der OrnamentCards, besitzt ein Spieler eine OrnamentCard
   * so wird über dessen Typ und mit dem stones Integer Array die Punktezahl zusammen gerechnet die dieser
   * für jene OrnamentCard erhält , sollte er mehrere besitzen werden die punkte kumuliert und zurückgegeben
   *
   * @param player die Id des Spielers im Game
   * @return points, die der Spieler durch die OrnamentCards erhält
   */

  private int getOrnamentPoints(int player){
    ArrayList<Integer> stonesList = new ArrayList<>();
    sites.forEach(stoneSite -> stonesList.add(stoneSite.getStones().size()));
    Integer[] stones = new Integer[stonesList.size()];
    stones = stonesList.toArray(stones);
    int points = 0;
    for (Card card : players[player].getCards()){
      if(card instanceof OrnamentCard) {
        OrnamentCard ornamentCard = (OrnamentCard) card;
        points += ornamentCard.calc(stones);
      }
    }
    return points;
  }

  /**
   * Berechnet die Punkte, die ein Spieler am Ende des Spiels für das sammeln von StatueCards erhält
   * über die auf einerStatueCard implementierten calc funktion wird pber die Anzahl  die Punktezahl erechnet
   *
   * @param player die Id des Spielers im Game
   * @return Punkte, die der Spieler durch die StatueCards erhält
   */

  private int getStatueCardPoints(int player){
    int statue = 0;
    for (Card card : players[player].getCards()){
      if (card.getType().equals(CardType.STATUE)) {
      statue++;
      }
    }
    if (statue > 0)
    return new StatueCard().calc(statue);
    else
      return 0;
  }


  /**
   * Berechnet die Punkte, die am Ende einer jeden Runde berechnet werden sollen und führt update Points aus
   *
   */

  private void addPointsEndOfRound() {
    int[] templePoints = temple.getPoints();
    for (int player = 0; player < this.players.length; player++) {
      this.players[player].addPoints(templePoints[player]);
    }
    updatePoints();
  }


  /**
   * Bestimmt den Gewinner des Spiels , erstellt einen String[][] der Usernamen und die dazugehörige
   * Punktezahl enthält und sendet ein WinEvent an alle Spieler im Spiel , welches die zuvor bestimmten Information enthält
   */

  private void nominateWinner() {
    Player winner = null;
    String[][] playerResult = new String[2][players.length];
    int i = 0;
    for (Player p : players) {
      if (winner == null || p.getPoints() > winner.getPoints()) {
        winner = p;
      }
      playerResult[0][i] = p.getUser().getUsername();
      playerResult[1][i] = String.valueOf(p.getPoints());
      i++;
    }
    sendAll(new WinEvent(winner.getUser().getUsername(), playerResult, gameID));
  }

  public Site getSiteByType(SiteType siteType) {
    EnumMap<SiteType, Site> site = new EnumMap<>(SiteType.class);
    site.put(SiteType.PYRAMID, pyramids);
    site.put(SiteType.MARKET, market);
    site.put(SiteType.OBELISKS, obelisks);
    site.put(SiteType.TEMPLE, temple);
    site.put(SiteType.BURIALCHAMBER, burialChamber);
    return site.get(siteType);
  }

  public Ship[] getShips() {
    return ships;
  }

  /**
   * Setzt den aktuellen Spieler , dazu wird die ProcedureFactory mit der id des aktuellen Spielers neuinitiiert und
   * ein TurnEvent an alle Spieler sendet welches die information enthält ob dieser nun an der Reihe ist
   *
   * @param player die Id des Spielers im Game
   */

  private void setActivePlayer(int player) {
    pf = new ProcedureFactory(player, this);
    for (Player p : this.players) {
      sendTo(p.getUser(),
          new TurnEvent(p == this.players[player], this.players[player].getUser().getUsername(),
              gameID));
    }
  }

  /**
   * Initiert die passende Procedure zum gesetzten Spielzug und führt executeProcedure aus
   *
   *
   * @return points, die der Spieler durch die OrnamentCards erhält
   */
  private void executeMove() {
    Procedure nextProcedure = pf.getProcedure(nextMove);
    executeProcedure(nextProcedure);
  }

  /**
   * Führt die übergebene Procedure aus und sendet allen Usern ein Event das die Procedure zurückwirft
   *
   * @param procedure die Id des Spielers im Game
   */

  private void executeProcedure(Procedure procedure) {
    log.info("[Game:" + gameID + "] führe Spielzug " + procedure.getClass().getName() + " aus für "
        + currentPlayer + " (Spieler: " + this.players[currentPlayer].getUser().getUsername()
        + ")");

    //Informiert alle User über den/die ausgeführten Move/s
    sendAll(procedure.exec());
  }

  /**
   * Initiirt die zu einem übergebenem Spielzug passende Procedure und führt
   * execute Procedure aus
   *
   * @param move der Spielzug der ausgeführt werden soll
   */

  public void executeMove(Move move) {
    Procedure nextProcedure = pf.getProcedure(move);
    executeProcedure(nextProcedure);
  }

  /**
   * Überprüft ob alle Schiffe an einen Hafen angedockt wurden
   *
   * @return boolean, der anzeigt ob alle Schiffe an einen Hafen angedockt haben
   */

  private boolean allshipsDocked() {
    for (Ship ship : this.ships) {
      if (!ship.isDocked()) {
        return false;
      }
    }
    return true;
  }



  public Player getPlayerByUser(User user) {
    for (Player player : players) {
      if (player.getUser().equals(user)) {
        return player;
      }
    }
    return null;
  }

  public Player[] getPlayers() {
    return players;
  }

  private void waitForMove(int p) {
    log.info("[Game:" + gameID + "] Warte auf Spielzug von Spieler " + (p + 1) + " (Name: "
        + this.players[p].getUser().getUsername() + ")");
    executor.waitForMove();
    nextMove = executor.getMove();
  }

  public int getCurrentPlayer() {
    return currentPlayer;
  }

  public MoveExecutor getExecutor() {
    return executor;
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

  public Player getPlayer(int id) {
    return players[id];
  }

  public int getGameID() {
    return gameID;
  }

  public void setCurrentPlayer(int currentPlayer) {
    this.currentPlayer = currentPlayer;
  }

  public void runOneRoundTest(Move[] moves) {
    if(this.round == 0) {
      this.sites.forEach(Site::prepareRound);
      market.prepareRound();
    }
    sendAll(getGameInfo());
    for (int player = 0; player < this.players.length; player++) {
      currentPlayer = player; //Leichterer Zugriff auf aktuellen Player
      pf = new ProcedureFactory(player, this);
      nextMove = setTestMove(moves[player]);
      if (this.nextMove != null) {
        executeMove();
      } else {
        log.error("[ Game: " + gameID + " ] Kein Spielzug gesetzt von Spieler "
            + players[currentPlayer] + "! ");
      }
    }
    this.round++;
    addPointsEndOfRound();
    if (allshipsDocked() == true) {
      nominateWinner();
      log.info("geschafft");
    }
  }

  public void delUser(User user){
    Arrays.asList(players).remove(user);
  }
  public Move setTestMove(Move move) {
    return move;
  }
}
