package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
import java.util.List;

public class Post implements HttpHandler{

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
            	//Send 405 error, method not found
				r.sendResponseHeaders(405, 0);
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

	private void handleGet(HttpExchange r) throws JSONException, IOException {
		// TODO Auto-generated method stub
		String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        MongoClient client = MongoClients.create();
        MongoDatabase database = client.getDatabase("csc301a2");
        MongoCollection<Document> collection = database.getCollection("posts");
        
        if (deserialized.has("_id")){
        	
    		String id_string = deserialized.getString("_id");
        	
        	ObjectId id = new ObjectId(id_string);
            
            Document myDoc = collection.find(eq("_id", id)).first();
            if (myDoc != null) {
            	JSONArray array = new JSONArray();
            	
            	JSONObject response = new JSONObject();
        		ObjectId idVal = (ObjectId) myDoc.get("_id");
        		String author = (String) myDoc.get("author");
        		String content = (String) myDoc.get("content");
        		String titleVal = (String) myDoc.get("title");
        		List<String> tags = (List<String>) myDoc.get("tags");
        		
        		JSONObject idObj = new JSONObject();
        		idObj.put("$oid", id);
        		
        		response.put("_id", idObj);
        		response.put("content", content);
        		response.put("title", titleVal);
        		response.put("tags", tags);
        		response.put("author", author);
        		
        		array.put(response);
            	
        		OutputStream os = r.getResponseBody();
        		r.sendResponseHeaders(200, array.toString().getBytes().length);
        		os.write(array.toString().getBytes());
        		os.close();
            } else {
            	r.sendResponseHeaders(404, 0);
        		OutputStream os = r.getResponseBody();
    	        os.close();
            }
        	
        	
        } else if (deserialized.has("title")) {
        	
    		String title = deserialized.getString("title");
        	
        	
        	MongoCursor<Document> cursor = collection.find(regex("title", ".*"+title+".*")).iterator();
        	//JSONObject response = new JSONObject();
        	
        	if (cursor != null) {
        		JSONArray array = new JSONArray();
            	
            	while (cursor.hasNext()) {
            		Document item = cursor.next();
            		
            		JSONObject response = new JSONObject();
            		ObjectId id = (ObjectId) item.get("_id");
            		String author = (String) item.get("author");
            		String content = (String) item.get("content");
            		String titleVal = (String) item.get("title");
            		List<String> tags = (List<String>) item.get("tags");
            		
            		JSONObject idObj = new JSONObject();
            		idObj.put("$oid", id);
            		
            		response.put("_id", idObj);
            		response.put("content", content);
            		response.put("title", titleVal);
            		response.put("tags", tags);
            		response.put("author", author);
            		
            		array.put(response);
            	}
            	cursor.close();
            	
            	OutputStream os = r.getResponseBody();
        		r.sendResponseHeaders(200, array.toString().getBytes().length);
        		os.write(array.toString().getBytes());
        		os.close();
        	} else {
        		r.sendResponseHeaders(404, 0);
        		OutputStream os = r.getResponseBody();
    	        os.close();
        	}
        	
        	
        } else {
        	//missing title and id in request body
        	r.sendResponseHeaders(400, 0);
        	OutputStream os = r.getResponseBody();
	        os.close();
        }
		
	}

}
