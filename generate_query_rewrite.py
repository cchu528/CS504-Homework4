import sys
import json
import libmc
from sets import Set
from pyspark import SparkContext
from libmc import ( 
    MC_HASH_MD5, MC_POLL_TIMEOUT, MC_CONNECT_TIMEOUT, MC_RETRY_TIMEOUT
)

def store_rewrite_query(memcache_client, key, val):
    memcache_client.set(key,val)
    print "key=",key
    print "val=",val
   
def generate_rewrite_query(query, synonyms_dict, memcache_client):
    result=[]
    terms = query.split(' ')

    for term in terms:
       synonyms = synonyms_dict[term]
       tmp=[]
       if len(result) > 0:
          for y in result:
             if len(synonyms) > 0:
                count = 0
                for x in synonyms:
                   if count >=  5:
                      break
                   tmp.append(y + "_" + x)
                   count = count + 1
             else:
                tmp.append(y + "_" + term)
       else:
          count = 0
          for x in synonyms:
             if count >=  5:
                break
             tmp.append(x)
             count = count + 1
             
       result = tmp

    key = "_".join(terms)
    val = json.dumps(result)
    store_rewrite_query(memcache_client, key, val)


if __name__ == "__main__":
   click_log_file = sys.argv[1] #raw click log
   synonyms_data_file = sys.argv[2] # synonyms file

   client = libmc.Client (
        ["127.0.0.1:11212"],comp_threshold=0, noreply=False, prefix=None,hash_fn=MC_HASH_MD5, failover=False
   )
   client.config(MC_POLL_TIMEOUT, 100) #100 ms
   client.config(MC_CONNECT_TIMEOUT, 300) # 300 ms
   client.config(MC_RETRY_TIMEOUT, 5) # 5 s

   synonyms_dict = {}

   with open(synonyms_data_file, "r") as lines:
        for line in lines:
            entry = json.loads(line.strip())
            if  "word" in entry and "synonyms" in entry:
                    word = entry["word"].lower().encode('utf-8')
                    synonyms = entry["synonyms"] 
                    synonyms = [elm.lower().encode('utf-8') for elm in synonyms]
                    synonyms_dict[word] = synonyms

   with open(click_log_file, "r") as lines:
        for line in lines:
            fields = line.split(",")         
            generate_rewrite_query(fields[3], synonyms_dict, client)

