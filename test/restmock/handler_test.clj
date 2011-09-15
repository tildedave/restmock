(ns restmock.handler-test
  (:use [clojure.test]
        [restmock.handler]))

(defn get-response [handler]
  (handler ()))

(deftest status-handler-test
  (is (= 404 (:status (get-response (status-handler 404)))))
  (is (= 503 (:status (get-response (status-handler 503 "Stuff went bad")))))
  (is (= "Server error" (:body (get-response (status-handler 503 "Server error"))))))

(status-handler-test)