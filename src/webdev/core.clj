(ns webdev.core
  (:require [webdev.item.model :as items]
            [webdev.item.handler :refer [handle-index-items
                                         handle-create-item
                                         handle-delete-item
                                         handle-update-item]])
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [compojure.core :refer [defroutes ANY GET POST PUT DELETE]]
            [compojure.route :refer [not-found]]
            [ring.handler.dump :refer [handle-dump]]
            [propertea.core :refer [read-properties]]))

(def props (read-properties "resources/webdev.properties"))

(def local-db
 (let [db-host (props :db-host)
       db-port (props :db-port)
       db-name (props :db-name)]
  {:classname "org.postgresql.Driver"
             :subprotocol "postgresql"
             :subname (str "//" db-host ":" db-port "/" db-name)
             :user (props :db-user)
             :password (props :db-password)}))

(def db (or
            (System/getenv "DATABASE_URL")
            local-db))

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

(defroutes routes
  (GET "/" [] greet)
  (GET "/goodbye" [] goodbye)
  (GET "/yo/:name" [] yo)
  (GET "/calc/:a/:op/:b" [] calc)

  (GET "/about" [] about)
  (ANY "/request" [] handle-dump)

  (GET "/items" [] handle-index-items)
  (POST "/items" [] handle-create-item)
  (DELETE "/items/:item-id" [] handle-delete-item)
  (PUT "/items/:item-id" [] handle-update-item)

  (GET "/floffy" [] floffy)

  (not-found "Hmmm.... nothing here.  Maybe you can try something more special...!"))

(defn wrap-db [hdlr]
    (fn [req]
      (hdlr (assoc req :webdev/db db))))

(defn wrap-server [hdlr]
    (fn [req]
      (assoc-in (hdlr req) [:headers "Server"] "FreakyThang 9000")))

(def sim-methods {"PUT" :PUT
                  "DELETE" :delete})

(defn wrap-simulated-methods [hdlr]
  (fn [req]
    (if-let [method (and (= :post (:request-method req))
                         (sim-methods (get-in req [:params "_method"])))]
      (hdlr (assoc req :request-method method))
      (hdlr req))))


(def app
  (wrap-server
    (wrap-file-info
     (wrap-resource
      (wrap-db
       (wrap-params
        routes))
      "static"))))

(defn -main [port]
  (items/create-table db)
  (jetty/run-jetty app                 {:port (Integer. port)}))

(defn -dev-main [port]
  (items/create-table db)
  (jetty/run-jetty (wrap-reload #'app) {:port (Integer. port)}))
