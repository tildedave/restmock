(ns restmock.soap-test
  (:use [clojure.test])
  (:require [restmock.soap :as soap]))

(deftest soap-test
  (is (soap/to-xml {})
      "<soap:Envelope></soap:Envelope>")
  (is (soap/to-xml {:header ""})
      "<soap:Envelope><soap:Header></soap:Header></soap:Envelope>")
  (is (soap/to-xml {:header "I'm a header!"})
      "<soap:Envelope><soap:Header>I'm a header!</soap:Header></soap:Envelope>"))

(soap-test)