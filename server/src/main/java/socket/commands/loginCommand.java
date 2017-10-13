package socket.commands;

import CLTrequests.Request;
import CLTrequests.loginRequest;
import SRVevents.loginEvent;
import socket.ClientAPI;
import socket.ClientListener;
import socket.Server;


public class loginCommand implements Command {
    private loginRequest request;
    private Request bare;
    private ClientListener clientListener;
    private Server server;
    private ClientAPI clientAPI;

    public loginCommand(ClientListener clientListener, Request bare){
        this.clientListener=clientListener;
        this.bare = bare;
        this.server=clientListener.getServer();
        this.clientAPI=clientListener.getClientAPI();
    }
    public void exec(){
        this.request =  (loginRequest) this.bare;
        loginEvent response = this.clientListener.getClientAPI().login(request, this.server.getLoggedUsers().getUserList());
        if (response.getSuccess()) {
            this.clientListener.setUser(this.clientAPI.getUser(request.getUsername()));
            this.server.sendToLoggedIn(this.server.getLoggedUsers());
        }
        if (response != null) {
            this.clientListener.send(response);
        }
    }
}
