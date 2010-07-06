package com.springsource.greenhouse.invites;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.oauth.consumer.token.OAuthConsumerToken;
import org.springframework.social.twitter.TwitterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.springsource.greenhouse.oauth.OAuthAccessToken;
import com.springsource.greenhouse.signin.GreenhouseUserDetails;

@Controller
@RequestMapping("/invites/twitter")
public class TwitterInvitesController {
	
	private TwitterService twitterService;
	
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	@Inject
	public TwitterInvitesController(TwitterService twitterService, JdbcTemplate jdbcTemplate) {
		this.twitterService = twitterService;		
		this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public void friendFinder(GreenhouseUserDetails currentUser, Model model) {
		if (currentUser.getUsername() != null) {
			model.addAttribute("username", currentUser.getUsername());
		}
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public String findFriends(@OAuthAccessToken("twitter") OAuthConsumerToken accessToken, @RequestParam String username, Model model) {
		List<String> twitterFriends = twitterService.getFriends(accessToken, username);
		model.addAttribute("friends", findGreenhouseTwitterFriends(twitterFriends));
		return "invites/twitterFriends";
	}
	
	private List<GreenhouseFriend> findGreenhouseTwitterFriends(List<String> twitterFriends) {
	    return jdbcTemplate.query("select username, firstName, lastName from User where username in ( :names )",
	    		Collections.singletonMap("names", twitterFriends),
	    		new RowMapper<GreenhouseFriend>() {
					public GreenhouseFriend mapRow(ResultSet rs, int rowNum) throws SQLException {
						GreenhouseFriend friend = new GreenhouseFriend();
						friend.setUsername(rs.getString("username"));
						friend.setName(rs.getString("firstName") + " " + rs.getString("lastName"));
					    return friend;
					}
				});
    }
}
