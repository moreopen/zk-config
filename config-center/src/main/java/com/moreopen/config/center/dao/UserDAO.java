package com.moreopen.config.center.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.moreopen.config.center.domain.User;

@Component
public class UserDAO {
	
	private static final String FIND_USER = "select * from user where name = ? and pwd = ?";
	
	@Resource
	private JdbcTemplate jdbcTemplate;

	public User findUser(String userName, String pwd) {
		List<User> list = jdbcTemplate.query(FIND_USER, new Object[] {userName, pwd}, new RowMapper<User>() {
			@Override
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
				User user = new User();
				user.setId(rs.getLong("id"));
				user.setName(rs.getString("name"));
				user.setCreateTime(rs.getTimestamp("create_time"));
				user.setUpdateTime(rs.getTimestamp("update_time"));
				return user;
			}
			
		});
		return list.isEmpty() ? null : list.iterator().next();
	}

}
