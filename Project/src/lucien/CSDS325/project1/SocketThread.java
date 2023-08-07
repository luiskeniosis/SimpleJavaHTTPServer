package lucien.CSDS325.project1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths; 
public class SocketThread implements Runnable { 
    private Socket client;
    private int visits = 1;

    public SocketThread(Socket connectionSocket){
	client = connectionSocket;
	System.out.println(client.getLocalAddress() + " established connection.");
	run();
    }

    public void run() {
	//Creates a buffered reader to accept input from connected client
	BufferedReader clientSocketStream = null;
	try {
	    clientSocketStream = new BufferedReader(new InputStreamReader(client.getInputStream()));

	    //Creates String processor to read request
	    StringBuilder requestProcessor = new StringBuilder();
	    String line;
	    while (!(line = clientSocketStream.readLine()).isEmpty()) {
		requestProcessor.append(line + "\r\n");
	    }

	    //Processes request lines/headers
	    String request = requestProcessor.toString();
	    String[] requestsLines = request.split("\r\n");
	    String path = requestsLines[0].split(" ")[1];
	    String cookieField = "";
	    for(String field : requestsLines) {
		if(field.contains("Cookie")) {
		    cookieField = field;
		    String[] cookies = cookieField.split(" ");
		    visits = Integer.parseInt(cookies[1].replace(";", "").replace("visits=", ""));
		}
	    }

	    //Formats and prepares file requested
	    if(path.equals("/") || path.equals("/lyl18") || path.equals("/lyl18/"))
		path = HTTPServer.getIndexFilePath();
	    Path filePath = Paths.get(System.getProperty("user.dir") + HTTPServer.getFilePathPrefix(), path);
	    System.out.println(client.getLocalAddress() + " requested:");
	    System.out.println(filePath.toString() + "\n");

	    //Sends final response to client
	    if (Files.exists(filePath)) {
		/*The iFrame element in test2.html loads test1.html, which incorrectly counts the visits counter by 3, rather than 2
		This code corrects it by simply not counting the visit to test2.html itself. */
		if(path.equals("/lyl18/test2.html") || path.equals("/lyl18/test2.html/"))
		    sendResponse(client, "200 OK", "text/html", false, Files.readAllBytes(filePath));
		else
		    //Regular response
		    sendResponse(client, "200 OK", "text/html", true, Files.readAllBytes(filePath));
	    }
	    //Response to the visits page
	    else if(path.equals("/lyl18/visits.html") || path.equals("/lyl18/visits.html/")) {
		byte[] visits = buildVisitsPage();
		sendResponse(client, "200 OK", "text/html", true, visits);
	    }
	    //All else - page not found response
	    else {
		byte[] notFound = "<h1>Page Not Found!</h1>".getBytes();
		sendResponse(client, "404 Not Found", "text/html", false, notFound);
	    }
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
    }

    //Generates the bytes from given HTML code to generate the visits page using the appropriate number of visits
    private byte[] buildVisitsPage() {
	String visitsHTMLToString = "<html><head><title>Visits Page (LYL18)</title></head><body><h1>Visits!</h1><p>Your browser visited various URLs on this site " + visits + " times (including this visit).</p></body></html>";
	return visitsHTMLToString.getBytes();
    }

    //Method to send back response
    private void sendResponse(Socket client, String status, String contentType, boolean sendCookie, byte[] content) throws IOException {
	OutputStream outputStream = client.getOutputStream();
	outputStream.write(("HTTP/1.1 \r\n" + status).getBytes());
	outputStream.write(("ContentType: " + contentType + "\r\n").getBytes());
	if(sendCookie) {
	    visits++;
	    outputStream.write(("Set-Cookie: visits=" + visits + "; Path=lyl18/; sessionId=1s2d3f4g; SameSite=Strict; HttpOnly" + "\r\n").getBytes());
	}
	outputStream.write("\r\n".getBytes());
	outputStream.write(content);
	outputStream.write("\r\n\r\n".getBytes());
	outputStream.flush();
	client.close();
    }

}   