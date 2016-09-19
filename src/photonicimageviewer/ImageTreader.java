/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photonicimageviewer;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javafx.scene.image.Image;

/**
 * The class functions as a getter for all the image files in
 * a particular directory.
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
    public ImageTreader(String[] paths) throws IncompatibleFileException{
        //Convert paths to files.
        File[] files = new File[paths.length];
        for (int i = 0; i < paths.length; i++)
            files[i] = new File(paths[i]);
        
        initTreader(files);
    }
    
    /**
     * Creates a new ImageTreader using the specified file(s).
     * If the parameter contains only one entry, then the ImageTreader will
     * use and process all the image files in entry's directory.
     * If there are multiple entries in the parameter, the ImageTreader will
     * only use the files specified.
     * Using an empty array as a parameter will most likely yield a broken
     * ImageTreader.
     * @param files Array of files to be used by this ImageTreader.
     */
    public ImageTreader(File[] files) throws IncompatibleFileException{
        initTreader(files);
    }
    
    /**
     * Initializes the treader.
     * If the parameter contains only one entry, then the ImageTreader will
     * use and process all the image files in entry's directory.
     * If there are multiple entries in the parameter, the ImageTreader will
     * only use the files specified.
     * Using an empty array as a parameter will most likely yield a broken
     * ImageTreader.
     * @param files The files to be processed by this ImageTreader. 
     */
    private void initTreader(File[] files) throws IncompatibleFileException{
        // In case array is empty.
        if (files.length == 0){
            imageList = new File[0];
            return;
        }
        
        if (files.length == 1){
            File homeDirectory;

            if (files[0].isDirectory())
                homeDirectory = files[0]; //Retrieve path of directory of file
            else
                //startFilePath is a dir 
                homeDirectory = files[0].getParentFile(); 
            ExtensionFilter filter = new ExtensionFilter();
            if (filter.accept(files[0], files[0].getName()))
                // Create list containing all supported image files in the dir
                imageList = homeDirectory.listFiles(filter); 
            else{
                throw new IncompatibleFileException();
            }
            
            // Set current array index to the original file in parameter.
            for (int i = 0; i < imageList.length; i++) 
                if (imageList[i].equals(files[0])){
                    treadIndex = i;
                    break;
                }
        }
        else{
            // Filter the files (weed out incompatibles)
            imageList = new ExtensionFilter().filterArray(files);
            treadIndex = 0;
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
    public Image getImage() throws IOException{
        if (imageList.length == 0) return null;
        
        Image image = null;
        image = new Image(new FileInputStream(imageList[treadIndex]));
        
        return image;
    }
    
    /**
     * Gets the current image file.
     */
    public File getImageFile(){
        if (imageList.length == 0) return null; 
        return imageList[treadIndex];
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
    
    /**
     * Retrieves the orientation data from the current image's EXIF data.
     * @return The value of the image's orientation tag.
     */
    public int getImageOrientation(){
        if (imageList.length == 0) return 1;
        int orientation;
        try{
            Metadata data = ImageMetadataReader.readMetadata(getImageFile());
            Directory directory =
                    data.getFirstDirectoryOfType(ExifIFD0Directory.class);
            orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            System.out.println("orientation: " + orientation);
        }
        catch (Exception e){ return 1; }
        return orientation;
    }
    
    public boolean getIsWrap(){ return isWrap; }
    public void setIsWrap(boolean c){ isWrap = c; }
}
