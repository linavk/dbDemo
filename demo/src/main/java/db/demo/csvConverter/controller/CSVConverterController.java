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
 * the entry is identified by an id in the first column of the csv
 * 
 * this program functions as is but there are some obvious next steps that could be taken to improve it such as:
 * automated tests
 * more precise error messages to the user
 * 
 */

@RestController
public class CSVConverterController {
	
	String htmlResponse = new String();
	
	//basic home page when no specific file paths are requested
	@GetMapping()
	public String Welcome() {
		htmlResponse = "<h1>Hallo, das ist ein csv reader.</h1>"
					 + "<p>Gib den gewünschten Pfad folgendermaßen in der Suchleiste ein: http://localhost:8080/Dateipfad/Betriebsstellen-Abkürzung</p>";	
		return htmlResponse;
	}
	
	//tries to read from file with path, returns file contents as object if successful, general instructions on how to phrase requests if unsuccessful
	@GetMapping("/{fpath}/{id}")
	public ResponseEntity<Object> getCSVEntry(@PathVariable String fpath,@PathVariable String id) {
		id = id.toUpperCase();
		JsonNode fileContent = readFromFile(fpath+".csv", id);
		System.out.println("did it work?"+id);
		if(fileContent == null) {//could not open file/id not found
			htmlResponse = "<p>Die Anfrage nach \""+id+"\" ist fehlgeschlagen.</p>"
						 + "<p>Gib den gewünschten Pfad folgendermaßen in der Suchleiste ein: http://localhost:8080/filepath/filename</p>";
			return new ResponseEntity<Object>(htmlResponse, HttpStatus.NOT_FOUND);
		}
		
		ResponseEntity<Object> response = new ResponseEntity<Object>(fileContent, HttpStatus.ACCEPTED);
		return response;
	
	}
	
	public static JsonNode readFromFile(String path, String id) {
		System.out.println("trying to read from"+path);
		try {
			File f = new File(path);
			Scanner sc = new Scanner(f);
			JsonNode json = null;
			String[] csvHeader = sc.nextLine().split(";");
			while( sc.hasNextLine() ) {
				String[] csvContent = sc.nextLine().split(";");
				System.out.println(csvContent[1]+"id"+id+"equals"+csvContent[1].equals(id));
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
	
	//this function parses the strings read from the csv file into the JSON format
	public static JsonNode stringsToJSON(String[] csvHeader, String[] csvContent) {
		
		String jsonString = new String();
		ObjectMapper objectMapper = new ObjectMapper();

		jsonString = "{";
		int j = 1;
		for(; j < csvHeader.length-1; j++) {
			jsonString = jsonString+"\""+csvHeader[j]+"\""+":"+"\""+csvContent[j]+"\""+",";//TODO: make actual object
		}
		jsonString = jsonString+"\""+csvHeader[j]+"\""+":"+"\""+csvContent[j]+"\""+"}";
		
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


/*
Aufgabe

Erstellen Sie ein Programm, das die Daten aus der CSV-Datei einliest und über einen REST-Endpoint zur Verfügung stellt. 
Der Endpunkt soll mit der Abkürzung einer Betriebsstelle angefragt werden und die Daten der Betriebsstelle als JSON-Objekt zurück liefern.

Tipp

Den Code können Sie uns gerne in einem öffentlichen git-repo (z. B. Github) zur Verfügung stellen.

Beispiel-Request:

.../betriebsstelle/aamp

Beispiel-Response:

HTTP-STATUS: 200

{

  Name: Hamburg Anckelmannsplatz      

  Kurzname: Anckelmannsplatz  

  Typ: Üst

}
*/