(ns mongo-query.core
  (:use seesaw.core)
  (:require mongo-query.mongo)
)

(defn generateTable [results]
    (table :id :table
      :model [
      :columns (vec (map #(hash-map :key % :text %) (set (mapcat keys results))))
      :rows results
      ]
    )
)

(defn testGenerateTable [] (generateTable [{:name "name" :location "london"} {:test "test"} {:name "noone" :location "world" :test "test1"}]))

(def queryText (text :columns 20 :multi-line? true))

(defn parseString [s] (parse-sql s))

(defn -main [& args]
  (invoke-later
    (-> (frame :title "Mongo Query Tool",
           :content 
           (form-panel
      			:items [
      				[nil :fill :both :insets (java.awt.Insets. 5 5 5 5) :gridx 0 :gridy 0 :gridwidth 1]
      				[(grid-panel :columns 2 
			            :items [			            	
			            	(label :text "Results:" :halign :left)
                    :fill-h
                    (testGenerateTable)
                    :fill-h
			            	(label :text "Query" :halign :left)
                    :fill-h
			            	queryText
                    (button :text "Query!" :listen [:action (fn [e] (alert (parseString (value queryText) )))])
			            ])
			         	:grid :next]
			        ]
        	),
           :on-close :exit)
     pack!
     show!))
)
