package com.wangyy.multilanes.core.infra.finagle

import com.twitter.finagle._
import com.twitter.finagle.loadbalancer.Balancers.MaxEffort
import com.twitter.finagle.loadbalancer.LoadBalancerFactory
import com.twitter.finagle.stats.StatsReceiver
import com.twitter.finagle.util.Rng
import com.twitter.util._

object OmniBalancers {

  def featureTagRoute(): LoadBalancerFactory = new LoadBalancerFactory {
    def newBalancer[Req, Rep](
                               endpoints: Activity[Set[ServiceFactory[Req, Rep]]],
                               sr: StatsReceiver,
                               exc: NoBrokersAvailableException
                             ): ServiceFactory[Req, Rep] =
      new FeatureTagBalancer(endpoints, MaxEffort, Rng.threadLocal, sr, exc) {
        private[this] val gauge = sr.addGauge("featureTag")(1)
      }
  }


  //TODO
  def areaPriorityRoute(): LoadBalancerFactory = new LoadBalancerFactory {
    def newBalancer[Req, Rep](endpoints: Activity[Set[ServiceFactory[Req, Rep]]], statsReceiver: StatsReceiver, emptyException: NoBrokersAvailableException): ServiceFactory[Req, Rep] = ???
  }

}
