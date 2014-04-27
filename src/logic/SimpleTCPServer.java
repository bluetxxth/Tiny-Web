package logic;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class SimpleTCPServer {

	private int m_port = 0;
	private String m_errorFileName = "error404.html";
	private String m_error404 = "error404.html";
	private String m_fileName = null;
	private String m_headerLine = null;
	private DataOutputStream m_writer;
	private InputStream m_reader;
	private final String m_CRLF = "\r\n";
	private String m_serverMessage = "Starting PytteWeb1.0_V1 Http Server";


	/**
	 * Constructs a simple server that can listen to a port
	 */
	public SimpleTCPServer(int mPort) {
		this.m_port = mPort;
	}

	/**
	 * Method starts up the server
	 * @throws IOException
	 */
	public void runServer() throws IOException {

		boolean run = true;

		try {
			ServerSocket serverSocket = new ServerSocket(m_port);

			while (run == true) {

				try {

					Socket clientHandlerSocket = serverSocket.accept();

					run = handleRequest(clientHandlerSocket);

					clientHandlerSocket.close();

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}/* end while */

		} catch (Exception ex) {
			ex.printStackTrace();

		}

	}/* end method */

	/**
	 * Method handles the http requests
	 * @param clientHandlerSocket
	 * @return run - true or false
	 * @throws Exception 
	 */
	private boolean handleRequest(Socket clientHandlerSocket)
			throws Exception {
		
		boolean run = true;

		// the request
		m_writer = new DataOutputStream(clientHandlerSocket.getOutputStream());

		// just need an input stream this time
		m_reader = clientHandlerSocket.getInputStream();

		m_headerLine = readRequest(m_reader, m_CRLF);
	
		// find out if the header starts with QUIT and if so close 
		//GET, HEAD, Etc.. continue running
		if (m_headerLine.toUpperCase().startsWith("QUIT")) {
			run = false;
		} else {

			// Apply parser
			m_fileName = parser();

			// remove the first character which will be the bar
			m_fileName = removeFirstChar(m_fileName);

			setupProtocolAndSend();

		}
		return run;
	}

	/**
	 * Method set up status codes and send messages.
	 * @throws Exception 
	 */
	private void setupProtocolAndSend() throws Exception {


		// Send status
		String statusLine = null;
		// content type
		String contentType = null;
		// content length
		String contentLength = "Request Error";
		//Check the content type
		contentType = getContentType(m_fileName);
		
		
		Boolean m_fileExist = true;

		// try sending filename to input stream
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(m_fileName);
		} catch (FileNotFoundException e) {
			m_fileExist = false;
		}

		// set the status lines for when the file exists
		if (m_fileExist != false) {
			// If everything okay it will be throwing this status line
			statusLine = "HTTP/1.0 200 OK";
			contentType = "Content-type: " + contentType;
			contentLength = "Content-Length: "
					+ (new Integer(fis.available())).toString();
			
		} else if (m_fileExist == false) {
			// if the file does not exist it will set this status line
			statusLine = "HTTP/1.0 404 NOT FOUND";
			// the content type of the error is text/html
			contentType = "tex/html";
			// set to file containing error
			m_fileName = m_error404.trim();
			
		} else {
			// if there is whatever miscellaneous failure it will throw this
			statusLine = "HTTP/1.0 400 BAD REQUEST";
			// the content type of the error is text/html
			contentType = "tex/html";
			//set to file containing error
			m_fileName = m_errorFileName.trim();
		}

		// Send the status line.
		m_writer.write(statusLine.getBytes());
		System.out.println(statusLine);
		
		// Send a blank line to indicate the end of the header lines.
		m_writer.write(m_CRLF.getBytes());
		System.out.println(m_CRLF);
		
		m_writer.write(m_serverMessage.getBytes());
		System.out.println(m_serverMessage);
		
		// Send a blank line to indicate the end of the header lines.
		m_writer.write(m_CRLF.getBytes());
		System.out.println(m_CRLF);

		// Send the content type
		m_writer.write(contentType.getBytes());
		System.out.println(contentType);
		
		// Send a blank line to indicate the end of the header lines.
		m_writer.write(m_CRLF.getBytes());
		System.out.println(m_CRLF);

		// Send the Content-Length
		m_writer.write(contentLength.getBytes());
		System.out.println(contentLength);
		
		// Send a blank line to indicate the end of the header lines.
		m_writer.write(m_CRLF.getBytes());
		System.out.println(m_CRLF);

		// Send header line
		m_writer.write(m_headerLine.getBytes());
		System.out.println(m_headerLine);

		// Send a blank line to indicate the end of the header lines.
		m_writer.write(m_CRLF.getBytes());
		System.out.println(m_CRLF);
		
		// Send a blank line to indicate the end of the header lines.
		m_writer.write(m_CRLF.getBytes());
		System.out.println(m_CRLF);

		// send file
		readAndSendFile(m_fileName, m_writer);
	    //readAndSendJarFile(m_fileName, m_writer);
	}


	/**
	 * Method reads the whole request
	 * 
	 * @param input
	 * @param stopSign
	 * @return
	 * @throws IOException
	 */
	private static String readRequest(InputStream input, String stopSign)
			throws IOException {
		int available = 0;
		String received = "";

		// read as long as the stop sign is not found
		while (!received.endsWith(stopSign)) {
			available = input.available();
			byte[] bytes = new byte[available];
			input.read(bytes);
			received += new String(bytes);
		}
		return received.trim();
	}

	/**
	 * Methods sends bytes
	 * 
	 * @param fileName the name of the file
	 * @param writer the data output stream
	 * @throws IOException - throws this exception if file not found
	 */
	private void readAndSendFile(String fileName, DataOutputStream writer)
			throws IOException {
		InputStream is = new FileInputStream(fileName);
		final int BUFFER_SIZE = 1024;
		byte[] buffer = new byte[BUFFER_SIZE];

		while (is.read(buffer, 0, BUFFER_SIZE) != -1) {
			writer.write(buffer);
		}
	}

	/**
	 * Method reads a jar file - must be placed on the bin directory of the java
	 * project folder
	 * 
	 * @param fileName - The name of the file
	 * @param writer - the data output stream
	 * @throws IOException throws this exception if file not found
	 */
	private void readAndSendJarFile(String fileName, DataOutputStream writer)
			throws IOException {
		InputStream is = this.getClass().getResourceAsStream("/" + fileName);

		final int BUFFER_SIZE = 2048;
		byte[] buffer = new byte[BUFFER_SIZE];

		while (is.read(buffer, 0, BUFFER_SIZE) != -1) {
			writer.write(buffer);
		}
	}
	

	/**
	 * Method parses the header line and advances gets the file name
	 * 
	 * @return the file name and the slash bar in front of it.
	 */
	private String parser() {

		// Use tokenizer to skip GET and go to the file name
		StringTokenizer s = new StringTokenizer(m_headerLine);
		String temp = s.nextToken();

		if (temp.equals("GET")) {
			// Advance to next this will be the file name
			m_fileName = s.nextToken();

		}
		return m_fileName;
	}

	/**
	 * Method removes the first char on a string
	 * 
	 * @param fileName - the file name
	 * @return fileName the file without the first forward slash
	 */
	private String removeFirstChar(String fileName) {
		return fileName.substring(1);
	}

	/**
	 * Method get file extensions
	 * @param fileName - the file name
	 * @return returns the applications octet streams.
	 */
	private static String getContentType(String fileName) {

		while (fileName.isEmpty() || fileName == null) {
			return "text/html";

		}
		if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}else if(fileName.endsWith(".pdf")){
			return "application/pdf";
		}else if (fileName.endsWith(".txt")) {
			return "text/plain";
		} else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
			return "image/jpeg";
		} else if (fileName.endsWith(".gif")) {
			return "image/gif";
		} else {
			return "application/octet-stream";

		}

	}

}/* end class */
