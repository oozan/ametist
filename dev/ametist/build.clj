(ns ametist.build
  (:require [clojure.tools.build.api :as b]
            [shadow.cljs.devtools.api :as shadow.api]
            [shadow.cljs.devtools.server :as shadow.server]))

(def lib 'ametist/app)
(def class-dir "target/classes")
(def uber-file "target/ametist.jar")
(def app-version "1.0.0")

(defn clean-target []
  "Deletes the target directory."
  (b/delete {:path "target"}))

(defn compile-cljs []
  "Compiles the ClojureScript using Shadow CLJS."
  (shadow.server/start!)
  (shadow.api/release :ametist)
  (shadow.server/stop!))

(defn generate-pom [basis]
  "Generates the POM file in the target/classes directory."
  (b/write-pom {:class-dir class-dir
                :lib       lib
                :version   app-version
                :basis     basis}))

(defn compile-clj [basis]
  "Compiles the Clojure source files to the target/classes directory."
  (b/compile-clj {:basis     basis
                  :class-dir class-dir}))

(defn build-uberjar [basis]
  "Packages the application into an uberjar."
  (b/uber {:class-dir class-dir
           :main      'ametist.server
           :uber-file uber-file
           :basis     basis}))

(defn -main
  [& _]
  (let [basis (b/create-basis {:project "deps.edn"})]
    (clean-target)
    (compile-cljs)
    (generate-pom basis)
    (compile-clj basis)
    (build-uberjar basis)
    (shutdown-agents)))
