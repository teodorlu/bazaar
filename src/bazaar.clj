(ns bazaar
  (:require
   [org.httpkit.server :as server]
   [nextjournal.clerk :as clerk]
   [ring.middleware.resource]
   [babashka.fs :as fs]))

(defn handler [_req]
  {:status 200 :body "you reached the ring handler"})

(def resolve-slash-to-index-html-map
  (->>
   (fs/glob "resources/public" "**/index.html")
   (map #(fs/relativize "resources/public" %))
   (map (fn [f]
          [(str "/" (fs/parent f) "/")
           (str "/" f)]))
   (into {})))

#_ (resolve-slash-to-index-html-map)

(defn rewrite-urls-resolve-slash-to-index-html
  "We rewrite urls to ensure Clerk documents can be found - resolving / requests
  to /index.html where warranted."
  [handler]
  (fn [req]
    (cond-> req
      (and (= :get (:request-method req))
           (contains? resolve-slash-to-index-html-map (:uri req)))
      (assoc :uri (resolve-slash-to-index-html-map (:uri req)))

      true
      handler)))

#_
(let [handler identity]
  ((rewrite-urls-resolve-slash-to-index-html handler)
   {:request-method :get
    :uri "/folder/"}))
;; rewrites to /folder/index.html if /folder/index.html is found in our resources.

(def wrapped-handler
  (-> #'handler
      (ring.middleware.resource/wrap-resource "public")
      rewrite-urls-resolve-slash-to-index-html))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn start! [opts]
  (let [server (server/run-server #'wrapped-handler
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
