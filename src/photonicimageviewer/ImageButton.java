/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photonicimageviewer;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


/**
 * An image view with active and inactive states. Useful for "button" style
 * image views.
 * @author MMAesawy
 */
public class ImageButton extends ImageView{
    private Image activeImage = null;
    private Image inactiveImage = null;
    private boolean isActive = false;
    
    public void setActiveImage(Image i){
        activeImage = i;
        if(isActive) setImage(activeImage);
    }
    public Image getActiveImage(){ return activeImage; }
    
    public void setInactiveImage(Image i){
        inactiveImage = i;
        if(!isActive) setImage(inactiveImage);
    }
    public Image getInactiveImage(){ return inactiveImage; }
    
    /**
     * Set whether or not the image view is in an active state. Automatically
     * handles setting the image view to the appropriate image.
     * @param a whether or not the image view should be in an active state
     */
    public void setActive(boolean a){
        if (isActive == a) return;
        
        
        isActive = a;
        if (isActive) setImage(activeImage); 
        else setImage(inactiveImage);
    }
    public boolean isActive(){ return isActive; }
    
}
