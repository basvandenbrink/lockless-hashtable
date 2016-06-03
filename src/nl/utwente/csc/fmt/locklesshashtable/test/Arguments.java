package nl.utwente.csc.fmt.locklesshashtable.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import nl.utwente.csc.fmt.locklesshashtable.spehashtable.Hashtable;

import com.lexicalscope.jewel.cli.*;

public class Arguments {
	private PrintStream out = System.out;
	private int[][] vectors;
	private boolean debug;
	private int numberOfThreads;
	private Hashtable hashtable = null;
	private boolean continueWithRandom;
	
	@Option(defaultToNull=true)
	void setDebugFile(String vectorFile) throws FileNotFoundException{
		if(vectorFile != null){
			out = new PrintStream(vectorFile);
		}
	}
	
	@Option
	void setVectorFile(String vectorFile) throws FileNotFoundException{
		vectors = Test.readVectors(new FileInputStream(new File(vectorFile)));
		hashtable = new Hashtable(vectors.length);
	}
	
	@Option
	void setDebug(boolean debug){
		this.debug = debug;
	}
	
	@Option
	void setcontinueWithRandom(boolean continueWithRandom){
		this.continueWithRandom = continueWithRandom;
	}
	
	@Option(defaultValue="4")
	void setNumberOfThreads(int numberOfThreads){
		this.numberOfThreads = numberOfThreads;
	}
	
	public PrintStream getPrintStream(){
		return out;
	}
	
	public int[][] getVectors(){
		return vectors;
	}
	
	public boolean getDebug(){
		return debug;
	}
	
	public boolean getContinueWithRandom(){
		return continueWithRandom;
	}
	
	public int getNumberOfThreads(){
		return numberOfThreads;
	}
	
	public Hashtable getHashtable(){
		return hashtable;
	}
}
