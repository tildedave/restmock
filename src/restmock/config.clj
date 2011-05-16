(ns restmock.config
  (:use restmock.handler
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
  (let [type (zf/xml1-> route-zip :type zf/text)]
    (match type
           "text" (text-handler (zf/xml1-> route-zip :config :text zf/text))
           "xml"  (xml-handler (zf/xml1-> route-zip :config :file zf/text))
           "json" (json-handler (zf/xml1-> route-zip :config :file zf/text))
;           "mock" (mock-handler (zf/xml1-> route-zip :config :name zf/text)
;                                (zf/xml1-> route-zip :config :contentType zf/text)))
           )))


      ;;      (if (nil? text)
      ;; (let [file (zf/xml1-> route-zip :file zf/text)
      ;;        type (zf/xml1-> route-zip :type zf/text)]
      ;;    {:file file
      ;;     :type type})
      ;; {:text text})))

(defn config-to-route-map [xml-zip]
  (for [route (zf/xml-> xml-zip :routes :route)]
    (let [uri-re (zf/xml1-> route :path zf/text)
          response (get-handler-for-route route)]
      {:uri-re uri-re
       :handler response})))
