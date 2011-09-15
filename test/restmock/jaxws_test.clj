(ns restmock.jaxws-test
  (:use [clojure.test])
  (:require [restmock.jaxws :as jaxws]))

(defn- matching? [func req] (func req))
(defn- soap-request [body-string]
  {:body body-string})

(deftest jaxws-test
  (is (matching?
       (jaxws/soap-message "m:GetStockPrice")
       (soap-request "<soap:Envelope><soap:Header></soap:Header><soap:Body><m:GetStockPrice xmlns:m=\"http://www.example.org/stock\"><m:StockName>IBM</m:StockName></m:GetStockPrice></soap:Body></soap:Envelope>")))
  (is (matching?
       (jaxws/soap-message "marco")
       (soap-request "<soap:Envelope><soap:Header><wsse:Security><wsse:Username>marco</wsse:Username></wsse:Security></soap:Header><soap:Body></soap:Body>"))))

(jaxws-test)



