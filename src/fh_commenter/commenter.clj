(ns fh-commenter.commenter
  (:require [fh-commenter.five-hundred :as fh]
            [fh-commenter.text-generator :as gen]))

(def policy-checkers
  {:dismiss-nsfw #(not (:nsfw %2))
   :exclude-user #(not= (-> %2 :user :username) %1)
   :rating-threshold #(>= (:rating %2) %1)
   :votes-threshold #(>= (:votes_count %2) %1)
   :favorites-threshold #(>= (:favorites_count %2) %1)})

(defn check-policy [photo policy]
  (->> policy
       (map (fn [[check val]] ((policy-checkers check) val photo)))
       (not-any? false?)))

(defn should-process? [photo policy]
  (and (not (:liked photo))
       (check-policy photo policy)))

(defn select-photos [base features rpp]
  (->> features
       (map (fn [feature] (fh/list-photos base {:feature feature :rpp rpp})))
       (flatten)))

(defn process [base photo]
  (let [name (-> photo :user :fullname)
        rating (:rating photo)
        meta {:name name :rating rating}
        comment-text (gen/generate-text meta)]
    (println "++ processing" (:id photo))
    (fh/like base photo)
    (fh/comment-on base photo comment-text)
    photo))

(defn do-comment [base features rpp policy]
  (->> (select-photos base features rpp)
       (filter #(should-process? % policy))
       ((fn [xs] (println "+ processing" (count xs) "photos") xs))
       (map #(process base %))))
