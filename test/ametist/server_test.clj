(ns ametist.server-test
  (:require [ametist.server :as ametist]
            [cheshire.core :as json]
            [clojure.test :refer [deftest is]]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(set! *warn-on-reflection* true)

(deftest hello
  (let [atm-conn (jdbc/with-options (jdbc/get-connection {:jdbcUrl "jdbc:h2:mem:"})
                   {:builder-fn rs/as-lower-maps})
        service-fn (-> {::ametist/atm-conn atm-conn}
                     ametist/create-service
                     http/dev-interceptors
                     http/create-servlet
                     ::http/service-fn)]
    (ametist/install-schema {::ametist/atm-conn atm-conn})
    (is (= []
          (-> service-fn
            (response-for :get "/todo")
            :body
            (json/parse-string true)))
      "fetching todos before creating")
    (is (= 201
          (-> service-fn
            (response-for :post "/todo"
              :body (json/generate-string {:note "hello world"}))
            :status))
      "creating a todo")
    (is (= [{:todo/id   1
             :todo/note "hello world"}]
          (-> service-fn
            (response-for :get "/todo")
            :body
            (json/parse-string true)))
      "fetching the todos, after creating one")
    (dotimes [idx 10]
      (is (= 201
            (-> service-fn
              (response-for :post "/todo"
                :body (json/generate-string {:note (str "hello world" idx)}))
              :status))
        (str "creating many todos: idx = " idx)))
    (is (== 10
          (-> service-fn
            (response-for :get "/todo")
            :body
            (json/parse-string true)
            count))
      "fetching many todos")))
