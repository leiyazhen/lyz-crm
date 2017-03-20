package lyz.crm.mapper;

import java.util.List;

import lyz.crm.pojo.Role;

public interface RoleMapper {
	/**
	 * 根据用户ID查找具有的角色列表
	 * 
	 * @param userId
	 * @return
	 */
	List<Role> findByUserId(Integer userId);

}
