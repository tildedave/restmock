(defproject restmock "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [ring/ring-core "0.3.7"]
                 [ring/ring-devel "0.3.7"]
                 [ring/ring-jetty-adapter "0.2.5"]
                 [pattern-match "1.0.0"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
                 [fleetdb-client "0.2.2"]
                 ]

  :dev-dependencies [[swank-clojure "1.2.1"]
                     [lein-ring "0.3.2"]]
  :ring {:handler restmock.core/handler}
  :main restmock.core
)