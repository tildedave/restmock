(routes
 (route "Hello, world!"
        (request (uri "/hello"))
        (response (text "Hello, world!")))
 (route "Can retrieve all the kittens"
        (request (uri "/kittens")
                 (method :get))
        (response (text "Some adorable kittens!")))
 (route "Can't make a new kitten"
        (request (uri "/kittens")
                 (method :post))
        (response (status 422)))
 (route "Can update a kitten"
        (request (uri "/kittens/([0-9]+)")
                 (method :put))
        (response (status 202)))
 (route "Kitten XML"
        (request (uri "/kittens/([0-9]+)"))
        (response (xml-file "cute-kitten.xml"))))
