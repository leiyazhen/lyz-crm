package lyz.crm.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import lyz.crm.mapper.RoleMapper;
import lyz.crm.mapper.UserMapper;
import lyz.crm.pojo.Role;
import lyz.crm.pojo.User;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

@Named
public class ShiroRealm extends AuthorizingRealm {

	@Inject
	private UserMapper userMapper;
	@Inject
	private RoleMapper roleMapper;

	/**
	 * 权限认证方法
	 * 
	 * @param principalCollection
	 * @return
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principalCollection) {
		User shiroUser = (User) principalCollection.getPrimaryPrincipal();

		User user = userMapper.findByTel(shiroUser.getTel());
		if (user != null) {
			SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
			// 获取用户对应的角色列表
			List<Role> roleList = roleMapper.findByUserId(user.getId());

			for (Role role : roleList) {
				info.addRole(role.getRolename());
			}
			return info;
		}

		return null;
	}

	/**
	 * 登录方法
	 * 
	 * @param authenticationToken
	 * @return
	 * @throws AuthenticationException
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken authenticationToken)
			throws AuthenticationException {

		UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
		String tel = token.getUsername();
		User user = userMapper.findByTel(tel);
		System.out.println("AuthenticationInfo[user]:" + user);
		if (user != null) {
			if (User.USER_STATE_DISABLE.equals(user.getState())) {
				throw new LockedAccountException("该账号已被禁用");
			}
			return new SimpleAuthenticationInfo(user, user.getPassword(),
					getName());
		}
		throw new UnknownAccountException("账号或密码错误");
	}
}
