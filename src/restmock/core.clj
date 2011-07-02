(ns restmock.core
  (:require [clojure.contrib.java-utils :as java-utils])
  (:use ;restmock.mock
        restmock.dsl
        restmock.handler
        ring.middleware.params
        clojure.contrib.logging
        clojure.contrib.command-line
        pattern-match
        [ring.adapter.jetty :only [run-jetty]])
  (:gen-class))

;; MIDDLEWARE

(defn log-response [resp]
  (log :info (format "[RESPONSE] %s" (:status resp)))
  (log :debug (format "[RESPONSE] %s" resp))
  resp)

(defn log-request [req]
  (do
    (log :info (format "[REQUEST] %s %s %s"
                       (:request-method req) (:uri req) (:body req)))
    req))

(defn expand-request-body [req]
  (assoc req :body (slurp (:body req))))

;; HANDLERS

(defn ring-handler [config-file req]
  (-> req
      expand-request-body
      log-request
      route-handler
      log-response))

(defn can-find-file [config]
  (or
   (not (nil? (ClassLoader/getSystemResource config)))
   (.exists (java-utils/file config))))

(defn -main [& args]
     (with-command-line args
       "Mock restful server"
       [[port "Port to run Jetty server on." "5000"]
        [config "Config file to read routes from." "config.clj"]
        remaining]
       (if (not (can-find-file config))
         (log :error (str "Could not find " config " on the classpath."))
         (do
           (log :info (str "Found configuration in " config))
           (load-restmock-config config)
           (run-jetty
            (fn [req] (ring-handler config req))
            {:port (Integer/parseInt port)})))))