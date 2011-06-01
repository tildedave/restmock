(ns restmock.dsl
  (:use restmock.handler
        clojure.contrib.logging)
  )

;; DSL for Restmock
;;
;; Intended syntax:
;;
;; (routes
;;  (route "Hello, world!"
;;         (request (uri "/hello"))
;;         (response (text "Hello, world!")))
;;  (route "Can retrieve all the kittens"
;;         (request (uri "/kittens")
;;                  (method :get))
;;         (response (text "Some adorable kittens!")))
;;  (route "Can't make a new kitten"
;;         (request (uri "/kittens")
;;                  (method :post))
;;         (response (status 422)))
;;  (route "Can update a kitten"
;;         (request (uri "/kittens/([0-9]+)")
;;                  (method :put))
;;         (response (status 202)))
;;  (route "Person XML"
;;         (request (uri "/person/([0-9]+)"))
;;         (response (file "person.xml")))
;;  )

(defmacro uri
  "Specifies a criteria of matching a URI"
  [path]
  `(fn [req#]
     (if (nil? (:uri req#))
       false
       (not (nil? (re-matches (re-pattern ~path) (:uri req#)))))))

(defmacro method
  "Specifies a criteria of matching a HTTP request's method"
  [method]
  `(fn [req#]
     (= ~method (:request-method req#))))

;; TODO: idiomatic way to get curried 'and'?
;; TODO: more idiomatic way to do (list ~@criteria) ?
(defmacro request
  "Specifies a list of criteria to match a request on"
  [& criteria]
  `(fn [req#]
     ;; safest to use dual-strategy reduce/map because
     ;; reduce's default behavior won't evaluate
     ;; function on 1 argument
     (reduce #(and %1 %2)
             (map #(% req#)
                  (list ~@criteria)))))

(defmacro response
  "Specifies a response handler"
  [handler]
  `(fn [req#] (~handler req#)))

(defmacro text
  "Specifies a text response handler"
  [text]
  `(text-handler ~text))

(defmacro xml-file
  "Specifies a xml file handler"
  [file]
  `(xml-handler ~file))

(defmacro json-file
  "Specifies a JSON file handler"
  [file]
  `(json-handler ~file))

(defmacro status
  "Specifies a status handler"
  [num]
  `(status-handler ~num))

(defmacro route
  "Specifies a route with request criteria and response"
  [id request response]
  `{:id ~id,
    :request ~request,
    :response ~response})

(defmacro routes
  "A routes is a collection of route handlers"
  [& routes]
  `(defn route-handler [req#]
     (matching-uri-handler (list ~@routes) req#)))

(def default-route-handler
  (route
   "Default route"
   (request (uri ".*"))
   (response (status 404))))

(defn route-handler [req]
  (matching-uri-handler
   (list default-route-handler)
   req))

(defn load-restmock-config [file]
  (do
    ;; UGLY UGLY UGLY FIGURE OUT HOW TO REMOVE
    (in-ns 'restmock.dsl)
    (load-file file)))