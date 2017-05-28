# CS504-Homework4

1. Look up synonyms table for each query term and construct rewritten query online then send to ads selector

	query: 3 terms, 5 synonyms per term
	how many rewritten query? 
	5 * 5 * 5 = 125
	5^n ,n is length of query
	Phone number combination
    
        Online query rewrite: Changes are in web.xml, SearchAds.java, AdsEngine.java, and QueryParser.java.

2. For each query in historical click log, construct rewritten query offline and store original query -> {rewritten query } to key value store.  On web server side, we look up this key value store for each query to get rewritten query list, then send to ads selector 

        Need memcached running and port is 11212
        To run offline query rewrite: python generate_query_rewrite.py click_log_small5.txt Synonyms_0502.txt
        Web server siade: Changes are in SearchAdsEngine.java and QueryParser.java.

