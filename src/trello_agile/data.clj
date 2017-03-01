(ns trello-agile.data
  (:require [environ.core :refer [env]]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [trello-agile.utils :refer [map-vals clog]]
            [slugger.core :refer [->slug]]))

; Utils

(defn get-plugin-data
  [data]
  (let [plugin-data (:pluginData data)]
    (if
      (> (count plugin-data) 0)
      (clojure.walk/keywordize-keys
        (get (json/read-str (get-in plugin-data [0 :value])) "fields"))
      {})))

(defn plugin-data->custom-fields
  [plugin-data]
  (into {} (map (fn [d] [(keyword (:id d)) (keyword (->slug (:n d)))]) plugin-data)))

(defn transform-card-plugin-data
  [custom-fields card]
  (let [card-plugin-data (get-plugin-data card)]
    (reduce
      (fn [new-card [k v]]
        (assoc new-card (get custom-fields k) v))
      (dissoc card :pluginData)
      card-plugin-data)))

(defn assert-name
  [expected actual]
  (assert
    (= expected actual)
    (str "Expected list " actual " to be named " expected ".")))

; Steps

(defn transform-plugin-data
  [data]
  (let [plugin-data (get-plugin-data data)
        custom-fields (plugin-data->custom-fields plugin-data)]
    (update-in
      (dissoc data :pluginData)
      [:cards]
      (fn [cards] (into [] (map (partial transform-card-plugin-data custom-fields) cards))))))

(defn transform-nest-cards
  [data]
  (let [all-lists (:lists data)
        all-cards (:cards data)
        num-lists (count all-lists)
        lists (zipmap [:next :dev :review :release :finished] all-lists)
        list-names (map-vals #(:name %) lists)
        list-keys-by-id (into {} (map (fn [[k l]] [(:id l) k]) lists))]
    ; Make sure the board is set up a certain way
    (assert
      (= 5 num-lists)
      (str "There are only " num-lists " lists. Did one get deleted?"))
    (assert-name "Next" (:next list-names))
    (assert-name "Development" (:dev list-names))
    (assert-name "Review" (:review list-names))
    (assert-name "Release" (:release list-names))
    (assert-name "Finished" (:finished list-names))
    ; Validate cards and add them under lists
    (reduce
      (fn [data card]
        (let [list-key (get list-keys-by-id (:idList card))]
          ; Only add the card to the lists if it's not closed, unless we're
          ; dealing with finished cards
          (if (or (not (:closed card)) (= list-key :finished))
            (do
              ; Validate only after filtering out closed ones
              (assert (:loe card) (str "Card " (:id card) " did not have a set LOE"))
              (assert (:hours-spent card) (str "Card " (:id card) " did not have a record of hours spent"))
              (update-in
                data
                [:lists list-key :cards]
                (fn [cards]
                  (vec (concat (if (nil? cards) [] cards) [(dissoc card :idList)])))))
            data)))
      {:lists lists}
      all-cards)))



(defn transform-data
  [data]
  (->>
    data
    (transform-plugin-data)
    (transform-nest-cards)))