package dev.gsitgithub.db.jdbi.mapper;

import dev.gsitgithub.entity.Authority;
import dev.gsitgithub.entity.User;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class UserMapper implements ResultSetMapper<User> {

    private User user;

    // this mapping method will get called for every row in the result set
    public User map(int index, ResultSet rs, StatementContext ctx) throws SQLException {

        // for the first row of the result set, we create the wrapper object
        if (index == 0) {
            user = new User(rs.getLong("id"), rs.getString("name"));
        }

        // ...and with every line we add one of the joined users
        Authority authority = new Authority(rs.getLong("a_id"), rs.getString("a_name"));
        if (authority.getId() > 0) {
            user.getAuthorities().add(authority);
        }

        return user;
    }
}