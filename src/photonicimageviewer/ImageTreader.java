/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photonicimageviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javafx.scene.image.Image;

/**
 * The class functions as a getter for all the image files a particular directory.
 * @author MMAesawy
 */
public class ImageTreader{
    private File[] imageList;
    private int treadIndex = 0;
    private boolean isWrap = true;
    
    /**
     * Creates a new ImageTreader using the specified file path(s).
     * If the parameter contains only one entry, then the ImageTreader will
     * use and process all the image files in entry's directory.
     * If there are multiple entries in the parameter, the ImageTreader will
     * only use the files specified.
     * Using an empty array as a parameter will most likely yield a broken
     * ImageTreader.
     * @param paths Array of file paths to be used by this ImageTreader.
     */
    public ImageTreader(String[] paths){
        // In case array is empty.
        if (paths.length == 0){
            imageList = new File[0];
            return;
        }
        
        if (paths.length == 1){
            File file = new File(paths[0]);
            File homeDirectory;

            if (file.isDirectory())
                homeDirectory = file; //Retrieve path of directory of file
            else homeDirectory = file.getParentFile(); //startFilePath is a directory; do nothing
            
            imageList = homeDirectory.listFiles(new ExtensionFilter()); // Create list containing all supported image files in the directory
        }
        else{
            // Get file array of specified files
            File[] fileArray = new File[paths.length];
            for (int i = 0; i < paths.length; i++)
                fileArray[i] = new File(paths[i]);
            
            // Filter the files (weed out incompatibles)
            imageList = new ExtensionFilter().filterArray(fileArray);
        }
        
        for (int i = 0; i < imageList.length; i++) // Set current array index to the original file in parameter.
            if (imageList[i].getAbsolutePath().equals(paths[0])){
                treadIndex = i;
                break;
            }
    }
        
    /**
     *   @return The name of the image file matching the current treader index.
     */
    public String getImageName(){
        if (imageList.length == 0) return "";
        
        return imageList[treadIndex].getName();
    }
    
    /**
     *  @return The image file matching the current treader index.
     */
    public Image getImage(){
        if (imageList.length == 0) return null;
        
        Image image = null;
        try{
            image = new Image(new FileInputStream(imageList[treadIndex]));
        }
        catch (IOException e){
            System.out.println("IO Error: " + e.toString());
        }
        return image;
    }
    
    /**
     * Increments the index of the treader. Will wrap around to the first image
     * file in the directory if getIsWrap() is true.
     */
    public void next(){
        if (treadIndex != imageList.length - 1) treadIndex++;
        else if (isWrap) treadIndex = 0;
    }
    
    /**
     * Decrements the index of the treader. Will wrap around to the last image
     * file in the directory if getIsWrap() is true.
     */
    public void prev(){
        if (treadIndex != 0) treadIndex--;
        else if (isWrap) treadIndex = imageList.length - 1;
    }
    
    /**
     * @return Whether or not the treader is at the edge of the directory. 
     */
    public boolean isIndexAtArrayEdge(){
        return (treadIndex == 0 || treadIndex == imageList.length - 1);
    }
    
    public boolean getIsWrap(){ return isWrap; }
    public void setIsWrap(boolean c){ isWrap = c; }
}
