(ns restmock.handler-test
  (:use [clojure.test]
        [restmock.handler]))

(with-test
  (defn get-response [handler]
    (handler ()))
  (is (= 404 (:status (get-response (status-handler 404)))))
  (is (= 503 (:status (get-response (status-handler 503 "Stuff went bad")))))
  (is (= "Server error" (:body (get-response (status-handler 503 "Server error"))))))