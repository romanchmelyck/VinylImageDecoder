package com.imageDecoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageProcessor {
	
	private int width;
	private int height;
	private int yCenter;
	private int endRadius;
	private int deltaRadius;
	private int realXCenter;
	private int realYCenter;
	private int soundLineWidth;
	private int xLeftPlateEdge;
	private int xRightPlateEdge;
	private int yUpperPlateEdge;
	private int yLowerPlateEdge;
	private int leftSoundLineLeftEdge;
	private int rightSoundLineLeftEdge;
	private int leftSoundLineRightEdge;
	private int spaceBetweenSoundLines;
	private int rightSoundLineRightEdge;
	private int arraySize = 0;
	private short[][] imageColors; 
	private short[] rawAudio;
	
	public byte[] colorExtract(String fileName){
	
			readPixels(fileName);
			locateImageEdges();
			calculateRadiuses();
			calculateTrack();
			readTrack();
			
			byte[] audioArray = new byte[arraySize];
					
		// reading from  array of shorts, decreasing values by -128 to match byte limits (-128 to 127) 
					
				for(int index = 0; index < arraySize; index++){
						audioArray[index] = (byte) (rawAudio[arraySize-1 -index]-128);
					}
			
			return audioArray;
		}
	
	private void locateImageEdges(){
		
			int yCoord = 0;
			int xCoord = 0;
			int tempColor = 0;
			int searchZone = 100;
			
		// upper edge  y search
			
			boolean run = true;
					
				while (run){
					for(int i = (width/2 - searchZone); i<(width/2 + searchZone); i++){
							tempColor = imageColors[i][yCoord];                   
								if(tempColor>90){
									yUpperPlateEdge = yCoord;
									run = false;
									} 
							}
						yCoord++;
					}
			
		//lower Y edge search
			
			yCoord = height-1;
			tempColor = 0;
			run	= true;
					
				while (run){
					for(int i = (width/2 - searchZone); i<(width/2 + searchZone); i++){
							tempColor = imageColors[i][yCoord];
								if(tempColor>90){
									yLowerPlateEdge = yCoord;
									run = false;
									} 
							}
						yCoord--;
					}
					
		// left X  plate edge 
					
			yCoord = 0;
			xCoord = 0;
			tempColor = 0;	
			run	= true;
					
				while (run){
					for(int i = yCenter - searchZone; i<yCenter + searchZone; i++){
							tempColor = imageColors[xCoord][i];      
								if(tempColor>90){
									xLeftPlateEdge = xCoord;
									run = false;
									} 
							}
						xCoord++;
					}
		
		// right X plate edge
						
			yCoord = 0;
			xCoord = width-1;
			tempColor = 0;
			run = true;
					
				while (run){
					for(int i = yCenter - searchZone; i<yCenter + searchZone; i++){
							tempColor = imageColors[xCoord][i];
								if(tempColor>90){
									xRightPlateEdge = xCoord;
									run = false;
									}	
							}
						xCoord--;
					}			
		}
	
	private void calculateRadiuses(){
			
			int yDiameter;
			int xDiameter;
			
			yCenter = (yLowerPlateEdge-yUpperPlateEdge )/2;
			yDiameter = yLowerPlateEdge-yUpperPlateEdge;
			xDiameter = xRightPlateEdge-xLeftPlateEdge;
			
		// searching for x and y deltas 
			
			int dX = (width - xRightPlateEdge - xLeftPlateEdge)/2;
			int dY = (height - yLowerPlateEdge -yUpperPlateEdge)/2;
			
		// real image center
			realXCenter = width/2 - dX;
			realYCenter = height/2 - dY;
			
		// END radius search
			
			endRadius = (xDiameter + yDiameter)/4 -(leftSoundLineLeftEdge -xLeftPlateEdge +1) ;
			
		}
	
	private void calculateTrack(){
		
		// search for left sound line left edge  
			int intPlateEdgeX;
			int locator = xLeftPlateEdge;
					
					 while((imageColors[locator][realYCenter])<150){
							locator++;
						}
								 
			leftSoundLineLeftEdge = locator;
		
		// left sound line right edge
		
			locator = leftSoundLineLeftEdge;

					 while(imageColors[locator][realYCenter]>150){
							locator++;
						}
								 
			leftSoundLineRightEdge = locator;
		
		// search for right sound line right edge  
		
			locator = xRightPlateEdge;

					 while(imageColors[locator][realYCenter]<150){
							locator--;
						}
								 
			rightSoundLineRightEdge = locator;
		
		// right sound line left edge
					
			locator = rightSoundLineRightEdge;

					 while(imageColors[locator][realYCenter]>150){
							locator--;
						}
		
			rightSoundLineLeftEdge = locator;
		
		// search for internal plate  x edge
	
			locator= width/2;
	
					while(imageColors[locator][realYCenter]<90){
							locator--;
						}
	
			intPlateEdgeX =locator;
	
		// search for internal sound line  right edge
		
			locator = intPlateEdgeX;
		
					while(imageColors[locator][realYCenter]<150){
							locator--;
						}
		
		
		// sound line width calculating
					
			soundLineWidth = Math.max((leftSoundLineRightEdge - leftSoundLineLeftEdge),(rightSoundLineRightEdge - rightSoundLineLeftEdge));
					
		// Calculating space between sound lines. Left side of plate.
					 
			int leftSpace = 0;
			int rightSpace =0;
							
			locator = leftSoundLineRightEdge;
							
					while(imageColors[locator][realYCenter]<180){
							locator++;
						}
										
			leftSpace = locator - leftSoundLineRightEdge;
					
		// left side of plate. Calculating space between sound lines	
							
			locator = rightSoundLineLeftEdge;
							
					while(imageColors[locator][realYCenter]<180){
							locator--;
						}
							
			rightSpace = rightSoundLineLeftEdge - locator;
							
		// Average space from left and right values		
							
			spaceBetweenSoundLines =Math.max(leftSpace, rightSpace);
						
		// Calculating  how radius changes on each turn 
					
			deltaRadius = spaceBetweenSoundLines + soundLineWidth; 
		}			
	
	private void readTrack(){
		
			rawAudio = new short[500000];				// destination array that will hold image data after processing
			double densityCoeff = 0.125;				// this value determines fractions of angle change  in degrees. 0.125 means 1/8-th of degree per iteration
			double angle = 0.0;					// angle in degrees
			double angleRad = 0;					// angle in radians
			int correction = 0;     				// correction of the radius 
			double radius =0;
			int x= 0;
			int y= 0;
			int colorLevel =150;					// threshold color level - needed to distinct track from background
			int centralReader = 0;					// central reader gets color from image to be saved and processed in audio decoder
			int leftTracker = 0;					// leftTracker reads color closer to the center of image
			int rightTracker = 0;					// rightTracker reads color closer to the right of image
			int counter = 0;
		 	int startRadius = 0;
		 	boolean run= true;
				 	
		// Searching x coordinate to start reading  
		// Moving X from the center of image  to the right until central and right trackers will be on tracks
					
			x = realXCenter;
									
				do {
					leftTracker =  imageColors[x-8][realYCenter]; 									
					centralReader = imageColors[x][realYCenter]; 
					rightTracker= imageColors[x+8][realYCenter];
														
						if ((rightTracker > colorLevel)&(centralReader > colorLevel)){
										run = false;
								}
						x ++;
													
					} while (run);
														
					startRadius = x - realXCenter;
											
									
		// calculating angle in radians and cartesian coordinates of sound track and getting corresponding color from array
					
				do{
					
					angleRad = (angle* Math.PI/ 180);
					radius = startRadius + correction + (deltaRadius * angleRad)/(2 * Math.PI);				
					x = realXCenter + (int) (radius* Math.cos(angleRad));
					y = realYCenter + (int) (radius* Math.sin(angleRad));
					rawAudio[ counter] = imageColors[x][y] ;  
													
				//radius corrector
								
						if ((angle%360)==0) { 				// correction is made once in a turn if needed
																																				
							leftTracker = imageColors[x-4][y]; 					//left and right trackers					
							rightTracker = imageColors[x+4][y];					// normally trackers should be between sound tracks
														
							if ((rightTracker>colorLevel)&&(leftTracker<colorLevel)) {
																
										correction ++;
																		
							} else if ((rightTracker<colorLevel)&&(leftTracker>colorLevel)) {
																			
										correction --;
																	
								} 
							}
												
				// end of radius corrector
						
					angle += densityCoeff;
					counter ++;
				
				} while (radius<(endRadius + soundLineWidth));
								
			arraySize = counter;
		}
	
	
	private void readPixels(String fileName){
		
			try {
						
				BufferedImage im = ImageIO.read(new File(fileName));
					
				height = im.getHeight();
				width  = im.getWidth();
				yCenter = height/2;
				imageColors = new short[height][width];
							
			// reading image color (only blue channel from RGB) to 2-dimensional array;
						
					for (int x = 0; x< width; x++){
						for (int y = 0; y < height; y++){
							imageColors[x][y] = (short) (im.getRGB(x, y)&0xFF);
							}
						}
						
					im.flush();
								
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
}
