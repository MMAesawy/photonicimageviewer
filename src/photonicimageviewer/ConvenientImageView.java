/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photonicimageviewer;

import javafx.scene.image.ImageView;


/**
 * A regular ImageView with added convenience methods.
 * @author MMAesawy
 */
public class ConvenientImageView extends ImageView{
    private int orientation = 1;
    
    /**
     * @return The X coordinate of the left boundary of the node relative to its parent.
     */
    public double getLeft(){
        return getBoundsInParent().getMinX();
        //return getLayoutX() + getTranslateX() - getActualWidth() * 0.5 * (1 - 1 / getScaleX());
    }
    
    /**
     * @return The Y coordinate of the top boundary of the node relative to its parent. 
     */
    public double getTop(){
        return getBoundsInParent().getMinY();
    }
    
    /**
     * @return The X coordinate of the right boundary of the node relative to its parent.
     */
    public double getRight(){
        return getBoundsInParent().getMaxX();
    }
    
    /**
     * @return The Y coordinate of the bottom boundary of the node relative to its parent.
     */
    public double getBottom(){
        return getBoundsInParent().getMaxY();
    }
    
    /**
     * @return The actual width of the node. Takes node scale into consideration.
     */
    public double getActualWidth(){
        return getBoundsInParent().getWidth();
    }
    
    /**
     * @return The actual height of the node Takes node scale into consideration.
     */
    public double getActualHeight(){
        return getBoundsInParent().getHeight();
    }
    
    /**
     * Increment the node's X translation property.
     * @param x value to be added to the node's X translation property.
     */
    public void offsetX(double x){
        setTranslateX(getTranslateX() + x);
    }
    
    /**
     * Increment the node's Y translation property.
     * @param y value to be added to the node's Y translation property.
     */
    public void offsetY(double y){
        setTranslateY(getTranslateY() + y);
    }
    
    /**
     * Scales the node in the X axis by the specified percent value.
     * @param percent the percentage with which the node will be scaled.
     */
    public void scaleXByPercent(double percent){
        setScaleX(getScaleX() * percent / 100.0);
    }
    
    /**
     * Scales the node in the Y axis by the specified percent value.
     * @param percent the percentage with which the node will be scaled.
     */
    public void scaleYByPercent(double percent){
        setScaleX(getScaleX() * percent / 100.0);
    }
    
    /**
     * Rotates the ImageView 90 degrees clockwise or counterclockwise
     * @param clockwise true if clockwise, false if counterclockwise
     */
    public void rotate(boolean clockwise){
        setRotate(getRotate() + 90 * (clockwise ? 1 : -1));
    }
    
    /**
     * Flips the ImageView horizontally.
     */
    public void flipHorizontally(){
        setScaleX(-getScaleX());
    }
    
    /**
     * Flips the ImageView vertically.
     */
    public void flipVertically(){
        setScaleY(-getScaleY());
    }
    
    /**
     * Returns this ImageView's EXIF orientation.
     * @return EXIF orientation.
     */
    public int getOrientation() { return orientation; }
    
    /**
     * Sets this ImageView's EXIF orientation data. Does not alter any of the
     * node's transformation properties.
     * @param o The EXIF orientation.
     */
    public void setOrientation(int o) { orientation = o; }
    
}
