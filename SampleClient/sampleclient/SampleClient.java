package jsr268gp.sampleclient;
import javax.swing.SwingUtilities;

public class SampleClient {
	public SampleClient() {
		super();
	}

	public static void main(String[] args){

			SwingUtilities.invokeLater(new Runnable() {
	    	    public void run() {
	    	        new inter1();
	    	    }
	    	});
	}

}