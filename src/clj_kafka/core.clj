(ns clj-kafka.core
  (:import [java.nio ByteBuffer]
           [java.util Properties]
           [kafka.message MessageAndMetadata MessageAndOffset]
           [kafka.javaapi PartitionMetadata TopicMetadata TopicMetadataResponse ConsumerMetadataResponse OffsetFetchResponse OffsetCommitResponse]
           [kafka.cluster Broker]
           ))

(defrecord KafkaMessage [topic offset partition key value])

;; Not needed anymore? Producer uses java.util.map == clojure map
(defn as-properties
  [m]
  (doto (Properties.) (.putAll m)))

;; Needed? can just use with-open ?
(defmacro with-resource
  [binding close-fn & body]
  `(let ~binding
     (try
       (do ~@body)
       (finally
        (~close-fn ~(binding 0))))))

(defprotocol ToClojure
  (to-clojure [x] "Converts type to Clojure structure"))

(extend-protocol ToClojure
  nil
  (to-clojure [x] nil)

  MessageAndMetadata
  (to-clojure [x] (KafkaMessage. (.topic x) (.offset x) (.partition x) (.key x) (.message x)))

  MessageAndOffset
  (to-clojure [x]
    (letfn [(byte-buffer-bytes [^ByteBuffer bb] (let [b (byte-array (.remaining bb))]
                                      (.get bb b)
                                      b))]
      (let [offset (.offset x)
            msg (.message x)]
        (KafkaMessage. nil offset nil (.key msg) (byte-buffer-bytes (.payload msg))))))

  Broker
  (to-clojure [x]
    {:connect (.connectionString x)
     :host (.host x)
     :port (.port x)
     :broker-id (.id x)})

  PartitionMetadata
  (to-clojure [x]
    {:partition-id (.partitionId x)
     :leader (to-clojure (.leader x))
     :replicas (map to-clojure (.replicas x))
     :in-sync-replicas (map to-clojure (.isr x))
     :error-code (.errorCode x)})

  TopicMetadata
  (to-clojure [x]
    {:topic (.topic x)
     :partition-metadata (map to-clojure (.partitionsMetadata x))})

  TopicMetadataResponse
  (to-clojure [x]
    (map to-clojure (.topicsMetadata x)))

  OffsetFetchResponse
  (to-clojure [x]
    (into {} (for [[k v] (.offsets x)] [(str (.topic k) ":" (.partition k)) v])))

  OffsetCommitResponse
  (to-clojure [x]
    (if (.hasError x)
      {:has-error true
       :errors (into {} (for [[k v] (.errors x)] [(str (.topic k) ":" (.partition k)) v]))}
      {:has-error false}))

  ConsumerMetadataResponse
  (to-clojure [x]
    {:error-code (.errorCode x)
     :coordinator (.coordinator x)
     }))
