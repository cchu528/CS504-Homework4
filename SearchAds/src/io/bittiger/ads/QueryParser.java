package io.bittiger.ads;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import net.spy.memcached.MemcachedClient;


public class QueryParser {
	private static final int SYNONYMS_PER_TERM = 5;
	private static QueryParser instance = null;
	
	protected QueryParser() {
		
	}
	public static QueryParser getInstance() {
	      if(instance == null) {
	         instance = new QueryParser();
	      }
	      return instance;
    }
	
	public List<String> QueryUnderstand(String query) {
		List<String> tokens = Utility.cleanedTokenize(query);
		return tokens;
	}
	
	
	// online query rewrite
	public List<List<String>> QueryRewrite(String query, SynonymsDictionary synonymsDict) {
		LinkedList<String> synonymsList = new LinkedList<>();
		List<String> tokens = Utility.cleanedTokenize(query);
		
		for (int i = 0, numQuery = 0; i < tokens.size(); i++) {
			List<String> synonyms = synonymsDict.lookup(tokens.get(i));
			if (synonyms.size() == 0) {
				synonyms = Arrays.asList(tokens.get(i));
			}
			int numSynonyms = Math.min(synonyms.size(), SYNONYMS_PER_TERM);
		
			/*
			System.out.print("Synonyms for word " + tokens.get(i) + ": ");
			for (int j=0; j<numSynonyms; j++) {
				if (j > 0) System.out.print(", ");
				System.out.print(synonyms.get(j));
			}
			System.out.println();
			*/
			
			if (synonymsList.isEmpty()) {
				for (int j=0; j<numSynonyms; j++) {
					synonymsList.add(synonyms.get(j));
				}
				numQuery = numSynonyms;
			} else {
				int count = 0;
				while (synonymsList.peek() != null && count < numQuery) {
					String q = synonymsList.remove();
					// System.out.println("Remove query: " + q);
					for (int j=0; j<numSynonyms; j++) {
						synonymsList.add(q + "_" + synonyms.get(j));
						// System.out.println("Add query: " + q + "_" + synonyms.get(j));
					}
					count++;
				}
				numQuery *= numSynonyms;
			}
		}
		
		// add result as queryTerms
		List<List<String>> result = new ArrayList<List<String>>();
		if (synonymsList.isEmpty()) {
			result.add(tokens);
		} else {
			for (String synonym : synonymsList) {
				List<String> tokenList = Arrays.asList(synonym.split("_"));
				result.add(tokenList);
			}
		}
		
		return result;
	}
	
	// offline query rewrite
	public List<List<String>> QueryRewrite(String query, String mMemcachedServer, int mMemcachedPortal) {
		List<List<String>> result = new ArrayList<List<String>>();
		List<String> tokens = Utility.cleanedTokenize(query);
		String query_key = Utility.strJoin(tokens, "_");
		try {
			MemcachedClient cache = new MemcachedClient(new InetSocketAddress(mMemcachedServer,mMemcachedPortal));
			if (cache.get(query_key) instanceof String) {
				String temp = (String) cache.get(query_key);
				temp = temp.substring(1, temp.length() - 2);
				temp = temp.replaceAll("\"", "");
				List<String> synonyms = Arrays.asList(temp.split(","));
				for (String synonym : synonyms) {
	    			List<String> tokenList = Arrays.asList(synonym.split("_"));
	    			result.add(tokenList);
	    		}		
		    } else {
		    		result.add(tokens);
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		
		// query rewrite testing
		SynonymsDictionary synonymsDict = SynonymsDictionary.getInstance("/Users/chifenchu/project/SearchAds/data/deduped/Synonyms_0502.txt");
		String query = "bluetooth speaker shower outdoor";
		
		List<List<String>> rewrittenQuery = QueryParser.getInstance().QueryRewrite("bluetooth speaker shower outdoor", synonymsDict);
		
		System.out.println("query: " + query);
		System.out.println("Online rewrittenQuery: ");
		for (List<String> queryTerms: rewrittenQuery) {
			System.out.println(Utility.strJoin(queryTerms, "_"));
		}
		
		rewrittenQuery = QueryParser.getInstance().QueryRewrite("bluetooth speaker shower outdoor", "127.0.0.1", 11212);
		System.out.println("query: " + query);
		System.out.println("Offline rewrittenQuery: ");
		for (List<String> queryTerms: rewrittenQuery) {
			System.out.println(Utility.strJoin(queryTerms, "_"));
		}
	}
}