# AbStore

AbStore is a simple in-memory key-value store that can be used to experiment with different **a**tomic **b**roadcast protocols.  

Currently it supports [Zab][1] and [Raft][2], through their JVM implementation: [jZab][3] and [CKite][4].

[1]: http://web.stanford.edu/class/cs347/reading/zab.pdf
[2]: http://raftconsensus.github.io/
[3]: https://github.com/zk1931/jzab
[4]: https://github.com/pablosmedina/ckite


## Usage

```
Usage: abstore [options]

  -r <value> | --smr <value>
        SMR protocol (raft, zab)
  -d <value> | --dataDir <value>
        Data directory
  -a <value> | --address <value>
        Listening address
  --bootstrap
        Bootstrap option (for the very first node)
  -m <value> | --members <value>
        Members addresses
```

To start a local cluster of 3 nodes use the following commands in different shells:

```bash
sbt "run -r zab -d /tmp/kvs/m1 -a localhost:9091 --bootstrap"
sbt "run -r zab -d /tmp/kvs/m2 -a localhost:9092 -m localhost:9091"
sbt "run -r zab -d /tmp/kvs/m3 -a localhost:9093 -m localhost:9091"
```
To add a key:

```
curl -X POST http://localhost:10091/kv/key1/value1
```

To retrieve a key:
```
curl http://localhost:10092/kv/key1
```

Admin console:
```bash
TODO
```

## Implementation details

 * Written in Scala, using sbt as building system
 * Keeps data only in memory (in a ``ConcurrentHashMap``)

## License

Apache 2.0