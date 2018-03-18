(ns clj-kafka.streams
  (:import [org.apache.kafka.streams StreamsConfig StreamsBuilder KafkaStreams ]
           [org.apache.kafka.streams.kstream KeyValueMapper KStreamBuilder Produced ValueMapper]
           [org.apache.kafka.common.serialization Serdes]))

(defn streams-config
  [{:keys [app-id bootstrap-servers commit-interval-ms key-serde value-serde]}]
  (StreamsConfig.
    {StreamsConfig/APPLICATION_ID_CONFIG     app-id
     StreamsConfig/BOOTSTRAP_SERVERS_CONFIG  bootstrap-servers
     StreamsConfig/COMMIT_INTERVAL_MS_CONFIG commit-interval-ms
     StreamsConfig/KEY_SERDE_CLASS_CONFIG    key-serde
     StreamsConfig/VALUE_SERDE_CLASS_CONFIG  value-serde }))

(defn ^KeyValueMapper key-value-mapper
  [f]
  (reify KeyValueMapper
    (apply [_ k v]
      (f [k v]))))

(defn ^ValueMapper value-mapper
  [f]
  (reify ValueMapper
    (apply [_ v]
      (f v))))

(defn word-counts [builder input-stream output-stream]
  (-> builder
    (.stream (Serdes/String) (Serdes/String) (into-array String [input-stream]))
    (.flatMapValues (value-mapper #(clojure.string/split % #"\s")))
    (.groupBy (key-value-mapper second))
    (.count "Counts")
    (.to (Serdes/String) (Serdes/Long) output-stream )))

(defn sum-odd-numbers [builder input-stream output-stream]
  (-> builder
    (.stream (Serdes/String) (Serdes/String) (into-array String [input-stream]))
    (.filter (key-value-mapper (fn [[_ v]] (odd? v))))
    (.selectKey (key-value-mapper (fn [[_ _]] 1)))
    (.to (Serdes/String) (Serdes/Long) output-stream )))

(defn streams-builder []
  (KStreamBuilder.))

(def config {:app-id "song-counts-app"
             :bootstrap-servers  "localhost:9092"
             :commit-interval-ms 100
             :key-serde          (.getName (.getClass (Serdes/String)))
             :value-serde        (.getName (.getClass (Serdes/String))) } )

(defn -main []
  (let [builder (streams-builder) ]
    (word-counts builder "song-list" "SongsWithCountsTopic")
    (-> builder
      (KafkaStreams. (streams-config config))
      .start )))


  ; (:import [org.apache.kafka.streams.kstream
  ;           Transformer
  ;           TransformerSupplier
  ;           KStream
  ;           Predicate
  ;           ValueJoiner
  ;           KeyValueMapper]
  ;          [org.apache.kafka.streams.processor ProcessorContext]
  ;          [org.apache.kafka.streams
  ;           StreamsConfig
  ;           StreamsBuilder
  ;           KafkaStreams
  ;           KeyValue]
  ;          [org.apache.kafka.streams.state KeyValueIterator ReadOnlyKeyValueStore]))

; (defn ^StreamsConfig streams-config
  ; [app-id host]
  ; (StreamsConfig.
  ;   {StreamsConfig/APPLICATION_ID_CONFIG     app-id
  ;    StreamsConfig/BOOTSTRAP_SERVERS_CONFIG  host
  ;    StreamsConfig/KEY_SERDE_CLASS_CONFIG    org.apache.kafka.common.serialization.Serdes$StringSerde
  ;    StreamsConfig/VALUE_SERDE_CLASS_CONFIG  org.apache.kafka.common.serialization.Serdes$StringSerde}))

; (deftype TransducerTransformer [step-fn ^{:volatile-mutable true} context]
  ; Transformer
  ; (init [_ c]
  ;   (set! context c))
  ; (transform [_ k v]
  ;   (try
  ;     (step-fn context [k v])
  ;     (catch Exception e
  ;       (.printStackTrace e)))
  ;   nil)
  ; (punctuate [^Transformer this ^long t])
  ; (close [_]))

; (defn- kafka-streams-step
  ; ([context] context)
  ; ([^ProcessorContext context [k v]]
  ;  (.forward context k v)
  ;  (.commit context)
  ;  context))

; (defn- transformer
  ; "Creates a transducing transformer for use in Kafka Streams topologies."
  ; [xform]
  ; (TransducerTransformer. (xform kafka-streams-step) nil))

; (defn- transformer-supplier
  ; [xform]
  ; (reify
  ;   TransformerSupplier
  ;   (get [_] (transformer xform))))

; (defn ^KafkaStreams kafka-streams
  ; [config xform output-topic & input-topics]
  ; (let [builder (StreamsBuilder.)]
  ;   (-> builder
  ;     (.stream input-topics)
  ;     ; (.transform (transformer-supplier xform) (into-array String []))
  ;     (.to output-topic))
  ;   ; (prn "Topology:" (-> builder .build .describe))
  ;   (KafkaStreams. (.build builder) config)) )


; (def word-count-xform
  ; (map identity))

; ; (def xform (comp (filter (fn [[k v]] (string? v)))
; ;                    (map (fn [[k v]] [v k]))
; ;                    (filter (fn [[k v]] (= "foo" v)))))

; (defn close-streams [^KafkaStreams streams latch]
  ; (fn []
  ;   (prn "closing")
  ;   (.close streams)
  ;   (prn "done")
  ;   (deliver latch true)))

; (defn streams-shutdown-hook [streams]
  ; (let [latch (promise)]
  ;   (.addShutdownHook (Runtime/getRuntime) (Thread. (close-streams streams latch)) )))




; (defn -main [& args]
  ; (let [config  (streams-config "streaming-test" "localhost:9092")
  ;       streams (kafka-streams config word-count-xform "streams-pipe-output" "streams-plaintext-input")
  ;       latch   (streams-shutdown-hook streams) ]
  ;   (try
  ;     (prn "Starting Streams...")
  ;     (.start streams)
  ;     (prn "Started!")
  ;     ; @latch
  ;     ; (shutdown-agents)
  ;     ; (System/exit 0)
  ;     (catch Throwable e
  ;       (prn "Error" e)
  ;       (System/exit 1)))))
