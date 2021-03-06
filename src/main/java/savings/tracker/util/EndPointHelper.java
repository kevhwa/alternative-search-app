package savings.tracker.util;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import savings.tracker.util.DatabaseJdbc;
import savings.tracker.Item;
import savings.tracker.Message;
import savings.tracker.OngoingTask;
import savings.tracker.PurchaseRecord;
import savings.tracker.User;

public class EndPointHelper {

  private static int ALTERNATIVE_NUMBER = 10;

  /**
   * No Alternative Found.
   * 
   * @throws SQLException Exception
   */
  public static Message noAlternativeHelper(DatabaseJdbc database, String id) {

    // delete ongoing task
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    List<Item> emptyList = new ArrayList<Item>();
    Item emptyItem = new Item();
    OngoingTask task = new OngoingTask(id, "", timestamp, emptyList, emptyItem,
        0, 0, emptyList, emptyItem, 0, 0);

    try {
      DatabaseJdbc.addTask(database, "Task", task);
      DatabaseJdbc.removeSearch(database, "Search", id + "1");
      DatabaseJdbc.removeSearch(database, "Search", id + "2");
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return new Message(200, "No Alternative");
  }

  /**
   * No Alternative Found.
   * 
   * @throws SQLException Exception
   */
  public static OngoingTask resumeHelper(DatabaseJdbc database, String id) {

    OngoingTask task = new OngoingTask();
    try {
      task = DatabaseJdbc.getTask(database, "Task", "Search", "Item", id);
      User user = DatabaseJdbc.getUser(database, "User", id);
      task.setUserLat(user.getLat());
      task.setUserLon(user.getLon());
    } catch (SQLException e1) {
      e1.printStackTrace();
    }
    return task;
  }

  /**
   * Search Item by string. creates both search items and alternative items
   * search items are items with exact match alternative items are items with
   * some but not all match
   * 
   * @param item name of item
   * @param lat  of user
   * @param lon  of user
   * @throws InterruptedException when sql errors
   */
  public static List<Item> searchItemHelper(DatabaseJdbc database, String item,
      String id, double lat, double lon) throws InterruptedException {

    // updates user location data
    try {
      User user = DatabaseJdbc.getUser(database, "User", id);
      user.setLat(lat);
      user.setLon(lon);
      DatabaseJdbc.updatesUser(database, "User", user);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    System.out.print(id);
    System.out.print(item);
    // save to on going task
    OngoingTask currentTask = new OngoingTask();
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    currentTask.setTaskStartTime(timestamp);
    currentTask.setUserId(id);
    currentTask.setSearchString(item);
    try {
      DatabaseJdbc.addTask(database, "Task", currentTask);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // search for item
    // TODO implement this part after rapid api

    // save to ongoing task
    currentTask.setInitialItem(new Item());
    currentTask.setInitialLat(0);
    currentTask.setInitialLon(0);
    currentTask.setFinalItem(new Item());
    currentTask.setFinalLat(0);
    currentTask.setFinalLon(0);
    List<List<Item>> list = WegmanApi.getItems(database, "Store", item, lat,
        lon);

    if (list.size() == 2) {
      // trader joe same as wegman
      List<Item> trader = Walmart.getItems(database, "Store", list.get(0), lat,
          lon, "Trader Joes");
      list.get(0).addAll(trader);
      List<Item> trader2 = Walmart.getItems(database, "Store", list.get(1), lat,
          lon, "Trader Joes");
      list.get(1).addAll(trader2);
    }
    // get target items
    int zip = TargetApi.getZip(lat, lon);
    List<List<Item>> target = TargetApi.getItem(zip, item);

    // add to list
    if (list.size() != 2) {
      list = target;
    } else {
      if (target.size() == 2) {
        list.get(0).addAll(target.get(0));
        list.get(1).addAll(target.get(1));
      } else if (target.size() == 1) {
        list.get(0).addAll(target.get(0));
      }
    }

    if (target.size() == 2) {
      // walmart same as target
      List<Item> walmart = Walmart.getItems2(database, "Store", target.get(0),
          lat, lon, "Walmart");
      list.get(0).addAll(walmart);
      List<Item> walmart2 = Walmart.getItems2(database, "Store", target.get(1),
          lat, lon, "Walmart");
      list.get(1).addAll(walmart2);
    }

    if (list.size() == 0) {
      currentTask.setSearchItems(new ArrayList<Item>());
      currentTask.setAlternativeItem(new ArrayList<Item>());

      System.out.println("\n search returned 0 results\n");
      System.out.flush();

    } else {
      // sort all
      List<Item> sorted = Walmart.sortItemByDistance(lat, lon, list.get(0));
      currentTask.setSearchItems(sorted);
      List<Item> sorted2 = Walmart.sortItemByDistance(lat, lon, list.get(1));
      currentTask.setAlternativeItem(sorted2);

    }

    try {
      DatabaseJdbc.removeSearch(database, "Search", id + "1");
      DatabaseJdbc.removeSearch(database, "Search", id + "2");
      DatabaseJdbc.addSearch(database, "Search", "Item",
          currentTask.getSearchItems(), id + "1");
      DatabaseJdbc.addSearch(database, "Search", "Item",
          currentTask.getAlternativeItem(), id + "2");
      DatabaseJdbc.addTask(database, "Task", currentTask);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return currentTask.getSearchItems();
  }

  /**
   * Select Item.
   * 
   * @param itemNumber number of item chosen
   * @throws SQLException Exception
   */
  public static Message selectItemHelper(DatabaseJdbc database, int itemNumber,
      String id) {
    OngoingTask task = new OngoingTask();
    try {
      task = DatabaseJdbc.getTask(database, "Task", "Search", "Item", id);
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    // ongoing task
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    task.setTaskStartTime(timestamp);
    List<Item> itemList = task.getSearchItems();
    task.setInitialItem(itemList.get(itemNumber));
    task.setInitialLat(Double.valueOf(task.getInitialItem().getLat()));
    task.setInitialLon(Double.valueOf(task.getInitialItem().getLon()));

    System.out.println("\n setting initial item\n");
    System.out.flush();

    System.out
        .println("\n initial item name" + task.getInitialItem().getName());
    System.out.flush();

    System.out.println(
        "\n initial item barcode" + task.getInitialItem().getBarcode() + "\n");
    System.out.flush();

    // save to ongoing task
    try {
      // DatabaseJdbc.removeSearch(database, "Search", id + "2");
      DatabaseJdbc.addTask(database, "Task", task);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return new Message(200, task.getInitialItem().getName());
  }

  /**
   * Search Item by string. creates both search items and alternative items
   * search items are items with exact match alternative items are items with
   * some but not all match
   * 
   * @param lat of user
   * @param lon of user
   * @throws SQLException Exception
   */
  public static List<Item> searchAlternativeItemHelper(DatabaseJdbc database,
      String id, double lat, double lon) {

    int zip = TargetApi.getZip(lat, lon);
    System.out.println("LAT: " + lat + " LON: " + lon);
    System.out.println("ZIP : " + zip);

    List<Item> result = new ArrayList<Item>();

    // get task info from table
    OngoingTask currentTask = new OngoingTask();
    try {

      currentTask = DatabaseJdbc.getTask(database, "Task", "Search", "Item",
          id);
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    // save to on going task
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    currentTask.setTaskStartTime(timestamp);
    if (currentTask.getInitialItem().getBarcode() == null) {
      System.out.println("\n no initial barcode\n");
      System.out.flush();
      return result;
    }
    currentTask.setFinalItem(new Item());
    currentTask.setFinalLat(0);
    currentTask.setFinalLon(0);

    result = currentTask.getAlternativeItem();

    if (result.size() < ALTERNATIVE_NUMBER) {
      // search for more item
      // TODO implement this part after rapid api
      List<Item> wegman = WegmanApi.getAlternativeItems(database, "Store",
          currentTask.getSearchString(), lat, lon,
          currentTask.getInitialItem().getPrice());
      List<Item> walmart = new ArrayList<Item>();
      List<Item> trader = new ArrayList<Item>();
      //List<Item> target = TargetApi.getSecTargetAlternatives(zip,
         // currentTask.getSearchString());
      List<Item> target = TargetApi.getTargetAlternatives(zip, wegman);
      try {
        walmart = Walmart.getItems2(database, "Store", target, lat, lon,
            "Walmart");
        trader = Walmart.getItems(database, "Store", wegman, lat, lon,
            "Trader Joes");
        // target = TargetApi.getSecTargetAlternatives(zip, result);

        result.addAll(walmart);
        result.addAll(trader);

      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      result = wegman;
      result.addAll(walmart);
      result.addAll(trader);
      result.addAll(target);

    }

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // save to ongoing task
    currentTask.setAlternativeItem(result);
    try {
      DatabaseJdbc.removeSearch(database, "Search", id + "2");
      DatabaseJdbc.addSearch(database, "Search", "Item", result, id + "2");
      DatabaseJdbc.addTask(database, "Task", currentTask);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return currentTask.getAlternativeItem();
  }

  /**
   * Filter Alternatives based on toggle switch.
   * 
   * @param lat          of user
   * @param lon          of user
   * @param alternatives list of alternatives
   * @param initialItem  initial chosen item
   * @throws SQLException Exception
   */
  public static List<Item> filterAlternativeItem(double lat, double lon,
      List<Item> alternatives, Item initialItem) {

    List<Item> result = new ArrayList<Item>();

    // check if there are alternatives
    if (alternatives.size() != 0) {
      // deep copy
      // non empty alternatives, check if there is alt requirement
      for (int i = 0; i < alternatives.size(); i++) {
        if (WegmanApi.isMustbecheaper() == false
            || alternatives.get(i).getPrice() <= initialItem.getPrice()) {
          // no cheaper requirement or yes cheaper requirement but cheaper

          // check distance reuirement
          double lat1 = alternatives.get(i).getLat(); // alt
          double lon1 = alternatives.get(i).getLon();
          double lat2 = initialItem.getLat(); // initial
          double lon2 = initialItem.getLon();
          // lat lon user
          double distance1 = WegmanApi.getDistance(lat, lon, lat1, lon1);
          double distance2 = WegmanApi.getDistance(lat, lon, lat2, lon2);
          if (WegmanApi.isMustbecloser() == false || distance1 <= distance2) {
            // no distance requirement or yes but closer

            String barcode1 = initialItem.getBarcode();
            String barcode2 = alternatives.get(i).getBarcode();
            boolean locationNotMatch = ((lat1 != lat2) || (lon1 != lon2));
            // check same item requirement
            if (WegmanApi.isMustbesameitem() == false
                || (barcode2.equals(barcode1) && locationNotMatch)) {
              // no same item requirement or yes but same item but different
              // location
              result.add(new Item(alternatives.get(i)));
            }
          }
        }
      }
    }

    return Walmart.sortItemByDistance(lat, lon, result);
  }

  /**
   * Select Final Item.
   * 
   * @param barcode unique barcode/upc of item chosen
   * @param lat     of store loaction
   * @param lon     of store location
   * @throws SQLException Exception
   */

  public static Message selectPurchaseHelper(DatabaseJdbc database,
      String barcode, String lat, String lon, String id) {

    OngoingTask task = new OngoingTask();
    Item finalItem = new Item();
    try {
      task = DatabaseJdbc.getTask(database, "Task", "Search", "Item", id);

      finalItem = DatabaseJdbc.getItem(database, "Item", barcode,
          Double.valueOf(lat), Double.valueOf(lon));
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    // ongoing task
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    task.setTaskStartTime(timestamp);
    task.setFinalItem(finalItem);
    task.setFinalLat(Double.valueOf(lat));
    task.setFinalLon(Double.valueOf(lon));

    // save to ongoing task
    try {
      DatabaseJdbc.addTask(database, "Task", task);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return new Message(200, task.getFinalItem().getName());
  }

  /**
   * Confirm Final puchasing Item.
   * 
   * @throws SQLException Exception
   */
  public static Message confirmPurchaseHelper(DatabaseJdbc database,
      String id) {
    // sleep for 5s to prevent race condition

    User user = new User();
    OngoingTask task = new OngoingTask();
    try {
      user = DatabaseJdbc.getUser(database, "User", id);
      task = DatabaseJdbc.getTask(database, "Task", "Search", "Item", id);
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    // ongoing task
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    task.setTaskStartTime(timestamp);
    Item finalItem = task.getFinalItem();
    Item initialItem = task.getInitialItem();
    if (initialItem.getBarcode() == null) {
      return new Message(201, user.getName() + " haven't chose any items yet!");
    }

    if (finalItem.getBarcode() == null) {
      return new Message(202,
          user.getName() + " haven't added any items to cart yet!");
    }

    // after using clear task to avoid multiple purchase
    task.setInitialItem(new Item());
    task.setFinalItem(new Item());
    task.setSearchString("");
    task.setInitialLat(0.0);
    task.setInitialLon(0.0);
    task.setFinalLat(0.0);
    task.setFinalLon(0.0);

    // calculate savings
    double saving = 0;
    saving = initialItem.getPrice() - finalItem.getPrice();
    saving = Math.max(saving, 0);
    // save to user info
    try {
      DatabaseJdbc.removeSearch(database, "Search", id + "1");
      DatabaseJdbc.removeSearch(database, "Search", id + "2");
      DatabaseJdbc.addTask(database, "Task", task);
      user.setSavings(user.getSavings() + saving);
      DatabaseJdbc.updatesUser(database, "User", user);

      LocalDate currentDate = LocalDate.now();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      String strCurrentDate = currentDate.format(formatter);
      DatabaseJdbc.addPurchaseData(database, "Purchase", finalItem,
          user.getUserId(), saving, strCurrentDate);

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return new Message(200,
        user.getName() + " purchased " + finalItem.getName() + " with $"
            + String.valueOf(finalItem.getPrice()) + " instead of "
            + initialItem.getName() + "with $" + initialItem.getPrice()
            + ". \n Good Job! You saved $" + String.valueOf(saving)
            + " on this purchase! \n You have accumulated $" + user.getSavings()
            + " so far!");
  }

  public static Message sendEmailHelper(DatabaseJdbc database, String id,
      String email) {

    String totalSavings = "0";
    String weeklySavings = "0";
    boolean res = false;

    try {
      User user = DatabaseJdbc.getUser(database, "user", id);
      System.out.println("user id = " + user.getUserId());
      double totalSavingsDouble = user.getSavings();
      List<PurchaseRecord> list = DatabaseJdbc.getPurchaseData(database,
          "Purchase", id);
      double weeklySavingsDouble = DatabaseJdbc.getWeekSavings(list);

      System.out.println("list size: " + list.size());

      for (int i = 0; i < list.size(); i++) {
        System.out.println(list.get(i).getSaving());
      }

      NumberFormat formatter = NumberFormat.getCurrencyInstance();

      totalSavings = formatter.format(totalSavingsDouble);
      weeklySavings = formatter.format(weeklySavingsDouble);

    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      res = SendGridEmailer.sendDynamicEmail(email, weeklySavings,
          totalSavings);

    } catch (IOException e) {
      e.printStackTrace();
    }

    if (res) {
      return new Message(200, "Email successfully sent to " + email);
    }

    return new Message(202, "Email couldn't be sent");
  }
}
