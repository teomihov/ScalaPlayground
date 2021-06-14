package quartzExamples

import org.quartz.{CronScheduleBuilder, Job, JobBuilder, JobExecutionContext, TriggerBuilder}
import org.quartz.impl._

import java.text.SimpleDateFormat
import java.util.Date

// https://github.com/quartz-scheduler/quartz/blob/master/docs/tutorials/tutorial-lesson-09.md
// https://www.programmersought.com/article/17075302774/
// https://github.com/quartz-scheduler/quartz/blob/d42fb7770f287afbf91f6629d90e7698761ad7d8/quartz-core/src/main/resources/org/quartz/impl/jdbcjobstore/tables_postgres.sql
object Example9 extends App {
  class MyJobStoreTX extends Job {
    override def execute(context: JobExecutionContext) {
      val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val nowDate = sdf.format(new Date())
      println("MyJobStoreTX backup database time is:" + nowDate)
    }
  }

  // Get Scheduler
  val scheduler = StdSchedulerFactory.getDefaultScheduler();

  scheduler.clear()

  val jobDetail = JobBuilder.newJob(classOf[MyJobStoreTX])
    .withIdentity("jobTX1", "groupTX1")
    .storeDurably(true)
    .build();
  // Create Trigger
  val trigger = TriggerBuilder.newTrigger()
    .withIdentity("triggerTX1", "groupTX1") // set the identity
    .startNow()
    // Execute every 3 seconds
    .withSchedule(CronScheduleBuilder.cronSchedule("0/2 * * * * ?"))
    .build();

  // Associate job and trigger
  scheduler.scheduleJob(jobDetail, trigger);

  // start scheduler
  scheduler.start();
}
