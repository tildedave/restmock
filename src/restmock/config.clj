(ns restmock.config
  (:use restmock.handler
        ring.util.response
        clojure.contrib.logging
        pattern-match)
  (:require [clojure.zip :as zip])
  (:require [clojure.xml :as xml])
  (:require [clojure.contrib.zip-filter.xml :as zf]))

;; CONFIG HELPERS

(defn config-zip [config-xml]
  (let [xml-str (slurp (ClassLoader/getSystemResource config-xml))
        stream (java.io.ByteArrayInputStream. (.getBytes (.trim xml-str)))]
    (zip/xml-zip (xml/parse stream))))

(defn get-handler-for-route [route-zip]
  (let [type (zf/xml1-> route-zip :response :type zf/text)]
    (do
      (log :debug (str "[CONFIG] Response type " type))
      (match type
             "text" (text-handler (zf/xml1-> route-zip :response :config :text zf/text))
             "xml"  (xml-handler (zf/xml1-> route-zip :response :config :file zf/text))
             "json" (json-handler (zf/xml1-> route-zip :response :config :file zf/text))
;           "mock" (mock-handler (zf/xml1-> route-zip :config :name zf/text)
;                                (zf/xml1-> route-zip :config :contentType zf/text)))
           ))))

(defn request-for-route [route]
  (let [uri-re (zf/xml1-> route :request :path zf/text)
        re (re-pattern uri-re)]
    (fn [req-uri]
      (do
        (log :debug (str "[CONFIG] Received URI " req-uri ".  "
                         "Matching against " re))
        (re-matches re req-uri)))))

(defn config-to-route-map [xml-zip]
  (for [route (zf/xml-> xml-zip :routes :route)]
    (let [route-map
          {:id (zf/xml1-> route :id zf/text)
           :request (request-for-route route)
           :response (get-handler-for-route route)}]
      (do 
        (log :info (str "[CONFIG] Read route " (:id route-map)))
        (log :info (str "[CONFIG] Request " (:request route-map)))
        (log :info (str "[CONFIG] Response " (:response route-map)))
        route-map))))
