/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photonicimageviewer;

import java.io.IOException;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 *
 * @author MMAesawy
 */
public class PhotonicImageViewer extends Application {
    private ImageTreader treader;
    public final static double WINDOW_WIDTH = 640;
    public final static double WINDOW_HEIGHT = WINDOW_WIDTH * 3 / 4;
    private MainUIController mainUI;
    
    /*
        Definitions for a singleton.
    */
    private static PhotonicImageViewer instance;
    public PhotonicImageViewer(){
        instance = this;
    }
    public static PhotonicImageViewer getInstance(){ return instance; }
    
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = 
                new FXMLLoader(getClass().getResource("MainUI.fxml"));
        
        Parent root = loader.load();
        mainUI = loader.getController();
       
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.setMinWidth(WINDOW_WIDTH);
        stage.setMinHeight(WINDOW_HEIGHT);
        
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void init() throws Exception{
        
        List<String> args = getParameters().getRaw();
        if (!args.isEmpty()) open((String[])args.toArray());
        //open("C:\\New folder\\DSC_0005.jpg");
        open("C:\\Users\\MMAesawy\\Pictures\\pengu.jpg");
        //TODO: Set proper no argument case.
        //TODO: Set case where start directory is a collection of file names.
    }
    
    /**
     * Displays an error message to the user flush with the UI.
     * @param msg The error message to display.
     */
    public void displayError(String msg){
        mainUI.displayMessage(msg);
    }
    
    /**
        Sets a new image treader in the specified directory.
        The method is functionally equivalent to File > Open
        @param filePaths The path to the file to open.
    */
    public void open(String[] filePaths){
        //TODO set case for incompatible file.
        try{
            treader = new ImageTreader(filePaths);
        }
        catch(NullPointerException e){
            displayError("Error loading image(s)."
                    + "\nThe program did not find a file"
                    + " at the specified file path.");
        }

        
    }
    public void open(String filePath){
        String[] a = { filePath };
        open(a);
    }
    
    public ImageTreader getMainAppTreader(){ return treader; }
   
    
}
