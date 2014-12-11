package fr.eurecom.kvstore.smr

/**
 * Created by paolo on 11/12/14.
 */
trait KVStore {

  def init(): Unit = {}

  def put(key: String, value: String) : Unit = {}

  def get(key: String) : String = { "NOT_IMPLEMENTED" }

}
