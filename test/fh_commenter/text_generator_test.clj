(ns fh-commenter.text-generator-test
  (:require [clojure.test :refer :all]
            [fh-commenter.text-generator :refer :all]))

(deftest probable-lister
  (is (= (list-only-probable {:a true :b nil :c true}) [:a :c])))

(deftest meta-prefiller
  (is (= (prefill-meta [[:a 1] [:b 2]])
         [{:idx 0 :first? true :last? false :item :a :value 1} {:idx 1 :first? false :last? true :item :b :value 2}])))

(deftest punctuating
  (is (= (punctuate-prefill [{:idx 0 :first? true :last? false :item :name :value "A"}{:idx 1 :first? false :last? true :item :adjective :value "b"}])
         [[:name "A"][:comma ","][:adjective "b"]])))

(deftest joining
  (is (= (join-prefill [{:idx 0 :first? true :last? false :item :name :value "A"}{:idx 1 :first? false :last? false :item :comma :value ","}{:idx 2 :first? false :last? true :item :adjective :value "b"}])
         "A, b")))
