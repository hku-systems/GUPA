package edu.hku.cs.dp

import org.apache.spark.rdd.RDD

import scala.math.{exp, pow}
import scala.reflect.ClassTag

/**
  * Created by lionon on 10/22/18.
  */
class dpobjectArray[T: ClassTag](
  var inputsample : RDD[(T,Long)],
  var inputsample_advance : RDD[(T,Long)],
  var inputoriginal : RDD[T])
  extends RDD[T](inputoriginal)
{

  var sample = inputsample //for each element, sample refers to "if this element exists"
  var sample_advance = inputsample_advance
  var original = inputoriginal
  var sample_addition = inputsample
  val epsilon = 0.1
  val delta = pow(10,-8)
  val k_distance_double = 1/epsilon
  val k_distance = k_distance_double.toInt
  val beta = epsilon / (2*scala.math.log(2/delta))


  override def compute(split: org.apache.spark.Partition, context: org.apache.spark.TaskContext): Iterator[T] =
  {
    inputoriginal.iterator(split, context)
  }

  override protected def getPartitions: Array[org.apache.spark.Partition] =
    inputoriginal.partitions

  def mapDP[U: ClassTag](f: T => U): dpobjectArray[U]= {
<<<<<<< HEAD
    val t1 = System.nanoTime
    val r1 = inputsample.map(p => p.map(f))
    val r2 = sample_advance.map(p => p.map(f))
    val r3 = inputoriginal.map(f)
    val duration = (System.nanoTime - t1) / 1e9d
    println("mapDP: " + duration)
    new dpobjectArray(r1,r2,r3)
  }

  def mapDPKV[K: ClassTag,V: ClassTag](f: T => (K,V)): dpobjectKVArray[K,V]= {
    val t1 = System.nanoTime
    val r1 = inputsample.map(p => p.map(f)).asInstanceOf[Array[RDD[(K,V)]]]
    val r2 = sample_advance.map(p => p.map(f)).asInstanceOf[Array[RDD[(K,V)]]]
    val r3 = inputoriginal.map(f).asInstanceOf[RDD[(K,V)]]
    val duration = (System.nanoTime - t1) / 1e9d
    println("mapDP: " + duration)
    new dpobjectKVArray(r1,r2,r3)
  }

  def filterDP(f: T => Boolean) : dpobjectArray[T] = {
    val t1 = System.nanoTime
    val r1 = inputsample.map(p => p.filter(f))
    val r2 = inputsample_advance.map(p => p.filter(f))
    val r3 = inputoriginal.filter(f)
    val duration = (System.nanoTime - t1) / 1e9d
    println("filterDP: " + duration)
    new dpobjectArray(r1,r2,r3)
  }

  def reduceDP(f: (T, T) => T) : (Array[RDD[T]],Array[RDD[T]],T) = {
    //The "sample" field carries the aggregated result already
    val t1 = System.nanoTime
=======
    new dpobjectArray(inputsample.map(p => (f(p._1),p._2)),sample_advance.map(p => (f(p._1),p._2)),inputoriginal.map(f))
  }

  def mapDPKV[K: ClassTag,V: ClassTag](f: T => (K,V)): dpobjectKVArray[K,V]= {
    new dpobjectKVArray(inputsample.map(p => (f(p._1),p._2)),sample_advance.map(p => (f(p._1),p._2)),inputoriginal.map(f))
  }

  def filterDP(f: T => Boolean) : dpobjectArray[T] = {
    new dpobjectArray(inputsample.filter(p => f(p._1)),inputsample_advance.filter(p => f(p._1)),inputoriginal.filter(f))
  }

  def reduceDP(f: (T, T) => T) : (RDD[Array[T]],RDD[Array[T]],T, Double) = {
    //The "sample" field carries the aggregated result already "
    val parameters = scala.io.Source.fromFile("security.csv").mkString.split(',')
    val epsilon = parameters(0).toDouble
    val delta = parameters(1).toDouble
    val k_distance_double = 1/epsilon
    val k_distance = k_distance_double.toInt
    val beta = epsilon / (2*scala.math.log(2/delta))

>>>>>>> master
    val result = original.reduce(f)
      val filtered_sample = sample.map(p => (p._2,p._1)).reduceByKey(f).map(_._2)
      var aggregatedResult = result //get the aggregated result
     if(!filtered_sample.isEmpty())
         aggregatedResult = f(filtered_sample.reduce(f), result) //get the aggregated result
      val filtered_sample_advance = sample_advance.map(p => (p._2,p._1)).reduceByKey(f).map(_._2)
      val broadcast_result = original.sparkContext.broadcast(result)
      val broadcast_aggregatedResult = original.sparkContext.broadcast(aggregatedResult)
      val s_collect = filtered_sample.collect()
      val a_collect = filtered_sample_advance.collect()
      //    val inner = new ArrayBuffer[V]
      var inner_num = 0
      var outer_num = k_distance
      val sample_count = filtered_sample.count //e.g., 64
      val sample_advance_count = filtered_sample_advance.count
      val broadcast_sample = original.sparkContext.broadcast(s_collect)
      val broadcast_sample_advance = original.sparkContext.broadcast(a_collect)
      //***********samples*********************

      val sample_array = sample_count match {
        case a if a == 0 =>
          val only_array = new Array[T](1)
          only_array(0) = aggregatedResult
          original.sparkContext.parallelize(Seq(only_array))
        case b if b == 1 =>
          val only_array = new Array[T](1)
          only_array(0) = f(result, s_collect.head)
          original.sparkContext.parallelize(Seq(only_array)) //without that sample
        case _ =>
          if (sample_count <= k_distance)
            outer_num = k_distance - 1
          else
            outer_num = k_distance //outer_num = 10
        var i = outer_num + 1 //11
        val up_to_index = (sample_count - i).toInt
          val b_i = original.sparkContext.broadcast(i)
          val inner_array = original.sparkContext.parallelize(0 to up_to_index - 1)
            .map(p => {
              f(broadcast_sample.value.patch(p, Nil, b_i.value).reduce(f), broadcast_result.value) //(0 -> 7, 8 -> 15, 16, 24, 32, 40, 48, 56)
            })
          val array_collected = inner_array.collect()
          val upper_array = original.sparkContext.broadcast(array_collected)
          original.sparkContext.parallelize(0 to up_to_index - 1) //(0,1,2,3,4,5,6,7)
            .map(p => {
            var neighnout_o = new Array[T](b_i.value - 1)
            var j = b_i.value - 1 //start form 10
            while (j >= 1) {
              if (j == b_i.value - 1)
                neighnout_o(j - 1) = f(upper_array.value(p), broadcast_sample.value(p + j + 1)) //add back 11, so would be 1 to 10
              else
                neighnout_o(j - 1) = f(broadcast_sample.value(p + j + 1), neighnout_o(j))
              j = j - 1
            }
            neighnout_o
          })
      }
      //**********sample advance*************

      val sample_array_advance = sample_advance_count match {
        case a if a == 0 =>
          var only_array_advance = new Array[T](1)
          only_array_advance(0) = aggregatedResult
          original.sparkContext.parallelize(Seq(only_array_advance))
        case b if b == 1 =>
          var only_array_advance = new Array[T](1)
          only_array_advance(0) = f(aggregatedResult, a_collect.head)
          original.sparkContext.parallelize(Seq(only_array_advance)) //without that sample
        case _ =>
          if (sample_advance_count <= k_distance)
            outer_num = k_distance - 1
          else
            outer_num = k_distance //outer_num = 10
        var i = outer_num
          val up_to_index = (sample_advance_count - i).toInt
          val b_i = original.sparkContext.broadcast(i)
          original.sparkContext.parallelize(0 to up_to_index - 1)
            .map(p => {
              var neighnout_o = new Array[T](b_i.value)
              var j = 0 //start form 10
              while (j < b_i.value) {
                if (j == 0)
                  neighnout_o(j) = f(broadcast_sample_advance.value(p), broadcast_aggregatedResult.value)
                else
                  neighnout_o(j) = f(broadcast_sample_advance.value(p + j), neighnout_o(j - 1))
                j = j + 1
              }
              neighnout_o
            })
      }
<<<<<<< HEAD
    }
    val duration = (System.nanoTime - t1) / 1e9d
    println("reduceDP: " + duration)
    (array,array_advance,aggregatedResult)
  }

def reduce_and_add_noise_KDE(f: (T, T) => T, app_name: String, k_dist: Int): T = {
  //computin candidates of smooth sensitivity
var array = reduceDP(f).asInstanceOf[(Array[RDD[Double]],Array[RDD[Double]],Double)]
  val t1 = System.nanoTime

  var a1_length = array._1.length
  var neigbour_local_senstivity = new Array[Double](a1_length)
  for(a1 <- 0 until array._1.length)
    {
      val p = array._1(a1)
      if(!p.isEmpty) {
        val max = p.max
        val min = p.min
        val mean = p.mean
        val sd = p.stdev
        val counting = p.count()
//        println(app_name + "," + k_dist + "," + ((a1 + 1) * (-1)) + "," + mean + "," + sd + "," + counting)
        neigbour_local_senstivity(a1) = scala.math.max(scala.math.abs(max - array._3), scala.math.abs(min - array._3))
      }
      else {
//        println(app_name + "," + k_dist + "," + ((a1 + 1) * (-1)) + "," + array._3 + "," + 0 + "," + 0)
        neigbour_local_senstivity(a1) = 0.0
=======
    (sample_array,sample_array_advance,aggregatedResult,beta)
  }

  def reduce_and_add_noise_KDE(f: (T, T) => T, app_name: String, k_dist: Int): T = {
    //computin candidates of smooth sensitivity

    var array = reduceDP(f).asInstanceOf[(RDD[Array[Double]],RDD[Array[Double]],Double, Double)]
    val beta = array._4

    if(!array._1.isEmpty() && !array._2.isEmpty()) {
      val stat_sample = array._1
        .map(p => (p, p, p, 1))
        .reduce((a, b) => {
          val max = a._1.zip(b._1).map(x => scala.math.max(x._1, x._2))
          val min = a._2.zip(b._2).map(x => scala.math.min(x._1, x._2))
          val sum = a._3.zip(b._3).map(x => x._1 + x._2)
          (max, min, sum, a._4 + b._4)
        })
      val sample_count = stat_sample._4

      val sample_mean = stat_sample._3.map(s => s / sample_count)

      val b_sample_mean = original.sparkContext.broadcast(sample_mean)

      val sample_variance = array._1.map(s => {
        var sd_inner = s
        for (sa <- 0 until s.length) {
          sd_inner(sa) = pow(s(sa) - b_sample_mean.value(sa), 2)
        }
        sd_inner
      }).reduce((a, b) => {
        val zipped = a.zip(b)
        val sum = zipped.map(x => x._1 + x._2)
        sum
      }).map(sv => sv / sample_count)

      val local_sensitivity_sample = stat_sample._1
        .zip(stat_sample._2)
        .map(q => {
          scala.math.max(scala.math.abs(q._1 - array._3), scala.math.abs(q._2 - array._3))
        })

      //********sample advance
      val stat_sample_advance = array._2
        .map(p => (p, p, p, 1))
        .reduce((a, b) => {
          val max = a._1.zip(b._1).map(x => scala.math.max(x._1, x._2))
          val min = a._2.zip(b._2).map(x => scala.math.min(x._1, x._2))
          val sum = a._3.zip(b._3).map(x => x._1 + x._2)
          (max, min, sum, a._4 + b._4)
        })
      val sample_count_advance = stat_sample_advance._4

      val sample_mean_advance = stat_sample_advance._3.map(s => s / sample_count_advance)

      val b_sample_mean_advance = original.sparkContext.broadcast(sample_mean_advance)

      val sample_variance_advance = array._2.map(s => {
        var sd_inner = s
        for (sa <- 0 until s.length) {
          sd_inner(sa) = pow(s(sa) - b_sample_mean_advance.value(sa), 2)
        }
        sd_inner
      }).reduce((a, b) => {
        val zipped = a.zip(b)
        val sum = zipped.map(x => x._1 + x._2)
        sum
      }).map(sv => sv / sample_count_advance)

      val local_sensitivity_sample_advance = stat_sample_advance._1
        .zip(stat_sample_advance._2)
        .map(q => {
          scala.math.max(scala.math.abs(q._1 - array._3), scala.math.abs(q._2 - array._3))
        })
      //End of sample advance

      for (m1 <- 0 until sample_mean.length) {
        println(app_name + "," + k_dist + "," + ((m1 + 1) * (-1)) + "," + sample_mean(m1) + "," + sample_variance(m1) + "," + sample_count)
>>>>>>> master
      }

<<<<<<< HEAD
  var a2_length = array._2.length
  var neigbour_local_advance_senstivity = new Array[Double](a2_length)
  for(a2 <- 0 until a2_length)
  {
    val p = array._2(a2)
    if(!p.isEmpty) {
      val max = p.max
      val min = p.min
      val mean = p.mean
      val sd = p.stdev
      val counting = p.count()
//      println(app_name + "," + k_dist + "," + (a2 + 1) + "," + mean + "," + sd + "," + counting)
      neigbour_local_advance_senstivity(a2) = scala.math.max(scala.math.abs(max - array._3), scala.math.abs(min - array._3))
    }
    else {
//      println(app_name + "," + k_dist + "," + (a2 + 1) + "," + array._3 + "," + 0 + "," + 0)
      neigbour_local_advance_senstivity(a2) = 0.0
    }
  }
=======
      for (m2 <- 0 until sample_mean_advance.length) {
        println(app_name + "," + k_dist + "," + (m2 + 1) + "," + sample_mean_advance(m2) + "," + sample_variance_advance(m2) + "," + sample_count_advance)
      }
>>>>>>> master

      var max_nls = 0.0
      for (i <- 0 until local_sensitivity_sample.length) {
        local_sensitivity_sample(i) = local_sensitivity_sample(i) * exp(-beta * (i + 1))
        if (local_sensitivity_sample(i) > max_nls)
          max_nls = local_sensitivity_sample(i)
      }

      for (i <- 0 until local_sensitivity_sample_advance.length) {
        local_sensitivity_sample_advance(i) = local_sensitivity_sample_advance(i) * exp(-beta * (i + 1))
        if (local_sensitivity_sample_advance(i) > max_nls)
          max_nls = local_sensitivity_sample_advance(i)
      }
    }

    array._3.asInstanceOf[T] //sensitivity
  }
<<<<<<< HEAD
  val duration = (System.nanoTime - t1) / 1e9d
  println("Calsensitivity: " + duration)
  array._3.asInstanceOf[T] //sensitivity
}
=======
>>>>>>> master

}
