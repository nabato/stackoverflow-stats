(ns stackoverflow-stats.core
  (:use [org.httpkit.server :only [run-server]])
  (:require [cheshire.core :refer :all]
            [compojure.core :refer :all]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [org.httpkit.client :as http]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.reload :as reload]))

;; as it's a test job done in my spare time, some trivial things that are not mentioned in requirements
;; like using a map for query params are omitted and some variables are hardcoded.
(def search-api-url "https://api.stackexchange.com/2.3/search?pagesize=100&order=desc&sort=creation&tagged=%s&site=stackoverflow")

(def max-requests 2)

(def client (org.httpkit.client.HttpClient. max-requests))

(defn compose-urls [tags]
  (map #(format search-api-url %) tags))

(defn compose-result-map [tags]
  (reduce #(assoc %1 %2 (assoc {} :total 0 :answered 0)) {} tags))

(defn parallel-queue-request [urls]
  ;; firing multiple requests asynchronously all at once and then dereferencing all of them in a single thread
  ;; is a more effective solution than using a thread-based queue of some type in general.
  (let [futures (doall (map #(http/get % {:client client}) urls))]
    (map deref futures)))

(defn collect-errors [tags responses]
  (into {} (keep-indexed #(let [parsed-body (parse-string (%2 :body))]
                            (when (or (contains? %2 :error) (contains? parsed-body "error_message"))
                              [(nth tags %1) {:http-error (%2 :error)
                                              :service-error (parsed-body "error_message")}])) responses)))

(defn retrieve-records [responses]
  (reduce
   #(into %1 ((parse-string (%2 :body) true) :items))
   []
   responses))

(defn collect-tag-stats [tags records]
  (reduce (fn [acc record]
            (reduce (fn [acc tag]
                      (if (some #(= tag %) (record :tags))
                        (-> acc
                            (assoc-in [tag :total] (inc (get-in acc [tag :total])))
                            (cond-> (record :is_answered)
                              (assoc-in [tag :answered] (inc (get-in acc [tag :answered])))))

                        acc))
                    acc
                    tags))
          (compose-result-map tags)
          records))

(defn get-stackoverflow-stats [tags]
  (let [urls (compose-urls tags)
        responses (parallel-queue-request urls)
        errors (collect-errors tags responses)]
    (if (empty? errors)
      (collect-tag-stats tags (retrieve-records responses))
      errors)))

(defn handle-search [tags]
  (if (or
       (nil? tags)
       (and (string? tags) (= (count tags) 0)))
    ""
    (let [result (get-stackoverflow-stats (if (string? tags) [tags] tags))]
      (generate-string result {:pretty true}))))

(defroutes app-routes
  (GET "/search" [tag] (handle-search tag))
  (route/not-found "Not Found."))

(def handler
  (wrap-defaults app-routes site-defaults))

(defn dev? [& args] true) ;; stub to read a config variable from command line, env, or file

(defn -main [& args]
  (let [hot-reload-wrapper (if (dev? args)
                             (reload/wrap-reload (site #'handler))
                             (site handler))]
    (run-server hot-reload-wrapper {:port 3000})))

