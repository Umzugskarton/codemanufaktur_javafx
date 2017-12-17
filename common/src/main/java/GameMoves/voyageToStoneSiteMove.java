package GameMoves;


import java.util.Date;

public class voyageToStoneSiteMove implements Move{
  private String move = "voyageToStoneSite";
  private int shipId;
  private String stonesite;

  public voyageToStoneSiteMove(){}

  public voyageToStoneSiteMove(int shipId, String stonesite){
    this.shipId = shipId;
    this.stonesite =stonesite;
  }

  public int getShipId() {
    return shipId;
  }

  public String getStonesite() {
    return stonesite;
  }

  public void setShipId(int shipId) {
    this.shipId = shipId;
  }

  public void setStonesite(String stonesite) {
    this.stonesite = stonesite;
  }

  @Override
  public String getType() {
    return move;
  }

  @Override
  public Date getDate() {
    return null;
  }
}
