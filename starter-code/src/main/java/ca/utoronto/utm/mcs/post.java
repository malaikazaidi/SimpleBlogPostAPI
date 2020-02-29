package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class post implements HttpHandler{

	@Override
	public void handle(HttpExchange r) throws IOException {
		// TODO Auto-generated method stub
		try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
               
            }
            else if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
               
            }
            else if (r.getRequestMethod().equals("DELETE")) {
                handleDelete(r);
               
            }
            else {
            	//Send 400 error
				r.sendResponseHeaders(400, 0);
				OutputStream os = r.getResponseBody();
		        os.close();
            }
        } catch (Exception e) {
        	//send 500 error
        	r.sendResponseHeaders(500, 0);
        	OutputStream os = r.getResponseBody();
	        os.close();
            e.printStackTrace();
        }
		
	}

	private void handleDelete(HttpExchange r) {
		// TODO Auto-generated method stub
		
	}

	private void handlePut(HttpExchange r) {
		// TODO Auto-generated method stub
		
	}

	private void handleGet(HttpExchange r) {
		// TODO Auto-generated method stub
		
	}

}
