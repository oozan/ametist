(ns ametist.server
  (:gen-class)
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [hiccup2.core :as h]
            [io.pedestal.http :as http]
            [ametist.note :as note]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor :as interceptor]
            [next.jdbc :as jdbc]))

(set! *warn-on-reflection* true)

;; HTML Renderer for the Index Page
(defn index
  [_]
  (let [html [:html {:lang "en"}
              [:head
               [:meta {:charset "UTF-8"}]
               [:link {:rel "icon" :href "data:"}]
               [:meta {:name    "viewport"
                       :content "width=device-width, initial-scale=1.0"}]
               [:meta {:name    "theme-color"
                       :content "#000000"}]
               [:meta {:name    "description"
                       :content "A simple full-stack clojure app"}]
               [:title "ametist"]]
              [:body
               [:div {:id "ametist"} "loading ..."]
               [:script {:src "/ametist/main.js"}]]]]
    {:body    (str "<!DOCTYPE html>\n" (h/html {:mode :html} html))
     :headers {"Content-Type" "text/html"}
     :status  200}))

;; Handlers for TODO Operations
(defn list-todo
  [{::keys [atm-conn]}]
  (let [response (jdbc/execute! atm-conn ["SELECT * FROM todo"])]
    {:body    (json/generate-string response)
     :headers {"Content-Type" "application/json"}
     :status  200}))

(defn create-todo
  [{::keys [atm-conn] :keys [body]}]
  (let [note (-> body
                 io/reader
                 (json/parse-stream true)
                 :note)]
    (when-not (note/valid? note)
      (throw (ex-info "Invalid note" {})))
    (jdbc/execute! atm-conn
      ["INSERT INTO todo (note) VALUES (?);
        DELETE FROM todo WHERE id IN (SELECT id FROM todo ORDER BY id DESC OFFSET 10)"
       note])
    {:status 201}))

(defn install-schema
  [{::keys [atm-conn]}]
  (jdbc/execute! atm-conn ["CREATE TABLE todo (id serial, note text)"])
  {:status 202})

;; Routes Definition
(def routes
  `#{["/" :get index]
     ["/todo" :get list-todo]
     ["/todo" :post create-todo]
     ["/install-schema" :post install-schema]})

;; Pedestal Service Creation
(defn create-service
  [service-map]
  (-> service-map
      (assoc ::http/secure-headers {:content-security-policy-settings ""}
             ::http/resource-path "public"
             ::http/routes (fn [] (route/expand-routes routes)))
      http/default-interceptors
      (update ::http/interceptors
              (partial cons
                (interceptor/interceptor
                 {:enter (fn [ctx] (update ctx :request merge service-map))})))))

;; Server State Management
(defonce state (atom nil))

(defn -main
  [& _]
  (let [port (Long/getLong "ametist.server.http-port" 8080)
        atm-db-url (System/getProperty "ametist.server.atm-db-url"
                                       "jdbc:postgresql://127.0.0.1:5432/postgres?user=postgres&password=postgres")]
    (swap! state
           (fn [st]
             (some-> st http/stop)
             (-> {::http/port      port
                  ::atm-conn       {:jdbcUrl atm-db-url}
                  ::http/file-path "target/classes/public"
                  ::http/host      "0.0.0.0"
                  ::http/type      :jetty
                  ::http/join?     false}
                 create-service
                 http/dev-interceptors
                 http/create-server
                 http/start)))))

(comment
  ;; Start PostgreSQL
  ;; docker run --name my-postgres --env=POSTGRES_PASSWORD=postgres --rm -p 5432:5432 postgres:alpine

  ;; Shadow CLJS Server for Frontend
  (-> `shadow.cljs.devtools.server/start! requiring-resolve (apply []))
  (-> `shadow.cljs.devtools.api/watch requiring-resolve (apply [:ametist]))
  (-> `shadow.cljs.devtools.api/watch requiring-resolve (apply [:node-test]))

  ;; Start the Backend
  (-main))
