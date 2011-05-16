(ns restmock.handler
  (:use ring.util.response
        ring.middleware.params)
  )

;; RESPONSE HELPERS

(defn contenttype-response [data type]
  (let [wrapped-response (response data)]
    (assoc wrapped-response :headers
           (merge (:headers wrapped-response)
                  {"Content-Type" type}))))

(defn xml-response [data]
  (contenttype-response data "application/xml"))

(defn json-response [data]
  (contenttype-response data "application/json"))

(defn serve-file [file response-wrapper]
  (-> (slurp (ClassLoader/getSystemResource file)) response-wrapper))

;; HANDLER TYPES

(defn text-handler [text]
  (fn [req] (response text)))

(defn xml-handler [file]
  (fn [req] (serve-file file xml-response)))

(defn json-handler [file]
  (fn [req] (serve-file file json-response)))

