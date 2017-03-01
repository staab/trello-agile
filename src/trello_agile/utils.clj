(ns trello-agile.utils)

(defn map-vals [f m] (zipmap (keys m) (map f (vals m))))

(defn clog [label value]
  (clojure.pprint/pprint (str "--- " label " ---"))
  (clojure.pprint/pprint value)
  value)