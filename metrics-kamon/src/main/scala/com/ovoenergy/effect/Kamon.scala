package com.ovoenergy.effect

import cats.effect.Sync
import cats.syntax.functor._
import com.ovoenergy.effect.Metrics.Metric
import kamon.{Kamon => K}

import kamon.tag.TagSet

object Kamon {

  /**
   * An instance of metrics for a Sync[F] which wraps the underlying calls
   */
  def syncKamonMetrics[F[_]: Sync]: Metrics[F] = new Metrics[F] {

    def counter(metric: Metric): F[Long => F[Unit]] =
      Sync[F]
        .delay(K.counter(metric.name).withTags(TagSet.from(metric.tags)))
        .map(
          counter => times => Sync[F].delay(counter.increment(times)).as(())
        )

    def histogram(metric: Metric): F[Long => F[Unit]] =
      Sync[F]
        .delay(K.histogram(metric.name).withTags(TagSet.from(metric.tags)))
        .map(
          histogram => duration => Sync[F].delay(histogram.record(duration)).as(())
        )
  }
}
