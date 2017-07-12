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

package com.vangav.vos_instagram_jobs.periodic_jobs.posts_rank;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import vangav_m.vangavmpostsrank.VangavMPostsRank;

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
import com.vangav.backend.thread_pool.periodic_jobs.CycleTicker.CycleTickerBuilder;
import com.vangav.backend.thread_pool.periodic_jobs.CycleLog;
import com.vangav.backend.thread_pool.periodic_jobs.PeriodicJob;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.CountPerWeek;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.PostCommentsCount;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.PostLikesCount;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.Posts;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.PostsIndex;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.PostsRankCountry;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.PostsRankGrid;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.PostsRankWorld;
import com.vangav.vos_instagram_jobs.cassandra_keyspaces.ig_app_data.UsersInfo;
import com.vangav.vos_instagram_jobs.common.Constants;

/**
 * @author mustapha
 * fb.com/mustapha.abdallah
 */
/**
 * PostsRank crawls all posts posted every day and rank them in clusters:
 *   - per grid (all)
 *   - per country (top 1000) - 250 per each of the 4 parallel jobs
 *   - the whole world (top 1000) - 250 per each of the 4 parallel jobs
 * To distribute load, this job works 4 times in parallel where each of them
 *   is responsible for ranking 25% of the posts
 */
public class PostsRank extends PeriodicJob<PostsRank> {

  private static final int kTopCountPerJob = 250;
  private static final long kCycleStep = 4L;
  
  private static long currInitialCycle = 1L;
  private long initialCycle;
  
  /**
   * Constructor - PostsRank
   * @return new PostsRank Object
   * @throws Exception
   */
  public PostsRank () throws Exception {
    
    this(currInitialCycle, false);
  }
  
  /**
   * Constructor - PostsRank
   * @param initialCycle
   * @param copy - true if this is a copy construct and false otherwise
   * @return new PostsRank Object
   * @throws Exception
   */
  private PostsRank (
    long initialCycle,
    boolean copy) throws Exception {
    
    super (
      "posts_rank_" + initialCycle,
      PeriodicJob.Type.ASYNC,
      RoundedOffCalendarInl.getRoundedCalendar(
        RoundingType.PAST,
        RoundingFactor.DAY_OF_MONTH),
      new Period(
        1.0,
        TimeUnitType.DAY),
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
          "PostsRank periodic job is designed to run 4 times in parallel only, "
            + "trying to run it for the ["
            + (currInitialCycle - 1)
            + "] is invalid.",
          ExceptionClass.INVALID);
      }
    }
  }

  @Override
  protected PeriodicJob<PostsRank> deepCopy () throws Exception {
    
    return new PostsRank(this.initialCycle, true);
  }

  @Override
  protected void process(CycleLog cycleLog) throws Exception {
    
    // get cycle's planned start calendar
    Calendar plannedStartCalendar =
      CalendarAndDateOperationsInl.getCalendarFromUnixTime(
        cycleLog.getPlannedStartTime() );
    
    // -1 day -- this cycle should rank posts from yesterday
    Calendar yesterdayCalendar = (Calendar)plannedStartCalendar.clone();
    yesterdayCalendar.set(Calendar.DAY_OF_MONTH, -1);
    
    // cassandra calendar key for yesterday
    String calendarKeyYesterday =
      CalendarFormatterInl.concatCalendarFields(
        yesterdayCalendar,
        Calendar.YEAR,
        Calendar.MONTH,
        Calendar.DAY_OF_MONTH);
    
    // -1 week -- this cycle gets a user's weekly counts from the last week
    Calendar lastWeekCalendar = (Calendar)plannedStartCalendar.clone();
    lastWeekCalendar.set(Calendar.WEEK_OF_YEAR, -1);
    
    // posts per-grid
    ResultSet postsIndexResultSet;
    
    // current post's id and time
    UUID currPostId;
    long currPostTime;
    
    // ranking factors queries
    ArrayList<BoundStatement> boundStatements;
    
    // ranking factors result sets
    ArrayList<ResultSet> currResultSets;
    
    // ranking factors rows
    Row currRow;
    
    // current post's elements
    double postHour;
    UUID userId;
    double latitude;
    double longitude;
    double postLikesCount;
    double postCommentsCount;
    long userRegistrationTime;
    double userRegisteredSinceDays;
    double userFollowCountLastWeek;
    double userUnfollowCountLastWeek;
    double userPostsCountLastWeek;
    double userLikesCountLastWeek;
    double userCommentsCountLastWeek;
    
    double postRank;
    
    String postCountryCode;
    long postGridId;
    
    // keep track of top posts worldwide and per country code
    MaxHeap worldTopRank = new MaxHeap(kTopCountPerJob);
    Map<String, MaxHeap> perCountryTopRank = new HashMap<String, MaxHeap>();
    
    // get a new vangavMPostsRank instance
    VangavMPostsRank vangavMPostsRank = new VangavMPostsRank();
    
    // for each grid of the 25% of the grids this job handles
    for (long gridId = this.getCurrentCycle();
         gridId <= Constants.kGridsCount;
         gridId += kCycleStep) {
      
      // get all posts in a grid
      postsIndexResultSet =
        PostsIndex.i().executeSyncSelect(
          calendarKeyYesterday
          + Constants.kCassandraKeySeparator
          + gridId);
      
      // for each post
      for (Row row : postsIndexResultSet) {
        
        if (postsIndexResultSet.getAvailableWithoutFetching() <=
            Constants.kCassandraPrefetchLimit &&
            postsIndexResultSet.isFullyFetched() == false) {
          
          // this is asynchronous
          postsIndexResultSet.fetchMoreResults();
        }
        
        // get rank per post
        try {
        
          // get post's id and time
          currPostId = row.getUUID(PostsIndex.kPostIdColumnName);
          currPostTime = row.getLong(PostsIndex.kPostTimeColumnName);
          
          // get post's hour
          postHour =
            (double)CalendarAndDateOperationsInl.getCalendarFromUnixTime(
              currPostTime).get(Calendar.HOUR_OF_DAY);
          
          // reset bound statements
          boundStatements = new ArrayList<BoundStatement>();
          
          // (0) - select from ig_app_data.posts
          boundStatements.add(
            Posts.i().getBoundStatementSelect(currPostId) );
          
          // (1) - select from ig_app_data.post_likes_count
          boundStatements.add(
            PostLikesCount.i().getBoundStatementSelect(currPostId) );
          
          // (2) - select from ig_app_data.post_comments_count
          boundStatements.add(
            PostCommentsCount.i().getBoundStatementSelect(currPostId) );
          
          // execute bound statements
          currResultSets =
            Cassandra.i().executeSync(
              boundStatements.toArray(new BoundStatement[0] ) );
          
          // (0) - get from ig_app_data.posts
          currRow = currResultSets.get(0).one();
          
          userId =
            currRow.getUUID(Posts.kUserIdColumnName);
          latitude =
            currRow.getDouble(Posts.kLatitudeColumnName);
          longitude =
            currRow.getDouble(Posts.kLongitudeColumnName);
          
          // (1) - get from ig_app_data.post_likes_count
          currRow = currResultSets.get(1).one();
          
          postLikesCount =
            (double)currRow.getLong(PostLikesCount.kLikesCountColumnName);
          
          postLikesCount =
            NumbersInl.normalize(
              postLikesCount,
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kPostLikesCountMin),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kPostLikesCountMax),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kPostLikesCountMinVangavM),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kPostLikesCountMaxVangavM) );
          
          // (2) - get from ig_app_data.post_comments_count
          currRow = currResultSets.get(2).one();
          
          postCommentsCount =
            (double)currRow.getLong(
              PostCommentsCount.kCommentsCountColumnName);
          
          postCommentsCount =
            NumbersInl.normalize(
              postCommentsCount,
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kPostCommentsCountMin),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kPostCommentsCountMax),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kPostCommentsCountMinVangavM),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kPostCommentsCountMaxVangavM) );
          
          // reset bound statements
          boundStatements = new ArrayList<BoundStatement>();
          
          // (0) - select from ig_app_data.users_info
          boundStatements.add(
            UsersInfo.i().getBoundStatementSelectRegistrationTime(userId) );
          
          // (1) - select from ig_app_data.count_per_week
          boundStatements.add(
            CountPerWeek.i().getBoundStatementSelect(
              userId.toString()
              + Constants.kCassandraKeySeparator
              + CalendarFormatterInl.concatCalendarFields(
                lastWeekCalendar,
                Calendar.YEAR,
                Calendar.WEEK_OF_YEAR) ) );
          
          // execute bound statements
          currResultSets =
            Cassandra.i().executeSync(
              boundStatements.toArray(new BoundStatement[0] ) );
          
          // (0) - get from ig_app_data.users_info
          currRow = currResultSets.get(0).one();
          
          userRegistrationTime =
            currRow.getLong(UsersInfo.kRegistrationTimeColumnName);
          
          userRegisteredSinceDays =
            ((plannedStartCalendar.getTimeInMillis() - userRegistrationTime) /
             (1000 * 60 * 60 * 24) );
          
          // (1) - get from ig_app_data.count_per_week
          currRow = currResultSets.get(1).one();
          
          userFollowCountLastWeek =
            (double)currRow.getLong(CountPerWeek.kFollowerCountColumnName);
          
          userFollowCountLastWeek =
            NumbersInl.normalize(
              userFollowCountLastWeek,
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserFollowCountLastWeekMin),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserFollowCountLastWeekMax),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserFollowCountLastWeekMinVangavM),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserFollowCountLastWeekMaxVangavM) );
          
          userUnfollowCountLastWeek =
            (double)currRow.getLong(CountPerWeek.kUnfollowerCountColumnName);
          
          userUnfollowCountLastWeek =
            NumbersInl.normalize(
              userUnfollowCountLastWeek,
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserUnfollowCountLastWeekMin),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserUnfollowCountLastWeekMax),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserUnfollowCountLastWeekMinVangavM),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserUnfollowCountLastWeekMaxVangavM) );
          
          userPostsCountLastWeek =
            (double)currRow.getLong(CountPerWeek.kPostsCountColumnName);
          
          userPostsCountLastWeek =
            NumbersInl.normalize(
              userPostsCountLastWeek,
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserPostsCountLastWeekMin),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserPostsCountLastWeekMax),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserPostsCountLastWeekMinVangavM),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserPostsCountLastWeekMaxVangavM) );
          
          userLikesCountLastWeek =
            (double)currRow.getLong(
              CountPerWeek.kLikesReceivedCountColumnName);
          
          userLikesCountLastWeek =
            NumbersInl.normalize(
              userLikesCountLastWeek,
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserLikesCountLastWeekMin),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserLikesCountLastWeekMax),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserLikesCountLastWeekMinVangavM),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserLikesCountLastWeekMaxVangavM) );
          
          userCommentsCountLastWeek =
            (double)currRow.getLong(
              CountPerWeek.kCommentsReceivedCountColumnName);
          
          userCommentsCountLastWeek =
            NumbersInl.normalize(
              userCommentsCountLastWeek,
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserCommentsCountLastWeekMin),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserCommentsCountLastWeekMax),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserCommentsCountLastWeekMinVangavM),
              PostRankProperties.i().getDoubleProperty(
                PostRankProperties.kUserCommentsCountLastWeekMaxVangavM) );
          
          // calculate post's rank
          
          vangavMPostsRank.setPostHour(
            postHour);
          vangavMPostsRank.setPostLikesCount(
            postLikesCount);
          vangavMPostsRank.setPostCommentsCount(
            postCommentsCount);
          vangavMPostsRank.setUserRegisteredSinceDays(
            userRegisteredSinceDays);
          vangavMPostsRank.setUserFollowCountLastWeek(
            userFollowCountLastWeek);
          vangavMPostsRank.setUserUnfollowCountLastWeek(
            userUnfollowCountLastWeek);
          vangavMPostsRank.setUserPostsCountLastWeek(
            userPostsCountLastWeek);
          vangavMPostsRank.setUserLikesCountLastWeek(
            userLikesCountLastWeek);
          vangavMPostsRank.setUserCommentsCountLastWeek(
            userCommentsCountLastWeek);
          
          vangavMPostsRank.process();
          
          postRank = vangavMPostsRank.getPostRank();
          
          // get post's country code and grid id
          
          if (latitude == Constants.kCassandraDefaultDouble ||
              longitude == Constants.kCassandraDefaultDouble) {
            
            postCountryCode = Constants.kDefaultRegion;
            postGridId = Constants.kDefaultGridId;
          } else {
            
            postCountryCode =
              ReverseGeoCoding.i().getReverseGeoCode(
                latitude,
                longitude).getCountryCode();
            
            postGridId =
              new GeoGrid(
                Constants.kGeoGridsConfig,
                new GeoCoordinates(
                  latitude,
                  longitude) ).getGeoGridId().getId();
          }
          
          // update top posts - world and per-country
          
          worldTopRank.insert(new HeapNodePostRank(postRank, currPostId) );
          
          if (perCountryTopRank.containsKey(postCountryCode) == false) {
            
            perCountryTopRank.put(
              postCountryCode,
              new MaxHeap(kTopCountPerJob) );
          }
          
          perCountryTopRank.get(
            postCountryCode).insert(
              new HeapNodePostRank(postRank, currPostId) );
          
          
          // insert into ig_app_data.posts_rank_grid
          PostsRankGrid.i().executeSyncInsert(
            CalendarFormatterInl.concatCalendarFields(
              plannedStartCalendar,
              Calendar.YEAR,
              Calendar.MONTH,
              Calendar.DAY_OF_MONTH)
            + Constants.kCassandraKeySeparator
            + postGridId,
            postRank,
            currPostId);
        } catch (VangavException ve) {
          
          cycleLog.addNonFatalVangavException(ve);
        } catch (Exception e) {
          
          cycleLog.addNonFatalException(e);
        }
      }
    }
    
    // insert into ig_app_data.posts_rank_world and
    //   ig_app_data.posts_rank_country
    
    HeapNodePostRank currHeapNodePostRank;
    
    // reset bound statements
    boundStatements = new ArrayList<BoundStatement>();
    
    // insert into ig_app_data.posts_rank_world
    while (worldTopRank.isEmpty() == false) {
      
      currHeapNodePostRank = (HeapNodePostRank)worldTopRank.remove();
      
      boundStatements.add(
        PostsRankWorld.i().getBoundStatementInsert(
          CalendarFormatterInl.concatCalendarFields(
            plannedStartCalendar,
            Calendar.YEAR,
            Calendar.MONTH,
            Calendar.DAY_OF_MONTH),
          currHeapNodePostRank.getValue(),
          currHeapNodePostRank.getPostId() ) );
    }
      
    // insert into ig_app_data.posts_rank_country
    
    MaxHeap currCountryMaxHeap;
    
    for (String countryCode : perCountryTopRank.keySet() ) {
      
      currCountryMaxHeap = perCountryTopRank.get(countryCode);
      
      while (currCountryMaxHeap.isEmpty() == false) {
        
        currHeapNodePostRank = (HeapNodePostRank)currCountryMaxHeap.remove();
        
        boundStatements.add(
          PostsRankCountry.i().getBoundStatementInsert(
            CalendarFormatterInl.concatCalendarFields(
              plannedStartCalendar,
              Calendar.YEAR,
              Calendar.MONTH,
              Calendar.DAY_OF_MONTH)
            + Constants.kCassandraKeySeparator
            + countryCode,
            currHeapNodePostRank.getValue(),
            currHeapNodePostRank.getPostId() ) );
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
