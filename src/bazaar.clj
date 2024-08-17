(ns bazaar
  (:require
   [org.httpkit.server :as server]
   [nextjournal.clerk :as clerk]
   [ring.middleware.resource]))

(defn handler [_req]
  {:status 200 :body "okayyy"})

(def wrapped-handler
  (-> #'handler
      (ring.middleware.resource/wrap-resource "public")))

(wrapped-handler {:request-method :get
                  :uri "hello.html"})

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn start! [opts]
  (let [server (server/run-server #'handler
                                  (merge {:legacy-return-value? false
                                          :host "0.0.0.0"
                                          :port 7777}
                                         opts))]
    (println (format "server started on port %s"
                     (server/server-port server)))))

(comment
  ;; conveniences for running Clerk locally, building static files, etc.
  (clerk/serve! {:port 7778 :browse? true})
  (clerk/halt!)
  ,)
