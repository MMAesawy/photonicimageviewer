/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photonicimageviewer;

import java.io.File;
import java.io.FilenameFilter;


/**
 *
 * @author MMAesawy
 */
public class ExtensionFilter implements FilenameFilter {
    
    public final static FileExtension[] SUPPORTED_FILE_EXTENSIONS = 
        {new FileExtension("JPG/JPEG", ".jpg", ".jpeg"),
        new FileExtension("PNG", ".png"),
        new FileExtension("TIF/TIFF", ".tif", ".tiff"),
        new FileExtension("GIF", ".gif"),
        new FileExtension("BMP", ".bmp"),
        new FileExtension("BPG", ".bpg"),
        new FileExtension("PPM/PGM/PBM/PNM", ".ppm", ".pgm", ".pbm", ".pnm"),
        new FileExtension("WebP", ".webp")};
    
    /**
     * Will only accept files which end in the extensions defined
     * in the supportedFileExtensions array.
     * @param dir The directory of the file.
     * @param name The name of the file.
     * @return 
     */
    @Override
    public boolean accept(File dir, String name){
        
        for (FileExtension i : SUPPORTED_FILE_EXTENSIONS){
            if (i.isMatch(name)) return true;
        }
        return false;
    }
    
    
    /**
     * Takes in an array of files and returns a filtered array using this
     * class's accept() function.
     * @param files files to be filtered
     * @return filtered files
     */
    public File[] filterArray(File[] files){
        int nullCount = 0;
        for (int i = 0; i < files.length; i++)
            if (!accept(files[i].getParentFile(), files[i].getName())){
                files[i] = null;
                nullCount++;
            }
        File[] returnArray = new File[files.length - nullCount];
        int index = 0;
        for (File f : files)
            if (f != null){
                returnArray[index] = f;
                index++;
            }
        return returnArray;
    }
}
