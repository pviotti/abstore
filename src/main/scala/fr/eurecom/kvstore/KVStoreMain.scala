package fr.eurecom.kvstore

import ckite.RaftBuilder
import fr.eurecom.kvstore.http.HttpServer
import fr.eurecom.kvstore.smr.raft.KVStore
import scopt.OptionParser

case class Config(smr: String = "raft", dataDir: String = "/tmp/kvstore", address: String = "localhost:9091",
                  bootstrap: Boolean = false, members: String = "")

/**
 * KVStore main class.
 * To launch it from command line, using sbt:
 * $ sbt "run -r raft -a localhost:9091 -d /tmp/kvstore -bootstrap"
 */
object KVStoreMain extends App {

  val parser = new OptionParser[Config]("kvstore") {
    head("kvstore", "0.1.0")
    opt[String]('r', "smr") required() action { (x, c) =>
      c.copy(smr = x) } text("SMR protocol (raft, zab)")
    opt[String]('d', "dataDir") required() action { (x, c) =>
      c.copy(dataDir = x) } text("Data directory")
    opt[String]('a', "address") required() action { (x, c) =>
      c.copy(address = x) } text("Listening address")
    opt[Unit]("bootstrap") action { (_, c) =>
      c.copy(bootstrap = true) } text("Bootstrap option (for the very first node)")
    opt[String]('m', "members") action { (x, c) =>
      c.copy(members = x) } text("Members addresses")
    checkConfig { c =>
      if (!c.members.isEmpty && c.bootstrap) failure("You set both bootstrap and members.") else success }
    help("help") text("A simple experimental datastore.")
  }

  parser.parse(args.toSeq, Config()) map { config =>

    val raft = RaftBuilder().listenAddress(config.address)
      .members(config.members.split(",")) //optional seeds to join the cluster
      .bootstrap(config.bootstrap)
      .dataDir(config.dataDir) //dataDir for persistent state (log, terms, snapshots, etc...)
      .stateMachine(new KVStore()) //KVStore is an implementation of the StateMachine trait
      .sync(false) //disables log sync to disk
      .build

    raft.start
    val http = HttpServer(raft)
    http.start

  } getOrElse { // arguments are bad, usage message will have been displayed
    sys.exit(-1)
  }
}
