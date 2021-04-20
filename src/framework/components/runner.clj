(ns framework.components.runner
  (:require
    [xiana.core :as xiana]))

(defn d
  [st a]
  (if a (a st)
      (xiana/ok st)))

(defn run
  ([state action]
   (run state [] action))
  ([state interceptors action]
   (if (empty? interceptors)
     (action state)
     (let [{:keys [enter leave error]} (first interceptors)]
       (try (xiana/flow-> state
                          (d enter)
                          (run (rest interceptors) action)
                          (d leave))
            (catch Exception e
              (if error
                (error state)
                (xiana/error (assoc state :response {:status 500
                                                     :body   e})))))))))
