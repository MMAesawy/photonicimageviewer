/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photonicimageviewer;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;


/**
 *
 * @author MMAesawy
 */
public class PhotonicImageViewer extends Application {
    private ImageTreader treader;
    private MainUIController mainUI;
    private Stage mainStage;
    private boolean readEXIF = true;
    private boolean wrapDirectory = true;
    private double windowWidth = 640;
    private double windowHeight = 480;
    private boolean isFullscreen = false;
    public final static String NULL_ERROR_MSG = "Error loading image(s)."
                    + "\nThe program did not find a file"
                    + " at the specified file path.";
    public final static String APP_NAME = "Photonic Image Viewer";
    public final static String ICON_PATH = "photoniclogo2.png";
    public final static String ASSET_PATH = "";
    
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
        stage.setMinWidth(640);
        stage.setMinHeight(480);
        stage.setWidth(windowWidth);
        stage.setHeight(windowHeight);
        stage.setFullScreen(isFullscreen);
        mainStage = stage;
        FXMLLoader loader = 
                new FXMLLoader(getClass().getResource("MainUI.fxml"));
        
        Parent root = loader.load();
        mainUI = loader.getController();
        
        Scene scene = new Scene(root);

        stage.setTitle(APP_NAME);
        Image icon = loadAppIcon();
            if (icon != null) stage.getIcons().add(icon);
        
        stage.setScene(scene);
        stage.show();
        List<String> args = getParameters().getRaw();
        if (!args.isEmpty()) open(args.toArray(new String[0]));    
    }
    
    public Stage getMainStage() { return mainStage; }
    
    @Override
    public void stop() throws Exception{
        writeConfig();
    }
    
    public void startAbout() throws Exception{
        
        
        WebView wb = new WebView();
        wb.getEngine().load(new File(PhotonicImageViewer.ASSET_PATH + 
                "about.html").toURI().toString());
        wb.setPrefSize(640, 400);
        wb.getEngine().getLoadWorker().stateProperty().
                addListener(new ChangeListener<Worker.State>() {
        @Override
        public void changed(ObservableValue<? extends Worker.State> observable,
                Worker.State oldValue, Worker.State newValue) {
            if (newValue != Worker.State.SUCCEEDED) {
                 return;
            }
            Document document = wb.getEngine().getDocument();
            NodeList nodeList = document.getElementsByTagName("a");
            for (int i = 0; i < nodeList.getLength(); i++)
            {
                Node node= nodeList.item(i);
                EventTarget eventTarget = (EventTarget) node;
                eventTarget.addEventListener("click",
                        new org.w3c.dom.events.EventListener()
                {
                    @Override
                    public void handleEvent(Event evt)
                    {
                        EventTarget target = evt.getCurrentTarget();
                        HTMLAnchorElement anchorElement
                                = (HTMLAnchorElement) target;
                        String href = anchorElement.getHref();
                        try{
                            if(Desktop.isDesktopSupported())
                                Desktop.getDesktop().browse(new URI(href));
                        }
                        catch(Exception exc) {
                            logError(exc);
                            exc.printStackTrace();
                        }
                        evt.preventDefault();
                    }
                }, false);
            }
            }
        });
        
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setTitle("About");
        stage.setScene(new Scene(wb));
        stage.show();
    }
    
    public Image loadAppIcon(){
        
        Image image = null;
        try{
            image = new Image(new FileInputStream(ASSET_PATH + ICON_PATH));
        }
        catch(FileNotFoundException e){
            logError(e);
            e.printStackTrace();
        }
        
        return image;
    }
    
    public void setWindowTitle(String t){
        mainStage.setTitle(t);
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
        try{
            launch(args);
        }
        catch(Exception e){
            PhotonicImageViewer.logError(e);
        }
    }
    
    @Override
    public void init(){
        readConfig();
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
            treader.setIsWrap(wrapDirectory);
        }
        catch(NullPointerException e){
            PhotonicImageViewer.logError(e);
            displayError(NULL_ERROR_MSG);
            return;
        }
        catch(IncompatibleFileException e){
            PhotonicImageViewer.logError(e);
            displayError(e.getMessage());
            return;
        }
        mainUI.loadImage();
    }
    
    public void open(String filePath){
        String[] a = { filePath };
        open(a);
    }
    
    public static void logError(Exception e){
        File config = new File("error_log.txt");
        
        try(FileWriter writer = new FileWriter(config, false)){
            writer.write(e.getMessage());
        }
        catch(IOException exc){
            exc.printStackTrace();
        }
    }
    
    /**
        Sets a new image treader in the specified directory.
        The method is functionally equivalent to File > Open
        @param files The file to open.
    */
    public void open(File[] files){
        try{
            treader = new ImageTreader(files);
            treader.setIsWrap(wrapDirectory);
        }
        catch(NullPointerException e){
            PhotonicImageViewer.logError(e);
            displayError(NULL_ERROR_MSG);
            return;
        }
        catch(IncompatibleFileException e){
            PhotonicImageViewer.logError(e);
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
    
    public boolean getIsWrap(){
        return wrapDirectory;
    }
    public void setIsWrap(boolean i){
        wrapDirectory = i;
        writeConfig();
    }
    
    public boolean getIsReadEXIF(){
        return readEXIF;
    }
    public void setIsReadEXIF(boolean i){
        readEXIF = i;
        writeConfig();
    }
    
    private void writeConfig(){
        File config = new File("config.ini");
        
        try(FileWriter writer = new FileWriter(config, false)){
            writer.write("wrapDirectory = " + wrapDirectory + ";\n");
            writer.write("readEXIF = " + readEXIF + ";\n");
            writer.write("windowWidth = " + mainStage.getWidth() + ";\n");
            writer.write("windowHeight = " + mainStage.getHeight() + ";\n");
            writer.write("isFullscreen = " + mainStage.isFullScreen() + ";\n");
        }
        catch(IOException e){
            logError(e);
            e.printStackTrace();
        }
    }
    
    private void readConfig(){
        File config = new File("config.ini");
        wrapDirectory = true;
        readEXIF = true;
        String configLine = "";
        if (config.exists()){
            try(FileReader reader = new FileReader(config)){
                int i;
                do{
                    i = reader.read();
                    if (i != -1) configLine += ((char) i);
                    if ((char) i == ';'){
                        if (configLine.contains("wrapDirectory")){
                            if (configLine.contains("true"))
                                wrapDirectory = true;
                            else if (configLine.contains("false"))
                                wrapDirectory = false;
                        }
                        else if (configLine.contains("readEXIF")){
                            if (configLine.contains("true"))
                                readEXIF = true;
                            else if (configLine.contains("false"))
                                readEXIF = false;
                        }
                        else if (configLine.contains("isFullscreen")){
                            if (configLine.contains("true"))
                                isFullscreen = true;
                            else if (configLine.contains("false"))
                                isFullscreen = false;
                        }
                        else if (configLine.contains("windowHeight")){
                            configLine = configLine.substring(
                                configLine.indexOf('=') + 1,
                                    configLine.indexOf(';')).trim();
                            try{
                                windowHeight = Double.parseDouble(configLine);
                            }
                            catch(Exception e){ PhotonicImageViewer.logError(e);}
                            
                        }
                        else if (configLine.contains("windowWidth")){
                            configLine = configLine.substring(
                                configLine.indexOf('=') + 1,
                                    configLine.indexOf(';')).trim();
                            try{
                                windowWidth = Double.parseDouble(configLine);
                            }
                            catch(Exception e){PhotonicImageViewer.logError(e);}
                        }
                        configLine = "";
                    }
                } while (i != -1);
            }
            catch(IOException e){
                logError(e);
                e.printStackTrace();
            }
        }
    }
   
    
}
