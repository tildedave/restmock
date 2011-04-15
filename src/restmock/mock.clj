(ns restmock.mock
  (:use fleetdb.client))

;; Mock endpoint --
;;
;; Do your best to pretend to be a real REST resource!
;; Backed by FleetDB
;;
;; Only useful for canonical CRUD actions, if you are
;; mostly testing views, you should use a static route.
;;
;; NO SCHEMA VALIDATION -- YOU GET WHAT YOU POST
;;
;; (until I figure out a way to fix that)

(def client (connect))

(defn- next-id [url]
   (inc (.length (client ["select" url]))))

(defn- join-string [list joiner]
  (if (empty? list)
    ""
    (reduce #(str %1 joiner %2) list)))

(defmacro where-id [id]
   {"where" [ "=" "id" id ]})

(defn- extract-data [rows]
  (map #(nth (find %1 "data") 1) rows))


(defn xml-combiner [name]
  (fn [args]
    (format "<%s>\n%s\n</%s>"
            name
            (join-string args "\n")
            name)))

;; todo: json-combiner (yawn)

(defn post [url data]
  (client ["insert" url
           [{"id" (next-id url), "data" data}]]))

(defn get-list [url combiner]
  (combiner (extract-data (client ["select" url]))))

(defn get-id [url id]
  (first (extract-data (client ["select" url (where-id id)]))))

(defn delete [url id]
  (client ["delete" url (where-id id)]))

(defn put-id [url id data]
  (client ["update" url {"data" data} (where-id id)]))

;; TODO: does ring support HEAD requests correctly here?