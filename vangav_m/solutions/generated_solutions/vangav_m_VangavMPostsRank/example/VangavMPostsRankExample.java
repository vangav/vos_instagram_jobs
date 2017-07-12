
// Solution UUID: e7ede357-712c-446b-9178-622309e272b6
// here's an example of using vangav_m_VangavMPostsRank.jar

// HOWTO: open your terminal, cd to this solution's directory
//          then enter > chmod u+x compileExample.sh 
//          then enter > ./compileExample.sh
//          then enter > java -jar VangavMPostsRankExample.jar

// 1- link vangav_m_VangavMPostsRank.jar

// 2- import VangavMPostsRank
import vangav_m.vangavmpostsrank.VangavMPostsRank;
 
public class VangavMPostsRankExample {

  private static void invokeVangavMPostsRank (
    double PostHour,
    double PostLikesCount,
    double PostCommentsCount,
    double UserRegisteredSinceDays,
    double UserFollowCountLastWeek,
    double UserUnfollowCountLastWeek,
    double UserPostsCountLastWeek,
    double UserLikesCountLastWeek,
    double UserCommentsCountLastWeek) throws Exception {
  
    // 3- set inputs
    VangavMPostsRank.i().setPostHour(PostHour);
    VangavMPostsRank.i().setPostLikesCount(PostLikesCount);
    VangavMPostsRank.i().setPostCommentsCount(PostCommentsCount);
    VangavMPostsRank.i().setUserRegisteredSinceDays(UserRegisteredSinceDays);
    VangavMPostsRank.i().setUserFollowCountLastWeek(UserFollowCountLastWeek);
    VangavMPostsRank.i().setUserUnfollowCountLastWeek(UserUnfollowCountLastWeek);
    VangavMPostsRank.i().setUserPostsCountLastWeek(UserPostsCountLastWeek);
    VangavMPostsRank.i().setUserLikesCountLastWeek(UserLikesCountLastWeek);
    VangavMPostsRank.i().setUserCommentsCountLastWeek(UserCommentsCountLastWeek);

    // 4- process
    VangavMPostsRank.i().process();
    
    System.out.println("----");
    System.out.println("Inputs:");
    System.out.println("PostHour:" + PostHour);
    System.out.println("PostLikesCount:" + PostLikesCount);
    System.out.println("PostCommentsCount:" + PostCommentsCount);
    System.out.println("UserRegisteredSinceDays:" + UserRegisteredSinceDays);
    System.out.println("UserFollowCountLastWeek:" + UserFollowCountLastWeek);
    System.out.println("UserUnfollowCountLastWeek:" + UserUnfollowCountLastWeek);
    System.out.println("UserPostsCountLastWeek:" + UserPostsCountLastWeek);
    System.out.println("UserLikesCountLastWeek:" + UserLikesCountLastWeek);
    System.out.println("UserCommentsCountLastWeek:" + UserCommentsCountLastWeek);

    System.out.println();
    
    // 5- get outputs
    System.out.println("Outputs:");
    System.out.println("PostRank: " + VangavMPostsRank.i().getPostRank() );

    System.out.println();
  }
  
  public static void main (String [] args) throws Exception {
  
    // here's an example of invoking VangavMPostsRank solution
    //   with 3 different sets of inputs
    invokeVangavMPostsRank(
      -0.01,
      -0.01,
      -0.01,
      -0.01,
      -0.01,
      -0.01,
      -0.01,
      -0.01,
      -0.01);
    invokeVangavMPostsRank(
      11.5,
      500.0,
      500.0,
      30.0,
      500.0,
      500.0,
      50.0,
      500.0,
      500.0);
    invokeVangavMPostsRank(
      23.01,
      1000.01,
      1000.01,
      60.01,
      1000.01,
      1000.01,
      100.01,
      1000.01,
      1000.01);

  }
}
