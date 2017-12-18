package board.presenter;


import GameEvents.GameInfoEvent;
import GameEvents.TurnEvent;
import board.model.TurnTimerThread;
import board.view.BoardViewImplFx;
import board.view.ShipViewImplFx;
import board.view.StorageViewImplFx;
import commonLobby.CLTLobby;
import commonLobby.LobbyUser;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import main.SceneController;
import org.apache.fop.fonts.FontType;

import java.io.IOException;
import java.util.ArrayList;


public class BoardPresenter {
    private BoardViewImplFx view;
    private SceneController sc;
    private CLTLobby lobby;
    private ArrayList<StoragePresenter> storagePresenters = new ArrayList<>();
    private ArrayList<ShipPresenter> shipPresenters = new ArrayList<>();

    //Board Variables
    private ArrayList<int[]> ships;
    private int round;
    private ArrayList<Integer> storages;
    private String[] order;
    private int turnTime;
    private Thread turnTimerThread;
    private TurnTimerThread turnTimer;

    // Konstruktor
    public BoardPresenter(BoardViewImplFx view, SceneController sc, CLTLobby legacy) {
        this.lobby = legacy;
        this.view = view;
        this.sc = sc;
        this.turnTime = 0;
        int render = 0;
        this.turnTimerThread = null;
        this.turnTimer = null;

        for (LobbyUser user : lobby.getUsers()) {
            try {
                AnchorPane root;
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/fxml/StorageView.fxml"));
                root = loader.load();
                StorageViewImplFx storage = loader.getController();
                StoragePresenter storagePresenter = new StoragePresenter(user, storage);
                storagePresenters.add(storagePresenter);
                view.addHouse(render, root);
                render++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Board View
    public BoardViewImplFx getView() {
        return view;
    }

    public void updateBoard(GameInfoEvent event) {
        storages = event.getStorages();
        if (ships == null) {
            ships = event.getShips();
            int render = 0;
            for (int[] ship : ships){
                try {
                    AnchorPane root;
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/fxml/ShipView.fxml"));
                    root = loader.load();
                    ShipViewImplFx shipView = loader.getController();
                    ShipPresenter shipPresenter = new ShipPresenter( lobby, shipView, ship);
                    shipPresenters.add(shipPresenter);
                    view.addShip(render, root);
                    render++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        round = event.getRound();
        order = event.getOrder();
        turnTime = event.getTurnTime();

        this.view.getRoundLabel().setText(round + " / 6");

        updateView();
    }

    private void updateView() {
        updateStorages();
    }

    private void updateStorages() {
        int i = 0 ;
        for (int stone : storages) {
            storagePresenters.get(i).setStoneCount(stone);
            i++;
        }
    }

    // Turns
    public void endTurn() {
        this.toggleUserInterface(false);
        this.stopTurnTimer();
    }

    public void newTurn(TurnEvent e) {
        // Buttons anzeigen, wenn Spieler aktuell an der Reihe ist
        this.toggleUserInterface(e.isMyTurn());

        this.view.getCurrentPlayerLabel().setText(e.getUsername());
        Stop[] stops = new Stop[] {
                new Stop(0, Color.web(lobby.getUserByName(e.getUsername()).getColor())),
                new Stop(1, Color.TRANSPARENT)};

        LinearGradient linearGradient =
                new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);

        this.view.getPlayerColorRectangle().setFill(linearGradient);

        // Aktuellen Spielernamen fettgedruckt anzeigen wenn der Client der aktuelle Spieler ist
        if(e.isMyTurn()) {
            this.view.getCurrentPlayerLabel().setFont(Font.font("Calibri", FontWeight.BOLD, 14));
        } else {
            this.view.getCurrentPlayerLabel().setFont(Font.font("Calibri", FontWeight.NORMAL, 14));
        }

        this.startTurnTimer();
    }

    // Timer
    private void startTurnTimer() {
        this.stopTurnTimer();

        this.turnTimerThread = new Thread(turnTimer = new TurnTimerThread(this, this.turnTime));
        this.turnTimerThread.start();
    }

    public void stopTurnTimer() {
        this.view.getTurnTimerProgress().setProgress(0.0);

        if(this.turnTimerThread != null) {
            this.turnTimer.forceEnd();
            this.turnTimer = null;
            this.turnTimerThread = null;
        }
    }

    public void updateTurnTimer(int seconds) {
        this.view.getTurnTimerProgress().setProgress((double) seconds / (double) turnTime);

        if(seconds <= 0) {
            this.endTurn();
        }
    }

    // User Interface
    public void toggleUserInterface(boolean show) {
        view.getUserInterface().setVisible(show);
    }

    public void fullscreen() {
        sc.toggleFullscreen();
    }
}
