(ns mongo-query.core
  (:use seesaw.core)
  (:require [monger.core :as mg]))

(defn -main [& args]
  (invoke-later
    (-> (frame :title "Mongo Query Tool",
           :content 
           (form-panel
      			:items [
      				[nil :fill :both :insets (java.awt.Insets. 5 5 5 5) :gridx 0 :gridy 0 :gridwidth 1]
      				[(grid-panel :columns 1 
			            :items [			            	
			            	(label :text "Result:" :halign :left)
			            	(text :columns 20 :multi-line? true)
			            	(label :text "Query" :halign :left)
			            	(text :columns 20)
			            ])
			         	:grid :next]
			        ]
        	),
           :on-close :exit)
     pack!
     show!))
)

(defn mongoAvailable? []
	(try 
		((mg/connect!)
  		(mg/get-db-names)
  		true)
	(catch Exception e false)
	)
)