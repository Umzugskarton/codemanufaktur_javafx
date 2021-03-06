package data.user;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

  private int id;
  private String username;
  private String passwordHash;
  private String email;
  private float sessionid;

  /**
   * Erstellt ein neues User-Objekt
   *
   * @param id ID des Users
   * @param username Name des Users
   * @param passwordHash Gehashtes Passwort des Users
   * @param email E-Mail des Users
   */
  public User(int id, String username, String passwordHash, String email) {
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.email = email;
    this.sessionid = new Date().hashCode() * this.id;
  }

  /*
   * Getter und Setter
   */
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public float getSessionid() {
    return sessionid;
  }
}