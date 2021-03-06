(ns trello-agile.client
  (:require [environ.core :refer [env]]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [trello-agile.utils :refer [map-vals clog]]
            [slugger.core :refer [->slug]]))

(def base-url "https://api.trello.com/1")

(def auth {:key "e86a65f72ce377e5438961a83071ef89"
           :token (env :trello-token)})

(defn qp [query-params] (merge query-params auth))

(defn url [rel-path] (str base-url "/" rel-path))

(defn api-get
  ([rel-path query-params]
   (json/read-str
     (:body
       (client/get (url rel-path) {:query-params (qp query-params)}))))
  ([rel-path]
   (api-get rel-path {})))

(defn get-data
  []
  (clojure.walk/keywordize-keys
    (api-get
      ; Engineering board
      "boards/bJZJFVds"
      {:fields "name"
       :pluginData "true"
       :cards "all"
       :card_fields "closed,idList,name"
       :card_pluginData "true"
       :lists "open"
       :list_fields "name"})))