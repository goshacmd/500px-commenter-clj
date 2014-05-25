(ns fh-commenter.commenter-test
  (:require [clojure.test :refer :all]
            [fh-commenter.commenter :refer :all]))

(deftest policy-check
  (is (true? (check-policy {} {})))
  (is (true? (check-policy {:nsfw true} {})))
  (is (false? (check-policy {:nsfw true} {:dismiss-nsfw true}))))
