(ns restmock.core
  (:use restmock.mock
        ring.util.response
        ring.middleware.params
        clojure.contrib.logging
        clojure.contrib.command-line
        pattern-match
        [ring.adapter.jetty :only [run-jetty]])
  (:require [clojure.zip :as zip])
  (:require [clojure.xml :as xml])
  (:require [clojure.contrib.zip-filter.xml :as zf])
  (:gen-class))

;; MIDDLEWARE

(defn log-response [resp]
  (log :info (format "[RESPONSE] %s" (:status resp)))
  resp)

(defn log-request [req]
  (do
    (log :info (format "[REQUEST] %s %s"
                       (:request-method req) (:uri req)))
    req))

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
           "mock" (mock-handler (zf/xml1-> route-zip :config :name zf/text)
                                (zf/xml1-> route-zip :config :contentType zf/text)))
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

;; HANDLERS

(defn matching-uri-handler [routes req]
  (let [req-uri (:uri req)
        matching-specs (filter
                        (fn [spec]
                          (let [re (re-pattern (:uri-re spec))
                                matches (re-matches re req-uri)]
                              matches))
                        routes)
        handlers (map :handler matching-specs)
        ]
    (if (empty? handlers)
      {:status 404}
      (do
        (log :info (str "[HANDLER] Matched route "
                        (:uri-re (first matching-specs))))
        ((first handlers) req)))))
  
(defn ring-handler [config-file req]
  (let [routes  (config-to-route-map (config-zip config-file))
        req (log-request req)
        resp (matching-uri-handler routes req)
        resp (log-response resp)]
    resp))

(defn -main [& args]
     (with-command-line args
       "Mock restful server"
       [[port "Port to run Jetty server on." "5000"]
        [config "Config file to read routes from." "config.xml"]
        remaining]
       (if (nil? (ClassLoader/getSystemResource config))
         (log :error (str "Could not find " config " on the classpath."))
         (do
           (log :info (str "Found configuration in " config))
           (run-jetty
            (fn [req] (ring-handler config req))
            {:port (Integer/parseInt port)})))))