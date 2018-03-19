(ns ^{:doc "Administration operations."}
  clj-kafka.admin
  (:import [org.apache.kafka.clients.admin AdminClient KafkaAdminClient NewTopic CreateTopicsOptions]
    ))

(defn ^AdminClient admin [config]
  (AdminClient/create config))

(defn new-topic [name num-partitions replication-factor]
  (NewTopic. name num-partitions replication-factor))

(defn create-topics [new-topics & {:keys [validate-only timeout-ms]}]
  (.createTopics topics (doto (CreateTopicsOptions.)
                          (.timeoutMs timeout-ms)
                          (.validateOnly (or validate-only false)))))
