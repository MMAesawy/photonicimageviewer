/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photonicimageviewer;

/**
 * Defines a file extension and provides a method for testing a file to see
 * if it matches this extension.
 * @author MMAesawy
 */
public class FileExtension {
    private String name; // Name of extension.
    private String[] endings; // The extension's file name endings (e.g. '.gif')
    
    /**
     * Creates a new FileExtension definition.
     * @param n The name of the extension.
     * @param e The file name endings of this extension (e.g. '.gif', '.png')
     */
    public FileExtension(String n, String... e){
        name = n;
        for (int i = 0; i < e.length; i++)
            e[i] = "*" + e[i];
        endings = e;
    }
    
    /**
     * Checks if the file name has one of the endings defined by this extension.
     * @param fileName The name of the file to be checked.
     * @return True if it ends with one of the extension's endings,
     * false otherwise.
     */
    public boolean isMatch(String fileName){
        int extensionIndex = fileName.lastIndexOf(".");
        if (extensionIndex == -1) return false;
        String extension = fileName.substring(extensionIndex);
        
        for (String ending : endings){
            if (extension.equalsIgnoreCase(ending.substring(1))) return true;
        }
        return false;
    }
    
    public String getName() { return name; }
    public String[] getExtensions() { return endings; }
}
