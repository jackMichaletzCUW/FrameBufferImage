import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Generator {

	BufferedImage image;
	BufferedImage grey;
	
	int frameBufferWidth;
	int frameBufferHeight;
	
	int[] colors = {
			0x000000,
			0x0414a7,
			0x18a818,
			0x1aaaa9,
			0xa8050e,
			0xa817a8,
			0xa85514,
			0xaaaaaa,
			0x555555,
			0x555afa,
			0x5afa5a,
			0x5afafa,
			0xfa5555,
			0xfa5afa,
			0xfafa64,
			0xffffff
	};
	
	int[] generatedColors;
	String outputDirectory;
	
	public Generator(String fileName, String outputDirectory, int frameBufferWidth, int frameBufferHeight) {
		try {
			image = ImageIO.read(new File(fileName));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		this.outputDirectory = outputDirectory;
		
		this.frameBufferWidth = frameBufferWidth;
		this.frameBufferHeight = frameBufferHeight;
		
		grey = toGray();
		
		try {
			ImageIO.write(grey, "png", new File(outputDirectory + "/output.png"));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		generateColors();
		generateText();
	}
	
	private void setClipboardContents(String string){
	    StringSelection stringSelection = new StringSelection(string);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(stringSelection, stringSelection);
	}
	
	private void generateColors() {
		generatedColors = new int[colors.length * colors.length * 4];
		
		BufferedImage picture = new BufferedImage(64, 16, BufferedImage.TYPE_3BYTE_BGR);

		for(int o = 0; o < colors.length; o++) {
			for(int i = 0; i < colors.length; i++) {
				int baseIndex = (o * (colors.length * 4)) + (i * 4);
				
				generatedColors[baseIndex] = colors[o];
				generatedColors[baseIndex + 1] = mixColors(colors[o], colors[i], 0.2);
				generatedColors[baseIndex + 2] = mixColors(colors[o], colors[i], 0.6);
				generatedColors[baseIndex + 3] = mixColors(colors[o], colors[i], 0.9);
				
				//System.out.println();
			}
		}
		
		for (int i = 0; i < generatedColors.length; i++) {
			picture.setRGB(i / 16, i % 16, generatedColors[i]);
		}
		
		try {
			ImageIO.write(picture, "png", new File(outputDirectory + "/palette.png"));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private int mixColors(int colorOne, int colorTwo, double ratio) {
		int backRed = (colorOne >> 16) & 0xFF;
		int backGreen = (colorOne >> 8) & 0xFF;
		int backBlue = (colorOne) & 0xFF;

		int frontRed = (colorTwo >> 16) & 0xFF;
		int frontGreen = (colorTwo >> 8) & 0xFF;
		int frontBlue = (colorTwo) & 0xFF;

		int mixedRed = (int)(((double)backRed * (1.0 - ratio)) + ((double)frontRed * ratio));
		int mixedGreen = (int)(((double)backGreen * (1.0 - ratio)) + ((double)frontGreen * ratio));
		int mixedBlue = (int)(((double)backBlue * (1.0 - ratio)) + ((double)frontBlue * ratio));
				
		return ((mixedRed << 16) | (mixedGreen << 8) | mixedBlue);
	}
	
	private int getClosestSixteen(int startx, int starty, int finishx, int finishy) {
		int averageRed = 0;
		int averageGreen = 0;
		int averageBlue = 0;
		
		for(int x = startx; x < finishx; x++) {
			for(int y = starty; y < finishy; y++) {
				averageRed += ((image.getRGB(x, y) >> 16) & 0xFf);
				averageGreen += ((image.getRGB(x, y) >> 8) & 0xFf);
				averageBlue += (image.getRGB(x, y) & 0xFf);
			}
		}
		
		int totalPixels = (finishx - startx) * (finishy - starty);
		
		averageRed /= totalPixels;
		averageGreen /= totalPixels;
		averageBlue /= totalPixels;
		
		int smallestDifference = 9999;
		int smallestIndex = 0;
		
		for(int c = 0; c < colors.length; c++) {
			int difference = (int)Math.pow(Math.abs(((colors[c] >> 16) & 0xff) - averageRed), 2) 
					+ (int)Math.pow(Math.abs(((colors[c] >> 8) & 0xff) - averageGreen), 2)
					+ (int)Math.pow(Math.abs((colors[c] & 0xff) - averageBlue), 2);
			
			if(difference < smallestDifference) {
				smallestDifference = difference;
				smallestIndex = c;
			}
		}
		
		return smallestIndex;
	}
	
	private int getClosestColor(int startx, int starty, int finishx, int finishy) {
		int averageRed = 0;
		int averageGreen = 0;
		int averageBlue = 0;
		
		for(int x = startx; x < finishx; x++) {
			for(int y = starty; y < finishy; y++) {
				averageRed += ((image.getRGB(x, y) >> 16) & 0xFf);
				averageGreen += ((image.getRGB(x, y) >> 8) & 0xFf);
				averageBlue += (image.getRGB(x, y) & 0xFf);
			}
		}
		
		int totalPixels = (finishx - startx) * (finishy - starty);
		
		averageRed /= totalPixels;
		averageGreen /= totalPixels;
		averageBlue /= totalPixels;
		
		int smallestDifference = 9999;
		int smallestIndex = 0;
		
		for(int c = 0; c < generatedColors.length; c++) {
			int difference = (int)Math.pow(Math.abs(((generatedColors[c] >> 16) & 0xff) - averageRed), 0.5) 
					+ (int)Math.pow(Math.abs(((generatedColors[c] >> 8) & 0xff) - averageGreen), 0.5)
					+ (int)Math.pow(Math.abs((generatedColors[c] & 0xff) - averageBlue), 0.5);
			
			if(difference < smallestDifference) {
				smallestDifference = difference;
				smallestIndex = c;
			}
		}
		
		return smallestIndex;
	}
	
	private void generateText( ) {
		String code = "short bob[] = {\n\t";
		
		int pixelLength = image.getWidth() / frameBufferWidth;
		int pixelHeight = image.getHeight() / frameBufferHeight;
		
		BufferedImage picture = new BufferedImage(frameBufferWidth, frameBufferHeight, BufferedImage.TYPE_3BYTE_BGR);
		
		for (int fy = 0; fy < frameBufferHeight; fy++) {
			for (int fx = 0; fx < frameBufferWidth; fx++) {	
				long averageValue = 0;
				
				for(int x = (pixelLength * fx); x < (pixelLength * (fx + 1)); x++) {
					for(int y = (pixelHeight * fy); y < (pixelHeight * (fy + 1)); y++) {
						averageValue  += (grey.getRGB(x, y) & 0xFF);
					}
				}
				
				averageValue /= (pixelLength * pixelHeight);
				
				double percentBlack = (double)averageValue / 255.0;
				
				char terminalOutput = '9';
				
				if(percentBlack <= 0.2) {
					terminalOutput = (char )9608;
				}else if(percentBlack <= 0.4) {
					terminalOutput = (char )9619;
				}else if(percentBlack <= 0.6) {
					terminalOutput = (char )9618;
				}else if(percentBlack <= 0.8) {
					terminalOutput = (char )9617;
				}else {
					terminalOutput = ' ';
				}
				
				int color = getClosestColor((pixelLength * fx), (pixelHeight * fy), (pixelLength * (fx + 1)), (pixelHeight * (fy + 1)));
				
				int top = getClosestSixteen((pixelLength * fx), (pixelHeight * fy), (pixelLength * (fx + 1)), (int)((double)pixelHeight * ((double)fy + 0.5)));
				int bottom = getClosestSixteen((pixelLength * fx), (int)((double)pixelHeight * ((double)fy + 0.5)), (pixelLength * (fx + 1)), (pixelHeight * (fy + 1)));
				 
				int right = getClosestSixteen((int)((double)pixelLength * (fx + 0.5)), (pixelHeight * fy), (pixelLength * (fx + 1)), (pixelHeight * (fy + 1)));
				int left = getClosestSixteen((pixelLength * fx), (pixelHeight * fy), (int)((double)pixelLength * (fx + 0.5)), (pixelHeight * (fy + 1)));
				 
							
				char output = '9';
				byte out = 0x00;
				
				if(color % 4 == 3) {
					output = (char )9619;
					out = (byte )178;
				}else if(color % 4 == 2) {
					output = (char )9618;
					out = (byte )177;
				}else if(color % 4 == 1) {
					output = (char )9617;
					out = (byte )176;
				}else {
					output = ' ';
					out = (byte )32;
				}
				
				System.out.printf("%c", terminalOutput);
				
				int background = color / (colors.length * 4);
				int foreground = (color % (colors.length * 4)) / 4;
				
				short codeOut = 0;
				
				if(top == bottom) {
					codeOut = (short)(((background & 0xF) << 12) | ((foreground & 0xF) << 8) | (out & 0xff));
					picture.setRGB(fx, fy, generatedColors[color]);
				} else /*if(right == left)*/ {
					codeOut = (short)(((top & 0xF) << 12) | ((bottom & 0xF) << 8) | 0xDC);
					picture.setRGB(fx, fy, colors[top]);
				} /*else {
					codeOut = (short)(((left & 0xF) << 12) | ((right & 0xF) << 8) | 0xDE);
					picture.setRGB(fx, fy, colors[left]);
				}*/
				
				if(fx % 8 == 0) {
					code += String.format("0x%X,\n\t", codeOut);
				} else {
					code += String.format("0x%X, ", codeOut);
				}
			}
			System.out.println();
		}
		
		code  += "};";
		
		//System.out.println(code);
		setClipboardContents(code);
		
		try {
			ImageIO.write(picture, "png", new File(outputDirectory + "/outputbmp.png"));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private BufferedImage toGray() {
		BufferedImage returnValue = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		
		for(int x = 0; x < image.getWidth(); x++) {
			for(int y = 0; y < image.getHeight(); y++) {
				int colorAverage = (image.getRGB(x, y) & 0xFF) + ((image.getRGB(x, y) >> 8) & 0xFF) + ((image.getRGB(x, y) >> 16) & 0xFF);
				colorAverage /= 3;
				
				int newColor = ((colorAverage & 0xFf) << 16) | ((colorAverage & 0xFF) << 8) | (colorAverage & 0xFF);
				returnValue.setRGB(x, y, newColor);
			}
		}
		
		return returnValue;
	}
	
}
