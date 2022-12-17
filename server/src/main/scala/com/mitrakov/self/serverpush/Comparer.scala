package com.mitrakov.self.serverpush

sealed trait Comparer {
  def compare(x: Double, y: Double): Boolean
  def toString: String
}

case object LessComparer extends Comparer {
  override def compare(x: Double, y: Double): Boolean = x <= y
  override def toString: String = "≤"
}

case object GreaterComparer extends Comparer {
  override def compare(x: Double, y: Double): Boolean = x >= y
  override def toString: String = "≥"
}
