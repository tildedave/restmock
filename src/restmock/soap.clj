(ns restmock.soap)

(defn- header-to-xml [map] "")

(defn- body-to-xml [map] "")

(defn to-xml [map]
  (format "<soap:Envelope>%s%s</soap:Envelope>"
          (header-to-xml (:header map))
          (body-to-xml (:body map))))
  