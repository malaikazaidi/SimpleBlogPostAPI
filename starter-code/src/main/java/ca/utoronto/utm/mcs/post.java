package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bson.Document;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.*;

public class post implements HttpHandler{
	
	private MongoClient db;
	private String title;
	private String author;
	private String content;
	private String tags;
	private JSONObject response = new JSONObject();
	private String id;
	
	public post(MongoClient mongoClient) {
		this.db = mongoClient;

	}

	@Override
	public void handle(HttpExchange r) throws IOException {
		// TODO Auto-generated method stub
		try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
               
               
            }
            else if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
                addpost(this.title, this.author, this.content, this.tags, r);
               
            }
            else if (r.getRequestMethod().equals("DELETE")) {
                handleDelete(r);
                deletepost(this.id,r);
               
            }
            else {
            	//Send 405 error
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

	private void deletepost(String id, HttpExchange r) throws JSONException, IOException {
		MongoDatabase database = db.getDatabase("csc301a2");
		MongoCollection<Document> collection = database.getCollection("posts");
		
		FindIterable<Document> cursor = null;
        try {
        	cursor = collection.find(eq("_id", new ObjectId(id)));
        	cursor.first().toString();
        } catch(Exception e) {
            r.sendResponseHeaders(404, 0);
          
        }
		collection.deleteOne(new Document("_id", new ObjectId(id)));
		
		r.sendResponseHeaders(200, 0);
    	OutputStream os = r.getResponseBody();
        os.close();
	}

	private void addpost(String title, String author, String content, String tags,HttpExchange r) throws IOException, JSONException {

		 MongoDatabase database = db.getDatabase("csc301a2");
		 MongoCollection<Document> collection = database.getCollection("posts");
		 
		 Document doc = new Document()
	                .append("title", this.title)
	                .append("author", this.author)
	                .append("content", this.content)
	                .append("tags", this.tags);
		 collection.insertOne(doc);
		 
		 response.put("_id", doc.getObjectId("_id"));
		 r.sendResponseHeaders(200, response.toString().getBytes().length);
		   
	        
	     OutputStream os = r.getResponseBody();
	     os.write(response.toString().getBytes());
	     os.close();
	
	}

	private void handleDelete(HttpExchange r) throws JSONException, IOException{
		// TODO Auto-generated method stub
		String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if(deserialized.has("_id")) {
        	id = deserialized.getString("_id");
        
        }
        else {
        	r.sendResponseHeaders(400, 0);
        	OutputStream os = r.getResponseBody();
	        os.close();
        }
		
	}

	private void handlePut(HttpExchange r) throws JSONException, IOException {
		// TODO Auto-generated method stub
		String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if(deserialized.has("title") && deserialized.has("author") && deserialized.has("content") && deserialized.has("tags")) {
        	title = deserialized.getString("title");
        	author = deserialized.getString("author");
        	content = deserialized.getString("content");
        	tags = deserialized.getString("tags");
        	
       
        }
        else {
        	r.sendResponseHeaders(400, 0);
        	OutputStream os = r.getResponseBody();
	        os.close();
        }
		
	}

	private void handleGet(HttpExchange r) {
		// TODO Auto-generated method stub
		
	}

}
