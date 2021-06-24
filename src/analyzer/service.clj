(ns analyzer.service
  (:require [pantomime.mime :as p]
            [pantomime.extract :as pextract]
            [cheshire.core :as json]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [ring.util.response :as ring-resp]
            [io.pedestal.interceptor.error :refer [error-dispatch]])
  (:import [org.apache.commons.validator.routines UrlValidator]))

(defn valid-url?
  [url-str]
  (let [default-validator (UrlValidator. UrlValidator/ALLOW_LOCAL_URLS)]
    (.isValid default-validator url-str)))

(defn home-page
  [_]
  (ring-resp/response "ok"))

(defn analyze-file-or-url
  [file-or-url]
  (merge
    {:mime-type (p/mime-type-of file-or-url)}
    (pextract/parse file-or-url)))

(defn upload-file
  [request]
  (let [[in _] ((juxt :tempfile :filename)
                (-> request :params (get "file")))]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/generate-string (analyze-file-or-url in))}))

(defn error-response
  [status error-map]
  {:status status
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string error-map)})

(defn analyze-url
  [request]
  (if-let [url (-> request :json-params :url)]
    (if (valid-url? url)
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/generate-string (analyze-file-or-url url))}
      (error-response 400 {:message "The supplied url is not a valid url."}))
    (error-response 400 {:message "Missing url. Make sure you send a json body with a \"url\" key."})))

(defn ex->error-map
  [ex]
  (let [data (ex-data ex)]
    (-> data
        (dissoc :exception)
        (dissoc :interceptor)
        (assoc :message (.getMessage (:exception data))))))

(def error-interceptor
  (error-dispatch
   [ctx ex]

   [{:exception-type :java.io.FileNotFoundException}]
   (assoc ctx :response
          (error-response 400 (ex->error-map ex)))

   [{:exception-type :java.net.ConnectException}]
   (assoc ctx :response
          (error-response 400 (ex->error-map ex)))

   :else
   (assoc ctx :respons
          (error-response 500 (ex->error-map ex)))))

(def common-interceptors [(body-params/body-params) http/html-body])
(def upload-interceptors [error-interceptor (middlewares/multipart-params) (body-params/body-params) http/html-body])

;; Tabular routes
(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/upload" :post (conj upload-interceptors `upload-file)]
              ["/analyze" :post (conj upload-interceptors `analyze-url)]})

;; Consumed by analyzer.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ::http/routes routes
              ::http/resource-path "/public"
              ::http/type :jetty
              ::http/host "0.0.0.0"
              ::http/port 9090
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false}})
