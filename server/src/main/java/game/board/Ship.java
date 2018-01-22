package game.board;


public class Ship {
  private int id;
  private int size;
  private int minimumStones;
  private Stone[] stones;
  private boolean docked;

  public Ship(int id, int size) {
    this.id = id;
    this.size = size;
    this.docked = false;
    this.minimumStones = Math.max(size-1, 1);
    stones = new Stone[size];
  }

  public int getId() {
    return id;
  }

  public boolean isDocked() {
    return docked;
  }

  /**
   *
   * @return minimum number of stones needed on the ship to sail
   */
  public int getMinimumStones() {
    return minimumStones;
  }

  public void setDocked(boolean docked) {
    this.docked = docked;
  }

  public Stone[] getStones() {
    return stones;
  }

  public boolean addStone(Stone stone, int position) {
    if (docked || (position > size || (stones.length > position && stones[position] != null))) return false;
    stones[position] =  stone;
    return true;
  }
}