(ns restmock.dsl-test
  (:use [clojure.test]
        [restmock.dsl]))

(defn matching-req? [func req] (func req))
(defn not-matching-req? [func req] (not (func req)))

(testing "uri macro"
  (testing "should match"
    (is (matching-req?
         (uri "/foo") {:uri "/foo"}))
    (is (matching-req?
         (uri "/bar") {:uri "/bar"}))
    (is (matching-req?
         (uri "/foo/([0-9]+)") {:uri "/foo/123"}))
    )
  (testing "should not match"
    (is (not-matching-req?
         (uri "/foo") {:uri "/bar"}))
    (is (not-matching-req?
         (uri "/foo/([0-9]+)") {:uri "/foo/bar"})))
  )

(testing "method macro"
  (testing "should match"
    (is (matching-req?
         (method :get) {:method :get}))
    (is (matching-req?
         (method :post) {:method :post}))))

(testing "request macro"
  (testing "should match"
    (is (matching-req?
         (request (method :get)) {:method :get}))
    (is (matching-req?
         (request (method :get)
                  (uri "/foo")) {:method :get, :uri "/foo"})))
  (testing "should not match"
    (is (not-matching-req?
         (request (uri "/foo")
                  (method :get))
         {:uri "/foo"}))
    (is (not-matching-req?
         (request (uri "/foo")
                  (method :get))
         {:method :get}))))
      