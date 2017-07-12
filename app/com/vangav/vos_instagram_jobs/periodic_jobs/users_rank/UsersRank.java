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

package com.vangav.vos_instagram_jobs.periodic_jobs.users_rank;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import vangav_m.vangavmusersrank.VangavMUsersRank;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.vangav.backend.cassandra.Cassandra;
import com.vangav.backend.cassandra.formatting.CalendarFormatterInl;
import com.vangav.backend.data_structures_and_algorithms.heap.MaxHeap;
import com.vangav.backend.exceptions.CodeException;
import com.vangav.backend.exceptions.VangavException;
import com.vangav.backend.exceptions.VangavException.ExceptionClass;
import com.vangav.backend.geo.geo_grids.GeoCoordinates;
import com.vangav.backend.geo.geo_grids.GeoGrid;
import com.vangav.backend.geo.reverse_geo_coding.ReverseGeoCoding;
import com.vangav.backend.math.NumbersInl;
import com.vangav.backend.metrics.time.CalendarAndDateOperationsInl;
import com.vangav.backend.metrics.time.Period;
import com.vangav.backend.metrics.time.RoundedOffCalendarInl;
import com.vangav.backend.metrics.time.TimeUnitType;
import com.vangav.backend.metrics.time.RoundedOffCalendarInl.RoundingFactor;
import com.vangav.backend.metrics.time.RoundedOffCalendarInl.RoundingType;
import com.vangav.backend.thread_pool.periodic_jobs.CycleLog;
import com.vangav.backend.thread_pool.periodic_jobs.PeriodicJob;
import com.vangav.backend.thread_pool.periodic_jobs.CycleTicker.CycleTickerBuilder;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.CountPerWeek;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.CountTotal;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.FollowerCount;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.UserPostsCount;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.UsersIndex;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.UsersInfo;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.UsersRankCountry;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.UsersRankGrid;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.UsersRankWorld;
import com.vangav.vos_instagram_jobs.common.Constants;
import com.vangav.vos_instagram_jobs.periodic_jobs.posts_rank.HeapNodePostRank;

/**
 * @author mustapha
 * fb.com/mustapha.abdallah
 */
/**
 * UsersRank crawls all registered users and rank them in clusters:
 *   - per grid (all)
 *   - per country (top 1000) - 250 per each of the 4 parallel jobs
 *   - the whole world (top 1000) - 250 per each of the 4 parallel jobs
 * To distribute load, this job works 4 times in parallel where each of them
 *   is responsible for ranking 25% of the posts
 */
public class UsersRank extends PeriodicJob<UsersRank> {

  private static final int kTopCountPerJob = 250;
  private static final long kCycleStep = 4L;
  
  private static long currInitialCycle = 1L;
  private long initialCycle;

  /**
   * Constructor - UsersRank
   * @return new UsersRank Object
   * @throws Exception
   */
  public UsersRank () throws Exception {
    
    this(currInitialCycle, false);
  }
  
  /**
   * Constructor - UsersRank
   * @param initialCycle
   * @param copy - true if this is a copy construct and false otherwise
   * @return new UsersRank Object
   * @throws Exception
   */
  private UsersRank (
    long initialCycle,
    boolean copy) throws Exception {
    
    super (
      "users_rank_" + initialCycle,
      PeriodicJob.Type.ASYNC,
      RoundedOffCalendarInl.getRoundedCalendar(
        RoundingType.PAST,
        RoundingFactor.MONTH),
      new Period(
        1.0,
        TimeUnitType.WEEK),
      new CycleTickerBuilder()
        .initialCycle(initialCycle)
        .cycleStep(kCycleStep)
        .build() );
    
    if (copy == false) {
    
      this.initialCycle = initialCycle;
      currInitialCycle += 1;
      
      if (currInitialCycle > 5) {
        
        throw new CodeException(
          500,
          1,
          "UsersRank periodic job is designed to run 4 times in parallel only, "
            + "trying to run it for the ["
            + (currInitialCycle - 1)
            + "] is invalid.",
          ExceptionClass.INVALID);
      }
    }
  }
  
  @Override
  protected PeriodicJob<UsersRank> deepCopy () throws Exception {
    
    return new UsersRank(this.initialCycle, true);
  }
  
  private static final Calendar kBackendStartTimeCalendar;
  static {
   
    try {
      kBackendStartTimeCalendar =
        CalendarAndDateOperationsInl.getCalendarFromUnixTime(
          Constants.kBackendStartTime);
    } catch (Exception e) {
      
      throw new CodeException(
        501,
        1,
        "Couldn't initialize kBackendStartTimeCalendar: "
          + VangavException.getExceptionStackTrace(e),
        ExceptionClass.INITIALIZATION);
    }
  }

  @Override
  protected void process(CycleLog cycleLog) throws Exception {
    
    // get cycle's planned start calendar
    Calendar plannedStartCalendar =
      CalendarAndDateOperationsInl.getCalendarFromUnixTime(
        cycleLog.getPlannedStartTime() );
    
    // get all calendar days since the start of the backend
    ArrayList<Calendar> allDaysCalendars =
      CalendarAndDateOperationsInl.getCalendarsFromTo(
        kBackendStartTimeCalendar,
        plannedStartCalendar);
    
    // -1 week -- this cycle gets a user's weekly counts from the last week
    Calendar lastWeekCalendar = (Calendar)plannedStartCalendar.clone();
    lastWeekCalendar.set(Calendar.WEEK_OF_YEAR, -1);
    
    // users registered per-day
    ResultSet usersIndexResultSet;
    
    // current user's id and time
    UUID currUserId;
    
    // ranking factors queries
    ArrayList<BoundStatement> boundStatements;
    
    // ranking factors result sets
    ArrayList<ResultSet> currResultSets;
    
    // ranking factors rows
    Row currRow;
    
    // current user's elements
    long userRegistrationTime;
    double userRegisteredSinceDays;
    double userFollowCountLastWeek;
    double userUnfollowCountLastWeek;
    double userPostsCountLastWeek;
    double userLikesCountLastWeek;
    double userCommentsCountLastWeek;
    double userFollowCountTotal;
    double userPostsCountTotal;
    double userLikesCountTotal;
    double userCommentsCountTotal;
    double latitude;
    double longitude;
    
    double userRank;
    
    String userCountryCode;
    long userGridId;
    
    // keep track of top users worldwide and per country code
    MaxHeap worldTopRank = new MaxHeap(kTopCountPerJob);
    Map<String, MaxHeap> perCountryTopRank = new HashMap<String, MaxHeap>();
    
    // get a new VangavMUsersRank instance
    VangavMUsersRank vangavMUsersRank = new VangavMUsersRank();
    
    // current day's calendar
    Calendar currDayCalendar;
    
    // for each 4th day
    for (long i = this.getCurrentCycle();
         i < allDaysCalendars.size();
         i += kCycleStep) {
      
      // current day's calendar
      currDayCalendar = allDaysCalendars.get((int)i);
      
      // get all user's registered on this day
      usersIndexResultSet =
        UsersIndex.i().executeSyncSelect(
          CalendarFormatterInl.concatCalendarFields(
            currDayCalendar,
            Calendar.YEAR,
            Calendar.MONTH,
            Calendar.DAY_OF_MONTH) );
      
      // for each user
      for (Row row : usersIndexResultSet) {
        
        if (usersIndexResultSet.getAvailableWithoutFetching() <=
            Constants.kCassandraPrefetchLimit &&
            usersIndexResultSet.isFullyFetched() == false) {
          
          // this is asynchronous
          usersIndexResultSet.fetchMoreResults();
        }
        
        // get rank per user
        try {
          
          // get user's id
          currUserId = row.getUUID(UsersIndex.kUserIdColumnName);
          
          // get days since user's registration
          
          userRegistrationTime =
            row.getLong(UsersIndex.kRegistrationTimeColumnName);
          
          userRegisteredSinceDays =
            ((plannedStartCalendar.getTimeInMillis() - userRegistrationTime) /
             (1000 * 60 * 60 * 24) );
          
          // reset bound statements
          boundStatements = new ArrayList<BoundStatement>();
          
          // (0) - select from ig_app_data.count_per_week
          boundStatements.add(
            CountPerWeek.i().getBoundStatementSelect(
              currUserId.toString()
              + Constants.kCassandraKeySeparator
              + CalendarFormatterInl.concatCalendarFields(
                lastWeekCalendar,
                Calendar.YEAR,
                Calendar.WEEK_OF_YEAR) ) );
          
          // (1) - select from ig_app_data.follower_count
          boundStatements.add(
            FollowerCount.i().getBoundStatementSelect(currUserId) );
          
          // (2) - select from ig_app_data.user_posts_count
          boundStatements.add(
            UserPostsCount.i().getBoundStatementSelect(currUserId) );
          
          // (3) - select from ig_app_data.count_total
          boundStatements.add(
            CountTotal.i().getBoundStatementSelect(currUserId) );
          
          // (4) - select from ig_app_data.users_info
          boundStatements.add(
            UsersInfo.i().getBoundStatementSelectLastLocation(currUserId) );
          
          // execute bound statements
          currResultSets =
            Cassandra.i().executeSync(
              boundStatements.toArray(new BoundStatement[0] ) );
          
          // (0) - get from ig_app_data.count_per_week
          currRow = currResultSets.get(0).one();
          
          userFollowCountLastWeek =
            (double)currRow.getLong(CountPerWeek.kFollowerCountColumnName);
          
          userFollowCountLastWeek =
            NumbersInl.normalize(
              userFollowCountLastWeek,
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserCommentsCountLastWeekMin),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserCommentsCountLastWeekMax),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserCommentsCountLastWeekMinVangavM),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserCommentsCountLastWeekMaxVangavM) );
          
          userUnfollowCountLastWeek =
            (double)currRow.getLong(CountPerWeek.kUnfollowerCountColumnName);
          
          userUnfollowCountLastWeek =
            NumbersInl.normalize(
              userUnfollowCountLastWeek,
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserUnfollowCountLastWeekMin),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserUnfollowCountLastWeekMax),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserUnfollowCountLastWeekMinVangavM),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserUnfollowCountLastWeekMaxVangavM) );
          
          userPostsCountLastWeek =
            (double)currRow.getLong(CountPerWeek.kPostsCountColumnName);
          
          userPostsCountLastWeek =
            NumbersInl.normalize(
              userPostsCountLastWeek,
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserPostsCountLastWeekMin),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserPostsCountLastWeekMax),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserPostsCountLastWeekMinVangavM),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserPostsCountLastWeekMaxVangavM) );
          
          userLikesCountLastWeek =
            (double)currRow.getLong(
              CountPerWeek.kLikesReceivedCountColumnName);
          
          userLikesCountLastWeek =
            NumbersInl.normalize(
              userLikesCountLastWeek,
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserLikesCountLastWeekMin),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserLikesCountLastWeekMax),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserLikesCountLastWeekMinVangavM),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserLikesCountLastWeekMaxVangavM) );
          
          userCommentsCountLastWeek =
            (double)currRow.getLong(
              CountPerWeek.kCommentsReceivedCountColumnName);
          
          userCommentsCountLastWeek =
            NumbersInl.normalize(
              userCommentsCountLastWeek,
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserCommentsCountLastWeekMin),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserCommentsCountLastWeekMax),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserCommentsCountLastWeekMinVangavM),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserCommentsCountLastWeekMaxVangavM) );
          
          // (1) - get from ig_app_data.follower_count
          currRow = currResultSets.get(1).one();
          
          userFollowCountTotal =
            (double)currRow.getLong(FollowerCount.kFollowerCountColumnName);
          
          userFollowCountTotal =
            NumbersInl.normalize(
              userFollowCountTotal,
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserFollowCountTotalMin),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserFollowCountTotalMax),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserFollowCountTotalMinVangavM),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserFollowCountTotalMaxVangavM) );
          
          // (2) - get from ig_app_data.user_posts_count
          currRow = currResultSets.get(2).one();
          
          userPostsCountTotal =
            (double)currRow.getLong(UserPostsCount.kPostsCountColumnName);
          
          userPostsCountTotal =
            NumbersInl.normalize(
              userPostsCountTotal,
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserPostsCountTotalMin),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserPostsCountTotalMax),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserPostsCountTotalMinVangavM),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserPostsCountTotalMaxVangavM) );
          
          // (3) - get from ig_app_data.count_total
          currRow = currResultSets.get(3).one();
          
          userLikesCountTotal =
            (double)currRow.getLong(CountTotal.kLikesReceivedCountColumnName);
          
          userLikesCountTotal =
            NumbersInl.normalize(
              userLikesCountTotal,
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserLikesCountTotalMin),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserLikesCountTotalMax),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserLikesCountTotalMinVangavM),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserLikesCountTotalMaxVangavM) );
          
          userCommentsCountTotal =
            (double)currRow.getLong(
              CountTotal.kCommentsReceivedCountColumnName);
          
          userCommentsCountTotal =
            NumbersInl.normalize(
              userCommentsCountTotal,
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserCommentsCountTotalMin),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserCommentsCountTotalMax),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserCommentsCountTotalMinVangavM),
              UserRankProperties.i().getDoubleProperty(
                UserRankProperties.kUserCommentsCountTotalMaxVangavM) );
          
          // (4) - get from ig_app_data.users_info
          currRow = currResultSets.get(4).one();
          
          latitude = currRow.getDouble(UsersInfo.kLastLatitudeColumnName);
          longitude = currRow.getDouble(UsersInfo.kLastLongitudeColumnName);
          
          // calculate user's rank
          
          vangavMUsersRank.setUserRegisteredSinceDays(
            userRegisteredSinceDays);
          vangavMUsersRank.setUserFollowCountLastWeek(
            userFollowCountLastWeek);
          vangavMUsersRank.setUserUnfollowCountLastWeek(
            userUnfollowCountLastWeek);
          vangavMUsersRank.setUserPostsCountLastWeek(
            userPostsCountLastWeek);
          vangavMUsersRank.setUserLikesCountLastWeek(
            userLikesCountLastWeek);
          vangavMUsersRank.setUserCommentsCountLastWeek(
            userCommentsCountLastWeek);
          vangavMUsersRank.setUserFollowCountTotal(
            userFollowCountTotal);
          vangavMUsersRank.setUserPostsCountTotal(
            userPostsCountTotal);
          vangavMUsersRank.setUserLikesCountTotal(
            userLikesCountTotal);
          vangavMUsersRank.setUserCommentsCountTotal(
            userCommentsCountTotal);
          
          vangavMUsersRank.process();
          
          userRank = vangavMUsersRank.getPostRank();
          
          // get user's country code and grid id
          
          if (latitude == Constants.kCassandraDefaultDouble ||
              longitude == Constants.kCassandraDefaultDouble) {
            
            userCountryCode = Constants.kDefaultRegion;
            userGridId = Constants.kDefaultGridId;
          } else {
            
            userCountryCode =
              ReverseGeoCoding.i().getReverseGeoCode(
                latitude,
                longitude).getCountryCode();
            
            userGridId =
              new GeoGrid(
                Constants.kGeoGridsConfig,
                new GeoCoordinates(
                  latitude,
                  longitude) ).getGeoGridId().getId();
          }
          
          // update top users - world and per-country
          
          worldTopRank.insert(new HeapNodeUserRank(userRank, currUserId) );
          
          if (perCountryTopRank.containsKey(userCountryCode) == false) {
            
            perCountryTopRank.put(
              userCountryCode,
              new MaxHeap(kTopCountPerJob) );
          }
          
          perCountryTopRank.get(
            userCountryCode).insert(
              new HeapNodePostRank(userRank, currUserId) );
          
          // insert into ig_app_data.users_rank_grid
          UsersRankGrid.i().executeSyncInsert(
            CalendarFormatterInl.concatCalendarFields(
              plannedStartCalendar,
              Calendar.YEAR,
              Calendar.WEEK_OF_YEAR)
              + Constants.kCassandraKeySeparator
              + userGridId,
            userRank,
            currUserId);
        } catch (VangavException ve) {
          
          cycleLog.addNonFatalVangavException(ve);
        } catch (Exception e) {
          
          cycleLog.addNonFatalException(e);
        }
      }
    }
    
    // insert into ig_app_data.users_rank_world and
    //   ig_app_data.users_rank_country
    
    HeapNodeUserRank currHeapNodeUserRank;
    
    // reset bound statements
    boundStatements = new ArrayList<BoundStatement>();
    
    // insert into ig_app_data.users_rank_world
    while (worldTopRank.isEmpty() == false) {
      
      currHeapNodeUserRank = (HeapNodeUserRank)worldTopRank.remove();
      
      boundStatements.add(
        UsersRankWorld.i().getBoundStatementInsert(
          CalendarFormatterInl.concatCalendarFields(
            plannedStartCalendar,
            Calendar.YEAR,
            Calendar.WEEK_OF_MONTH),
          currHeapNodeUserRank.getValue(),
          currHeapNodeUserRank.getUserId() ) );
    }
    
    // insert into ig_app_data.users_rank_country
    
    MaxHeap currCountryMaxHeap;
    
    for (String countryCode : perCountryTopRank.keySet() ) {
      
      currCountryMaxHeap = perCountryTopRank.get(countryCode);
      
      while (currCountryMaxHeap.isEmpty() == false) {
        
        currHeapNodeUserRank = (HeapNodeUserRank)currCountryMaxHeap.remove();
        
        boundStatements.add(
          UsersRankCountry.i().getBoundStatementInsert(
            CalendarFormatterInl.concatCalendarFields(
              plannedStartCalendar,
              Calendar.YEAR,
              Calendar.MONTH,
              Calendar.DAY_OF_MONTH)
            + Constants.kCassandraKeySeparator
            + countryCode,
            currHeapNodeUserRank.getValue(),
            currHeapNodeUserRank.getUserId() ) );
      }
    }
    
    // execute bound statements
    Cassandra.i().executeSync(
      boundStatements.toArray(new BoundStatement[0] ) );
  }

  @Override
  protected void postProcess(CycleLog cycleLog) throws Exception {
    
  }
}
