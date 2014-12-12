package fr.eurecom.abstore.kvs

trait KVStore {
  def init(): Unit = {}
  def put(key: String, value: String) : Unit = {}
  def get(key: String) : String = { "" }
}
