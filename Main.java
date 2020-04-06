
public class Main {

	public static void main(String[] args) {
		String fileName = args[0];
		String outputDirectory = args[1];
		int frameBufferWidth = Integer.parseInt(args[2]);
		int frameBufferHeight = Integer.parseInt(args[3]);
		
		Generator g = new Generator(fileName, outputDirectory, frameBufferWidth, frameBufferHeight);
	}
	
}
