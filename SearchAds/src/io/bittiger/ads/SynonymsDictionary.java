package io.bittiger.ads;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class SynonymsDictionary {
	private static SynonymsDictionary instance = null;
	private static HashMap<String, ArrayList<String>> synonymsTable;

	public SynonymsDictionary(String synonyms_File) {
		synonymsTable = new HashMap<String, ArrayList<String>>();
		try (BufferedReader synonymsReader = new BufferedReader(new FileReader(synonyms_File))) {
			String line;
			while((line = synonymsReader.readLine()) != null) {
				JSONObject parameterJson = new JSONObject(line);
				JSONArray synonyms = parameterJson.isNull("synonyms")? null : parameterJson.getJSONArray("synonyms");
				ArrayList<String> synonymsList = new ArrayList<String>();
				for (int i = 0; i < synonyms.length(); i++) {
					synonymsList.add(synonyms.getString(i));
				}
				synonymsTable.put(parameterJson.getString("word"), synonymsList);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static SynonymsDictionary getInstance(String synonyms_File) {
		if (instance == null) {
			instance = new SynonymsDictionary(synonyms_File);
		}
		return instance;
	}
	
	public List<String> lookup (String word) {
		
		return synonymsTable.get(word);
	}
}
