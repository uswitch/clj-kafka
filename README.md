# clj-kafka

Clojure library for [Kafka](https://kafka.apache.org).

Current build status: [![Build Status](https://travis-ci.org/pingles/clj-kafka.png)](https://travis-ci.org/pingles/clj-kafka)

Development is against the 1.0 release of Kafka, and will only work with brokers 0.10.0 or newer! Support for older versions has been dropped.
For older versions than 1.0, not all functionality may work and it is advised to read the documentation.

## Installing

Add the following to your [Leiningen](http://github.com/technomancy/leiningen) `project.clj`:

![latest clj-kafka version](https://clojars.org/clj-kafka/latest-version.svg)

## Usage

### Producer

Discovery of Kafka brokers from Zookeeper:

```clj
(brokers {"zookeeper.connect" "127.0.0.1:2181"})
;; ({:host "localhost", :jmx_port -1, :port 9092, :version 1})
```

```clj
(use 'clj-kafka.producer)
(require '[clj-kafka.serialization :as s])

(def config {"bootstrap.servers" "localhost:9092"
             "key.serializer"    "org.apache.kafka.common.serialization.StringSerializer"
             "value.serializer"  "org.apache.kafka.common.serialization.StringSerializer" })

(with-open [p (producer config)]
  (send p (record "test-topic" "hello world!")))

(def byte-array-serializer (s/serializer s/bytearray))

(with-open [p (producer config byte-array-serializer byte-array-serializer]
  (send p (record "test-topic" (.getBytes "hello world!"))))
```

One key difference is that sending is asynchronous by default. `send`
returns a `Future` immediately. If you want synchronous behaviour
you can deref it right away:

```clj
(with-open [p (producer config)]
  @(send p (record "test-topic" "hello world!")))
```

See: [clj-kafka.new.producer](https://pingles.github.io/clj-kafka/clj-kafka.new.producer.html)


### Consumer

The Zookeeper consumer uses broker information contained within
Zookeeper to consume messages. This consumer also allows the client to
automatically commit consumed offsets so they're not retrieved again.

```clj
(use 'clj-kafka.consumer)

(def config {"bootstrap.servers" "localhost:9092"
             "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"
             "value.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"
             "group.id" "clj-kafka.consumer1"
             "auto.offset.reset" "earliest"
             "enable.auto.commit" "false"})

(with-open [c (consumer config ["topic1"])]
  (records c))
```

#### Usage with transducers

An alternate way of consuming is using `records-seq`. These are
`Iterable` which means, amongst other things, that they work nicely
with transducers.

Continuing previous example:

```clj
;; hypothetical transformation
(def xform (comp (map deserialize-message)
                 (filter production-traffic)
                 (map parse-user-agent-string)))

(with-open [c (consumer config ["test-topic"])]
  (let [stream (records-seq c)]
    (run! write-to-database! (eduction xform stream))))
```


### Administration Operations

There is support the following simple administration operations:

- checking if a topic exists
- creating a topic
- deleting a topic (requires that the Kafka cluster supports deletion
and has `delete.topic.enable` set to `true`)
- retrieving topic configuration
- changing topic configuration

```clj
(require '[clj-kafka.admin :as admin])

(with-open [zk (admin/zk-client "127.0.0.1:2181")]
  (if-not (admin/topic-exists? zk "test-topic")
    (admin/create-topic zk "test-topic"
                        {:partitions 3
                         :replication-factor 1
                         :config {"cleanup.policy" "compact"}})))
```

See: [clj-kafka.admin](https://pingles.github.io/clj-kafka/clj-kafka.admin.html)


### Kafka Offset Manager Operations

There is support the following simple Kafka offset management operations:

- fetch the current offsets of a consumer group
- reset the current offsets of a consumer group

```clj
(require '[clj-kafka.offset :as offset])

(fetch-consumer-offsets "broker1:9092,broker1:9092" {"zookeeper.connect" "zkhost:2182"} "my-topic" "my-consumer")
(reset-consumer-offsets "broker1:9092,broker1:9092" {"zookeeper.connect" "zkhost:2182"} "my-topic" "my-consumer" :earliest)
(reset-consumer-offsets "broker1:9092,broker1:9092" {"zookeeper.connect" "zkhost:2182"} "my-topic" "my-consumer" :latest)
```

See: [clj-kafka.admin](https://pingles.github.io/clj-kafka/clj-kafka.offset.html)


## License

Copyright &copy; 2013 Paul Ingles

Distributed under the Eclipse Public License, the same as Clojure.

## Thanks

YourKit is kindly supporting this open source project with its full-featured Java Profiler. YourKit, LLC is the creator of innovative and intelligent tools for profiling Java and .NET applications. Take a look at YourKit's leading software products:
[YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp) and
[YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp).
