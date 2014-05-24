(ns fh-commenter.core
  (:require [fh-commenter.five-hundred :as fh]
            [fh-commenter.commenter :as com])
  (:gen-class))

(defn env [k] (System/getenv k))

(def consumer-key (env "500PX_CONSUMER_KEY"))
(def consumer-secret (env "500PX_CONSUMER_SECRET"))
(def username (env "500PX_USERNAME"))
(def password (env "500PX_PASSWORD"))

(defn -main [& m]
  (in-ns 'fh-commenter.core)
  (let [fh-base (fh/make-base consumer-key consumer-secret username password)]
    (com/do-comment fh-base
                    ["fresh_today" "upcoming" "popular"]
                    30
                    {:dismiss-nsfw true :rating-threshold 25 :votes-threshold 5 :favorites-threshold 2 :exclude-user username})))
