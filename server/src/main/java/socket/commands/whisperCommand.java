package socket.commands;

import CLTrequests.Request;
import CLTrequests.whisperRequest;
import SRVevents.userNotFoundError;
import SRVevents.voidEvent;
import org.json.simple.JSONObject;
import socket.ClientAPI;
import socket.ClientListener;
import socket.Server;

public class whisperCommand implements Command {
    private Request bare;
    private whisperRequest request;
    private ClientListener clientListener;
    private Server server;
    private ClientAPI clientAPI;

    public whisperCommand(ClientListener clientListener, Request bare){
        this.clientListener=clientListener;
        this.bare = bare;
        this.server=clientListener.getServer();
        this.clientAPI=clientListener.getClientAPI();
    }
    public void exec() {
        this.request =(whisperRequest) this.bare;
        voidEvent response = null;
        String receiverUsername = this.server
                .getLoggedInUsername(request.getTo());
        if (receiverUsername != null) {
            this.server.sendTo(this.clientAPI.whisper(request, this.clientListener.getUser()), receiverUsername);
        } else {
            userNotFoundError error = new userNotFoundError();
            error.setMsg(request.getTo());
            response = error;
        }

        if (response != null) {
            this.clientListener.send(response);
        }
    }
}
