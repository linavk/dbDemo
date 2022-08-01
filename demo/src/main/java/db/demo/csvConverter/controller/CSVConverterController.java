package db.demo.csvConverter.controller;


import java.io.File;
import java.util.Scanner;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * this program returns a JSON object from an entry in a csv file
 * the entry is identified by an id(RL100-Code)in the second column of the csv file
 * 
 * this program functions as is but there are some obvious next steps that could be taken to improve it such as:
 * automated tests
 * more precise error messages to the user
 * have more flexibility in what the file path looks like
 * 
 */

@RestController
public class CSVConverterController {
	String htmlResponse = new String();
	
	//basic home page when no specific file paths are requested
	@GetMapping()
	public String Welcome() {
		htmlResponse = "<h1>Hallo, das ist ein csv reader.</h1>"
					 + "<p>Gib den gewünschten Pfad folgendermaßen in der Suchleiste ein:"
					 + "http://localhost:8080/betriebsstelle/aamp</p>";	
		return htmlResponse;
	}
	
	//tries to read from file, 
	//returns file contents as JSON object if successful, 
	//gives general instructions on how to phrase requests if unsuccessful
	@GetMapping("/{fpath}/{id}")
	public ResponseEntity<Object> getCSVEntry(@PathVariable String fpath,@PathVariable String id) {
		id = id.toUpperCase();
		JsonNode fileContent = readFromFile(fpath+".csv", id);
		if(fileContent == null) {//could not open file/id not found
			htmlResponse = "<p>Die Anfrage nach \""+id+"\" ist fehlgeschlagen.</p>"
						 + "<p>Gib den gewünschten Pfad folgendermaßen in der Suchleiste ein"
						 + "http://localhost:8080/betriebsstelle/aamp</p>";
			return new ResponseEntity<Object>(htmlResponse, HttpStatus.NOT_FOUND);
		}
		ResponseEntity<Object> response = new ResponseEntity<Object>(fileContent, HttpStatus.ACCEPTED);
		return response;
	
	}
	
	//finds the desired entry in the given file and passes it to stringsToJSON to return the entry as a JSON Object
	public static JsonNode readFromFile(String path, String id) {
		try {
			File f = new File(path);
			Scanner sc = new Scanner(f);
			JsonNode json = null;
			String[] csvHeader = sc.nextLine().split(",");
			while( sc.hasNextLine() ) {
				String[] csvContent = sc.nextLine().split(",");
				if( csvContent[1].equals(id)) {
					json = stringsToJSON(csvHeader, csvContent);
					break;
				}
			}
			sc.close();
			return json;
		}catch (Exception e){
			System.out.println("Exception while trying to access "+path);
			return null;
		}	
	}
	
	//parses the strings read from the csv file into the JSON format
	public static JsonNode stringsToJSON(String[] csvHeader, String[] csvContent) {
		//convert Strings into JSON String
		String jsonString = new String();
		ObjectMapper objectMapper = new ObjectMapper();
		jsonString = "{";
		int j;
		for(j = 0; j < csvHeader.length-1; j++) {
			jsonString = jsonString+"\""+csvHeader[j]+"\""+":"+"\""+csvContent[j]+"\""+",";
		}
		jsonString = jsonString+"\""+csvHeader[j]+"\""+":"+"\""+csvContent[j]+"\""+"}";
		
		//convert JSON String into JSON Object
		JsonNode jsonNode= null;
		try {
			jsonNode = objectMapper.readTree(jsonString);
		} catch (JsonProcessingException e) {
			System.out.println("Exception while trying to convert the string "+ jsonString+ " to JSON");
			e.printStackTrace();
		}
		return jsonNode;
	}
}
