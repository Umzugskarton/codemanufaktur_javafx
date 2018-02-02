package ui.app.game.board.sites.Obelisks;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import connection.Connection;
import data.lobby.CommonLobby;
import events.app.game.ShipDockedEvent;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mvp.presenter.Presenter;
import ui.app.game.board.sites.ISitePresenter;
import ui.app.game.board.sites.ISiteView;
import java.util.ArrayList;


public class ObelisksPresenter extends Presenter<ISiteView> implements ISitePresenter {
  private final Connection connection;
  private CommonLobby lobby;


  public ObelisksPresenter(ISiteView view, EventBus eventBus, Connection connection, CommonLobby lobby) {
    super(view, eventBus);
    this.connection = connection;
  }


  @Subscribe
  public void setStones(ShipDockedEvent e) {
    int[] playerStones = new int[lobby.getUsers().size()];
    for (Integer stone : e.getNewstones()){
      playerStones[stone]++;
    }
    int k = 0;
    for (int player : playerStones) {
      ArrayList<Group> stoneGroups = getView().getStones();
      int playerroot = (player*5);
      for (int i = 0; i < player; i++) {
        stoneGroups.get(playerroot+i).setVisible(true);
        Rectangle r = getView().getColorStones(playerroot+i);
        r.setFill(Color.web(lobby.getUserbyLobbyId(k).getColor()));
      }
      k++;
    }
  }
}