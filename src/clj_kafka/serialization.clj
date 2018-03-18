(ns clj-kafka.serialization
  (:import [org.apache.kafka.common.serialization Serializer Deserializer
            ByteArraySerializer ByteArrayDeserializer
            ByteBufferSerializer ByteBufferDeserializer
            DoubleSerializer DoubleDeserializer
            FloatSerializer FloatDeserializer
            IntegerSerializer IntegerDeserializer
            LongSerializer LongDeserializer
            ShortSerializer ShortDeserializer
            StringSerializer StringDeserializer]))

(def bytearray (Class/forName "[B"))

(defn serializer [t]
  (condp = t
    bytearray           (ByteArraySerializer.)
    java.nio.ByteBuffer (ByteBufferSerializer.)
    Double              (DoubleSerializer.)
    Float               (FloatSerializer.)
    Integer             (IntegerSerializer.)
    Long                (LongSerializer.)
    Short               (ShortSerializer.)
    String              (StringSerializer.)
    "unknown type"))

(defn deserializer [t]
  (condp = t
    bytearray           (ByteArrayDeserializer.)
    java.nio.ByteBuffer (ByteBufferDeserializer.)
    Double              (DoubleDeserializer.)
    Float               (FloatDeserializer.)
    Integer             (IntegerDeserializer.)
    Long                (LongDeserializer.)
    Short               (ShortDeserializer.)
    String              (StringDeserializer.)
    "unknown type"))

