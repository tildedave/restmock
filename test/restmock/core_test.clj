(ns restmock.core-test
  (:use [clojure.test])
  (:require [restmock.core :as core]))

(testing "body expander"
  (testing "should expand body"
    (let [request {:body (new java.io.StringReader "pikachu")}]
      (is (:body (core/expand-request-body request))
          "pikachu"))))