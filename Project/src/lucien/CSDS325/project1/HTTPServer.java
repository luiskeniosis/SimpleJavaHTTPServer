package lucien.CSDS325.project1;

import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
public class HTTPServer {
    private static int serverPort;
    private static String filePathPrefix;
    private static String indexFilePath;

    public static void main(String[] args) {
	//Reads config file, default location is //src/server.config
	try {readConfig();}
	catch (Exception exception) {
	    exception.printStackTrace();
	    System.out.println("Error in reading config file. Please check that the file exists and contains no errors. Stopping execution.");
	    System.exit(1);
	}
	
	//Establishes welcoming socket
	System.out.println("Starting server on port: " + serverPort);
	ServerSocket serverSocket = null;
	try {
	    serverSocket = new ServerSocket(serverPort);
	    serverSocket.setReuseAddress(true);
	    //Blocking method, to accept incoming requests in individual threads
	    while(true) {
		try(Socket client = serverSocket.accept()){
		    new SocketThread(client);
		}
	    }
	}
	catch (Exception exception){
	    exception.printStackTrace();
	    System.out.println("Encountered error: " + exception.toString()+ ". Stopping execution.");
	}
	
	//Closes the socket on program exit from error
	finally {
	    if(serverSocket != null)
		try {serverSocket.close();}
	    catch (Exception exception) {
		exception.printStackTrace();
		System.out.println("Error in closing socket, socket likely already closed. Proceed as usual.");
	    }
	}
    }

    private static void readConfig() throws Exception {
	//Uses the Properties API from Java 7 to read a simple config file
	Properties properties = new Properties();
	String configFileLocation = System.getProperty("user.dir") + "/server.config";
	properties.load(new FileInputStream(configFileLocation));
	serverPort = Integer.parseInt(properties.getProperty("server.port"));
	filePathPrefix = properties.getProperty("html.filePathPrefix");
	indexFilePath = properties.getProperty("html.indexFilePath");
    }

    //Getter methods used by SocketThread class to get information read from the config file
    public static String getFilePathPrefix() { return filePathPrefix;}
    public static String getIndexFilePath() { return indexFilePath;}
}
