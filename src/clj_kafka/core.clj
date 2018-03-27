(ns clj-kafka.core
  (:import [org.apache.kafka.clients.consumer ConsumerRecord OffsetAndMetadata OffsetAndTimestamp]
           [org.apache.kafka.clients.producer RecordMetadata]
           [org.apache.kafka.common TopicPartition PartitionInfo Metric MetricName Node]))

(defprotocol ToClojure
  (to-clojure [x] "Converts type to Clojure structure"))

(extend-protocol ToClojure
  nil (to-clojure [x] nil)
  String (to-clojure [x] x)
  Integer (to-clojure [x] x)
  Long (to-clojure [x] x)
  Short (to-clojure [x] x)

  Node
  (to-clojure [x]
    {:id (.id x)
     :id-string (.idString x)
     :host (.host x)
     :port (.port x)
     :rack (.rack x)
     :no-node (-> x .noNode to-clojure)
     :is-empty (.isEmpty x)
     :has-rack (.hasRack x) })

  PartitionInfo
  (to-clojure [x]
    {:topic            (.topic x)
     :partition        (.partition x)
     :replicas         (->> x .replicas (map to-clojure))
     :insync-replicas  (->> x .inSyncReplicas (map to-clojure))
     :offline-replicas (->> x .offlineReplicas (map to-clojure))
     :leader           (-> x .leader to-clojure) })

  Metric
  (to-clojure [x]
    {:name (.metricName x)
      :value (.metricValue x) })

  MetricName
  (to-clojure [x]
    {:name (.name x)
     :description (.description x)
     :group (.group x)
     :tags (.tags x) })

  ConsumerRecord
  (to-clojure [x]
    {:headers    (.headers x)
     :timestamp  (.timestamp x)
     :topic      (.topic x)
     :partition  (.partition x)
     :offset     (.offset x)
     :key-size   (.serializedKeySize x)
     :value-size (.serializedValueSize x)
     :key        (.key x)
     :value      (.value x) })

  RecordMetadata
  (to-clojure [x]
  {:topic     (.topic x)
   :partition (.partition x)
   :offset    (.offset x)})

  TopicPartition
  (to-clojure [x]
    {:topic     (.topic x)
     :partition (.partition x) })

  OffsetAndMetadata
  (to-clojure [x]
    {:offset (.offset x)
     :metadata (.metadata x) })

  OffsetAndTimestamp
  (to-clojure [x]
    {:offset (.offset x)
     :timestamp (.timestamp x) })

  Object
  (to-clojure [x]
    (prn "to-clojure OBJECT" (class x))
    x))

(defn map-to-clojure [m]
  (into {} (for [[k v] m]
             [(to-clojure k) (to-clojure v)])))

