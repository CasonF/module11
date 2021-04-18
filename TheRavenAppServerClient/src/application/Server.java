package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class Server extends Application {

	static Connection connection = null;
	static String databaseName = "";
	static String url = "jdbc:mysql://localhost:3306/" + databaseName;
	
	static String username = "root";
	static String password = "#g&C02232019";
	
	static String output;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		TextArea ta = new TextArea();
		
		Scene scene = new Scene(new ScrollPane(ta), 450, 200);
		primaryStage.setTitle("Server");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		new Thread( () -> {
			try {
				ServerSocket serverSocket = new ServerSocket(8000);
				Platform.runLater(() ->
				ta.appendText("Server started at " + new Date() + '\n'));
				
				Socket socket = serverSocket.accept();
				
				DataInputStream inputFromClient = new DataInputStream(
						socket.getInputStream());
				DataOutputStream outputToClient = new DataOutputStream(
						socket.getOutputStream());
				
				while(true) {
					String txt = inputFromClient.readUTF();
					
					selectWord(txt);
					
					outputToClient.writeUTF(output);
					
					Platform.runLater(() -> {
						ta.appendText("Word received from client: "
								+ txt + '\n');
						ta.appendText(output);
					});
				}
			}
			catch(IOException ex) {
				ex.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}).start();
		
	}
	
	public static void main(String[] args) throws SQLException {
		ArrayList<String> wordList = new ArrayList<String>();
		Word word = null;
		int i = 1, updatedOccurrence = 0;
		
		System.out.println("Connecting to database...");
		connection = DriverManager.getConnection(url, username, password);
		System.out.println("Connected!");
		
		//**Map<String, Integer> wordMap = new HashMap<>();
		
		try {
			String user = System.getProperty("user.home");
			//Simply using user + \\Desktop\\TheRaven.txt didn't work for me,
			//but if you have a Desktop folder in your main user folder,
			//feel free to remove the \\OneDrive part of this code
			File raven = new File(user + "\\OneDrive\\Desktop\\TheRaven.txt");
			Scanner reader = new Scanner(raven);
		
			while (reader.hasNextLine())
			{
				//This separates words that are separated by spaces. Also accounts for additional symbols between words.
				String words[] = reader.next().toLowerCase().split("[\\s*,.!?\"\'-+\\–\\—\\;]");
				for (String w : words)
				{
					if(wordList.contains(w))
					{
						word.occurrence += 1;
						if (checkWord(word, connection)) {
							updateWord(word);
						}
					}
					else
					{
						word = new Word(w);
						word.occurrence = 1;
						word.id = i;
						i++;
						wordList.add(w);
						System.out.println("Shows that word occurrences and ids are changing. Added: " + word.word + " with id = " + word.id);
						insertWord(word);
					}
					
					//Here I use the conditional operator to check if a word has
					//already been counted, then I add 1 to the count (number of
					//that word present in the text file).
					//**Integer q = wordMap.get(w);
					//**q = (q == null) ? 1 : ++q; //if q doesn't exist for this var, q = 1, otherwise ++q
					//**wordMap.put(w, q);
				}
			}
			reader.close();
		}
		catch(FileNotFoundException e)
		{
			//If you have TheRaven.txt in another directory, you can ignore this if you have edited the
			//file searcher to locate your .txt
			System.out.println("An error occurred. Please make sure TheRaven.txt is on your desktop.");
			e.printStackTrace();
		}
		
		launch(args);
	}
	
	public static void insertWord(Word word) throws SQLException 
	{
		PreparedStatement ps = connection.prepareStatement("INSERT INTO `wordoccurrences`.`word` (`word`, `occurrence`) VALUES ('" + word.word + "', '" + 1 + "');");
		int status = ps.executeUpdate();
		
		if (status != 0)
		{
			//System.out.println(word.word + "'s record was inserted!"); //this is used to check that the information is reaching the database
		}
	}
	
	public static boolean checkWord(Word word, Connection connection) throws SQLException
	{
		boolean exists = false;
		Statement stmt = connection.createStatement();
		String sql = "SELECT word FROM wordoccurrences.word WHERE (`word` = '" + word.word + "');";
		ResultSet rs = stmt.executeQuery(sql);
		Word thisEntry = word;
		while(rs.next())
		{
			thisEntry.word = rs.getString("word");
		}
		System.out.println("This is to test that checkWord is working. Word: " + word.word);
		if (word.word.equals(thisEntry.word))
		{
			exists = true;
			System.out.println(word.word + " already exists in this table. Return true.");
		}
		else
		{
			exists = false;
		}
		return exists;
	}
	
	public static void updateWord(Word word) throws SQLException
	{
		Statement stmt = connection.createStatement();
		String sql = "SELECT word, occurrence FROM wordoccurrences.word WHERE (`word` = '" + word + "');";
		ResultSet rs = stmt.executeQuery(sql);
		Word thisEntry = word;
		while(rs.next())
		{
			thisEntry.word = rs.getString("word");
			thisEntry.occurrence = rs.getInt("occurrence");
		}
		thisEntry.occurrence = word.occurrence;
		PreparedStatement ps = connection.prepareStatement("UPDATE wordoccurrences.word SET occurrence = ('" + thisEntry.occurrence + "') WHERE (`word` = '" + thisEntry.word + "');");
		ps.executeUpdate();
		System.out.println("Set " + thisEntry.word + " to " + thisEntry.occurrence + " occurrences.");
		/*int status = ps.executeUpdate();
		
		if (status != 0)
		{
			//System.out.println(word.word + "'s record was updated!"); //this is used to check that the information is reaching the database
		}*/
	}
	
	public static void selectWord(String word) throws SQLException
	{
		connection = DriverManager.getConnection(url, username, password);
		Statement stmt = connection.createStatement();
		String sql = "SELECT word, occurrence FROM wordoccurrences.word WHERE (`word` = '" + word + "');";
		ResultSet rs = stmt.executeQuery(sql);
		Word thisEntry = new Word("");
		int occurrence = 0;
		while(rs.next())
		{
			thisEntry.word = rs.getString("word");
			occurrence = rs.getInt("occurrence");
		}
		
		output = word + " appears " + occurrence + " times within The Raven. ";
		
	}

}
