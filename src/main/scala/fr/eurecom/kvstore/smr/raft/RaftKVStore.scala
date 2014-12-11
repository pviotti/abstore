package fr.eurecom.kvstore.smr.raft

import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

import ckite.Raft
import ckite.statemachine.StateMachine
import ckite.util.{Logging, Serializer}
import fr.eurecom.kvstore.smr.KVStore


class RaftKVStore extends StateMachine with Logging with KVStore {

  val map = new ConcurrentHashMap[String, String]()

  var raft: Raft = _

  def setRaft(r: Raft): Unit = { raft = r }

  override def init(): Unit = { raft start }

  override def get(key: String): String = { raft.readLocal(new Get(key)) }

  override def put(key: String, value: String) : Unit = { raft.write(Put(key, value)) }

  @volatile
  var lastIndex: Long = 0

  def applyWrite = {
    case (index, Put(key: String, value: String)) => {
      LOG.debug(s"Put $key=$value")
      map.put(key, value)
      lastIndex = index
      value
    }
  }

  def applyRead = {   // FIXME implement Raft non local read
    case Get(key) => {
      LOG.debug(s"Get $key")
      map.get(key)
    }
  }

  def lastAppliedIndex: Long = lastIndex

  def deserialize(byteBuffer: ByteBuffer) = {
    val snapshotBytes = byteBuffer.array()
    val deserializedMap: ConcurrentHashMap[String, String] = Serializer.deserialize[ConcurrentHashMap[String, String]](snapshotBytes)
    map.clear()
    map.putAll(deserializedMap)
  }

  def serialize(): ByteBuffer = ByteBuffer.wrap(Serializer.serialize(map))

}