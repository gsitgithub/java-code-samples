package dev.gsitgithub.db.jdbi;

import dev.gsitgithub.db.jdbi.mapper.UserMapper;
import dev.gsitgithub.entity.User;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import java.util.List;

public interface UserDao {
    @Mapper(UserMapper.class)
    @SqlQuery("SELECT Account.id, Account.name, User.id as u_id, User.name as u_name FROM Account LEFT JOIN User ON User.accountId = Account.id WHERE Account.id = :id")
    List<User> getAccountById(@Bind("id") int id);

}
