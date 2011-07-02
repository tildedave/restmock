;; Macros for mocking a JAX-WS endpoint
;; (receive and respond to SOAP Messages)

(ns restmock.jaxws
  (:use clojure.contrib.logging))

(defmacro soap-message
  "Specifies a soap message matcher"
  [message-string]
  `(fn [req#]
     (.contains (:body req#) ~message-string)))