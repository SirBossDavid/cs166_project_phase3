/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class AirlineManagement {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of AirlineManagement
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public AirlineManagement(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end AirlineManagement

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            AirlineManagement.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      AirlineManagement esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the AirlineManagement object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new AirlineManagement (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");

                //**the following functionalities should only be able to be used by Management**
                if(authorisedUser.equals("Management")){
                  System.out.println("1. View Flights");
                  System.out.println("2. View Flight Seats");
                  System.out.println("3. View Flight Status");
                  System.out.println("4. View Flights of the day");  
                  System.out.println("5. View Full Order ID History");
                  System.out.println("6. View Traveler Information");
                  System.out.println("7. View Plane Information");
                  System.out.println("8. View Technician Repair History");
                  System.out.println("9. View Plane Repair History");
                  System.out.println("10. View Flight Statistics");
                }
                //**the following functionalities should only be able to be used by customers**
                if(authorisedUser.equals("Customer")){
                  System.out.println("10. Search Flights");
                  System.out.println(".........................");
                  System.out.println(".........................");
                }
                //**the following functionalities should ony be able to be used by Pilots**
                if(authorisedUser.equals("Pilot")){
                  System.out.println("15. Maintenace Request");
                  System.out.println(".........................");
                  System.out.println(".........................");
                }
               //**the following functionalities should ony be able to be used by Technicians**
               if(authorisedUser.equals("Technician")){
                System.out.println(".........................");
                System.out.println(".........................");
               }
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: feature1(esql); break;
                   case 2: feature2(esql); break;
                   case 3: feature3(esql); break;
                   case 4: feature4(esql); break;
                   case 5: feature5(esql); break;
                   case 6: feature6(esql); break;
                   case 7: feature7(esql); break;
                   case 8: feature8(esql); break;
                   case 9: feature9(esql); break;
                   case 10: feature10(esql); break;


                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(AirlineManagement esql){
      try{
         System.out.print("\tCreate Username: ");
         String username = in.readLine();
         System.out.print("\tCreate Password: ");
         String password = in.readLine();
         System.out.print("\tCreate Role (Management, Customer, Pilot, Technician): ");
         boolean isValidRole = false;
         String role = "";
         
         while(!isValidRole){
            role = in.readLine();
            
            // Check if the role is valid
            if (!role.equalsIgnoreCase("Management") && 
               !role.equalsIgnoreCase("Customer") && 
               !role.equalsIgnoreCase("Pilot") && 
               !role.equalsIgnoreCase("Technician")) {
               System.out.println("Invalid role. Please enter a valid role.");
            }
            else {
               isValidRole = true;
               role = role.substring(0,1).toUpperCase() + role.substring(1).toLowerCase();
            }
         }
         String query  = "INSERT INTO UserAccount (Username, Password, Role) " +
                         "VALUES ('" + username + "', '" + password + "', '" + role + "');";
      
         esql.executeUpdate(query);
         
      }
      catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(AirlineManagement esql){
       try {
      System.out.print("\tEnter Username: ");
      String username = in.readLine();

      System.out.print("\tEnter Password: ");
      String password = in.readLine();

      String query = "SELECT Role FROM UserAccount " +
                     "WHERE Username = '" + username + "' AND Password = '" + password + "';";

      List<List<String>> result = esql.executeQueryAndReturnResult(query);

      if (result.size() == 1) {
         String role = result.get(0).get(0);
         System.out.println("\nLogin successful! Logged in as " + role + ".");
         return role; 
      } else {
         System.out.println("Login failed: Invalid username or password.");
         return null;
      }
   } catch (Exception e) {
      System.err.println("Error during login: " + e.getMessage());
      return null;
   }
   }//end

// Rest of the functions definition go in here

   public static void feature1(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Flight Number: ");
         String input = in.readLine();
         String query = "SELECT * FROM Schedule WHERE FlightNumber = '" + input + "' " +
                        "ORDER BY CASE " +
                        "WHEN DayOfWeek = 'Monday' THEN 1 " +
                        "WHEN DayOfWeek = 'Tuesday' THEN 2 " +
                        "WHEN DayOfWeek = 'Wednesday' THEN 3 " +
                        "WHEN DayOfWeek = 'Thursday' THEN 4 " +
                        "WHEN DayOfWeek = 'Friday' THEN 5 " +
                        "WHEN DayOfWeek = 'Saturday' THEN 6 " +
                        "WHEN DayOfWeek = 'Sunday' THEN 7 " +
                        "ELSE 8 END;";
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);
         System.out.println();
      }
      
      catch (Exception e) {
      System.err.println(e.getMessage());
      }
   }
   public static void feature2(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Flight Number: ");
         String flightNum = in.readLine();
         System.out.print("\tEnter Flight Date (DD-MM-YY): ");
         String date = in.readLine();
         String query = "SELECT SeatsTotal - SeatsSold AS SeatsAvailable, SeatsSold " +
                        "FROM FlightInstance " +
                        "WHERE FlightNumber = '" + flightNum + "' " +
                        "AND FlightDate = '" + date + "';";
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);
         System.out.println();
      } 
      catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void feature3(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Flight Number: ");
         String flightNum = in.readLine();
         System.out.print("\tEnter Flight Date (DD-MM-YY): ");
         String date = in.readLine();
         String query = "SELECT DepartedOnTime, ArrivedOnTime " +
                        "FROM FlightInstance " +
                        "WHERE FlightNumber = '" + flightNum + "' " +
                        "AND FlightDate = '" + date + "';";
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void feature4(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Flight Date (DD-MM-YY): ");
         String date = in.readLine();
         String query = "SELECT * FROM FlightInstance fi " +
                        "JOIN Flight f ON fi.FlightNumber = f.FlightNumber " +
                        "WHERE fi.FlightDate = '" + date + "' " +
                        "ORDER BY fi.DepartureTime;";
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);
         System.out.println();
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void feature5(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Flight Number: ");
         String flightNum = in.readLine();
         System.out.print("\tEnter Flight Date (DD-MM-YY): ");
         String date = in.readLine();
         String query = "SELECT C.FirstName, C.LastName, R.Status " +
                        "FROM Reservation R " +
                        "JOIN Customer C ON R.CustomerID = C.CustomerID " +
                        "JOIN FlightInstance FI ON R.FlightInstanceID = FI.FlightInstanceID " +
                        "WHERE FI.FlightNumber = '" + flightNum + "' " +
                        "AND FI.FlightDate = '" + date + "' " +
                        "ORDER BY CASE " +
                        "WHEN R.Status = 'reserved' THEN 1 " +
                        "WHEN R.Status = 'waitlist' THEN 2 " +
                        "WHEN R.Status = 'flown' THEN 3 " +
                        "ELSE 4 END, C.LastName, C.FirstName;";
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);
         System.out.println();
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void feature6(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Reservation ID: ");
         String resID = in.readLine();
         String query = "SELECT C.CustomerID, C.FirstName, C.LastName, C.Gender, " +
                        "C.DOB, C.Address, C.Phone, C.Zip " +
                        "FROM Reservation R " +
                        "JOIN Customer C ON R.CustomerID = C.CustomerID " +
                        "WHERE R.ReservationID = '" + resID + "';";
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);
         System.out.println();
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void feature7(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Plane ID: ");
         String planeID = in.readLine();
         String query = "SELECT P.Make, P.Model, " +
                        "EXTRACT(YEAR FROM AGE(CURRENT_DATE, TO_DATE(P.Year::TEXT, 'YYYY'))) AS Age, " +
                        "(SELECT MAX(Rp.RepairDate) FROM Repair Rp WHERE Rp.PlaneID = P.PlaneID) AS LastRepairDate " +
                        "FROM Plane P " +
                        "WHERE P.PlaneID = '" + planeID + "';";
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);
         System.out.println();
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void feature8(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Technician ID: ");
         String techID = in.readLine();
         String query = "SELECT R.RepairID, R.PlaneID, R.RepairCode, R.RepairDate " +
                        "FROM Repair R " +
                        "WHERE R.TechnicianID = '" + techID + "' " +
                        "ORDER BY R.RepairDate DESC;";
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);
         System.out.println();
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void feature9(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Plane ID: ");
         String planeID = in.readLine();
         System.out.print("\tEnter Start Date (DD-MM-YY): ");
         String startDate = in.readLine();
         System.out.print("\tEnter End Date (DD-MM-YY): ");
         String endDate = in.readLine();

         String query = "SELECT R.RepairID, R.RepairCode, R.RepairDate, R.TechnicianID " +
                        "FROM Repair R " +
                        "WHERE R.PlaneID = '" + planeID + "' " +
                        "AND R.RepairDate BETWEEN '" + startDate + "' AND '" + endDate + "' " +
                        "ORDER BY R.RepairDate;";
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);
         System.out.println();
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void feature10(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Flight Number: ");
         String flightNum = in.readLine();
         System.out.print("\tEnter Start Date (DD-MM-YY): ");
         String startDate = in.readLine();
         System.out.print("\tEnter End Date (DD-MM-YY): ");
         String endDate = in.readLine();

         String query = "SELECT " +
                        "COUNT(*) FILTER (WHERE FI.DepartedOnTime = TRUE) AS NumDepartedOnTime, " +
                        "COUNT(*) FILTER (WHERE FI.DepartedOnTime = FALSE) AS NumDepartedDelayed, " +
                        "COUNT(*) FILTER (WHERE FI.ArrivedOnTime = TRUE) AS NumArrivedOnTime, " +
                        "COUNT(*) FILTER (WHERE FI.ArrivedOnTime = FALSE) AS NumArrivedDelayed, " +
                        "SUM(COALESCE(FI.SeatsSold,0)) AS TotalSeatsSold, " +
                        "SUM(COALESCE(FI.SeatsTotal,0) - COALESCE(FI.SeatsSold,0)) AS TotalSeatsUnsold " +
                        "FROM FlightInstance FI " +
                        "WHERE FI.FlightNumber = '" + flightNum + "' " +
                        "AND FI.FlightDate BETWEEN '" + startDate + "' AND '" + endDate + "';";
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);
         System.out.println();
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }




  


}//end AirlineManagement

