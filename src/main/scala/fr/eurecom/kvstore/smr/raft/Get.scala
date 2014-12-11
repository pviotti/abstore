package fr.eurecom.kvstore.smr.raft

import ckite.rpc.ReadCommand

case class Get(key: String) extends ReadCommand[String]