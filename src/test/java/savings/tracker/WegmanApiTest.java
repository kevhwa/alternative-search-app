package savings.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import savings.tracker.util.DatabaseJdbc;
import savings.tracker.util.WegmanApi;

@TestMethodOrder(OrderAnnotation.class)
public class WegmanApiTest {

  private static DatabaseJdbc database = new DatabaseJdbc();
  
  
  @Test
  @Order(1)
  public void testGetStore() throws Exception {
    Thread.sleep(20000);
    DatabaseJdbc.createStoreTable(database, "Store");
    
    List<Store> stores = WegmanApi.getStores();
    for (int i = 0; i < stores.size(); i++) {
      DatabaseJdbc.addStore(database, "Store", stores.get(i)); 
    }
    
    assert (stores.size() > 0);
  }
  
  @Test
  @Order(2)
  public void testGetNearestStore() throws Exception {
    List<Store> nearestStores = WegmanApi.getNearestStores(database, "Store", 37.7510,
        -97.8220, "Wegmans Store");
    
    assert (nearestStores.size() > 0);
  }
  
  @Test
  @Order(3)
  public void testGetSecondNearestStore() throws Exception {
    List<Store> secNearestStores = WegmanApi.getSecNearestStores(database, "Store", 37.7510,
        -97.8220, "Wegmans Store");
    
    assert (secNearestStores.size() > 0);
  }
  
  @Test
  @Order(4)
  public void testDistanceCalc()  {

    double dist = WegmanApi.getDistance(43.663, -72.368, 37.7510, -97.8220);

    assert (dist == 2233.713617103588);
  }
  
  @Test
  @Order(5)
  public void testEqualDistanceCalc()  {

    double dist = WegmanApi.getDistance(43.663, -72.368, 43.663, -72.368);
    
    assert (dist == 0);
  }
  
  @Test
  @Order(6)
  public void testGetItems() throws InterruptedException  {

    List<List<Item>> list = WegmanApi.getItems(database, "Store", "whole milk", 43.663,
        -72.368);
    
    assertEquals(list.size(), 2);
    //assert (list.get(0).size() > 0);
    //assert (list.get(1).size() > 0);
  }
  @Test
  @Order(7)
  public void sleepToAvoidlimit()  {
    try {
      Thread.sleep(40000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  @Test
  @Order(8)
  public void testGetAlternatives()  {


    List<Item> list = WegmanApi.getAlternativeItems(database, "Store",
        "whole milk", 43.663, -72.368, 20);
    
    assert (list.size() > 0);
  }
  @Test
  @Order(9)
  public void sleepToAvoidlimit2()  {
    try {
      Thread.sleep(40000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  @Test
  @Order(10)
  public void testInvalidGetItems() throws InterruptedException  {

 
    List<List<Item>> list = WegmanApi.getItems(database, "Store", "!!&[]", -99,
        -190);
    
    assertEquals(list.size(), 0);
    //assert (list.get(0).size() > 0);
    //assert (list.get(1).size() > 0);
  }
  
  @Test
  @Order(11)
  public void testInvalidDistanceCalc()  {

    double dist = WegmanApi.getDistance(-999,-999,-100,-200);

    assert (dist == -1);
  }
  
  @Test
  @Order(12)
  public void testGetInvalidNearestStore() throws Exception {
    List<Store> nearestStores = WegmanApi.getNearestStores(database, "Store", -99,
        -999, "!!!!");
    
    assert (nearestStores.size() == 0);
  }
  
  @Test
  @Order(13)
  public void testGetInvalidSecNearestStore() throws Exception {
    List<Store> nearestStores = WegmanApi.getSecNearestStores(database, "Store", -99,
        -999, "!!!!");
    
    assert (nearestStores.size() == 0);
  }

  @Test
  @Order(14)
  public void testGetInvalidAlternatives()  {


    List<Item> list = WegmanApi.getAlternativeItems(database, "Store",
        "!!!", -99, -999, -1);
    
    assert (list.size() == 0);
  }
  
  
}
