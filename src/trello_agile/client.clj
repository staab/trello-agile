(ns trello-agile.client
  (:gen-class)
  (:require [environ.core :refer [env]]
            [clj-http.client :as client]
            [clojure.data.json :as json]))

(def base-url "https://api.trello.com/1")

(def auth {:key "e86a65f72ce377e5438961a83071ef89"
           :token (env :trello-token)})

(defn qp [query-params] (merge query-params auth))

(defn url [rel-path] (str base-url "/" rel-path))

(defn get
  ([rel-path query-params]
   (json/read-str
     (:body
       (client/get (url rel-path) {:query-params (qp query-params)}))))
  ([rel-path]
   (get rel-path {})))