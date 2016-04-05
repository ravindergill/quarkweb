(ns webdev.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found]]
            [ring.handler.dump :refer [handle-dump]]))

(defn greet [req]
  {:status 200
   :body "Hello World, now with compojure and no reload!"})

(defn about [req]
  {:status 200
   :body "By Quark Quark to learn how to write a web app in clojure."})

(defn goodbye [req]
  {:status 200
   :body "Tataaa!"})

(defn request [req]
  {:status 200
   :body (pr-str req)})

(defn yo [req]
  (let [name (get-in req [:route-params :name])]
    {:status 200
     :body (str "Yo, " name "!")}))


(def ops
  {"+" +
   "-" -
   "*" *
   ":" /})

(defn calc [req]
  (let [a (Integer. (get-in req [:route-params :a]))
        b (Integer. (get-in req [:route-params :b]))
        op (get-in req [:route-params :op])
        f (get ops op)]
    (if f
    {:status 200
     :body (str (f a b))}
    {:status 404
     :body (str "Unknown operator " op)})))


(defn floffy [req]
  {:status 200
   :body "Oh my, you found it!  Piggy Love Floffy, you know?! X x x X"})

(defroutes app
  (GET "/" [] greet)
  (GET "/about" [] about)
  (GET "/goodbye" [] goodbye)
  (GET "/request" [] handle-dump)
  (GET "/yo/:name" [] yo)
  (GET "/calc/:a/:op/:b" [] calc)
  (GET "/floffy" [] floffy)
  (not-found "Hmmm.... nothing here.  Maybe you can try something more special...!"))


(defn -main [port]
  (jetty/run-jetty app                 {:port (Integer. port)}))

(defn -dev-main [port]
  (jetty/run-jetty (wrap-reload #'app) {:port (Integer. port)}))

