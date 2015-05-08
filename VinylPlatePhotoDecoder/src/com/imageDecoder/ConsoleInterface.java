package com.imageDecoder;

import java.util.Scanner;

public class ConsoleInterface {

	public static void main(String[] args) {
		
		String imageName = "";
		AudioDecoder decoder = new AudioDecoder();
		Scanner scanIn = new Scanner(System.in);
		String input;
			
				while(true){	
					
							System.out.println("Please select the image to process:");
							System.out.println("Enter " + 1 + ", " + 2 + " or " + 3 + " to make a choice or enter " + 0 + " to exit..."); 
						
							input = scanIn.nextLine();
						
								switch (input){
								
										case "1": 	imageName = "images\\01.png";
													break;
										
										case "2": 	imageName = "images\\02.png";
													break;
										
										case "3": 	imageName = "images\\03.png";
													break;
										
										case "0": 	scanIn.close();
													System.out.println("Closing program.."); 
													System.exit(0);
										
										default: 	imageName = null;
													System.out.println("Invalid input! Please enter valid value! Only 1, 2 and 3 allowed!");
													break;					
									}
						
										if(imageName!= null){
											decoder.makeAudioFile(imageName);
										}
						}
		
		}

}
