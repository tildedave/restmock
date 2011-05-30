(ns restmock.dsl)

;; DSL for Restmock
;;
;; Intended syntax:
;;
;; (handler
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
  `(fn [req#] (= ~method (:method req#))))

;; TODO: idiomatic way to get curried 'and'?
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