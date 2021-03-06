(ns mongo-query.core
          (:require [monger.core :as mongo])
          (:require [monger.collection :as mc])
          (:use [clojure.string :only [trim split]])
          )

(def tablename "mongo-query")
(def host "127.0.0.1")
(def port 27017)

(let [^ServerAddress serverAddress (mongo/server-address host port)]
  (mongo/connect! serverAddress)
  (mongo/set-db! (mongo/get-db "documents"))
  )

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



;;(allDocuments)


;;(mongoQuery {:fields '("name"), :table "documents", :filters {:name "John"} :sort-fields nil, :direction nil})
;;(mongoQuery {:table "documents" :fields '("name")})


;;(parse-sql "select name from documents where name = 'John'")

(defn mongoQuery [query]
  (mc/find-maps
   (:table query)
   (:filters query)
   (:fields query)))

(defn getIds [query]
        (map #(get % :_id) (mongoQuery query))
)

;;(convert-filter-operator {:key "name", :operator "=", :value "John"})


(defn convert-filter-operator [filter-operator]
        (let [op (get filter-operator :operator)
              keyAsKeyword (keyword (get filter-operator :key))
              value (get filter-operator :value)
              keyValue (get filter-operator :key)]
                (cond
                        (= op "=")  {:key keyValue :value value}
                        (= op "!=") {keyAsKeyword (hash-map :$ne value)}
                        (= op ">")  {keyAsKeyword (hash-map :$gt value)}
                        (= op "<")  {keyAsKeyword (hash-map :$lt value)}
                        (= op ">=") {keyAsKeyword (hash-map :$gte value)}
                        (= op "<=") {keyAsKeyword (hash-map :$lte value)}
                        :else nil
                )))



(defn type-coerce-filter-expression [filter-list]
  (letfn [(convert [val]
            (cond
             (re-matches #"'.*'" val) ((re-find #"'(.*)'" val) 1)
             (re-matches #"\d+" val) (Integer/parseInt val)
             (re-matches #"\d*\.\d+" val) (Float/parseFloat val)
             (re-matches #"\d+\.\d*" val) (Float/parseFloat val)
             (re-matches #"(true|false)" val) (Boolean/parseBoolean val)
             :else val))]
    (map (fn [{field :field op :op val :val}] {:field field :op op :val (convert val)}) filter-list)))

(defn parse-sql [sql-string]
  (letfn [(parse-filters [filter-string]
            (if (nil? filter-string)
              nil
              (map #(zipmap '(:field :op :val) %)(map #(split % #" +") (map trim (split filter-string #"(?i)and"))))))
          (parse-fields [field-string]
            (if (nil? field-string)
              nil
              (map trim (split field-string #","))))]
    (let [sql-regex #"(?i)^\s*select(.+?) from(.+?)( where (.+?))?( order by (.+?))?(asc|desc)?\s*;?\s*$"
          group-matches (re-seq sql-regex sql-string)]
      (if (nil? group-matches)
        nil
        (let [groups (first group-matches)
              [_ fields table & filters-and-sort] groups
              [_ filters _ sort-fields direction] filters-and-sort]
          {:fields (parse-fields fields)
           :table (trim table)
           :filters (type-coerce-filter-expression (parse-filters filters))
           :sort-fields (parse-fields sort-fields)
           :direction direction})))))
