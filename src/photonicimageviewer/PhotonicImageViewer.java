/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photonicimageviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
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
    
    public final static String NULL_ERROR_MSG = "Error loading image(s)."
                    + "\nThe program did not find a file"
                    + " at the specified file path.";
    
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
        List<String> args = getParameters().getRaw();
        if (!args.isEmpty()) open(args.toArray(new String[0]));
        //open("C:\\New folder\\DSC_0005.jpg");
        //open("C:\\Users\\MMAesawy\\Pictures\\pengu.jpg");
        
    }
    
    /**
     * Displays a file chooser to be used to select an image for the app.
     */
    public void openFileChooser(){
        FileChooser fileChooser = new FileChooser();
        
        fileChooser.setTitle("Open Image");
        
        //Set the initial directory to current image directory if it exists,
        //otherwise set it to the user's pictures folder if it exists.
        //otherwise set it to the user's home folder.
        if (treader != null)
            fileChooser.setInitialDirectory(
                    treader.getImageFile().getParentFile());
        else{
            File pictures =
                    new File(System.getProperty("user.home")+ "/Pictures");
            
            if (pictures.exists())
                fileChooser.setInitialDirectory(pictures);
            else fileChooser.setInitialDirectory(
                    new File(System.getProperty("user.home")));
        }
        
        
        // Add all the supported extensions to the file chooser.
        ArrayList<String> allExtensions = new ArrayList<>();
        for (FileExtension i :ExtensionFilter.SUPPORTED_FILE_EXTENSIONS){
            fileChooser.getExtensionFilters()
                    .add(new FileChooser.
                            ExtensionFilter(i.getName(), i.getExtensions()));
            for (String e : i.getExtensions())
                allExtensions.add(e);
        }
        fileChooser.getExtensionFilters().add(0,
                new FileChooser.ExtensionFilter("All image files",
                        allExtensions));
        //fileChooser.getExtensionFilters().add(
        //        new FileChooser.ExtensionFilter("All files", "*.*"));
        
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());
        
        if (files != null) open(files.toArray(new File[0]));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
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
        try{
            treader = new ImageTreader(filePaths);
        }
        catch(NullPointerException e){
            displayError(NULL_ERROR_MSG);
            return;
        }
        catch(IncompatibleFileException e){
            displayError(e.getMessage());
            return;
        }
        mainUI.loadImage();
    }
    
    public void open(String filePath){
        String[] a = { filePath };
        open(a);
    }
    
    /**
        Sets a new image treader in the specified directory.
        The method is functionally equivalent to File > Open
        @param files The file to open.
    */
    public void open(File[] files){
        try{
            treader = new ImageTreader(files);
        }
        catch(NullPointerException e){
            displayError(NULL_ERROR_MSG);
            return;
        }
        catch(IncompatibleFileException e){
            displayError(e.getMessage());
            return;
        }
        mainUI.loadImage();
    }
    
    public void open(File file){
        File[] a = { file };
        open(a);
    }
    
    public ImageTreader getMainAppTreader(){ return treader; }
   
    
}
