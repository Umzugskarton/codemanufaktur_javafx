package lobby;

import data.lobby.LobbyUser;
import data.user.User;
import events.app.lobby.ChangeLobbyUserColorEvent;
import events.app.lobby.JoinLobbyEvent;
import events.app.lobby.LeaveLobbyEvent;
import events.app.lobby.SetReadyToPlayEvent;
import game.Game;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socket.ClientListener;

public class Lobby {

  private final Logger log = LoggerFactory.getLogger(getClass().getName());

  private String name;
  private int lobbyID;
  private User[] lobby;
  private boolean[] readyList;
  private String password = null;
  private boolean show = true;
  private int size;
  private boolean vacancy = true;
  private ArrayList<Integer> userColor = new ArrayList<>();
  private ArrayList<String> colors = new ArrayList<>();
  private Game game;

  public Lobby(int size, User host, String name, String password) {
    readyList = new boolean[size];
    this.lobby = new User[size];
    this.userColor.add(0);
    this.lobby[0] = host;
    this.name = name;
    this.size = size;
    this.password = password;
    generateColors();
  }

  public boolean isHost(User user) {
    return user == lobby[0];
  }

  private void swapHost() {
    for (int i = 0; i < lobby.length; i++) {
      if (lobby[i] != lobby[0]) {
        User temp = lobby[0];
        lobby[0] = lobby[i];
        lobby[i] = temp;
        break;
      }
    }
  }

  private void swapHost(User user) {
    if(user != null && user != lobby[0]) {
      User temp = lobby[0];
      lobby[0] = user;
      user = temp;
    }
  }

  public void startGame(ClientListener cl) {
    game = new Game(this, cl);
    Thread thread = new Thread(game);
    thread.start();
  }

  public Game getGame() {
    return game;
  }

  public boolean isShow() {
    return show;
  }

  public String getHostName() {
    if (lobby[0] == null) {
      return "Kein User vorhanden";
    } else {
      return this.lobby[0].getUsername();
    }
  }

  public String getName() {
    return this.name;
  }

  public JoinLobbyEvent join(User user, String password) {
    if (!this.password.equals(password)) {
      return new JoinLobbyEvent("Das Passwort ist falsch.", false);
    }
    if (vacancy) {
      for (int i = 0; i < lobby.length; i++) {
        if (lobby[i] == null) {
          lobby[i] = user;
          int c = i;
          while (userColor.contains(c)) {
            c++;
          }
          userColor.add(c);
          return new JoinLobbyEvent("Erfolgreich beigetreten!", true);
        }
      }
      this.vacancy = false;
    }
    return new JoinLobbyEvent("Die Lobby ist voll.", false);
  }

  public boolean[] getReady() {
    return readyList;
  }

  public boolean hasPW() {
    return !this.password.equals("");
  }

  public int getSize() {
    return this.size;
  }

  public ArrayList<String> getColors() {
    ArrayList<String> currentColors = new ArrayList<>();
    for (Integer user : userColor) {
      currentColors.add(colors.get(user));
    }
    return currentColors;
  }

  public void setLobbyID(int id) {
    this.lobbyID = id;
  }

  public int getLobbyID() {
    return this.lobbyID;
  }

  public User[] getUsers() {
    return this.lobby;
  }

  public String[] getUsersStringArray() {
    String[] j = new String[this.lobby.length];
    int i = 0;
    for (User user : this.lobby) {
      if (user != null) {
        j[i] = user.getUsername();
        i++;
      }
    }
    return j;
  }

  public SetReadyToPlayEvent setReady(User user) {
    int userid = Arrays.asList(lobby).indexOf(user);
    readyList[userid] = !readyList[userid];
    return new SetReadyToPlayEvent(readyList, lobbyID);
  }

  public ChangeLobbyUserColorEvent replaceColor(User user) {
    int userid = Arrays.asList(lobby).indexOf(user);
    int newcolor = userColor.get(userid);
    do {
      newcolor = (newcolor + 1) % 10;
    } while (userColor.contains(newcolor));
    userColor.set(userid, newcolor);
    return new ChangeLobbyUserColorEvent(userid, colors.get(newcolor));
  }

  public ArrayList<LobbyUser> getLobbyUserArrayList() {
    ArrayList<LobbyUser> temp = new ArrayList<>();
    int i = 0;
    for (User user : this.lobby) {
      if (user != null) {
        temp.add(new LobbyUser(user, colors.get(userColor.get(i)), readyList[i]));
        i++;
      }
    }
    return temp;
  }

  private int getUserCount() {
    int users = 0;
    for (User user : this.lobby) {
      if (user != null) {
        users++;
      }
    }
    return users;
  }

  private void generateColors() {
    float interval = 360 / 10;
    for (float x = 0; x < 360; x += interval) {
      Color c = Color.getHSBColor(x / 360, 1, 1);
      String hex = String.format("#%02x%02x%02x", (c.getRed() + 255) / 2, (c.getGreen() + 255) / 2,
          (c.getBlue() + 255) / 2);
      this.colors.add(hex);
    }
  }

  public void show(boolean show) {
    this.show = show;
  }

  public boolean isVisible() {
    return show;
  }

  public LeaveLobbyEvent leave(User user) {
    if (user == lobby[0] && getUserCount() != 1) {
      swapHost();
    }
    for (int i = 0; i < lobby.length; i++) {
      if (lobby[i] == user) {
        lobby[i] = null;
        userColor.remove(i);
        break;
      }
    }
    log.info(
        "[Lobby " + this.getLobbyID() + "] " + user.getUsername() + " hat die Lobby verlassen.");
    Arrays.fill(readyList, false);
    if (getUserCount() == 0) {
      //Lobby wird unsichtbar gesetzt, wenn alle diese verlassen haben
      this.show = false;
    }
    this.vacancy = true;
    return new LeaveLobbyEvent(true);
  }
}
