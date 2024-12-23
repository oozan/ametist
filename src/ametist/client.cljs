(ns ametist.client
  (:require [reagent.core :as r]
            [ametist.note :as note]
            [reagent.dom.client :as rdc]))

;; Application State
(defonce *state (r/atom {}))

;; Fetch TODO Items
(defn fetch-todos
  []
  (-> (js/fetch "/todo")
      (.then (fn [response]
               (when-not (.-ok response)
                 (throw (ex-info (.-statusText response)
                                 {:response response})))
               (swap! *state dissoc :error)
               (.json response)))
      (.then (fn [todos]
               (swap! *state assoc :todos (js->clj todos :keywordize-keys true))))
      (.catch (fn [ex]
                (swap! *state assoc :error (ex-message ex))))))

;; UI Components
(defn ui-note-form
  []
  [:form
   {:on-submit (fn [^js evt]
                 (.preventDefault evt)
                 (let [note-el (-> evt .-target .-elements .-note)
                       note    (.-value note-el)]
                   (when-not (note/valid? note)
                     (.setCustomValidity note-el "Invalid note")
                     (.reportValidity note-el)
                     (throw (ex-info "Invalid note" {})))
                   (set! (.-disabled note-el) true)
                   (-> (js/fetch "/todo" #js{:method "POST"
                                             :body   (js/JSON.stringify #js{:note note})})
                       (.then (fn [response]
                                (when (.-ok response)
                                  (set! (.-value note-el) ""))
                                (fetch-todos)))
                       (.finally (fn []
                                   (set! (.-disabled note-el) false))))))}
   [:label
    "note: "
    [:input {:on-change (fn [evt]
                          (-> evt .-target (.setCustomValidity "")))
             :name      "note"}]]])

(defn ui-error-handler
  [error]
  [:<>
   [:pre (str error)]
   [:button {:on-click (fn []
                         (js/fetch "/install-schema" #js{:method "POST"}))}
    "install schema"]])

(defn ui-todo-list
  [todos]
  [:ul
   (for [{:todo/keys [id note]} todos]
     [:li {:key id} note])])

(defn ui-root
  []
  (let [{:keys [error todos]} @*state]
    [:<>
     [:p "This is a sample clojure app to demonstrate how to use "
      [:a {:href "https://clojure.org/guides/tools_build"} "tools.build"]
      " to create and deploy a full-stack clojure app."]
     [ui-note-form]
     (when error
       [ui-error-handler error])
     [ui-todo-list todos]]))

;; Application Lifecycle
(defonce *root (atom nil))

(defn after-load
  []
  (some-> @*root
          (rdc/render [ui-root])))

(defn start
  []
  (let [container (js/document.getElementById "ametist")
        root      (rdc/create-root container)]
    (fetch-todos)
    (rdc/render root [ui-root])
    (reset! *root root)))
