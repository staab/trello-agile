(ns trello-agile.core
  (:gen-class)
  (:require [trello-agile.client :as client]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (clojure.pprint/pprint (client/get "tokens/09849c1f9c029e784e6bee82239bc847785df54c612c0f595ace6477355fb892")))
