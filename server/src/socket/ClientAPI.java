package socket;

import json.ServerCommands;
import org.json.simple.JSONObject;
import user.UserManager;

public class ClientAPI {

  private UserManager userManager;

  public ClientAPI() {
    this.userManager = new UserManager();
  }

  /**
   * Wenn Client eine Login-Anfrage sendet, werden die Logindaten
   * via validateLogin überprüft. Wenn die Daten korrekt sind, wird
   * eine Erfolgsnachricht an den Client gesendet.
   * Wenn Logindaten inkorrekt sind, wird eine Fehlermeldung an den
   * Client gesendet.
   *
   * @param request JSON-Objekt, das User-Daten für Login enthält
   * @return JSON-Objekt, das entweder Erfolg oder Misserfolg als Nachricht enthält
   */
  public JSONObject login(JSONObject request) {
    JSONObject response = new JSONObject();

    if (request.containsKey("username") && request.containsKey("password")) {
      String username = (String) request.get("username");
      String password = (String) request.get("password");

      boolean isLoginValid = this.userManager.validateLogin(username, password);

      if (isLoginValid) {
        response = ServerCommands.loginCommand("Login erfolgreich!", true);
      } else {
        response = ServerCommands
            .loginCommand("Login fehlgeschlagen: Username oder Passwort inkorrekt", false);
      }
    } else {
      response = ServerCommands.loginCommand("Login fehlgeschlagen: Ungültige Anfrage", false);
    }
    return response;
  }

  /**
   * Wenn Client eine Registrierungs-Anfrage sendet, versucht der UserManager
   * einen neuen Benutzer anzulegen. Wenn die Erstellung erfolgreich war, wird
   * eine Erfolgsnachricht an den Client gesendet.
   * Wenn die Erstellung nicht erfolgreich war, wird eine Fehlermeldung an den
   * Client gesendet.
   *
   * @param request JSON-Objekt, das User-Daten für Registrierung enthält
   * @return JSON-Objekt, das entweder Erfolg oder Misserfolg als Nachricht enthält
   */
  public JSONObject register(JSONObject request) {
    JSONObject response;

    if (request.containsKey("username") && request.containsKey("password") && request
        .containsKey("email")) {
      String username = (String) request.get("username");
      String password = (String) request.get("password");
      String email = (String) request.get("email");

      boolean createUser = this.userManager.createUser(username, password, email);

      if (createUser) {
        response = ServerCommands.registerCommand("Registrierung erfolgreich!", true);
      } else {
        response = ServerCommands
            .registerCommand("Registrierung fehlgeschlagen: Username oder E-Mail existiert bereits",
                false);
      }
    } else {
      response = ServerCommands
          .registerCommand("Registrierung fehlgeschlagen: Ungültige Anfrage", false);
    }
    return response;
  }
}