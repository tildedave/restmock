(ns restmock.dsl-test
  (:use [clojure.test]
        [restmock.dsl]))

(defn matching-req? [func req] (func req))
(defn not-matching-req? [func req] (not (func req)))

(deftest uri-test
  (is (matching-req? (uri "/foo") {:uri "/foo"}))
  (is (matching-req? (uri "/bar") {:uri "/bar"}))
  (is (matching-req? (uri "/foo/([0-9]+)") {:uri "/foo/123"}))
  (is (not-matching-req? (uri "/foo") {:uri "/bar"}))
  (is (not-matching-req? (uri "/foo/([0-9]+)") {:uri "/foo/bar"})))

(deftest method-test
  (is (matching-req? (method :get) {:request-method :get}))
  (is (matching-req? (method :post) {:request-method :post})))

(deftest request-test
  (is (matching-req? (request (method :get))
                     {:request-method :get}))
  (is (matching-req? (request (method :get)
                              (uri "/foo"))
                     {:request-method :get, :uri "/foo"}))
  (is (not-matching-req? (request (uri "/foo")
                                  (method :get))
                         {:uri "/foo"}))
  (is (not-matching-req? (request (uri "/foo")
                                  (method :get))
                         {:request-method :get})))

(deftest dsl-test
  (method-test)
  (uri-test)
  (request-test)
  (request-test))

(dsl-test)