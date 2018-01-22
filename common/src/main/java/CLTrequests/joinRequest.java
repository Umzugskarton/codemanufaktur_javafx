package CLTrequests;

import java.util.Date;

/**
 * Created on 13.10.2017.
 */
public class joinRequest implements Request {
    private String request = "join";
    private Date date;
    private int id;
    private String pw;

    public joinRequest() {

    }

    public joinRequest(int id, String password) {
        this.id = id;
        this.pw = password;
        this.date = new Date();
    }

    public String getType() {
        return this.request;
    }

    public int getId() {
        return this.id;
    }

    public String getPassword() {
        return this.pw;
    }

    public Date getDate() {
        return this.date;
    }

}