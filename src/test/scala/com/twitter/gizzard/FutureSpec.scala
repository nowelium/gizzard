package com.twitter.gizzard

import java.util.concurrent.{CountDownLatch, ExecutionException, SynchronousQueue,
  ThreadPoolExecutor, TimeoutException, TimeUnit}
import scala.collection.mutable
import com.twitter.xrayspecs.TimeConversions._
import org.specs.Specification
import org.specs.mock.{ClassMocker, JMocker}


object FutureSpec extends Specification with JMocker with ClassMocker {

  "Future" should {
    var future: Future = null

    doBefore {
      future = new Future("test", 1, 1, 1.hour, 50.milliseconds)
    }

    doAfter {
      future.shutdown()
    }

    "execute in the future" in {
      future { 3 * 4 }.get mustEqual 12
    }

    "timeout appropriately" in {
      future { Thread.sleep(20) }.get(10, TimeUnit.MILLISECONDS) must throwA[TimeoutException]
    }

    "timeout a stuffed-up queue" in {
      val startFlag = new CountDownLatch(1)
      val continueFlag = new CountDownLatch(1)
      future { startFlag.await(); continueFlag.countDown(); Thread.sleep(200) }
      startFlag.countDown()
      continueFlag.await()
      future { 3 * 4 }.get must throwA[ExecutionException]
    }
  }
}
