
// Solution UUID: 5e9973c1-972e-40ca-b89f-f89e5cac315d
// here's an example of using vangav_m_VangavMUsersRank.jar

// HOWTO: open your terminal, cd to this solution's directory
//          then enter > chmod u+x compileExample.sh 
//          then enter > ./compileExample.sh
//          then enter > java -jar VangavMUsersRankExample.jar

// 1- link vangav_m_VangavMUsersRank.jar

// 2- import VangavMUsersRank
import vangav_m.vangavmusersrank.VangavMUsersRank;
 
public class VangavMUsersRankExample {

  private static void invokeVangavMUsersRank (
    double UserRegisteredSinceDays,
    double UserFollowCountLastWeek,
    double UserUnfollowCountLastWeek,
    double UserPostsCountLastWeek,
    double UserLikesCountLastWeek,
    double UserCommentsCountLastWeek,
    double UserFollowCountTotal,
    double UserPostsCountTotal,
    double UserLikesCountTotal,
    double UserCommentsCountTotal) throws Exception {
  
    // 3- set inputs
    VangavMUsersRank.i().setUserRegisteredSinceDays(UserRegisteredSinceDays);
    VangavMUsersRank.i().setUserFollowCountLastWeek(UserFollowCountLastWeek);
    VangavMUsersRank.i().setUserUnfollowCountLastWeek(UserUnfollowCountLastWeek);
    VangavMUsersRank.i().setUserPostsCountLastWeek(UserPostsCountLastWeek);
    VangavMUsersRank.i().setUserLikesCountLastWeek(UserLikesCountLastWeek);
    VangavMUsersRank.i().setUserCommentsCountLastWeek(UserCommentsCountLastWeek);
    VangavMUsersRank.i().setUserFollowCountTotal(UserFollowCountTotal);
    VangavMUsersRank.i().setUserPostsCountTotal(UserPostsCountTotal);
    VangavMUsersRank.i().setUserLikesCountTotal(UserLikesCountTotal);
    VangavMUsersRank.i().setUserCommentsCountTotal(UserCommentsCountTotal);

    // 4- process
    VangavMUsersRank.i().process();
    
    System.out.println("----");
    System.out.println("Inputs:");
    System.out.println("UserRegisteredSinceDays:" + UserRegisteredSinceDays);
    System.out.println("UserFollowCountLastWeek:" + UserFollowCountLastWeek);
    System.out.println("UserUnfollowCountLastWeek:" + UserUnfollowCountLastWeek);
    System.out.println("UserPostsCountLastWeek:" + UserPostsCountLastWeek);
    System.out.println("UserLikesCountLastWeek:" + UserLikesCountLastWeek);
    System.out.println("UserCommentsCountLastWeek:" + UserCommentsCountLastWeek);
    System.out.println("UserFollowCountTotal:" + UserFollowCountTotal);
    System.out.println("UserPostsCountTotal:" + UserPostsCountTotal);
    System.out.println("UserLikesCountTotal:" + UserLikesCountTotal);
    System.out.println("UserCommentsCountTotal:" + UserCommentsCountTotal);

    System.out.println();
    
    // 5- get outputs
    System.out.println("Outputs:");
    System.out.println("PostRank: " + VangavMUsersRank.i().getPostRank() );

    System.out.println();
  }
  
  public static void main (String [] args) throws Exception {
  
    // here's an example of invoking VangavMUsersRank solution
    //   with 3 different sets of inputs
    invokeVangavMUsersRank(
      -0.01,
      -0.01,
      -0.01,
      -0.01,
      -0.01,
      -0.01,
      -0.01,
      -0.01,
      -0.01,
      -0.01);
    invokeVangavMUsersRank(
      30.0,
      500.0,
      500.0,
      50.0,
      500.0,
      500.0,
      500.0,
      50.0,
      500.0,
      500.0);
    invokeVangavMUsersRank(
      60.01,
      1000.01,
      1000.01,
      100.01,
      1000.01,
      1000.01,
      1000.01,
      100.01,
      1000.01,
      1000.01);

  }
}
