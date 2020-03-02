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
	
	private Object title;
	private Object author;
	private Object content;
	private Object tags;
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

	private void deletepost(ObjectId id, HttpExchange r) throws JSONException, IOException {
		MongoDatabase database = db.getDatabase("csc301a2");
		MongoCollection<Document> collection = database.getCollection("posts");
		System.out.println(id);
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

	private void addpost(Object title, Object author, Object content, Object tags,HttpExchange r) throws IOException, JSONException {

		 MongoDatabase database = db.getDatabase("csc301a2");
		 MongoCollection<Document> collection = database.getCollection("posts");
		 List<String> tagarray = new ArrayList<String>();
		 JSONArray tag = (JSONArray) tags;
		 for (int i =0; i<tag.length();i++) {
			 if((tag.get(i)).getClass() != String.class) {
				 r.sendResponseHeaders(400, 0);
			     OutputStream os = r.getResponseBody();
			     os.close();
			 }
			 else {
				 tagarray.add(tag.get(i).toString());
			 }
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
	     addpost(title, author,content, tags, r);
	
	}

	private void handleDelete(HttpExchange r) throws JSONException, IOException{
		// TODO Auto-generated method stub
		try {
			String body = Utils.convert(r.getRequestBody());
	        JSONObject deserialized = new JSONObject(body);
	        
	        if(deserialized.has("_id")) {
	        	Object idd = deserialized.get("_id");
	        	if(!(idd.getClass() == String.class )) { 
	        		r.sendResponseHeaders(400, 0);
	            	OutputStream os = r.getResponseBody();
	    	        os.close();
	        	}
	        	else {
	        		
	        		String check = "^[0-9a-fA-F]{24}$";
	        		if(idd.toString().matches(check)) {
	        			id = new ObjectId(idd.toString());
	        			deletepost(id,r);
	        		}
	        		else {
	        			r.sendResponseHeaders(400, 0);
	                	OutputStream os = r.getResponseBody();
	        	        os.close();
	        		}
	        		
	        	}
	        
	        }
	        else {
	        	r.sendResponseHeaders(400, 0);
	        	OutputStream os = r.getResponseBody();
		        os.close();
	        }
		}
		catch(Exception e) {
			r.sendResponseHeaders(400, 0);
        	OutputStream os = r.getResponseBody();
	        os.close();
		}
		
	}

	private void handlePut(HttpExchange r) throws JSONException, IOException {
		// TODO Auto-generated method stub
		try {
			String body = Utils.convert(r.getRequestBody());
	        JSONObject deserialized = new JSONObject(body);
	        
	        if(deserialized.has("title") && deserialized.has("author") && deserialized.has("content") && deserialized.has("tags") && deserialized.length() == 4) {
	        	title = deserialized.get("title");
	        	author = deserialized.get("author");
	        	content = deserialized.get("content");
	        	tags = deserialized.get("tags");
	        	
	        	if(!(title.getClass() == String.class && author.getClass() == String.class  && content.getClass() == String.class && tags.getClass() == JSONArray.class)) { 
	        		r.sendResponseHeaders(400, 0);
	            	OutputStream os = r.getResponseBody();
	    	        os.close();
	        	}
	        	
	        }
	        else {
	        	r.sendResponseHeaders(400, 0);
	        	OutputStream os = r.getResponseBody();
		        os.close();
	        }
		}
		catch(Exception e) {
			r.sendResponseHeaders(400, 0);
        	OutputStream os = r.getResponseBody();
	        os.close();
		}
		
	}

	private void handleGet(HttpExchange r) throws JSONException, IOException {
		// TODO Auto-generated method stub
		String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        MongoDatabase database = this.db.getDatabase("csc301a2");
        MongoCollection<Document> collection = database.getCollection("posts");
        
        if (deserialized.has("_id")){
        	this.id = new ObjectId(deserialized.getString("_id"));
            
            Document myDoc = collection.find(eq("_id", id)).first();
            if (myDoc != null) {
            	JSONArray array = new JSONArray();
            	
            	//JSONObject response = new JSONObject();
        		//this.id = (ObjectId) myDoc.get("_id");
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
            } else {
            	r.sendResponseHeaders(404, 0);
        		OutputStream os = r.getResponseBody();
    	        os.close();
            }
        } else if (deserialized.has("title")) {
        	
    		this.title = deserialized.getString("title");
        	
        	
        	MongoCursor<Document> cursor = collection.find(regex("title", ".*"+this.title+".*")).iterator();
        	//JSONObject response = new JSONObject();
        	
        	if (cursor != null) {
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
