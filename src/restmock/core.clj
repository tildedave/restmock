(ns restmock.core
  (:use ring.util.response
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

(defn serve [file response-wrapper]
  (-> (slurp (ClassLoader/getSystemResource file)) response-wrapper))

(defn response-for-spec [spec]
  (let [response-info (:response spec)
        text (:text response-info)
        file (:file response-info)
        type (:type response-info)]
    (if (nil? text)
      (match type
             "xml" (serve file xml-response)
             "json" (serve file json-response)
             _     {:status 404})
      (response text))))

;; CONFIG HELPERS

(defn config-zip [config-xml]
     (let [xml-str (slurp (ClassLoader/getSystemResource config-xml))
           stream (java.io.ByteArrayInputStream. (.getBytes (.trim xml-str)))]
       (zip/xml-zip (xml/parse stream))))

(defn get-response-for-route [route-zip]
  (let [text (zf/xml1-> route-zip :text zf/text)]
    (if (nil? text)
      (let [file (zf/xml1-> route-zip :file zf/text)
             type (zf/xml1-> route-zip :type zf/text)]
         {:file file
          :type type})
      {:text text})))

(defn config-to-route-map [xml-zip]
  (for [route (zf/xml-> xml-zip :routes :route)]
    (let [uri-re (zf/xml1-> route :path zf/text)
          response (get-response-for-route route)]
      {:uri-re uri-re
       :response response})))

;; HANDLERS

(defn matching-uri-handler [routes req]
  (let [req-uri (:uri req)
        matching-specs (filter
                        (fn [spec]
                          (let [re (re-pattern (:uri-re spec))
                                matches (re-matches re req-uri)]
                              matches))
                        routes)
        responses (map response-for-spec matching-specs)
        ]
    (if (empty? responses)
      {:status 404}
      (do
        (log :info (str "[HANDLER] Matched route "
                        (:uri-re (first matching-specs))))
        (first responses)))))
  
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