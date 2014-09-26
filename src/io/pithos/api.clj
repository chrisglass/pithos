(ns io.pithos.api
  "Our main HTTP facade. Serving functionality is provided by aleph.
   Aleph is preferred over more traditionnal HTTP servers because
   it avoids creating one thread per (potentially) long streaming
   requests or responses. Moreover, certain specific operations
   just cannot be handled by the traditional synchronous handlers
   like ring, such as the 100: Continue response expected for uploads.
"
  (:require [qbits.jet.server      :refer [run-jetty]]
            [clojure.tools.logging :refer [info]]
            [io.pithos.system      :refer [service]]
            [io.pithos.operations  :refer [dispatch]]
            [io.pithos.request     :refer [safe-prepare]]))

(defn run
  "Run an asynchronous API handler through Netty thanks to aleph http.
   The request handler is an anonymous function which stores the channel
   inside the request to mimick the operations of http-kit then runs
   several wrappers defined in `io.pithos.api.request` before letting
   `io.pithos.operations` dispatch based on the type of request"
  [system]
  (let [handler (fn [request]
                  (-> (safe-prepare request system)
                      (dispatch system)))]
    (run-jetty (merge (service system) {:ring-handler handler})))
  (info "server up and running"))
