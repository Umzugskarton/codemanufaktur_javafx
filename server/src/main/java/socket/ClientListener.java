package socket;

import commands.Command;
import commands.CommandFactory;
import commands.Invoker;
import data.user.User;
import events.Event;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import lobby.Lobby;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import requests.IRequest;
import requests.gamemoves.Move;

/**
 * Enthält alle wichtigen Objekte zur und über die Kommunikation mit dem dazugehörigen Client.
 * Wartet auf ankommende Nachrichten, und verarbeitet diese dann, mithilfe von Gson
 * und einer Factory, die Nachrichten werden zu Requests oder Moves zurückgeschlüsselt.
 * Dafür wird dann bei einer IRequest der zugehörige Command auch mit einer Factory initiert
 * und durch das Invoker Objekt (Command Pattern) ausgeführt.
 * ist die eingehende Nachricht ein Move, wird dieser an das GameObjekt in der
 * zugehörigen Lobby übergeben.
 */

public class ClientListener implements Runnable {

  private final Logger log = LoggerFactory.getLogger(getClass().getName());

  private Server server;
  private ClientAPI clientAPI;
  private ObjectOutputStream out;
  private ObjectInputStream in;
  private User user = null;
  private ArrayList<Lobby> lobbies = new ArrayList<>();

  public ClientListener(Server server, ObjectOutputStream os, ObjectInputStream is,
      ClientAPI clientAPI) {
    this.server = server;
    this.out = os;
    this.in = is;
    this.clientAPI = clientAPI;
  }

  @Override
  public void run() {
    try {
      Object o;
      while ((o = in.readObject()) != null) {
        log.info("Nachricht erhalten: " + o.getClass().getSimpleName());
        if (o instanceof IRequest) {
          if (o instanceof Move) {
            Move move = (Move) o;
            Lobby lobby = getLobbyByID(move.getLobbyId());
            if (lobby != null && !lobby.isVisible()) {
              if (lobby.getGame().getCurrentPlayer() == lobby.getGame().getPlayerByUser(user)
                  .getId()) {
                lobby.getGame().getExecutor().setMove(move);
              }
            }
          } else {
            IRequest request = (IRequest) o;
            CommandFactory commandFactory = new CommandFactory(this);
            Command c = commandFactory.getCommand(request);
            Invoker invoker = new Invoker(c);
            invoker.call();
          }
        } else {
          log.error("Nachricht konnte nicht gelesen werden");
        }

      }
    } catch (ClassNotFoundException e) {
      log.error("Klasse wurde nicht gefunden", e);
    } catch (SocketException ex) {
      if (this.user != null) {
        log.error("User " + this.user.getUsername() + " hat die Verbindung unerwartet beendet");
      } else {
        log.error("Client hat die Verbindung unerwartet beendet");
      }
    } catch (IOException ex) {
      log.error("Ein Fehler ist aufgetreten", ex);
    } finally {
      if (this.isLoggedIn() && lobbies != null) {
        for (Lobby lobby : lobbies) {
          lobby.leave(user);
        }
      }
      if (this.user != null) {
        this.user = null;
      }
      this.server.sendToAll(server.getLoggedUsers());
    }
    this.server.removeClient(this);

  }

  public void send(Event event) {
    if (this.out != null) {
      log.debug(
          "Nachricht wird gesendet an " + (user != null ? user.getUsername() : "[UNLOGGED USER]")
              + ": {" + event.getClass().getSimpleName() + "}", event);
      try {
        this.out.writeObject(event);
        this.out.flush();
      } catch (IOException e) {
        log.error("IO-Fehler beim senden vom Event", e);
      }
    }
  }

  public void addLobby(Lobby lobby) {
    this.lobbies.add(lobby);
  }

  public Lobby getLobbyByID(int lobbyId) {
    for (Lobby lobby : lobbies) {
      if (lobby.getLobbyID() == lobbyId) {
        return lobby;
      }
    }
    return null;
  }

  public boolean isLoggedIn() {
    return this.user != null;
  }

  public User getUser() {
    return this.user;
  }

  public List<Lobby> getLobbies() {
    return lobbies;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Thread getThread() {
    return Thread.currentThread();
  }

  public ClientAPI getClientAPI() {
    return this.clientAPI;
  }

  public Server getServer() {
    return this.server;
  }
}