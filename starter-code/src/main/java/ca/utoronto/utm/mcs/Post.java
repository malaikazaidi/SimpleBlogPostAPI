package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
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

import javax.inject.Inject;

public class Post implements HttpHandler{
	
	private String title;
	private String author;
	private String content;
	private String tags;
	private JSONObject response = new JSONObject();
	private ObjectId id;
	private MongoClient db;
	
	@Inject 
	public Post(MongoClient db) {
		this.db = db;
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

	private void deletepost(ObjectId id, HttpExchange r) throws JSONException, IOException {
		MongoDatabase database = db.getDatabase("csc301a2");
		MongoCollection<Document> collection = database.getCollection("posts");
		
		FindIterable<Document> cursor = null;
        try {
        	cursor = collection.find(eq("_id", id));
        	cursor.first().toString();
        } catch(Exception e) {
            r.sendResponseHeaders(404, 0);
          
        }
		collection.deleteOne(new Document("_id",id));
		
		r.sendResponseHeaders(200, 0);
    	OutputStream os = r.getResponseBody();
        os.close();
	}

	private void addpost(String title, String author, String content, String tags,HttpExchange r) throws IOException, JSONException {

		 MongoDatabase database = db.getDatabase("csc301a2");
		 MongoCollection<Document> collection = database.getCollection("posts");
		 List<String> tagarray = new ArrayList<String>();
		 for (String i: Utils.parseRecord(tags)) {
			 tagarray.add(Utils.removequotations(i));
		 }
		 Document doc = new Document()
	                .append("title", title)
	                .append("author", author)
	                .append("content", content)
	                .append("tags", tagarray);
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
        	id = new ObjectId(deserialized.getString("_id"));
        
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
        
        if(deserialized.has("title") && deserialized.has("author") && deserialized.has("content") && deserialized.has("tags") && deserialized.length() == 4) {
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

	private void handleGet(HttpExchange r) throws JSONException, IOException {
		// TODO Auto-generated method stub
		JSONObject deserialized;
		try {
			deserialized = new JSONObject(Utils.convert(r.getRequestBody()));
		} catch (Exception e){
			r.sendResponseHeaders(400, 0);
        	OutputStream os = r.getResponseBody();
	        os.close();
	        return;
		}
        
        
        MongoDatabase database = this.db.getDatabase("csc301a2");
        MongoCollection<Document> collection = database.getCollection("posts");
        
        if (deserialized.has("_id")){
        	Object obj= deserialized.get("_id");
        	if (!obj.getClass().getSimpleName().equals("String")) {
        		//wrong type
        		r.sendResponseHeaders(400, 0);
            	OutputStream os = r.getResponseBody();
    	        os.close();
        	} else {
	        	
	            Document myDoc;
	        	try {
		    		this.id = new ObjectId((String)obj);
		    		myDoc = collection.find(eq("_id", id)).first();
		    		
		    		JSONArray array = new JSONArray();
		        	
		    		this.author = (String) myDoc.get("author");
		    		this.content = (String) myDoc.get("content");
		    		this.title = (String) myDoc.get("title");
		    		List<String> myTags = (List<String>) myDoc.get("tags");
		    		
		    		JSONObject idObj = new JSONObject();
		    		idObj.put("$oid", this.id);
		    		
		    		this.response.put("_id", idObj);
		    		this.response.put("content", this.content);
		    		this.response.put("title", this.title);
		    		this.response.put("tags", myTags);
		    		this.response.put("author", this.author);
		    		
		    		array.put(this.response);
		        	
		    		OutputStream os = r.getResponseBody();
		    		r.sendResponseHeaders(200, array.toString().getBytes().length);
		    		os.write(array.toString().getBytes());
		    		os.close();
	        	} catch (Exception e) {
	        		//not found 
	        		r.sendResponseHeaders(404, 0);
	            	OutputStream os = r.getResponseBody();
	    	        os.close();
	    	        return;
	        	}
	            
            	
        	}
        } else if (deserialized.has("title")) {
        	Object titleObj = deserialized.get("title");
        	
        	if (!titleObj.getClass().getSimpleName().equals("String")) {
        		//wrong type
        		r.sendResponseHeaders(400, 0);
            	OutputStream os = r.getResponseBody();
    	        os.close();
        	} else {
        		this.title = (String)titleObj;
            	
        		MongoCursor<Document> cursor = null;
        		cursor = collection.find(regex("title", ".*"+this.title+".*")).iterator();
        		if (cursor.hasNext()) {
            		JSONArray array = new JSONArray();
                	
                	while (cursor.hasNext()) {
                		Document item = cursor.next();
                		
                		//JSONObject response = new JSONObject();
                		this.id = (ObjectId) item.get("_id");
                		this.author = (String) item.get("author");
                		this.content = (String) item.get("content");
                		//String titleVal = (String) item.get("title");
                		List<String> myTags = (List<String>) item.get("tags");
                		
                		JSONObject idObj = new JSONObject();
                		idObj.put("$oid", id);
                		
                		this.response.put("_id", idObj);
                		this.response.put("content", this.content);
                		this.response.put("title", this.title);
                		this.response.put("tags", myTags);
                		this.response.put("author", this.author);
                		
                		array.put(this.response);
                	}
                	cursor.close();
                	
                	OutputStream os = r.getResponseBody();
            		r.sendResponseHeaders(200, array.toString().getBytes().length);
            		os.write(array.toString().getBytes());
            		os.close();
            	} else {
            		//no data found in database
            		r.sendResponseHeaders(404, 0);
            		OutputStream os = r.getResponseBody();
        	        os.close();
                }
        	}
        } else {
        	//missing title and id in request body
        	r.sendResponseHeaders(400, 0);
        	OutputStream os = r.getResponseBody();
	        os.close();
        }
		
	}

}
