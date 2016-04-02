(ns webdev.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found]]))

(defn greet [req]
  {:status 200
   :body "Hello World, now with compojure and no reload!"
   :headers {}})

(defn goodbye [req]
  {:status 200
   :body "Tataaa!"
   :headers{}})

(defn floffy [req]
  {:status 200
   :body "Oh my, you found it!  Piggy Love Floffy, you know?! X x x X"
   :headers {}})

(defroutes app
  (GET "/" [] greet)
  (GET "/goodbye" [] goodbye)
  (GET "/floffy" [] floffy)
  (not-found "Hmmm.... nothing here.  Maybe you can try something more special...!"))


(defn -main [port]
  (jetty/run-jetty app                 {:port (Integer. port)}))

(defn -dev-main [port]
  (jetty/run-jetty (wrap-reload #'app) {:port (Integer. port)}))

