import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author sqlitetutorial.net
 */
public class JDBC {

    /**
     * Connect to the BDD.db database
     * @return the Connection object
     */
    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:src/main/java/BDD.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
    
    public static void createNewDatabase(String fileName) {
    	String url = "jdbc:sqlite:src/main/java/" + fileName;
    	try (Connection conn = DriverManager.getConnection(url)) {
    		if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
        }
    }
    
    public static void createNewTable() {
        // SQLite connection string
        String url = "jdbc:sqlite:src/main/java/BDD.db";
        
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS Conversation (\n"
                + "	ip1 varchar(32),\n"
                + "	ip2 varchar(32),\n"
                + "	primary key (ip1,ip2), \n"
                + " foreign key (ip1) references Annuaire(ip), \n"
                + " foreign key (ip2) references Annuaire(ip));";
        
        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

   
    /**
     * Insert a new row into the Annuaire table
     *
     * @param ip
     * @param login
     * @param port 
     */
   public void insertA(String ip, String login, String port) {
      String sql = "INSERT INTO Annuaire(ip,login,port) VALUES(?,?,?)";
      try (Connection conn = this.connect();
   		PreparedStatement pstmt = conn.prepareStatement(sql)) {
           pstmt.setString(1,ip);
           pstmt.setString(2, login);
           pstmt.setString(3, port);
           pstmt.executeUpdate();
           } catch (SQLException e) {
               System.out.println(e.getMessage());
           }
   }
   
   public void insertAwithoutP(String ip, String login) {
	      String sql = "INSERT INTO Annuaire(ip,login) VALUES(?,?)";
	      try (Connection conn = this.connect();
	   		PreparedStatement pstmt = conn.prepareStatement(sql)) {
	           pstmt.setString(1,ip);
	           pstmt.setString(2, login);
	           pstmt.executeUpdate();
	           } catch (SQLException e) {
	               System.out.println(e.getMessage());
	           }
	   }
   
   /**
    * Delete a Contact specified by the ip
    *
    * @param ip
    */
   public void deleteA(String ip) {
       String sql = "DELETE FROM Annuaire WHERE ip = ?";

       try (Connection conn = this.connect();
               PreparedStatement pstmt = conn.prepareStatement(sql)) {

           // set the corresponding param
           pstmt.setString(1, ip);
           // execute the delete statement
           pstmt.executeUpdate();

       } catch (SQLException e) {
           System.out.println(e.getMessage());
       }
   }
   
   /**
    * select all rows in the table Annuaire
    */
   public void selectAllA(){
       String sql = "SELECT * FROM Annuaire";
       try {Connection conn = this.connect();
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
           // loop through the result set
           while (rs.next()) {
               System.out.println(rs.getString("ip") +"\t"+ rs.getString("login") +"\t"+ rs.getString("port")+ "\t");
           }
       } catch (SQLException e) {
           System.out.println(e.getMessage());
       }
   }
   
   public boolean IsLoginUsed(String login){
	   boolean a = false;
       String sql = "SELECT login FROM Annuaire WHERE login = ? ";
       try {Connection conn = this.connect();
       		PreparedStatement pstmt = conn.prepareStatement(sql);
       		pstmt.setString(1, login);
            ResultSet rs    = pstmt.executeQuery();
           // loop through the result set
           while (rs.next()) {
        	   if (rs.getString("login").equals(login)) {
        		   a= true;
        	   }
           }
       } catch (SQLException e) {
           System.out.println(e.getMessage());
       }
       return a; 
   }
   
   /**
    * Insert a new row into the Conversation table
    *
    * @param ip1
    * @param ip2
    */
  public void insertC(String ip1, String ip2 ) {
     String sql = "INSERT INTO Conversation(ip1,ip2) VALUES(?,?)";
     try (Connection conn = this.connect();
  		PreparedStatement pstmt = conn.prepareStatement(sql)) {
          pstmt.setString(1,ip1);
          pstmt.setString(2,ip2);
          pstmt.executeUpdate();
          } catch (SQLException e) {
              System.out.println(e.getMessage());
          }
  }
  
  
  
  public void Alter() {
	     String sql = "ALTER TABLE Messages \n"
	     		+ "  ADD COLUMN date text";
	     try (Connection conn = this.connect();
	    		 Statement stmt = conn.createStatement()) {
	            // Modify a new table
	            stmt.execute(sql);
	          } catch (SQLException e) {
	              System.out.println(e.getMessage());
	          }
	  }
  
  /**
   * Delete a Contact specified by the ips
   *
   * @param ip1
   * @param ip2
   */
  public void deleteC(String ip1, String ip2) {
      String sql = "DELETE FROM Conversation WHERE ip1 = ? and ip2 = ?";

      try (Connection conn = this.connect();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

          // set the corresponding param
          pstmt.setString(1, ip1);
          pstmt.setString(1, ip2);
          // execute the delete statement
          pstmt.executeUpdate();

      } catch (SQLException e) {
          System.out.println(e.getMessage());
      }
  }
  
  /**
   * select all rows in the table Conversation
   */
  public void selectAllC(){
      String sql = "SELECT * FROM Conversation";
      try {Connection conn = this.connect();
           Statement stmt  = conn.createStatement();
           ResultSet rs    = stmt.executeQuery(sql);
          // loop through the result set
          while (rs.next()) {
              System.out.println(rs.getString("ip1") +"\t"+ rs.getString("ip2"));
          }
      } catch (SQLException e) {
          System.out.println(e.getMessage());
      }
  }
        
     /**
      * Insert a new row into the Messages table
      *
      * @param id
      * @param message
      * @param ip1
      * @param ip2
      */
    public void insertM(int id, String message, String ip1, String ip2) {
       String sql = "INSERT INTO Messages(id,message,ip1,ip2) VALUES(?,?,?,?)";
       try (Connection conn = this.connect();
    		PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1,id);
            pstmt.setString(2, message);
            pstmt.setString(3, ip1);
            pstmt.setString(4, ip2);
            pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
    }
    /**
     * Delete a message specified by the id
     *
     * @param id
     */
    public void deleteM(int id) {
        String sql = "DELETE FROM Messages WHERE id = ?";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setInt(1, id);
            // execute the delete statement
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    /**
     * select all rows in the table Messages
     */
    public void selectAllM(){
        String sql = "SELECT * FROM Messages";
        try {Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getInt("id") +  "\t" + rs.getString("message")+ "\t"
                		+ rs.getString("ip1")+ "\t" + rs.getString("ip2") + "\t" + rs.getString("date"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JDBC app = new JDBC();
        //app.createNewDatabase("BDD.db");
        //app.createNewTable();
        //app.Alter();
        app.insertM(3, "Tu fais quoi en ce moment?", "192.168.10.1","192.168.12.1");
        app.insertM(4, "Je suis tellement débordé avec ce projet de chat.","192.168.12.1","192.168.10.1");
        app.selectAllM();
        app.deleteM(3);
        app.selectAllM();
        app.deleteM(4);
        app.selectAllM();
        app.insertA("192.168.10.1","Herstory","6002");
        app.insertAwithoutP("192.168.12.1","Lemonade");
        app.selectAllA();
        if (app.IsLoginUsed("Caton")) {
        	System.out.println("Yes, It's used\n");
        }else System.out.println("No, It's free\n");
        app.deleteA("192.168.10.1");
        app.deleteA("192.168.12.1");
    }

}