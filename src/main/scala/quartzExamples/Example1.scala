package quartzExamples

import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.{Job, JobBuilder, JobExecutionContext}
import org.quartz.impl._


// https://github.com/quartz-scheduler/quartz/blob/master/docs/tutorials/tutorial-lesson-01.md
object Example1 extends App {
  val schedFact = new StdSchedulerFactory();
  val sched = schedFact.getScheduler();

  sched.start();

  // define the job and tie it to our HelloJob class
  val helloJob = new Job {

    override def execute(jobExecutionContext: JobExecutionContext) = {
      println(s"Hello from HelloJob. Context: $jobExecutionContext")
    }
  }
  val job = JobBuilder.newJob(helloJob.getClass)
    .withIdentity("Job", "Group")
    .build

  // Trigger the job to run now, and then every 40 seconds
  val trigger = newTrigger()
    .withIdentity("myTrigger", "group1")
    .startNow()
    .withSchedule(simpleSchedule()
      .withIntervalInSeconds(5)
      .withRepeatCount(5))
    .build();

  // Tell quartz to schedule the job using our trigger
  sched.scheduleJob(job, trigger);
  Thread.sleep(11000)
  sched.shutdown(true);
}
