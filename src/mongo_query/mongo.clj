(ns mongo-query.core
	  (:require [monger.core :as mongo])
	  (:require [monger.collection :as mc]))

(def tablename "mongo-query")
(def host "127.0.0.1")
(def port 27017)

(let [^ServerAddress serverAddress (mongo/server-address host port)]
  (mongo/connect! serverAddress))

(defn insertTestData []
	(mc/remove "documents")
	(map #(mc/insert "documents" %) [
		{:name "John" :age 18}
		{:name "Mary" :age 49}
		{:name "Bob" :age 30 :location "London"}
		{:name "Noone" :age 31 :location "Oslo"}
	])
)

(defn allDocuments [] (mc/find-maps "documents"))

(defn mongoQuery [query] (
	mc/find-maps 
		(get query :table "documents")
		(get query :filter) 
		(get query :select)
))

(defn getIds [query]
	(map #(get % :_id) (mongoQuery query))
)

(defn convertFilterOperator [filterOperator]
	(let [
			op (get filterOperator :operator)
			keyAsKeyword (keyword (get filterOperator :key))
			value (get filterOperator :value)
			keyValue (get filterOperator :key)
		 ]
		(cond
			(= op "=")  {:key keyValue :value value}
			(= op "!=") {keyAsKeyword (hash-map :$ne value)}
			(= op ">")  {keyAsKeyword (hash-map :$gt value)}
			(= op "<")  {keyAsKeyword (hash-map :$lt value)}
			(= op ">=") {keyAsKeyword (hash-map :$gte value)}
			(= op "<=") {keyAsKeyword (hash-map :$lte value)}
			:else nil
		)
	)
)