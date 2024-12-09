(ns ametist.note-test
  (:require [clojure.test :refer [deftest is testing]]
            [ametist.note :as note]))


(deftest valid?
  (is (= false
        (note/valid? "")))
  (is (= true
        (note/valid? "hello"))))
