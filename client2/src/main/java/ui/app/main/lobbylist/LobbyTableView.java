package ui.app.main.lobbylist;

import com.google.common.eventbus.EventBus;
import connection.Connection;
import data.user.User;
import data.lobby.Lobby;
import helper.fxml.GenerateFXMLView;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import mvp.view.INavigateableView;
import mvp.view.ShowViewEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class LobbyTableView implements ILobbyTableView {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox lobbyListViewRoot;

    @FXML
    private TableView lobbyTableView;

    @FXML
    private TableColumn<Lobby, String> tableColumnId;

    @FXML
    private TableColumn<Lobby, String> tableColumnName;

    @FXML
    private TableColumn<Lobby, String> tableColumnBelegung;



    private Parent myParent;

    private final INavigateableView parentView;
    private final LobbyTablePresenter presenter;
    private final EventBus eventBus;

    public LobbyTableView(INavigateableView parentView, EventBus eventBus, Connection connection, User user) {
        this.parentView = parentView;
        this.presenter = new LobbyTablePresenter(this, eventBus, connection, user);
        this.eventBus = eventBus;
        initOwnView();
    }

    @FXML
    void initialize() {
        setLobbyListViewData(this.presenter.getLobbyList());
    }


    @Override
    public void initOwnView() {
        if (this.myParent == null){
            this.myParent = GenerateFXMLView.getINSTANCE().loadView("/ui/fxml/app/main/lobbyList/LobbyListView.fxml", this, eventBus);

            this.tableColumnId.setCellValueFactory(new PropertyValueFactory<Lobby, String>("lobbyID"));
            this.tableColumnName.setCellValueFactory(new PropertyValueFactory<Lobby, String>("name"));
            this.tableColumnBelegung.setCellValueFactory(new PropertyValueFactory<Lobby, String>("belegung"));

            TableColumn<Lobby, Lobby> tableColumnJoinButton;
            tableColumnJoinButton = new TableColumn<>("");
            tableColumnJoinButton.setMinWidth(40);
            tableColumnJoinButton.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
            tableColumnJoinButton.setCellFactory(param -> new TableCell<Lobby, Lobby>() {
                private final Button joinButton = new Button("Join");

                @Override
                protected void updateItem(Lobby lobby, boolean empty) {
                    super.updateItem(lobby, empty);

                    if (lobby == null) {
                        setGraphic(null);
                        return;
                    }

                    setGraphic(joinButton);
                    joinButton.setOnAction(event -> System.out.println("Push the Button"));
                }
            });

            this.lobbyTableView.getColumns().add(tableColumnJoinButton);
        }
    }


    public LobbyTablePresenter getPresenter(){
        return this.presenter;
    }

    @FXML
    private void handleLobbyViewClick(MouseEvent click){
        System.out.println("Lobby doubleclick");
        Lobby selectedLobby = (Lobby) this.lobbyTableView.getFocusModel().getFocusedItem();

        if (click.getClickCount() == 2) {
            this.presenter.joinLobby(selectedLobby.getLobbyId(),"");;
        }
    }

    @FXML
    private void handleCreateLobbyButton(ActionEvent event) {

    }

    @Override
    public INavigateableView getParentView() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public ShowViewEvent getEventToShowThisView() {
        return null;
    }

    @Override
    public Parent getRootParent() {
        return this.myParent;
    }

    @Override
    public void setLobbyListViewData(ObservableList<Lobby> datasource){
        this.lobbyTableView.setItems(datasource);
    }

    @Override
    public void joinLobby() {

    }
}
