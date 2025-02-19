(ns xiana.handler
  "Provides the default handler function"
  (:require
    [xiana.interceptor.queue :as interceptor.queue]
    [xiana.route :as route]
    [xiana.state :as state]))

(defn handler-fn
  "Returns handler function for server, which  do the routing, and executes interceptors and given action.

   Execution order:
    router interceptors: enters in order
    router interceptors leaves in reversed order
      routing
    around interceptors enters in order
    controller interceptors enters in order
    inside interceptors enters in order
      action
    inside interceptors leaves in reversed order
    controller interceptors leaves in reversed order
    around interceptors leaves in reversed order"
  [deps]
  (fn handle*
    ([http-request]
     (let [state (state/make deps http-request)
           queue (list #(interceptor.queue/execute % (:router-interceptors deps))
                       #(route/match %)
                       #(interceptor.queue/execute % (:controller-interceptors deps)))
           result (reduce (fn [s f] (f s)) state queue)
           channel (get-in result [:response-data :channel])]
       (:response result)))
    ([request respond _]
     (respond (handle* request)))))
