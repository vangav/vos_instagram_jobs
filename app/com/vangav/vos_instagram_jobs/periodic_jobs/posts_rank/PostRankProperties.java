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

import java.util.HashMap;
import java.util.Map;

import com.vangav.backend.properties.PropertiesFile;
import com.vangav.backend.properties.PropertiesLoader;

/**
 * @author mustapha
 * fb.com/mustapha.abdallah
 */
/**
 * PostRankProperties is the properties file hold post-rank-related constants
 */
public class PostRankProperties extends PropertiesFile {

  private static PostRankProperties instance = null;
  
  // disable default instantiation
  private PostRankProperties () {}
  
  public static PostRankProperties i () {
    
    if (instance == null) {
      
      instance = new PostRankProperties();
    }
    
    return instance;
  }
  
  private static final String kName = "posts_rank_properties.prop";
  
  @Override
  public String getName () {
    
    return kName;
  }
  
  // properties names
  public static final String kPostLikesCountMin =
    "post_likes_count_min";
  public static final String kPostLikesCountMax =
    "post_likes_count_max";
  public static final String kPostLikesCountMinVangavM =
    "post_likes_count_min_vangav_m";
  public static final String kPostLikesCountMaxVangavM =
    "post_likes_count_max_vangav_m";
  
  public static final String kPostCommentsCountMin =
    "post_comments_count_min";
  public static final String kPostCommentsCountMax =
    "post_comments_count_max";
  public static final String kPostCommentsCountMinVangavM =
    "post_comments_count_min_vangav_m";
  public static final String kPostCommentsCountMaxVangavM =
    "post_comments_count_max_vangav_m";
  
  public static final String kUserFollowCountLastWeekMin =
    "user_follow_count_last_week_min";
  public static final String kUserFollowCountLastWeekMax =
    "user_follow_count_last_week_max";
  public static final String kUserFollowCountLastWeekMinVangavM =
    "user_follow_count_last_week_min_vangav_m";
  public static final String kUserFollowCountLastWeekMaxVangavM =
    "user_follow_count_last_week_max_vangav_m";
  
  public static final String kUserUnfollowCountLastWeekMin =
    "user_unfollow_count_last_week_min";
  public static final String kUserUnfollowCountLastWeekMax =
    "user_unfollow_count_last_week_max";
  public static final String kUserUnfollowCountLastWeekMinVangavM =
    "user_unfollow_count_last_week_min_vangav_m";
  public static final String kUserUnfollowCountLastWeekMaxVangavM =
    "user_unfollow_count_last_week_max_vangav_m";
  
  public static final String kUserPostsCountLastWeekMin =
    "user_posts_count_last_week_min";
  public static final String kUserPostsCountLastWeekMax =
    "user_posts_count_last_week_max";
  public static final String kUserPostsCountLastWeekMinVangavM =
    "user_posts_count_last_week_min_vangav_m";
  public static final String kUserPostsCountLastWeekMaxVangavM =
    "user_posts_count_last_week_max_vangav_m";
  
  public static final String kUserLikesCountLastWeekMin =
    "user_likes_count_last_week_min";
  public static final String kUserLikesCountLastWeekMax =
    "user_likes_count_last_week_max";
  public static final String kUserLikesCountLastWeekMinVangavM =
    "user_likes_count_last_week_min_vangav_m";
  public static final String kUserLikesCountLastWeekMaxVangavM =
    "user_likes_count_last_week_max_vangav_m";
  
  public static final String kUserCommentsCountLastWeekMin =
    "user_comments_count_last_week_min";
  public static final String kUserCommentsCountLastWeekMax =
    "user_comments_count_last_week_max";
  public static final String kUserCommentsCountLastWeekMinVangavM =
    "user_comments_count_last_week_min_vangav_m";
  public static final String kUserCommentsCountLastWeekMaxVangavM =
    "user_comments_count_last_week_max_vangav_m";
  
  public static final String kPostRankMin =
    "post_rank_min";
  public static final String kPostRankMax =
    "post_rank_max";
  public static final String kPostRankMinVangavM =
    "post_rank_min_vangav_m";
  public static final String kPostRankMaxVangavM =
    "post_rank_max_vangav_m";
  
  // property name -> property default value
  private static final Map<String, String> kProperties;
  static {
    
    kProperties = new HashMap<String, String>();
    
    kProperties.put(
      kPostLikesCountMin,
      "0");
    kProperties.put(
      kPostLikesCountMax,
      "1000");
    kProperties.put(
      kPostLikesCountMinVangavM,
      "0");
    kProperties.put(
      kPostLikesCountMaxVangavM,
      "1000");
    
    kProperties.put(
      kPostCommentsCountMin,
      "0");
    kProperties.put(
      kPostCommentsCountMax,
      "100");
    kProperties.put(
      kPostCommentsCountMinVangavM,
      "0");
    kProperties.put(
      kPostCommentsCountMaxVangavM,
      "1000");
    
    kProperties.put(
      kUserFollowCountLastWeekMin,
      "0");
    kProperties.put(
      kUserFollowCountLastWeekMax,
      "1000");
    kProperties.put(
      kUserFollowCountLastWeekMinVangavM,
      "0");
    kProperties.put(
      kUserFollowCountLastWeekMaxVangavM,
      "1000");
    
    kProperties.put(
      kUserUnfollowCountLastWeekMin,
      "0");
    kProperties.put(
      kUserUnfollowCountLastWeekMax,
      "100");
    kProperties.put(
      kUserUnfollowCountLastWeekMinVangavM,
      "0");
    kProperties.put(
      kUserUnfollowCountLastWeekMaxVangavM,
      "1000");
    
    kProperties.put(
      kUserPostsCountLastWeekMin,
      "0");
    kProperties.put(
      kUserPostsCountLastWeekMax,
      "100");
    kProperties.put(
      kUserPostsCountLastWeekMinVangavM,
      "0");
    kProperties.put(
      kUserPostsCountLastWeekMaxVangavM,
      "100");
    
    kProperties.put(
      kUserLikesCountLastWeekMin,
      "0");
    kProperties.put(
      kUserLikesCountLastWeekMax,
      "3000");
    kProperties.put(
      kUserLikesCountLastWeekMinVangavM,
      "0");
    kProperties.put(
      kUserLikesCountLastWeekMaxVangavM,
      "1000");
    
    kProperties.put(
      kUserCommentsCountLastWeekMin,
      "0");
    kProperties.put(
      kUserCommentsCountLastWeekMax,
      "300");
    kProperties.put(
      kUserCommentsCountLastWeekMinVangavM,
      "0");
    kProperties.put(
      kUserCommentsCountLastWeekMaxVangavM,
      "1000");
    
    kProperties.put(
      kPostRankMin,
      "0");
    kProperties.put(
      kPostRankMax,
      "100000");
    kProperties.put(
      kPostRankMinVangavM,
      "0");
    kProperties.put(
      kPostRankMaxVangavM,
      "1000");
  }
  
  @Override
  protected String getProperty (String name) throws Exception {
    
    return PropertiesLoader.i().getProperty(
      this.getName(),
      name,
      kProperties.get(name) );
  }
}
