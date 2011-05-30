(ns restmock.dsl-test
  (:use [clojure.test]
        [restmock.dsl]))

(defn matching-request? [req-matcher req]
  (req-matcher req))

(testing "uri macro"
  (testing "should match"
    (with-test
      (defn matching-uri? [path req] (path req))
      (is (matching-uri?
           (uri "/foo") {:uri "/foo"}))
      (is (matching-uri?
           (uri "/bar") {:uri "/bar"}))
      (is (matching-uri?
           (uri "/foo/([0-9]+)") {:uri "/foo/123"}))
      ))
  (testing "should not match"
    (with-test
      (defn not-matching-uri? [path req] (not (path req)))
      (is (not-matching-uri?
           (uri "/foo") {:uri "/bar"}))
      (is (not-matching-uri?
           (uri "/foo/([0-9]+)") {:uri "/foo/bar"}))))
)