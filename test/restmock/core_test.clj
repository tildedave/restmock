(ns restmock.core-test
  (:use [clojure.test])
  (:require [restmock.core :as core]))

(deftest expand-request-body-test
  (is (:body (core/expand-request-body
              {:body (new java.io.StringReader "pikachu")}))
      "pikachu"))

(expand-request-body-test)