package com.wangyy.multilanes.core.infra.finagle

import com.twitter.finagle._
import com.twitter.finagle.loadbalancer.{Balancer, LeastLoaded, Updating}
import com.twitter.finagle.stats.StatsReceiver
import com.twitter.finagle.util.Rng
import com.twitter.util._
import com.wangyy.multilanes.core.trace.{FTConstants, FeatureTagContext}

import scala.collection.mutable.ListBuffer

/**
 * feature路由 + P2C
 */
private class FeatureTagBalancer[Req, Rep](
    protected val activity: Activity[Traversable[ServiceFactory[Req, Rep]]],
    protected val maxEffort: Int,
    protected val rng: Rng,
    protected val statsReceiver: StatsReceiver,
    protected val emptyException: NoBrokersAvailableException)
  extends Balancer[Req, Rep]
  with LeastLoaded[Req, Rep]
  with FeatureTagRoute[Req, Rep]
  with Updating[Req, Rep] {

  protected[this] val maxEffortExhausted = statsReceiver.counter("max_effort_exhausted")

}


private trait FeatureTagRoute[Req, Rep] { self: Balancer[Req, Rep] =>

  protected def rng: Rng

  protected class Distributor(val vector: Vector[Node]) extends DistributorT {
    type This = Distributor

    // Indicates if we've seen any down nodes during pick which we expected to be available
    @volatile private[this] var sawDown = false

    private[this] val nodeUp: Node => Boolean = { node =>
      node.status == Status.Open
    }

    private[this] val (up, down) = vector.partition(nodeUp)

    def needsRebuild: Boolean =
      sawDown || (down.nonEmpty && down.exists(nodeUp))

    def rebuild(): This = new Distributor(vector)
    def rebuild(vec: Vector[Node]): This = new Distributor(vec)

    def pick(): Node = {
      if (vector.isEmpty)
        return failingNode(emptyException)

      // if all nodes are down, we might as well try to send requests somewhere
      // as our view of the world may be out of date.
      val vec = if (up.isEmpty) down else up

      val rft = FeatureTagContext.get()

      val fts = new ListBuffer[Node]
      val bases = new ListBuffer[Node]
      for (node <- vec; if node.attachments != null) {
        val featureTagAttaOpt = node.attachments.get(FTConstants.FEATURE_TAG_PATH)
        featureTagAttaOpt match {
          case Some(sft) =>
            if (sft.equals(rft)) {
              fts += node
            } else if (sft.equals(FTConstants.FEATURE_TAG_BASE_LANE_VALUE)) {
              bases += node
            }
          case None =>
        }
      }

      if (fts.nonEmpty) {
        //有对应featureTag的server
        pickFromP2C(fts)
      } else {
        //路由到base_line
        pickFromP2C(bases)
      }
    }

    //P2C兜底
    def pickFromP2C(vec: ListBuffer[Node]): Node = {
      if (vec.isEmpty)
        return failingNode(emptyException)

      val size = vec.size

      if (size == 1) vec.head else {
        val a = rng.nextInt(size)
        var b = rng.nextInt(size)

        // Try to pick b, b != a, up to 10 times.
        var i = 10
        while (a == b && i > 0) {
          b = rng.nextInt(size)
          i -= 1
        }

        val nodeA = vec(a)
        val nodeB = vec(b)

        if (nodeA.status != Status.Open || nodeB.status != Status.Open)
          sawDown = true

        if (nodeA.load < nodeB.load) nodeA else nodeB
      }
    }
  }

  protected def initDistributor() = new Distributor(Vector.empty)
}

