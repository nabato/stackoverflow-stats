(defproject stackoverflow-stats "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cheshire "5.11.0"]
                 [compojure "1.6.1"]
                 [ring/ring-core "1.9.6"]
                 [ring/ring-devel "1.9.6"]
                 [ring/ring-defaults "0.3.4"]
                 [http-kit "2.6.0"]]
  :plugins [[lein-cljfmt "0.9.2"]
            [lein-ring "0.12.5"]]
  :ring {:handler stackoverflow-stats.core/handler}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.4.0"]]}
   :uberjar {:aot :all
             :main stackoverflow-stats.core}})
