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

package com.vangav.vos_instagram_jobs.common;

import com.vangav.backend.exceptions.CodeException;
import com.vangav.backend.exceptions.VangavException;
import com.vangav.backend.exceptions.VangavException.ExceptionClass;
import com.vangav.backend.geo.geo_grids.GeoGridsConfig;
import com.vangav.backend.metrics.distance.Distance;
import com.vangav.backend.metrics.distance.DistanceUnitType;
import com.vangav.vos_instagram_jobs.common.properties.ConstantsProperties;

/**
 * @author mustapha
 * fb.com/mustapha.abdallah
 */
/**
 * Constants has all vos_instagram_jobs's constants
 */
public class Constants {

  // disable default instantiation
  private Constants () {}
  
  /**
   * kBackendStartTime is used to mark the unix time stamp (in milli-seconds)
   *   when the backend was first started in production
   */
  public static final long kBackendStartTime;
  static {
    
    try {
      
      kBackendStartTime =
        ConstantsProperties.i().getLongProperty(
          ConstantsProperties.kBackendStartTime);
    } catch (Exception e) {
      
      throw new CodeException(
        300,
        1,
        "Couldn't initialize kBackendStartTime: "
          + VangavException.getExceptionStackTrace(e),
        ExceptionClass.INITIALIZATION);
    }
  }
  
  /**
   * kCassandraPrefetchLimit defines the limit at which new result set's
   *   rows get fetched
   */
  public static final int kCassandraPrefetchLimit = 100;
  
  /**
   * kCassandraKeySeparator is used to concat multi-part cassandra keys
   */
  public static final String kCassandraKeySeparator = "_";
  
  /**
   * kCassandraDefaultDouble is the double's equivalent of NULL
   */
  public static final double kCassandraDefaultDouble = 0.0;
  
  /**
   * kDefaultRegion is used when the needed region can't be determined
   */
  public static final String kDefaultRegion;
  static {
    
    try {
      
      kDefaultRegion =
        ConstantsProperties.i().getStringPropterty(
          ConstantsProperties.kDefaultRegion);
    } catch (Exception e) {
      
      throw new CodeException(
        300,
        2,
        "Couldn't initialize kDefaultRegion: "
          + VangavException.getExceptionStackTrace(e),
        ExceptionClass.INITIALIZATION);
    }
  }
  
  /**
   * kWorldRegion is used to represent the whole world's region
   */
  public static final String kWorldRegion;
  static {
    
    try {
      
      kWorldRegion =
        ConstantsProperties.i().getStringPropterty(
          ConstantsProperties.kWorldRegion);
    } catch (Exception e) {
      
      throw new CodeException(
        300,
        3,
        "Couldn't initialize kWorldRegion: "
          + VangavException.getExceptionStackTrace(e),
        ExceptionClass.INITIALIZATION);
    }
  }
  
  /**
   * kGeoGridDimensionMetres defines the dimension of geo grids
   */
  public static final double kGeoGridDimensionMetres;
  static {
    
    try {
      
      kGeoGridDimensionMetres =
        ConstantsProperties.i().getDoubleProperty(
          ConstantsProperties.kGeoGridDimensionMetres);
    } catch (Exception e) {
      
      throw new CodeException(
        300,
        4,
        "Couldn't initialize kGeoGridDimensionMetres: "
          + VangavException.getExceptionStackTrace(e),
        ExceptionClass.INITIALIZATION);
    }
  }
  
  /**
   * kDefaultGridId is used when the needed grid id can't be determined
   */
  public static final long kDefaultGridId;
  static {
    
    try {
      
      kDefaultGridId =
        ConstantsProperties.i().getLongProperty(
          ConstantsProperties.kDefaultGridId);
    } catch (Exception e) {
      
      throw new CodeException(
        300,
        5,
        "Couldn't initialize kDefaultGridId: "
          + VangavException.getExceptionStackTrace(e),
        ExceptionClass.INITIALIZATION);
    }
  }
  
  /**
   * kGeoGridsConfig defines the geo grids config
   */
  public static final GeoGridsConfig kGeoGridsConfig;
  static {
    
    try {
      
      kGeoGridsConfig =
        new GeoGridsConfig(
          "vos_instagram",
          new Distance(
            kGeoGridDimensionMetres,
            DistanceUnitType.METRE) );
    } catch (Exception e) {
      
      throw new CodeException(
        300,
        6,
        "Couldn't initialize kGeoGridsConfig: "
          + VangavException.getExceptionStackTrace(e),
        ExceptionClass.INITIALIZATION);
    }
  }
  
  /**
   * kGridsCount represents the total count of grids in kGeoGridsConfig
   */
  public static final long kGridsCount =
    kGeoGridsConfig.getGridsHorizontalCount() *
    kGeoGridsConfig.getGridsVerticalCount();
}
