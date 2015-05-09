package com.imageDecoder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class AudioDecoder {
	
	private byte[] inputArray;
	
	public void makeAudioFile(String imageName){
		
		ImageProcessor processor = new ImageProcessor();
		String outputName = imageName.substring(6, 9);
		AudioInputStream ais = null;
			try {
	
				inputArray = processor.colorExtract(imageName);
				int rate =(int) (inputArray.length/18);
				String path = System.getProperty("user.home") + "\\Desktop\\";
							
				AudioFormat   format= new AudioFormat(rate,8,1,true,false);
				ais = new AudioInputStream(new ByteArrayInputStream(inputArray),format ,inputArray.length);
				AudioSystem.write(ais, AudioFileFormat.Type.WAVE ,new File(path + outputName + ".wav"));
				System.out.println("Audio file is ready for listening! Location  "+ path + outputName + ".wav");
				
				} catch (IOException e) { 
					
					System.err.println("Cannot create or save a file!");
					e.printStackTrace();
					
				} finally {
							try {
								ais.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
				}
		}
}	
	
