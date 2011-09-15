(ns restmock.handler
  (:use ring.util.response
        ring.middleware.params
        clojure.contrib.logging
        )
  )

;; RESPONSE WRAPPERS

(defn contenttype-response-wrapper [data type]
  (let [wrapped-response (response data)]
    (assoc wrapped-response :headers
           (merge (:headers wrapped-response)
                  {"Content-Type" type}))))

(defn xml-response-wrapper [data]
  (contenttype-response-wrapper data "application/xml"))

(defn json-response-wrapper [data]
  (contenttype-response-wrapper data "application/json"))

(defn serve-file [file response-wrapper]
  (-> (slurp (ClassLoader/getSystemResource file)) response-wrapper))

;; HANDLER TYPES

(defn text-handler [text]
  (fn [req] (response text)))

(defn xml-handler [xml]
  (fn [req] (xml-response-wrapper xml)))

(defn xml-file-handler [file]
  (fn [req] (serve-file file xml-response-wrapper)))

(defn json-file-handler [file]
  (fn [req] (serve-file file json-response-wrapper)))

(defn status-handler [status & [text]]
  (if (nil? text)
    (fn [req] {:status status})
    (fn [req] (assoc (response text) :status status))))

(defn- get-matching-routes [routes req]
  (filter (fn [r] ((:request r) req)) routes))
  
(defn matching-uri-handler [routes req]
  (let [matching-routes (get-matching-routes routes req)]
    (if (empty? matching-routes)
      {:status 404}
      (do
        (log :info (str "[HANDLER] Matched route "
                        (:id (first matching-routes))))
        ((:response (first matching-routes)) req)))))
