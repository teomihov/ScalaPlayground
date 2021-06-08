package quartzExamples
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.{Job, JobBuilder, JobExecutionContext}
import org.quartz.impl._

//https://github.com/quartz-scheduler/quartz/blob/master/docs/tutorials/tutorial-lesson-03.md
object Example3 extends App {
  val schedFact = new StdSchedulerFactory();
  val sched = schedFact.getScheduler();

  sched.start();

  // define the job and tie it to our HelloJob class
  val dumpJob = new Job {
    override def execute(context: JobExecutionContext) = {
      val key = context.getJobDetail().getKey();

      val dataMap = context.getJobDetail().getJobDataMap();

      val jobSays = dataMap.getString("jobSays");
      val myDoubleValue = dataMap.getFloat("myDoubleValue");

      println("Instance " + key + " of DumbJob says: " + jobSays + ", and val is: " + myDoubleValue);
    }
  }
  val job = JobBuilder.newJob(dumpJob.getClass)
    .withIdentity("Job", "Group")
    .usingJobData("jobSays", "Hello World!")
    .usingJobData("myDoubleValue", 33.2)
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

