package game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import requests.gamemoves.Move;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MoveExecutor {

  private final Lock lock = new ReentrantLock();
  private final Condition notSet = lock.newCondition();
  private Move move;
  private final Logger log = LoggerFactory.getLogger(getClass().getName());
  static final int TURN_TIME = 20;
  private static final int TURN_TIME_BUFFER = 3;

  /**
   * Wartet TURN_TIME Sekunden auf die ausführung eines Spielzuges durch den aktuellen Spieler in einem Game Thread
   *
   */

  public void waitForMove() {
    move = null;
    lock.lock();
    try {
      notSet.await(TURN_TIME + TURN_TIME_BUFFER, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.error(e.getMessage());
    } finally {
      lock.unlock();
    }
  }

  public Move getMove() {
    return move;
  }

  /**
   * Bei Rückmeldung des Clients wird der Spielzug gesetzt und der wartende Thread aufgeweckt
   */

  public void setMove(Move move) {
    lock.lock();
    try {
      this.move = move;
      notSet.signalAll();
    } finally {
      lock.unlock();
    }
  }

}
