package lyz.crm.service;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import lyz.crm.mapper.UserMapper;
import lyz.crm.pojo.User;
import lyz.crm.utils.ShiroUtil;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

@Named
@Transactional
public class UserService {

	private static Logger logger = LoggerFactory.getLogger(UserService.class);

	@Inject
	private UserMapper userMapper;
	@Value("${user.salt}")
	private static String passwordSalt;
	@Inject
	private WeiXinService weiXinService;

	public List<User> findAllUser() {
		return userMapper.findAll();
	}

	/**
	 * 根据DataTables中的参数进行查询
	 * 
	 * @param param
	 * @return
	 */
	public List<User> findUserByParam(Map<String, Object> param) {
		return userMapper.findByParam(param);
	}

	/**
	 * 获取用户的总数量
	 * 
	 * @return
	 */
	public Integer findUserCount() {
		return userMapper.findUserCount().intValue();
	}

	/**
	 * 根据查询条件获取用户的数量
	 * 
	 * @param param
	 * @return
	 */
	public Integer findUserCountByParam(Map<String, Object> param) {
		return userMapper.findUserCountByParam(param).intValue();
	}

	/**
	 * 保存新用户
	 * 
	 * @param user
	 * @param role
	 */
	public void saveNewUser(User user, String[] role) {
		// 产生微信端使用的userid
		user.setUserid(String.valueOf(DateTime.now().getMillis()));
		user.setPassword(DigestUtils.md5Hex(user.getPassword() + passwordSalt));
		user.setCreatetime(DateTime.now().toString("yyyy-MM-dd HH:mm"));
		user.setState(User.USER_STATE_OK);
		userMapper.save(user);

		// 保存用户的角色的关系
		if (role != null) {
			userMapper.saveUserAndRole(user, role);
		}
		logger.info("{}添加了新用户{}", ShiroUtil.getCurrentUserName(),
				user.getUsername());

		// 将用户添加到微信的通讯录中
		weiXinService.bindUserWeixin(user, role);
	}

	public static void main(String[] args) {
		User user = new User();
		user.setCreatetime(DateTime.now().toString("yyyy-MM-dd HH:mm"));
		user.setPassword(DigestUtils.md5Hex("engine" + passwordSalt));
		user.setTel("test");
		user.setState(User.USER_STATE_OK);
		System.out.println(DigestUtils.md2Hex("engine" + passwordSalt));
	}

	/**
	 * 删除用户
	 * 
	 * @param id
	 *            用户ID
	 */
	public void delUserById(Integer id) {
		User user = userMapper.findById(id);
		// 先删除微信通讯录中的数据
		weiXinService.delUser(user.getUserid());
		// 先删除和角色的对应关系
		userMapper.delUserAndRole(id);
		// 再删除用户
		userMapper.del(id);
		logger.info("{}删除了用户{}", ShiroUtil.getCurrentUserName(), id);
	}

	/**
	 * 根据用户的ID查询用户以及管理的角色
	 * 
	 * @param id
	 *            用户ID
	 * @return
	 */
	public User findUserWithRoleById(Integer id) {
		return userMapper.findUserWithRoleById(id);
	}

	/**
	 * 编辑用户
	 * 
	 * @param user
	 * @param role
	 */
	public void editUser(User user, String[] role) {
		// 删除原有的和角色之间的关系，
		userMapper.delUserAndRole(user.getId());
		// 然后重建
		userMapper.saveUserAndRole(user, role);
		// 修改用户
		userMapper.editUser(user);
	}

	/**
	 * 根据微信的UserID获取用户信息
	 * 
	 * @param userId
	 * @return
	 */
	public User findByUserId(String userId) {
		return userMapper.findByUserId(userId);
	}
}
