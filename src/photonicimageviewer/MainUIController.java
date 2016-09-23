/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photonicimageviewer;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author MMAesawy
 */
public class MainUIController implements Initializable {
    
    private static final double ZOOM_AMOUNT = 0.008 * 40;
    private static final double IMAGEVIEW_TRANSLATION_PAD = 10;
    private static final double INITIAL_SCALE = 1.0;
    private static final double MINIMUM_WIDTH = 320;
    private static final double MINIMUM_HEIGHT = 200;
    private static final double NEXT_PREV_REGION_DIVIDER_RATIO = 0.35;
    private static final double IMAGE_MARGIN = 10;
    private static final double CIRCLE_BUTTON_WIDTH = 20;
    private static final double CIRCLE_BUTTON_HEIGHT = 20;
    private static final double CIRCLE_BUTTON_ACTIVE_ALPHA = 0.95;
    private static final double CIRCLE_BUTTON_INACTIVE_ALPHA = 0.70;
    
    private static double minimumScale = 0.5;
    private boolean isUIDisabled = true;
    private ArrayList<MenuItem> disableableItems = new ArrayList<>();
    
    
    @FXML private Label file_name_label;
    @FXML private ImageView next_button;
    @FXML private ImageView prev_button;
    @FXML private ConvenientImageView main_imageview;
    @FXML private BorderPane main_container;
    @FXML private BorderPane bottom_toolbar;
    @FXML private MenuBar top_toolbar;
    @FXML private ImageButton right_arrow;
    @FXML private ImageButton left_arrow;
    @FXML private Label error_label;
    @FXML private CheckMenuItem wrap_check;
    @FXML private CheckMenuItem exif_check;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO: Refactor node names to camel case
        initMainImageView();
        initMainContainer();
        initArrows();
        initCircleButtons();
        initTopToolbar();
        wrap_check.setSelected(
                PhotonicImageViewer.getInstance().getIsWrap());
        exif_check.setSelected(
                PhotonicImageViewer.getInstance().getIsReadEXIF());
        disableUI();
       
    }
    
    /**
     * Initializes the main container and sets its event handlers.
     */
    private void initMainContainer(){
        //TODO: Documentation=
        
        main_container.setOnMouseClicked(new EventHandler<MouseEvent>(){
            public void handle(MouseEvent e){
                if (isUIDisabled) return;
                if (!e.isStillSincePress()) return;
                
                if (e.getButton() == MouseButton.MIDDLE){
                    resetImageView();
                }
                else if (right_arrow.isActive()){
                    next();
                }
                else if(left_arrow.isActive()){
                    prev();
                }
                
            }
        });        
        main_container.setOnMouseMoved(new EventHandler<MouseEvent>(){
            public void handle(MouseEvent e){
                if (isUIDisabled) return;
                
                // Top bound of the center of the BorderPane.
                double topBound 
                        = main_container.getTop()
                                .getBoundsInParent().getHeight();
                
                // Bottom bound of teh center of the BorderPane.
                double bottomBound
                        = main_container.getBottom()
                                .getLayoutY();
                
                // Y value of the mouse pointer.
                double y = e.getSceneY();
                
                // If Y is between the top bound and the bottom bound:
                if (y > topBound && y < bottomBound){
                    // If X is on the right side of the window.
                    if (e.getSceneX() > main_container.getWidth() *
                            NEXT_PREV_REGION_DIVIDER_RATIO){
                        //Activate right arrow.
                        if (wrap_check.isSelected() 
                                || !getTreader().isIndexAtEndArrayEdge())
                            right_arrow.setActive(true);
                        left_arrow.setActive(false);
                    }
                    else{
                        //Activate left arrow.
                        if (wrap_check.isSelected() 
                                || !getTreader().isIndexAtStartArrayEdge())
                            left_arrow.setActive(true);
                        right_arrow.setActive(false);
                    }
                }
                else{
                    //Deactivate both arrows.
                    right_arrow.setActive(false);
                    left_arrow.setActive(false);
                }     
            }
        });
        main_container.setOnMouseExited(new EventHandler<MouseEvent>(){
            public void handle(MouseEvent e){
                if (isUIDisabled) return;
                //Deactivate both arrows when mouse pointer is outside
                //the app window.
                right_arrow.setActive(false);
                left_arrow.setActive(false);
            }
        });
    }
    
    /**
     * Initializes the main ImageView and sets its event handlers.
     */
    private void initMainImageView(){
        main_imageview.setPreserveRatio(true);
        main_imageview.setOnScroll(new EventHandler<ScrollEvent>(){
           @Override
           public void handle(ScrollEvent e){
               double X = e.getSceneX() - main_imageview.getLeft();
               double Y = e.getSceneY() - main_imageview.getTop();
               zoom(X,Y, e.getDeltaY() > 0);
           }
        });
        
        // Setup click-drag implementation
        ImageViewMouseDragHandler dragHandler = new ImageViewMouseDragHandler();
        main_imageview.setOnMouseDragged(dragHandler);
        main_imageview.setOnMouseReleased(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent e){
                dragHandler.reset();
            }
        });
    }
    
    /**
     * Initializes the top MenuBar and sets its item's event handlers.
     */
    private void initTopToolbar(){
        for (int i = 0; i < top_toolbar.getMenus().size(); i++){
            Menu menu = top_toolbar.getMenus().get(i);
            for (int j = 0; j < menu.getItems().size(); j++){
                MenuItem item = menu.getItems().get(j);
                if (item instanceof SeparatorMenuItem) continue;
                String label = item.getText();
                if (label.equalsIgnoreCase("Open")){
                    item.setAccelerator(KeyCombination
                            .keyCombination("Ctrl+O"));
                    item.setOnAction(new EventHandler<ActionEvent>(){
                        public void handle(ActionEvent e){
                            PhotonicImageViewer.getInstance().openFileChooser();
                        }
                    });
                }
                else if (label.equalsIgnoreCase("Exit")){
                    item.setAccelerator(KeyCombination
                            .keyCombination("Alt+F4"));
                    item.setOnAction(new EventHandler<ActionEvent>(){
                        public void handle(ActionEvent e){
                            Platform.exit();
                        }
                    });
                }
                else if (label.equalsIgnoreCase("Rotate Clockwise")){
                    disableableItems.add(item);
                    item.setAccelerator(KeyCombination
                            .keyCombination("Ctrl+Shift+Right"));
                    item.setOnAction(new EventHandler<ActionEvent>(){
                        public void handle(ActionEvent e){
                            main_imageview.rotate(true);
                            resetImageView();
                        }
                    });
                }
                else if (label.equalsIgnoreCase("Rotate Counter-clockwise")){
                    item.setAccelerator(KeyCombination
                            .keyCombination("Ctrl+Shift+Left"));
                    disableableItems.add(item);
                    item.setOnAction(new EventHandler<ActionEvent>(){
                        public void handle(ActionEvent e){
                            main_imageview.rotate(false);
                            resetImageView();
                        }
                    });
                }
                else if (label.equalsIgnoreCase("Flip Horizontally")){
                    disableableItems.add(item);
                    item.setAccelerator(KeyCombination
                            .keyCombination("Shift+Right"));
                    item.setOnAction(new EventHandler<ActionEvent>(){
                        public void handle(ActionEvent e){
                            if (main_imageview.getRotate() % 180 == 0)
                                main_imageview.flipHorizontally();
                            else main_imageview.flipVertically();
                        }
                    });
                }
                else if (label.equalsIgnoreCase("Flip Vertically")){
                    disableableItems.add(item);
                    item.setAccelerator(KeyCombination
                            .keyCombination("Shift+Up"));
                    item.setOnAction(new EventHandler<ActionEvent>(){
                        public void handle(ActionEvent e){
                            if (main_imageview.getRotate() % 180 == 0)
                                main_imageview.flipVertically();
                            else main_imageview.flipHorizontally();
                        }
                    });
                }
                else if (label.equalsIgnoreCase("About")){
                    item.setOnAction(new EventHandler<ActionEvent>(){
                        public void handle(ActionEvent e){
                            try{
                                PhotonicImageViewer.getInstance().startAbout();
                            }
                            catch(Exception exc) { exc.printStackTrace(); }
                        }
                    });
                }
                else if (label.equalsIgnoreCase("Reset Zoom")){
                    disableableItems.add(item);
                    item.setOnAction(new EventHandler<ActionEvent>(){
                        public void handle(ActionEvent e){
                            resetImageView();
                        }
                    });
                }
                else if (label.equalsIgnoreCase("Zoom In")){
                    disableableItems.add(item);
                    item.setAccelerator(KeyCombination
                            .keyCombination("Ctrl+Equals"));
                    item.setOnAction(new EventHandler<ActionEvent>(){
                        public void handle(ActionEvent e){
                           zoom(true);
                        }
                    });
                }
                else if (label.equalsIgnoreCase("Zoom Out")){
                    disableableItems.add(item);
                    item.setAccelerator(KeyCombination
                            .keyCombination("Ctrl+Minus"));
                    item.setOnAction(new EventHandler<ActionEvent>(){
                        public void handle(ActionEvent e){
                            zoom(false);
                        }
                    });
                }
                else if (label.equalsIgnoreCase("Wrap image directory")){
                    item.setOnAction(new EventHandler<ActionEvent>(){
                        public void handle(ActionEvent e){
                            PhotonicImageViewer.getInstance().
                                    setIsWrap(wrap_check.isSelected());
                        }
                    });
                }
                else if (label.equalsIgnoreCase("Read EXIF data")){
                    item.setOnAction(new EventHandler<ActionEvent>(){
                        public void handle(ActionEvent e){
                            PhotonicImageViewer.getInstance().
                                    setIsReadEXIF(exif_check.isSelected());
                        }
                    });
                }
            }
        }
    }
    
    /**
     * Unloads the current image and displays a message to the user on
     * middle of the screen.
     * @param msg The message to display.
     */
    public void displayMessage(String msg){
        error_label.setDisable(false);
        error_label.setOpacity(1);
        error_label.setText(msg);
        disableUI();
        PhotonicImageViewer.getInstance().setWindowTitle(
                PhotonicImageViewer.APP_NAME);
    }
    
    /**
     * Resets the error message label.
     */
    public void resetError(){
        error_label.setText("");
        error_label.setDisable(true);
        error_label.setOpacity(0);
        enableUI();
    }
    
    /**
     * Disables essential UI elements such as the next and previous buttons.
     */
    private void disableUI(){
        isUIDisabled = true;
        main_imageview.setImage(null);
        right_arrow.setActive(false);
        left_arrow.setActive(false);
        next_button.setDisable(true);
        prev_button.setDisable(true);
        file_name_label.setText("");
        for (MenuItem i : disableableItems) i.setDisable(true);
    }
    
    /**
     * Enables essential UI elements such as the next and previous buttons.
     */
    private void enableUI(){
        isUIDisabled = false;
        next_button.setDisable(false);
        prev_button.setDisable(false);
        for (MenuItem i : disableableItems) i.setDisable(false);
    }
    
    /**
        Loads the image file associated with the application's image treader.
    */
    public void loadImage(){
        resetError();
        ImageTreader treader = getTreader();
        Image image = null;
        try{
             image = treader.getImage();
        }
        catch(IOException e){
            PhotonicImageViewer.getInstance().displayError(
                    "Error loading image. "
                    + "\nThe image was probably deleted or moved"
                            + " \nafter the program was launched.");
        }
        main_imageview.setImage(image);
        String filename = treader.getImageName();
        file_name_label.setText(filename);
        PhotonicImageViewer.getInstance().setWindowTitle(filename);
        
        //Read EXIF data
        if (exif_check.isSelected()){
            main_imageview.setOrientation(treader.getImageOrientation());
            switch (main_imageview.getOrientation()){
                case 1:
                    main_imageview.setRotate(0);
                    main_imageview.setScaleX(1);
                    main_imageview.setScaleY(1);
                    break;
                case 2:
                    main_imageview.setRotate(0);
                    main_imageview.setScaleX(-1);
                    main_imageview.setScaleY(1);
                    break;
                case 3:
                    main_imageview.setRotate(180);
                    main_imageview.setScaleX(1);
                    main_imageview.setScaleY(1);
                    break;
                case 4:
                    main_imageview.setRotate(0);
                    main_imageview.setScaleX(1);
                    main_imageview.setScaleY(-1);
                    break;
                case 5:
                    main_imageview.setRotate(270);
                    main_imageview.setScaleX(-1);
                    main_imageview.setScaleY(1);
                    break;
                case 6:
                    main_imageview.setRotate(90);
                    main_imageview.setScaleX(1);
                    main_imageview.setScaleY(1);
                    break;
                case 7:
                    main_imageview.setRotate(90);
                    main_imageview.setScaleX(-1);
                    main_imageview.setScaleY(1);
                    break;
                case 8:
                    main_imageview.setRotate(270);
                    main_imageview.setScaleX(1);
                    main_imageview.setScaleY(1);
                    break;
            }
        }
        else{
            main_imageview.setRotate(0);
            main_imageview.setScaleX(1);
            main_imageview.setScaleY(1);
        }
        resetImageView();
    }
        
    /**
     * Centers the ImageView and correctly scales it to the window size.
     */
    public void resetImageView(){
        //TODO: Test different image sizes and extreme cases for errors.
        //TODO: Rotate image based on EXIF data
        //(search for metadata-extractor lib)
        main_imageview.setScaleX(INITIAL_SCALE *
                (main_imageview.getScaleX() > 0? 1 : -1));
        main_imageview.setScaleY(INITIAL_SCALE
                * (main_imageview.getScaleY() > 0? 1 : -1));
        main_imageview.setTranslateX(0);
        main_imageview.setTranslateY(0);
        main_imageview.setFitWidth(200);
        main_imageview.setFitHeight(200);

        Scene scene = main_container.getScene();
        
        // Get width of available space for the ImageView.
        double centerWidth =  0;
        if (scene != null)
            centerWidth += main_container.getWidth();
        else
            centerWidth += 640 - 20;

        if (main_container.getLeft() != null)
            centerWidth -= main_container.getLeft()
                    .getBoundsInParent().getWidth();
        
        if (main_container.getRight() != null)
            centerWidth -= main_container.getRight()
                    .getBoundsInParent().getWidth();

        centerWidth -= IMAGE_MARGIN * 2;

        // Get height of available space for the ImageView.
        double centerHeight =  0;
        if (scene != null)
            centerHeight += main_container.getHeight();
        else
            centerHeight += 480 - 60;

        if (main_container.getTop() != null)
            centerHeight -= main_container.getTop()
                    .getBoundsInParent().getHeight();
        
        if (main_container.getBottom() != null)
            centerHeight -= main_container.getBottom()
                    .getBoundsInParent().getHeight();

        centerHeight -= IMAGE_MARGIN * 2;
        
        // Fill ImageView to available space.
        double newScale;
        if(centerWidth/centerHeight 
                > main_imageview.getActualWidth()
                / main_imageview.getActualHeight()){
            newScale = centerHeight/main_imageview.getActualHeight();
        }
        else{
            newScale = centerWidth/main_imageview.getActualWidth();
        }
        main_imageview.setScaleX(newScale * main_imageview.getScaleX());
        main_imageview.setScaleY(newScale * main_imageview.getScaleY()); 
    }
    
    /**
     * Overwrites the image's EXIF data with the current orientation displayed.
     * DOES NOT WORK.
     */
    private void saveImageOrientation(){
        int orientation;
        boolean isHorizontallyFlipped = main_imageview.getScaleX() < 0;
        boolean isVerticallyFlipped = main_imageview.getScaleY() < 0;
        double rotation = main_imageview.getRotate();
        
        // Make sure 0 <= rotation < 360
        while(rotation >= 360) rotation -= 360;
        while(rotation < 0) rotation += 360;
        
        if (rotation == 0){
            if (isHorizontallyFlipped && isVerticallyFlipped) orientation = 3;
            else if (isHorizontallyFlipped) orientation = 2;
            else if(isVerticallyFlipped) orientation = 4;
            else orientation = 1;
        }
        else if (rotation == 90){
            if (isHorizontallyFlipped && isVerticallyFlipped) orientation = 8;
            else if (isHorizontallyFlipped) orientation = 7;
            else if(isVerticallyFlipped) orientation = 5;
            else orientation = 6;
        }
        else if (rotation == 180){
            if (isHorizontallyFlipped && isVerticallyFlipped) orientation = 1;
            else if (isHorizontallyFlipped) orientation = 4;
            else if(isVerticallyFlipped) orientation = 2;
            else orientation = 3;
        }
        else{ //rotation == 270
            if (isHorizontallyFlipped && isVerticallyFlipped) orientation = 6;
            else if (isHorizontallyFlipped) orientation = 5;
            else if(isVerticallyFlipped) orientation = 7;
            else orientation = 8;
        }
        
        //getTreader().setImageOrientation(orientation);
    }
    
    /**
     * Scales the main ImageView around a specific point.
     * @param pointX X coordinate of the point to be scaled around.
     * @param pointY Y coordinate of the point to be scaled around.
     * @param in True if zoom IN (increase scale),
     * false if zoom OUT (decrease scale). 
     */
    public void zoom(double pointX, double pointY, boolean in){
        double newScale = Math.abs(main_imageview.getScaleX())
                + ZOOM_AMOUNT * (in ? 1 : -1);
        
        if (newScale >= minimumScale 
                || newScale > Math.abs(main_imageview.getScaleX())){
            double oldWidth =  main_imageview.getActualWidth();
            double oldHeight =  main_imageview.getActualHeight();
            
            // X coordinate of the mouse pointer relative to the node
            double X = pointX; 
            // Y coordinate of the mouse pointer relative to the node
            double Y = pointY; 

            /*
            mX & mY are the ratios between position of pointer
            and the node width & height, where 0.0 <= mX <= 1.0,
            0.0 meaning the pointer is on the left side of the node,
            1.0 meaning the pointer is on the right side of the node,
            and 0.5 meaning the pointer is in the center of the node.
            */
            double mX = X / oldWidth; 
            double mY = Y / oldHeight;

            // Zoom
            main_imageview.setScaleX(newScale * 
                    (main_imageview.getScaleX() > 0? 1 : -1));
            main_imageview.setScaleY(newScale *
                    (main_imageview.getScaleY() > 0? 1 : -1));

            double newWidth = main_imageview.getActualWidth();
            double newHeight = main_imageview.getActualHeight();

            /*
            Consider the base values of offsetX & offsetY
            to be the additve inverse of the translation applied
            by setScaleX() & setScaleY(). Adding the two values to 
            the translate property of the node scales the node around
            the top left corner of the node rather than its center.
            By further modifying the two base values, scaling around
            any point on the node can be achieved. Note that adding the
            additive inverse of the two values to the traslation 
            property of the node makse the scaling occur around the 
            bottom right corner of the node.
            */
            double offsetX = (newWidth - oldWidth) / 2;
            double offsetY = (newHeight - oldHeight) / 2;

            /*
            Focus on (2 * offsetX * mX). Using the rations
            mX & mY from before, scaling around any point can be 
            achieved. To fully understand this code, study the following
            example cases:

            mX = 0.0, mY = 0.0:
                Nothing is subtracted from the base values of offsetX
                & offsetY and scaling occurs around the top left corner
                of the node.
            mX = 1.0, mY = 1.0:
                offsetX & offsetY have become the additive inverse of
                their base values, causing scaling to occur around
                the bottom right corner of the node.
            mX = 0.5, mY = 0.5:
                offsetX & offsetY are now equal to 0.0. No further
                translation is applied and the node is scaled around
                its center.

            You get the idea for all intermediate values of mX & mY.
            */
            offsetX = offsetX - (2 * offsetX * mX);
            offsetY = offsetY - (2 * offsetY * mY);

            // Adjust offset to insure the imageview does not go out of view.
            if (main_imageview.getLeft() + offsetX
                    > main_container.getWidth() - IMAGEVIEW_TRANSLATION_PAD){
                // Scale around left side.
                offsetX = (newWidth - oldWidth) / 2; 
            }
            else if(main_imageview.getRight() + offsetX
                    < IMAGEVIEW_TRANSLATION_PAD){
                // Scale around right side.
                offsetX =  (newWidth - oldWidth) / -2; 
            }

            if (main_imageview.getTop() + offsetY
                    > main_container.getHeight() 
                    - IMAGEVIEW_TRANSLATION_PAD 
                    - bottom_toolbar.getHeight()){
                // Scale around top side.
                offsetY = (newHeight - oldHeight) / 2; 
            }
            else if(main_imageview.getBottom() + offsetY
                    < IMAGEVIEW_TRANSLATION_PAD + top_toolbar.getHeight()){
                // Scale around bottom side.
                offsetY =  (newHeight - oldHeight) / -2; 
            }

            // Apply translation
            main_imageview.offsetX(offsetX);
            main_imageview.offsetY(offsetY);
        }
    }  
    public void zoom(boolean in){
        zoom(main_imageview.getActualWidth() / 2,
                main_imageview.getActualHeight() / 2, in);
    }
    
    /**
     * Initializes the two arrows at either side of the UI.
     */
    private void initArrows(){
        Image inactiveArrow = null;
        Image activeArrow = null;
        try{
            //TODO: Put both arrows in the same file and use viewports??
            // (sort of like a texture sheet)
            inactiveArrow = new Image(
                    new FileInputStream("assets/boxArrowInactive.png")); 
            activeArrow = new Image(
                    new FileInputStream("assets/boxArrowActive.png"));
        }
        catch (IOException e){
            System.out.println("IO Error: " + e.toString());
        }
        
        right_arrow.setActiveImage(activeArrow);
        right_arrow.setInactiveImage(inactiveArrow);
        
        left_arrow.setActiveImage(activeArrow);
        left_arrow.setInactiveImage(inactiveArrow);
    }
    
    /**
     * Initializes the two circle buttons at the bottom of the UI.
     */
    private void initCircleButtons(){
        Image image = null;
        try{
            image = new Image(new FileInputStream("assets/circlebutton.png"));
        }
        catch (IOException e){
            System.out.println("IO Error: " + e.toString());
        }
        
        //Next button functionality
        next_button.setImage(image);
        next_button.setOpacity(CIRCLE_BUTTON_INACTIVE_ALPHA);
        next_button.setFitWidth(CIRCLE_BUTTON_WIDTH);
        next_button.setFitHeight(CIRCLE_BUTTON_HEIGHT);
        next_button.setOnMouseClicked(new EventHandler<MouseEvent>(){
            public void handle(MouseEvent e){
                next();
            }
        });
        next_button.setOnMouseEntered(new EventHandler<MouseEvent>(){
            public void handle(MouseEvent e){
                next_button.setOpacity(CIRCLE_BUTTON_ACTIVE_ALPHA);
            }
        });
        next_button.setOnMouseExited(new EventHandler<MouseEvent>(){
            public void handle(MouseEvent e){
                next_button.setOpacity(CIRCLE_BUTTON_INACTIVE_ALPHA);
            }
        });
        
        //Previous button functionality
        prev_button.setImage(image);
        prev_button.setOpacity(CIRCLE_BUTTON_INACTIVE_ALPHA);
        prev_button.setFitWidth(CIRCLE_BUTTON_WIDTH);
        prev_button.setFitHeight(CIRCLE_BUTTON_HEIGHT);
        prev_button.setOnMouseClicked(new EventHandler<MouseEvent>(){
            public void handle(MouseEvent e){
                prev();
            }
        });
        prev_button.setOnMouseEntered(new EventHandler<MouseEvent>(){
            public void handle(MouseEvent e){
                prev_button.setOpacity(CIRCLE_BUTTON_ACTIVE_ALPHA);
            }
        });
        prev_button.setOnMouseExited(new EventHandler<MouseEvent>(){
            public void handle(MouseEvent e){
                prev_button.setOpacity(CIRCLE_BUTTON_INACTIVE_ALPHA);
            }
        });
    }
    
    /**
     * Loads the next image and resets the ImageView.
     */
    public void next(){
        getTreader().next();
        loadImage();
    }
    
    /**
     * Loads the previous image and resets the ImageView.
     */
    public void prev(){
        getTreader().prev();
        loadImage();
    }
    
    private ImageTreader getTreader(){
        return PhotonicImageViewer.getInstance().getMainAppTreader();
    }
    
    /**
     * Event handler which handles dragging for the main UI ImageView.
     */
    private class ImageViewMouseDragHandler implements EventHandler<MouseEvent>{
            private double initialMouseX;
            private double initialMouseY;
            private boolean wasMousePressed = false;
            
            @Override
            public void handle(MouseEvent e){
                // Set the initial position of the mouse when the user began
                // dragging.
                if (!e.isPrimaryButtonDown()) return;
                
                if (!wasMousePressed && e.isPrimaryButtonDown()){
                    initialMouseX =
                            e.getSceneX() - main_imageview.getTranslateX();
                    
                    initialMouseY =
                            e.getSceneY() - main_imageview.getTranslateY();
                    
                    wasMousePressed = true;
                }
                
                /*
                    newTranslation defines the difference between the initial
                    position of the node when the user began dragging and the 
                    current position of the node.
                */
                double newTranslationX = e.getSceneX() - initialMouseX;
                double newTranslationY = e.getSceneY() - initialMouseY;
                
                if (!willBeOutOfBoundsX(newTranslationX))
                    main_imageview.setTranslateX(newTranslationX);
                
                if (!willBeOutOfBoundsY(newTranslationY))
                    main_imageview.setTranslateY(newTranslationY);
            }
            
            /**
             * Checks if the node will be out of its parent's bounds with the
             * new translation.
             * @param newTranslationX The new X translation value
             * @return Whether or not the node will be out of its parent's
             * bounds
             */
            private boolean willBeOutOfBoundsX(double newTranslationX){
                double addedTranslationX =
                        newTranslationX - main_imageview.getTranslateX();
                
                boolean willBeOutOfRightBounds =
                        main_imageview.getLeft() + addedTranslationX 
                        > main_imageview.getScene().getWidth() - IMAGE_MARGIN;
                
                boolean willBeOutOfLeftBounds =
                        main_imageview.getRight() + addedTranslationX  
                        <= IMAGE_MARGIN;
                
                return willBeOutOfRightBounds || willBeOutOfLeftBounds;
            }
            
            /**
             * Checks if the node will be out if its parent's bounds with the 
             * new translation.
             * @param newTranslationY The new Y translation value
             * @return Whether or not the node will be out of its parent's
             * bounds
             */        
            private boolean willBeOutOfBoundsY(double newTranslationY){
                double addedTranslationY =
                        newTranslationY - main_imageview.getTranslateY();
                
                boolean willBeOutOfBottomBounds =
                        main_imageview.getTop() + addedTranslationY 
                        > main_imageview.getScene().getHeight()
                        - bottom_toolbar.getHeight() - IMAGE_MARGIN;
                
                boolean willBeOutOfTopBounds =
                        main_imageview.getBottom() + addedTranslationY
                        <= top_toolbar.getHeight() + IMAGE_MARGIN;
                
                return willBeOutOfBottomBounds || willBeOutOfTopBounds;
            }
            
            /**
             * Resets the drag handler. To be called once the user stops
             * dragging the node.
             */
            public void reset(){ 
                wasMousePressed = false;
            }
        }
    
}