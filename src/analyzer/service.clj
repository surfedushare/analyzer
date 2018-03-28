(ns analyzer.service
  (:require [clojure.java.io :as io]
            [pantomime.mime :as p]
            [pantomime.extract :as pextract]
            [cheshire.core :as json]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [ring.util.response :as ring-resp]))

(defn home-page
  [request]
  (ring-resp/response "ok"))

(defn analyze
  [file]
  (merge
    {:mime-type (p/mime-type-of file)}
    (pextract/parse file)))

(defn upload
  [request]
  (let [[in file-name] ((juxt :tempfile :filename)
                        (-> request :params (get "file")))]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/generate-string (analyze in))}))


(def common-interceptors [(body-params/body-params) http/html-body])
(def upload-interceptors [(middlewares/multipart-params) (body-params/body-params) http/html-body])

;; Tabular routes
(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/upload" :post (conj upload-interceptors `upload)]})

;; Consumed by analyzer.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ::http/routes routes
              ::http/resource-path "/public"
              ::http/type :jetty
              ::http/port 8080
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false}})

