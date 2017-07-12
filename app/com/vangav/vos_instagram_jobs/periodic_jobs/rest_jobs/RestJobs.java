/**
 * "First, solve the problem. Then, write the code. -John Johnson"
 * "Or use Vangav M"
 * www.vangav.com
 * */

/**
 * MIT License
 *
 * Copyright (c) 2016 Vangav
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 * */

/**
 * Community
 * Facebook Group: Vangav Open Source - Backend
 *   fb.com/groups/575834775932682/
 * Facebook Page: Vangav
 *   fb.com/vangav.f
 * 
 * Third party communities for Vangav Backend
 *   - play framework
 *   - cassandra
 *   - datastax
 *   
 * Tag your question online (e.g.: stack overflow, etc ...) with
 *   #vangav_backend
 *   to easier find questions/answers online
 * */

package com.vangav.vos_instagram_jobs.periodic_jobs.rest_jobs;

import java.util.Calendar;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.vangav.backend.cassandra.formatting.CalendarFormatterInl;
import com.vangav.backend.content.formatting.EncodingInl;
import com.vangav.backend.content.formatting.SerializationInl;
import com.vangav.backend.metrics.time.CalendarAndDateOperationsInl;
import com.vangav.backend.metrics.time.Period;
import com.vangav.backend.metrics.time.RoundedOffCalendarInl;
import com.vangav.backend.metrics.time.RoundedOffCalendarInl.RoundingFactor;
import com.vangav.backend.metrics.time.TimeUnitType;
import com.vangav.backend.metrics.time.RoundedOffCalendarInl.RoundingType;
import com.vangav.backend.networks.jobs.Job;
import com.vangav.backend.networks.jobs.JobsExecutorInl;
import com.vangav.backend.thread_pool.periodic_jobs.CycleLog;
import com.vangav.backend.thread_pool.periodic_jobs.PeriodicJob;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_jobs.CurrentJobs;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_jobs.HourlyCurrentJobs;
import com.vangav.vos_instagram_jobs.common.Constants;

/**
 * @author mustapha
 * fb.com/mustapha.abdallah
 */
/**
 * RestJobs this job crawls hourly unfinished jobs
 */
public class RestJobs extends PeriodicJob<RestJobs> {
  
  /**
   * Constructor - RestJobs
   * @return new RestJobs Object
   * @throws Exception
   */
  public RestJobs () throws Exception {

    super(
      "rest_jobs",
      PeriodicJob.Type.ASYNC,
      RoundedOffCalendarInl.getRoundedCalendar(
        RoundingType.PAST,
        RoundingFactor.HOUR_OF_DAY),
      new Period(
        1.0,
        TimeUnitType.HOUR) );
  }

  @Override
  protected PeriodicJob<RestJobs> deepCopy() throws Exception {
    
    return new RestJobs();
  }

  @Override
  protected void process(CycleLog cycleLog) throws Exception {
    
    // get cycle's planned start calendar
    Calendar plannedStartCalendar =
      CalendarAndDateOperationsInl.getCalendarFromUnixTime(
        cycleLog.getPlannedStartTime() );
    
    // -1 hour -- this cycle should check for the jobs from the past hour
    plannedStartCalendar.set(Calendar.HOUR_OF_DAY, -1);
    
    // query all jobs within cycle's hour
    ResultSet resultSet =
      HourlyCurrentJobs.i().executeSyncSelect(
        CalendarFormatterInl.concatCalendarFields(
          plannedStartCalendar,
          Calendar.YEAR,
          Calendar.MONTH,
          Calendar.DAY_OF_MONTH,
          Calendar.HOUR_OF_DAY) );
    
    // to to fetch each job
    ResultSet currJobResultSet;
    
    String currSerializedJob;
    Job currJob;
    
    // retry executing every found job (failed to execute job)
    for (Row row : resultSet) {
      
      if (resultSet.getAvailableWithoutFetching() <=
          Constants.kCassandraPrefetchLimit &&
          resultSet.isFullyFetched() == false) {
        
        // this is asynchronous
        resultSet.fetchMoreResults();
      }
      
      // select job
      currJobResultSet =
        CurrentJobs.i().executeSyncSelect(
          row.getUUID(HourlyCurrentJobs.kJobIdColumnName) );
      
      // couldn't get job?
      if (currJobResultSet.isExhausted() == true) {
        
        // may need to log an exception here depending how how this service,
        //   the main service and the dispense work together - in terms of sync
        continue;
      }
      
      // get serialized job
      currSerializedJob =
        EncodingInl.decodeStringFromByteBuffer(
          currJobResultSet.one().getBytes(
            CurrentJobs.kJobColumnName) );
      
      // deserialize
      currJob = SerializationInl.<Job>deserializeObject(currSerializedJob);
      
      // execute job (retry)
      JobsExecutorInl.executeJobsAsync(currJob);
    }
  }

  @Override
  protected void postProcess(CycleLog cycleLog) throws Exception {
    
  }
}
