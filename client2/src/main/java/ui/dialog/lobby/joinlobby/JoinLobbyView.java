package ui.dialog.lobby.joinlobby;

import com.google.common.eventbus.EventBus;
import connection.Connection;
import data.lobby.CommonLobby;
import helper.fxml.GenerateFXMLView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import ui.app.main.lobbylist.LobbyTableData;

import java.net.URL;
import java.util.ResourceBundle;

public class JoinLobbyView implements IJoinLobbyView {

  @FXML
  private ResourceBundle resources;

  @FXML
  private URL location;

  @FXML
  private GridPane joinLobbyRoot;

  @FXML
  private TextField lobbyNameField;

  @FXML
  private PasswordField passwordField;

  @FXML
  private Label statusMessageLabel;

  private final JoinLobbyPresenter presenter;
  private final EventBus eventBus;

  // Own Parent
  private Parent myParent;

  public JoinLobbyView(EventBus eventBus, LobbyTableData lobbydata, Connection connection) {
    this.eventBus = eventBus;
    this.presenter = new JoinLobbyPresenter(this, lobbydata, eventBus, connection);
    bind();
    initOwnView();
  }

  private void bind() {
    eventBus.register(this);
  }

  @Override
  public void initOwnView() {
    if (this.myParent == null)
      this.myParent = GenerateFXMLView.getINSTANCE().loadView("/ui/fxml/dialog/joinlobby/JoinLobbyView.fxml", this, eventBus);

    this.lobbyNameField.setText("[" + presenter.getLobbyData().getLobbyId() + "] - " + presenter.getLobbyData().getName());
  }

  @FXML
  void initialize() {
    this.lobbyNameField.requestFocus();
  }

  @FXML
  public void handleJoinButtonAction(ActionEvent event) {
    this.presenter.joinLobby(this.passwordField.getText());
  }

  @Override
  public void updateStatusLabel(String m) {
    this.statusMessageLabel.setText(m);
  }

  @Override
  public String getTitle() {
    return "JoinLobby";
  }

  @Override
  public Parent getRootParent() {
    return this.myParent;
  }
}
