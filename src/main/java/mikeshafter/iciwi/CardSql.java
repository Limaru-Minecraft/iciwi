package mikeshafter.iciwi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CardSql{
  public void sql(String[] args){
    Connection card;
    
    try{
      Class.forName("org.sqlite.JDBC");
      card = DriverManager.getConnection("jdbc:sqlite:cards.db");
      
      Statement statement = card.createStatement();
      String[] discounts = {"Lipan", "Entetsu", "HarlonTransit"};
      StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS card (\"serial_prefix\" TEXT, \"serial\"	INTEGER, \"value\"	NUMERIC NOT NULL DEFAULT 0.00, PRIMARY KEY(\"serial\",\"serial_prefix\") );"+
                                                "CREATE TABLE IF NOT EXISTS \"journeys\" (\"time\"INTEGER,\"from\"TEXT,\"to\"TEXT,\"fare\"NUMERIC,PRIMARY KEY(\"time\",\"from\",\"to\") );");
      for (String discount : discounts){
        sql.append("CREATE TABLE IF NOT EXISTS \"").append(discount).append("\" (\"serial_prefix\"TEXT,\"serial\"INTEGER,\"expiry\"INTEGER,FOREIGN KEY(\"serial_prefix\") REFERENCES \"card\"(\"serial_prefix\"),FOREIGN KEY(\"serial\") REFERENCES \"card\"(\"serial_prefix\"),PRIMARY KEY(\"serial_prefix\",\"serial\") );");
      }
      statement.executeUpdate(sql.toString());
      
      statement.close();
      card.commit();
      card.close();
      
    } catch (Exception e){
      System.err.println(e.getClass().getName()+": "+e.getMessage());
      System.exit(0);
    }
    System.out.println("Opened database successfully");
  }
}