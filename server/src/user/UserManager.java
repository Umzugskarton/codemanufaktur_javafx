package user;
import java.sql.*;

import org.apache.commons.codec.digest.DigestUtils;

import database.DBController;

public class UserManager {

  // Datenbankcontroller
  private DBController dbController;

  /**
   * Erstellt ein neues UserManager-Objekt
   * Stellt eine Verbindung zur DB her
   */
  public UserManager() {
    this.dbController = new DBController();
    this.dbController.connect();
  }

  /**
   * Legt einen neuen User-Eintrag in der Datenbank an, wenn weder Username noch E-Mail vergeben
   * sind
   *
   * @param   password  Passwort des Users aus Registrierung
   * @param   username  Name des Users aus Registrierung
   * @param   email     E-Mail des Users aus Registrierung
   * @return  true, wenn User erfolgreich angelegt wurde
   */
  public boolean createUser(String username, String password, String email) {
    // Wenn weder Username noch E-Mail in der Datenbank vorhanden sind
    if (!this.userExists(UserIdentifier.USERNAME, username) && !this.userExists(UserIdentifier.EMAIL, email)) {
      // Passwort hashen
      String hashedPassword = DigestUtils.md5Hex(password);
      PreparedStatement stmt = null;

      try {
        // User in der Datenbank anlegen
        String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        stmt = this.dbController.getConnection().prepareStatement(query);
        stmt.setString(1, username);
        stmt.setString(2, hashedPassword);
        stmt.setString(3, email);
        int result = stmt.executeUpdate();

        // Wenn User erfolgreich angelegt wurde, true zur�ckgeben
        if (result == 1) {
          System.out.println("User \"" + username + "\" wurde erfolgreich angelegt.");
          return true;
        }
      } catch (SQLException se) {
        // TODO Auto-generated catch block
        se.printStackTrace();
      } finally {
        try {
          // Statement freigeben
          if (stmt != null) {
            stmt.close();
          }
        } catch (SQLException se) {
          // TODO Auto-generated catch block
          se.printStackTrace();
        }
      }
    }

    return false;
  }

  /**
   * Löscht einen User-Eintrag aus der Datenbank
   *
   * @param   identifier  Ein einzigartiger Identifier (z.B. UserIdentifier.ID), anhand dessen der User ermittelt wird
   * @param   value       Wert des Identifiers
   * @return  true, wenn User gelöscht wurde
   */
  public boolean deleteUser(UserIdentifier identifier, Object value) {
    // Wenn User in der Datenbank existiert
    if (this.userExists(identifier, value)) {
      PreparedStatement stmt = null;
      int result = 0;

      try {
        // User aus der Datenbank löschen
        String query = "DELETE FROM users WHERE " + identifier.getColumnName() + " = ?";
        stmt = this.dbController.getConnection().prepareStatement(query);
        stmt.setObject(1, value);
        result = stmt.executeUpdate();

        // Wenn User erfolgreich gelöscht wurde, true zur�ckgeben
        if (result == 1) {
          System.out.println("User \"" + value.toString() + "\" (" + identifier.getColumnName() + ") wurde erfolgreich gelöscht.");
          return true;
        }
      } catch (SQLException se) {
        // TODO Auto-generated catch block
        se.printStackTrace();
      } finally {
        try {
          // Statement und ResultSet freigeben
          if (stmt != null) {
            stmt.close();
          }
        } catch (SQLException se) {
          // TODO Auto-generated catch block
          se.printStackTrace();
        }
      }
    }

    return false;
  }

  /**
   * Ändert Email oder Passwort eines Users in der Datenbank
   *
   * @param  user         User-Objekt des Users, dessen Passwort geändert werden soll
   * @param  identifier   Ein änderbarer Identifier (z.B. UserIdentifier.EMAIL), d.h. die Spalte die geändert werden soll
   * @param  value        Der neue Wert der Spalte
   * @return true, wenn User gelöscht wurde
   */
  public boolean changeUser(User user, UserIdentifier identifier, Object value) {
    // Wenn Username in der Datenbank existiert
    int id = user.getId();

    if (this.userExists(UserIdentifier.ID, id)) {
      // Wenn das zu ändernde Objekt eine Spalte ist die nicht geändert werden darf, abbrechen
      if(!identifier.isChangeable()) {
        System.out.println(identifier.getColumnName() + " des Users mit der ID " + id + " konnte nicht geändert werden, da diese Spalte nicht geändert werden darf.");
        return false;
      }

      // Wenn das zu ändernde Objekt die Email ist, prüfen ob diese noch nicht vergeben ist
      if(identifier.equals(UserIdentifier.EMAIL) && userExists(UserIdentifier.EMAIL, value)) {
        System.out.println(identifier.getColumnName() + " des Users mit der ID " + id + " konnte nicht geändert werden, da die gewünschte E-Mail bereits benutzt wird.");
        return false;
      }
      
      // Wenn das zu ändernde Objekt das Passwort ist, hashen
      if(identifier.equals(UserIdentifier.PASSWORD)) {
        value = DigestUtils.md5Hex((String) value);
      }

      PreparedStatement stmt = null;
      int result = 0;

      try {
        // Wert in der Datenbank ändern
        String query = "UPDATE users SET " + identifier.getColumnName() + " = ? WHERE id = ?";
        stmt = this.dbController.getConnection().prepareStatement(query);
        stmt.setObject(1, value);
        stmt.setInt(2, id);
        result = stmt.executeUpdate();

        // Wenn Wert erfolgreich geändert wurde, true zurückgeben
        if (result == 1) {
          System.out.println(identifier.getColumnName() + " von User \"" + id + "\" wurde erfolgreich geändert.");
          return true;
        }
      } catch (SQLException se) {
        // TODO Auto-generated catch block
        se.printStackTrace();
      } finally {
        try {
          // Statement und ResultSet freigeben
          if (stmt != null) {
            stmt.close();
          }
        } catch (SQLException se) {
          // TODO Auto-generated catch block
          se.printStackTrace();
        }
      }
    }

    return false;
  }

  /**
   * Erstellt und returned ein User-Objekt mit Datensatz aus Datenbank, wenn Username existiert
   *
   * @param  identifier  Ein einzigartiger Identifier (z.B. UserIdentifier.ID), anhand dessen der User ermittelt wird
   * @param  value       Wert des Identifiers
   * @return User-Objekt mit Daten aus der Datenbank oder Null-Objekt, falls kein User mit username
   * gefunden wird
   */
  public User getUser(UserIdentifier identifier, Object value) {
    // Zunächst überprüfen ob der User überhaupt mit diesem identifier ermittelt werden kann
    if(userExists(identifier, value)) {
      PreparedStatement stmt = null;
      ResultSet result = null;

      try {
        // Query vorbereiten und ausführen
        String query = "SELECT id, username, password, email FROM users WHERE " + identifier.getColumnName() + " = ? LIMIT 0,1";
        stmt = this.dbController.getConnection().prepareStatement(query);
        stmt.setObject(1, value);
        result = stmt.executeQuery();

        // Wenn result einen Eintrag hat (also der User existiert)
        if (this.dbController.countResults(result) == 1) {
          while (result.next()) {
            // Neues User-Objekt erstellen
            User user = new User(
                    result.getInt("id"),
                    result.getString("username"),
                    result.getString("password"),
                    result.getString("email")
            );
            // User-Objekt zurückgeben
            return user;
          }
        }
      } catch (SQLException se) {
        // TODO Auto-generated catch block
        se.printStackTrace();
      } finally {
        try {
          // Statement und ResultSet freigeben
          if (stmt != null) {
            stmt.close();
          }
          if (result != null) {
            result.close();
          }
        } catch (SQLException se) {
          // TODO Auto-generated catch block
          se.printStackTrace();
        }
      }
    } else {
      System.out.println("Es konnte kein User-Objekt erstellt werden, da der gesuchte User nicht existiert.");
    }
    return null;
  }

  /**
   * Validiert die Eingaben eines Users für Login
   *
   * @param  username  Username
   * @param  password  Passwort
   * @return true, wenn Logindaten korrekt sind
   */
  public boolean validateLogin(String username, String password) {
    // Passwort hashen
    String hashedPassword = DigestUtils.md5Hex(password);

    PreparedStatement stmt = null;
    ResultSet result = null;

    try {
      // Prüfen, ob Username und gehashtes PW in Datenbank existieren
      String query = "SELECT id, username, password, email FROM users WHERE username = ? AND password = ? LIMIT 0,1";
      stmt = this.dbController.getConnection().prepareStatement(query);
      stmt.setString(1, username);
      stmt.setString(2, hashedPassword);
      result = stmt.executeQuery();

      // Anzahl Einträge mit username und gehashtem Passwort
      int rowCount = this.dbController.countResults(result);

      // Wenn result einen Eintrag hat, also Daten stimmen, true zurückgeben, sonst false
      if (rowCount == 1) {
        return true;
      }
    } catch (SQLException se) {
      // TODO Auto-generated catch block
      se.printStackTrace();
    } finally {
      try {
        // Statement und ResultSet freigeben
        if (stmt != null) {
          stmt.close();
        }
        if (result != null) {
          result.close();
        }
      } catch (SQLException se) {
        // TODO Auto-generated catch block
        se.printStackTrace();
      }
    }

    return false;
  }


  /**
   * Prueft, ob ein User bereits in der Datenbank existiert
   *
   * @param   identifier  Ein einzigartiger Identifier (z.B. UserIdentifier.ID), anhand dessen der User ermittelt wird
   * @param   value       Wert des Identifiers
   * @return  true, wenn User existiert
   */
  private boolean userExists(UserIdentifier identifier, Object value) {
    // Prüfen, ob der Identifier einzigartig ist (d.h. so dass z.B. nicht über das Passwort die Existenz eines Users überprüft wird)
    if(identifier.isUnique()) {
      return this.dbController.exists("users", identifier.getColumnName(), value);
    } else {
      System.out.println("Es konnte nicht überprüft werden, ob der User existiert, da die Spalte " + identifier.getColumnName() + " nicht einzigartig ist.");
      return false;
    }
  }
}