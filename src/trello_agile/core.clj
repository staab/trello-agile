(ns trello-agile.core
  (:gen-class)
  (:require [trello-agile.client :refer [get-data]]
            [trello-agile.data :refer [transform-data]]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (->>
    (get-data)
    (transform-data)
    (clojure.pprint/pprint)))
