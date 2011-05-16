(ns restmock.core
  (:use ;restmock.mock
        restmock.config
        restmock.handler
        ring.util.response
        ring.middleware.params
        clojure.contrib.logging
        clojure.contrib.command-line
        pattern-match
        [ring.adapter.jetty :only [run-jetty]])
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