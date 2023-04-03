import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.stage.FileChooser.*;
import javafx.geometry.*;

import java.lang.Object;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * ClientStarter - class to upload a file to a server
 * Name: Luke Doherty
 * Course/Section: ISTE-121-02
 */
 
public class ClientStarter extends Application implements EventHandler<ActionEvent> {
   // Window attributes
   private Stage stage;
   private Scene scene;
   private VBox root = new VBox(8);
   
   // Declared GUI Objects and the server's port here
   private static final int SERVER_PORT = 32323;
   
   private TextField tfServerIP = new TextField();
   private TextField tfFileName = new TextField();
   private Button btnConnectAndSend = new Button("Connect and Send...");
   private TextArea taLog = new TextArea();
   
   //Networking
   private Socket socket = null;
   private PrintWriter pwt = null;
   private Scanner scn = null;
   private Scanner scnt = null;
   
   //File IO
   private File fileObj = null;
   private FileInputStream fis = null;
   private DataInputStream dis = null;
   private FileOutputStream fos = null;
   private DataOutputStream dos = null;
   
   /**
    * Main program ... 
    * @args - command line arguments (ignored)
    */
   public static void main(String[] args) {
      launch(args);
   }
   
   /**
    * Constructor ... draws the GUI
    */
   public void start(Stage _stage) {
      // Window set up
      stage = _stage;
      stage.setTitle("Text File Uploader Client (Luke Doherty)");
      final int WIDTH = 450;
      final int HEIGHT = 400;
      final int X = 50;
      final int Y = 100;
      stage.setX(X);
      stage.setY(Y);
      stage.setOnCloseRequest(
         new EventHandler<WindowEvent>() {
            public void handle(WindowEvent evt) { System.exit(0); }
         });
           
      // Scene is drawn here
      root.getChildren().add(btnConnectAndSend);
      
      FlowPane fpIPRow = new FlowPane(8,8);
      tfServerIP.setPrefColumnCount(21);
      fpIPRow.getChildren().addAll(new Label("Server Name or IP: "), tfServerIP);
      root.getChildren().add(fpIPRow);
      
      FlowPane fpFileRow = new FlowPane(8,8);
      tfFileName.setPrefColumnCount(25);
      tfFileName.setEditable(false);
      fpFileRow.getChildren().addAll(new Label("File name: "), tfFileName);
      root.getChildren().add(fpFileRow);
      
      taLog.setPrefWidth(WIDTH);
      taLog.setPrefHeight(HEIGHT);
      root.getChildren().add(taLog);
      
      // Set the button to react to clicks here
      btnConnectAndSend.setOnAction(this);
      // Set the scene and show the stage
      scene = new Scene(root, WIDTH, HEIGHT);
      stage.setScene(scene);
      stage.show();      
   }
   
   // Only one button
   public void handle(ActionEvent ae) 
   {
      doClient();
   }
   
   // The client code to choose and upload a text file
   public void doClient() {
      try {
         socket = new Socket(tfServerIP.getText(), SERVER_PORT);
         pwt = new PrintWriter(socket.getOutputStream());
         scn = new Scanner(socket.getInputStream());
         
         taLog.appendText("Connected!");
         
         FileChooser fileChooser = new FileChooser();
         fileChooser.setTitle("Open File");
         fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("TXT Files", "*.txt"),
            new ExtensionFilter("All Files", "*.*"));
         
         // Show the chooser
         File selectedFile = fileChooser.showOpenDialog(stage);
         if (selectedFile == null) {
            // Canceled
            return;
         }
      
         // File selected. Display file name in text field
         tfFileName.setText(selectedFile.getAbsolutePath());
         String flName = selectedFile.getName();
         flName = flName.replace(".txt", "-UPLOAD.txt");
         taLog.appendText("\nFilename sent to server: "+flName);
         
         pwt.println(flName);
         pwt.flush();
         
         try {
            scnt = new Scanner(new FileInputStream(selectedFile));
            String temp = "";
            while(scnt.hasNext()) {
               temp = scnt.nextLine();
               pwt.println(temp);
               pwt.flush();
               taLog.appendText("\nSending: "+temp);
            }
            scnt.close();   
            pwt.println("END-TRANSMISSION");
            pwt.flush();
            String receipt = scn.nextLine();
            taLog.appendText("\nReply from server: "+receipt);
         }
         catch(Exception e) {
            Alert alert = new Alert(AlertType.ERROR, String.format("(%s) opening file ... fatal", e));
            Optional<ButtonType> result = alert.showAndWait();
            System.exit(1);
         } 
      
         pwt.close();
         scn.close();
         socket.close();
         
         
      }  // try
      catch(Exception e) {
         Alert alert = new Alert(AlertType.ERROR);
         alert.setTitle("Exception");
         alert.setContentText(e.toString());
         alert.showAndWait();
         System.exit(1);
      }
   }
}