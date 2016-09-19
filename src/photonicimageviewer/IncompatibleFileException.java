/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photonicimageviewer;

/**
 *
 * @author MMAesawy
 */
public class IncompatibleFileException extends Exception {
    public IncompatibleFileException(){
        super("Error: Unsupported file extension.");
    }
}
